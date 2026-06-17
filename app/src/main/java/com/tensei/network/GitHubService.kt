package com.tensei.network

import com.tensei.models.RepositoryInfo
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import retrofit2.http.GET
import retrofit2.http.Query

@JsonClass(generateAdapter = true)
data class GitHubSearchResponse(
    @Json(name = "items") val items: List<GitHubRepo>
)

@JsonClass(generateAdapter = true)
data class GitHubRepo(
    @Json(name = "id") val id: Long,
    @Json(name = "name") val name: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "owner") val owner: GitHubOwner,
    @Json(name = "description") val description: String?,
    @Json(name = "stargazers_count") val stars: Int,
    @Json(name = "forks_count") val forks: Int,
    @Json(name = "language") val language: String?,
    @Json(name = "topics") val topics: List<String>?,
    @Json(name = "html_url") val htmlUrl: String
) {
    fun toRepositoryInfo(): RepositoryInfo {
        return RepositoryInfo(
            id = id.toString(),
            name = name,
            author = owner.login,
            description = description ?: "",
            language = language ?: "Unknown",
            languageColor = getLanguageColor(language),
            stars = stars,
            forks = forks,
            labels = topics ?: emptyList(),
            avatarUrl = owner.avatarUrl ?: "",
            htmlUrl = htmlUrl
        )
    }

    private fun getLanguageColor(lang: String?): Long {
        return when (lang?.lowercase()) {
            "kotlin" -> 0xFFA97BFF
            "java" -> 0xFFb07219
            "python" -> 0xFF3572A5
            "typescript" -> 0xFF3178C6
            "javascript" -> 0xFFf1e05a
            "go" -> 0xFF00ADD8
            "rust" -> 0xFFdea584
            "c++" -> 0xFFf34b7d
            "dart" -> 0xFF00B4AB
            else -> 0xFF888888
        }
    }
}

@JsonClass(generateAdapter = true)
data class GitHubOwner(
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String?
)

@JsonClass(generateAdapter = true)
data class GitHubUserSearchResponse(
    @Json(name = "items") val items: List<GitHubSearchUser>
)

@JsonClass(generateAdapter = true)
data class GitHubSearchUser(
    @Json(name = "id") val id: Long,
    @Json(name = "login") val login: String,
    @Json(name = "avatar_url") val avatarUrl: String?,
    @Json(name = "html_url") val htmlUrl: String
)

interface GitHubService {
    @GET("search/repositories")
    suspend fun searchRepositories(
        @Query("q") query: String,
        @Query("sort") sort: String = "stars",
        @Query("order") order: String = "desc",
        @Query("per_page") perPage: Int = 30
    ): GitHubSearchResponse

    @GET("search/users")
    suspend fun searchUsers(
        @Query("q") query: String,
        @Query("per_page") perPage: Int = 30
    ): GitHubUserSearchResponse

    @GET("repos/{owner}/{repo}")
    suspend fun getRepository(
        @retrofit2.http.Path("owner") owner: String,
        @retrofit2.http.Path("repo") repo: String
    ): GitHubRepoDetailed

    @GET("repos/{owner}/{repo}/zipball/{ref}")
    @retrofit2.http.Streaming
    suspend fun downloadRepositoryZip(
        @retrofit2.http.Path("owner") owner: String,
        @retrofit2.http.Path("repo") repo: String,
        @retrofit2.http.Path("ref") ref: String
    ): okhttp3.ResponseBody
}

@JsonClass(generateAdapter = true)
data class GitHubRepoDetailed(
    @Json(name = "default_branch") val defaultBranch: String
)
