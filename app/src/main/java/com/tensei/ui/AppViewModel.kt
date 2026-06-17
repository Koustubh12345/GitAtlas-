package com.tensei.ui

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.tensei.models.RepositoryInfo
import com.tensei.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import com.squareup.moshi.Types

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val repoListType = Types.newParameterizedType(List::class.java, RepositoryInfo::class.java)
    private val repoListAdapter = moshi.adapter<List<RepositoryInfo>>(repoListType)

    private val _bookmarks = MutableStateFlow<List<RepositoryInfo>>(
        prefs.getString("bookmarks", null)?.let { repoListAdapter.fromJson(it) } ?: emptyList()
    )
    val bookmarks: StateFlow<List<RepositoryInfo>> = _bookmarks.asStateFlow()

    private val _searchResults = MutableStateFlow<List<RepositoryInfo>>(emptyList())
    val searchResults: StateFlow<List<RepositoryInfo>> = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _trendingRepos = MutableStateFlow<List<RepositoryInfo>>(emptyList())
    val trendingRepos: StateFlow<List<RepositoryInfo>> = _trendingRepos.asStateFlow()

    private val _recommendedRepos = MutableStateFlow<List<RepositoryInfo>>(emptyList())
    val recommendedRepos: StateFlow<List<RepositoryInfo>> = _recommendedRepos.asStateFlow()

    data class DownloadInfo(val repo: RepositoryInfo, val progress: Float)
    private val _downloads = MutableStateFlow<Map<String, DownloadInfo>>(emptyMap())
    val downloads: StateFlow<Map<String, DownloadInfo>> = _downloads.asStateFlow()
    
    private val downloadJobs = mutableMapOf<String, kotlinx.coroutines.Job>()

    private val _downloadedRepos = MutableStateFlow<List<RepositoryInfo>>(
        prefs.getString("downloads", null)?.let { repoListAdapter.fromJson(it) } ?: emptyList()
    )
    val downloadedRepos: StateFlow<List<RepositoryInfo>> = _downloadedRepos.asStateFlow()

    private fun saveState() {
        prefs.edit()
            .putString("bookmarks", repoListAdapter.toJson(_bookmarks.value))
            .putString("downloads", repoListAdapter.toJson(_downloadedRepos.value))
            .apply()
    }

    private val _searchUsersResults = MutableStateFlow<List<com.tensei.network.GitHubSearchUser>>(emptyList())
    val searchUsersResults: StateFlow<List<com.tensei.network.GitHubSearchUser>> = _searchUsersResults.asStateFlow()

    private val _trendingDevelopers = MutableStateFlow<List<com.tensei.network.GitHubSearchUser>>(emptyList())
    val trendingDevelopers: StateFlow<List<com.tensei.network.GitHubSearchUser>> = _trendingDevelopers.asStateFlow()

    fun searchDevelopers(query: String) {
        if (query.isBlank()) {
            _searchUsersResults.value = emptyList()
            return
        }
        viewModelScope.launch {
            _isLoading.value = true
            try {
                val response = NetworkModule.gitHubService.searchUsers(query = query)
                _searchUsersResults.value = response.items
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun loadTrendingDevelopers() {
        viewModelScope.launch {
            try {
                // Fetch some interesting users e.g. language:kotlin followers:>1000
                val response = NetworkModule.gitHubService.searchUsers(
                    query = "followers:>1000",
                    perPage = 20
                )
                _trendingDevelopers.value = response.items.shuffled().take(15)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    init {
        loadTrendingRepos()
        loadTrendingDevelopers()
    }

    fun downloadRepo(repo: RepositoryInfo) {
        if (_downloads.value.containsKey(repo.id)) return
        val currentDownloads = _downloads.value.toMutableMap()
        currentDownloads[repo.id] = DownloadInfo(repo, 0f)
        _downloads.value = currentDownloads

        val context = getApplication<Application>()
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "downloads"
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, "Downloads", NotificationManager.IMPORTANCE_LOW)
            notificationManager.createNotificationChannel(channel)
        }

        val notificationId = repo.id.hashCode()

        val job = viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                // Fetch default branch
                val details = NetworkModule.gitHubService.getRepository(repo.author, repo.name)
                val defaultBranch = details.defaultBranch
                
                // Download ZIP
                val responseBody = NetworkModule.gitHubService.downloadRepositoryZip(repo.author, repo.name, defaultBranch)
                val inputStream = responseBody.byteStream()
                
                val downloadsDir = context.getExternalFilesDir(null)
                val gitHubDownloadsDir = java.io.File(downloadsDir, "GitHub_Downloads")
                gitHubDownloadsDir.mkdirs()
                val targetDir = java.io.File(gitHubDownloadsDir, repo.name)
                if (targetDir.exists()) targetDir.deleteRecursively()
                targetDir.mkdirs()
                
                val zis = java.util.zip.ZipInputStream(inputStream)
                var zipEntry = zis.nextEntry
                val buffer = ByteArray(8192)
                var fileCount = 0
                
                while (zipEntry != null) {
                    val newFile = java.io.File(targetDir, zipEntry.name)
                    // Security check against zip path traversal
                    if (!newFile.canonicalPath.startsWith(targetDir.canonicalPath)) {
                        zipEntry = zis.nextEntry
                        continue
                    }
                    if (zipEntry.isDirectory) {
                        newFile.mkdirs()
                    } else {
                        newFile.parentFile?.mkdirs()
                        val fos = java.io.FileOutputStream(newFile)
                        var len: Int
                        while (zis.read(buffer).also { len = it } > 0) {
                            fos.write(buffer, 0, len)
                        }
                        fos.close()
                    }
                    zis.closeEntry()
                    zipEntry = zis.nextEntry
                    fileCount++
                    
                    if (fileCount % 20 == 0) {
                        val newProgressMap = _downloads.value.toMutableMap()
                        // Artificial progress curved asymptotically to 90%
                        val p = 0.1f + (0.8f * (1.0f - 1.0f / (1.0f + fileCount / 100.0f)))
                        newProgressMap[repo.id] = DownloadInfo(repo, p)
                        _downloads.value = newProgressMap

                        val notification = NotificationCompat.Builder(context, channelId)
                            .setContentTitle("Downloading ${repo.name}")
                            .setContentText("Extracted $fileCount files...")
                            .setSmallIcon(android.R.drawable.stat_sys_download)
                            .setProgress(100, (p * 100).toInt(), false)
                            .setOngoing(true)
                            .build()
                        try { notificationManager.notify(notificationId, notification) } catch (e: Exception) {}
                    }
                }
                zis.close()
                responseBody.close()

                val finalMap = _downloads.value.toMutableMap()
                finalMap[repo.id] = DownloadInfo(repo, 1.0f)
                _downloads.value = finalMap

                val notification = NotificationCompat.Builder(context, channelId)
                    .setContentTitle("Downloaded ${repo.name}")
                    .setContentText("Download complete")
                    .setSmallIcon(android.R.drawable.stat_sys_download_done)
                    .build()
                try { notificationManager.notify(notificationId, notification) } catch (e: Exception) {}

                val downloadedList = _downloadedRepos.value.toMutableList()
                if (!downloadedList.any { it.id == repo.id }) {
                    downloadedList.add(repo)
                }
                _downloadedRepos.value = downloadedList
                saveState()
                downloadJobs.remove(repo.id)

            } catch (e: Exception) {
                if (e !is kotlinx.coroutines.CancellationException) {
                    e.printStackTrace()
                }
                val newProgressMap = _downloads.value.toMutableMap()
                newProgressMap.remove(repo.id)
                _downloads.value = newProgressMap
                downloadJobs.remove(repo.id)
                notificationManager.cancel(notificationId)
            }
        }
        downloadJobs[repo.id] = job
    }

    fun cancelDownload(repoId: String) {
        downloadJobs[repoId]?.cancel()
        downloadJobs.remove(repoId)
        val newProgressMap = _downloads.value.toMutableMap()
        newProgressMap.remove(repoId)
        _downloads.value = newProgressMap
    }

    private fun loadTrendingRepos() {
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // Fetch general trending
                val response = NetworkModule.gitHubService.searchRepositories(
                    query = "stars:>1000 created:>2023-01-01",
                    sort = "stars"
                )
                _trendingRepos.value = response.items.map { it.toRepositoryInfo().copy(isTrending = true) }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun deleteRepo(repo: RepositoryInfo) {
        val rootDownloadsDir = getApplication<Application>().getExternalFilesDir(null)
        val gitHubDownloadsDir = java.io.File(rootDownloadsDir, "GitHub_Downloads")
        val targetDir = java.io.File(gitHubDownloadsDir, repo.name)
        if (targetDir.exists()) {
            targetDir.deleteRecursively()
        }
        val currentDownloadsMap = _downloads.value.toMutableMap()
        currentDownloadsMap.remove(repo.id)
        _downloads.value = currentDownloadsMap

        val currentDownloadedList = _downloadedRepos.value.toMutableList()
        currentDownloadedList.removeAll { it.id == repo.id }
        _downloadedRepos.value = currentDownloadedList
        saveState()
    }

    private val searchHistory = mutableListOf<String>()

    private fun updateRecommendations() {
        if (_bookmarks.value.isEmpty() && searchHistory.isEmpty()) return
        viewModelScope.launch {
            try {
                val tags = _bookmarks.value.map { it.language }.filter { it.isNotBlank() && it != "Unknown" } + searchHistory
                val mostFrequent = tags.groupingBy { it }.eachCount().maxByOrNull { it.value }?.key

                val query = if (mostFrequent != null) {
                    val sb = java.lang.StringBuilder()
                    if (mostFrequent.lowercase().contains("root") || mostFrequent.lowercase().contains("magisk") || mostFrequent.lowercase().contains("kernelsu")) {
                        sb.append("root OR KernelSU OR Magisk ")
                    } else if (mostFrequent.lowercase().contains("compose") || mostFrequent.lowercase().contains("kotlin")) {
                        sb.append("Jetpack Compose OR Kotlin OR Android ")
                    } else if (mostFrequent.lowercase().contains("linux") || mostFrequent.lowercase().contains("bash")) {
                        sb.append("Linux OR Bash OR Shell OR Scripting ")
                    } else {
                        sb.append("$mostFrequent ")
                    }
                    sb.append("stars:>50")
                    sb.toString()
                } else {
                    "good-first-issues:>0 stars:>500"
                }
                
                val response = NetworkModule.gitHubService.searchRepositories(
                    query = query,
                    sort = "stars"
                )
                // Filter out already downloaded/bookmarked? No need, just show them as recommendations
                _recommendedRepos.value = response.items.map { it.toRepositoryInfo().copy(isHiddenGem = true) }
            } catch (e: Exception) {
                // Ignore API limit errors silently for background updates
            }
        }
    }

    fun search(query: String) {
        if (query.isBlank()) {
            _searchResults.value = emptyList()
            return
        }
        searchHistory.add(query)
        updateRecommendations()
        viewModelScope.launch {
            _isLoading.value = true
            try {
                // If it's a simple term, make it broader. GitHub usually does this anyway, but specifying in:name,description helps.
                val enhancedQuery = if (query.contains(":")) query else "$query in:name,description,readme"
                val response = NetworkModule.gitHubService.searchRepositories(query = enhancedQuery)
                _searchResults.value = response.items.map { it.toRepositoryInfo() }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun toggleBookmark(repo: RepositoryInfo) {
        val current = _bookmarks.value.toMutableList()
        val index = current.indexOfFirst { it.id == repo.id }
        if (index != -1) {
            current.removeAt(index)
        } else {
            current.add(repo.copy(isBookmarked = true))
        }
        _bookmarks.value = current
        saveState()
        updateRecommendations()
    }
}
