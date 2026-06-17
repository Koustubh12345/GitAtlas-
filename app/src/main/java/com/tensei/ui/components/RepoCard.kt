package com.tensei.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.AltRoute
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CallSplit
import androidx.compose.material.icons.rounded.Code
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensei.models.RepositoryInfo
import io.github.fletchmckee.liquid.liquid

import android.content.Intent
import android.net.Uri
import androidx.compose.ui.platform.LocalContext

import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.filled.Delete

@Composable
fun RepoCard(
    repo: RepositoryInfo,
    isDownloaded: Boolean = false,
    onToggleBookmark: () -> Unit = {},
    onDownloadClick: (() -> Unit)? = null,
    onViewFilesClick: (() -> Unit)? = null,
    onDeleteClick: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val showWebView = androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(false) }
    
    val liquidState = com.tensei.ui.LocalLiquidState.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    if (showWebView.value) {
        com.tensei.ui.components.WebViewDialog(
            url = repo.htmlUrl,
            title = repo.name,
            onDismissRequest = { showWebView.value = false }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .background(
                color = if (isDark) Color(0x3BFFFFFF) else Color(0x4DFFFFFF),
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp, 
                brush = androidx.compose.ui.graphics.Brush.linearGradient(
                    colors = if (isDark) listOf(Color(0x66FFFFFF), Color(0x11FFFFFF)) 
                             else listOf(Color(0x33000000), Color(0x05000000))
                ),
                shape = RoundedCornerShape(20.dp)
            )
            .clip(RoundedCornerShape(20.dp))
            .clickable { 
                if (isDownloaded && onViewFilesClick != null) {
                    onViewFilesClick()
                } else {
                    showWebView.value = true
                }
            }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    // Profile placeholder
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        coil.compose.AsyncImage(
                            model = repo.avatarUrl,
                            contentDescription = "Developer avatar",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = androidx.compose.ui.layout.ContentScale.Crop
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${repo.author} /",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 12.sp
                        )
                        Text(
                            text = repo.name,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 16.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
                Row {
                    if (isDownloaded && onViewFilesClick != null) {
                        IconButton(onClick = onViewFilesClick) {
                            Icon(
                                imageVector = Icons.Rounded.FolderOpen,
                                contentDescription = "View Files",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        if (onDeleteClick != null) {
                            IconButton(onClick = onDeleteClick) {
                                Icon(
                                    imageVector = androidx.compose.material.icons.Icons.Filled.Delete,
                                    contentDescription = "Delete Repo",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else if (onDownloadClick != null) {
                        IconButton(onClick = onDownloadClick) {
                            Icon(
                                imageVector = Icons.Rounded.Download,
                                contentDescription = "Download Repo",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    IconButton(onClick = {
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "Check out this repo: ${repo.htmlUrl}")
                        }
                        context.startActivity(Intent.createChooser(shareIntent, "Share Repo"))
                    }) {
                        Icon(
                            imageVector = Icons.Filled.Share,
                            contentDescription = "Share",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onToggleBookmark) {
                        Icon(
                            imageVector = if (repo.isBookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                            contentDescription = "Bookmark",
                            tint = if (repo.isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = repo.description,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 14.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                lineHeight = 20.sp
            )
            Spacer(modifier = Modifier.height(14.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(Color(repo.languageColor))
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = repo.language,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Filled.Star,
                        contentDescription = "Stars",
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFFFFC107)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCompactNumber(repo.stars),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Icon(
                        imageVector = Icons.Rounded.CallSplit,
                        contentDescription = "Forks",
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = formatCompactNumber(repo.forks),
                        color = MaterialTheme.colorScheme.onSurface,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
                
                if (repo.dailyStars != null) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .background(
                                MaterialTheme.colorScheme.tertiaryContainer,
                                RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = "Daily stars",
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "+${repo.dailyStars} today",
                            color = MaterialTheme.colorScheme.onTertiaryContainer,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

fun formatCompactNumber(number: Int): String {
    if (number < 1000) return number.toString()
    val exp = (Math.log10(number.toDouble()) / 3).toInt()
    val formatted = String.format("%.1f", number / Math.pow(1000.0, exp.toDouble()))
    val suffix = "kMGTPE"[exp - 1]
    return "$formatted$suffix".replace(".0", "")
}
