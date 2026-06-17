package com.tensei

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.material.icons.rounded.Group
import androidx.compose.ui.graphics.vector.ImageVector

enum class MainRoute(val title: String, val icon: ImageVector) {
    Trending("Trending", Icons.Rounded.Whatshot),
    Search("Search", Icons.Rounded.Search),
    Developers("Developers", Icons.Rounded.Group),
    Bookmarks("Saved", Icons.Rounded.Bookmark),
    Downloads("Downloads", Icons.Rounded.Download)
}
