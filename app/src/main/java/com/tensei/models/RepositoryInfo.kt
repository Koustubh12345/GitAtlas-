package com.tensei.models

import java.util.UUID

data class RepositoryInfo(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val author: String,
    val description: String,
    val language: String,
    val languageColor: Long,
    val stars: Int,
    val forks: Int,
    val dailyStars: Int? = null,
    val isTrending: Boolean = false,
    val isHiddenGem: Boolean = false,
    val labels: List<String> = emptyList(),
    val isBookmarked: Boolean = false,
    val avatarUrl: String = "https://avatars.githubusercontent.com/u/1?v=4", // Placeholder
    val htmlUrl: String = "https://github.com/trending"
)

data class TrendDeveloper(
    val id: String = UUID.randomUUID().toString(),
    val name: String,
    val username: String,
    val avatarUrl: String,
    val sponsorRank: String? = null
)
