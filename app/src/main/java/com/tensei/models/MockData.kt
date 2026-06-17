package com.tensei.models

object MockData {
    val trendingRepos = listOf(
        RepositoryInfo(
            name = "Awesome-Tech",
            author = "Tech-Pioneers",
            description = "A curated list of awesome Technology resources, machine learning frameworks, and tutorials.",
            language = "Python",
            languageColor = 0xFF3572A5,
            stars = 45201,
            forks = 6200,
            dailyStars = 1042,
            isTrending = true,
            labels = listOf("Tech", "Machine Learning", "Python")
        ),
        RepositoryInfo(
            name = "Compose-Beautiful",
            author = "UI-Ninja",
            description = "100+ beautiful and fully functional Jetpack Compose UI components for your next Android project.",
            language = "Kotlin",
            languageColor = 0xFFA97BFF,
            stars = 12040,
            forks = 850,
            dailyStars = 450,
            isTrending = true,
            labels = listOf("Android", "Jetpack Compose", "UI")
        ),
        RepositoryInfo(
            name = "React-Speed",
            author = "web-wizards",
            description = "A blazing fast framework for building React applications with zero-config SSR and static generation.",
            language = "TypeScript",
            languageColor = 0xFF3178C6,
            stars = 23901,
            forks = 1400,
            dailyStars = 320,
            isTrending = true,
            labels = listOf("React", "Web", "Performance")
        ),
        RepositoryInfo(
            name = "Flutter-Animations",
            author = "cross-platform-hero",
            description = "Production-ready, highly interactive animations for Flutter applications.",
            language = "Dart",
            languageColor = 0xFF00B4AB,
            stars = 8900,
            forks = 405,
            dailyStars = 120,
            isTrending = true,
            labels = listOf("Flutter", "Animation")
        )
    )

    val hiddenGems = listOf(
        RepositoryInfo(
            name = "CyberSec-Tools",
            author = "SecurityDefenders",
            description = "A collection of lesser-known tools for penetration testing and vulnerability scanning.",
            language = "Go",
            languageColor = 0xFF00ADD8,
            stars = 340,
            forks = 20,
            dailyStars = 15,
            isHiddenGem = true,
            labels = listOf("Cybersecurity", "Tools")
        ),
        RepositoryInfo(
            name = "DevOps-Scripts",
            author = "SysAdminPro",
            description = "A very useful set of bash scripts that automate routine cloud infrastructure deployments.",
            language = "Shell",
            languageColor = 0xFF89E051,
            stars = 550,
            forks = 40,
            dailyStars = 30,
            isHiddenGem = true,
            labels = listOf("DevOps", "Automation")
        )
    )
}
