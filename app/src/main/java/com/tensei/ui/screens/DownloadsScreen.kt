package com.tensei.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensei.ui.AppViewModel
import com.tensei.ui.components.RepoCard
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadsScreen(
    viewModel: AppViewModel,
    onViewRepoFiles: (com.tensei.models.RepositoryInfo) -> Unit
) {
    val downloads by viewModel.downloads.collectAsState()
    val downloadedRepos by viewModel.downloadedRepos.collectAsState()

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Downloads", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (downloads.isEmpty() && downloadedRepos.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("No downloads yet", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp)
            ) {
                // Show downloaded repos
                if (downloadedRepos.isNotEmpty()) {
                    item {
                        SectionHeader("Downloaded Repositories")
                    }
                    items(downloadedRepos, key = { it.id }) { repo ->
                        RepoCard(
                            repo = repo,
                            isDownloaded = true,
                            onToggleBookmark = { viewModel.toggleBookmark(repo) },
                            onDownloadClick = null,
                            onViewFilesClick = { onViewRepoFiles(repo) },
                            onDeleteClick = { viewModel.deleteRepo(repo) }
                        )
                    }
                }
                
                // Show actively downloading
                val activeDownloads = downloads.filter { it.value.progress < 1.0f }
                if (activeDownloads.isNotEmpty()) {
                    item {
                        SectionHeader("Active Downloads")
                    }
                    items(activeDownloads.entries.toList()) { entry ->
                        val repoId = entry.key
                        val downloadInfo = entry.value
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 8.dp),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                            )
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Downloading ${downloadInfo.repo.name}...", fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(8.dp))
                                    LinearProgressIndicator(
                                        progress = { downloadInfo.progress },
                                        modifier = Modifier.fillMaxWidth(),
                                    )
                                    Text("${(downloadInfo.progress * 100).toInt()}%", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                IconButton(onClick = { viewModel.cancelDownload(repoId) }) {
                                    Icon(Icons.Filled.Close, contentDescription = "Cancel", tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
