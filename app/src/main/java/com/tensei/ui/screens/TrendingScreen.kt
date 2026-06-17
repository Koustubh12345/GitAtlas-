package com.tensei.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tensei.models.MockData
import androidx.compose.ui.Alignment
import com.tensei.ui.components.RepoCard
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.material3.CircularProgressIndicator
import com.tensei.ui.AppViewModel

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Info
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.draw.clip
import coil.compose.AsyncImage
import androidx.compose.foundation.clickable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TrendingScreen(viewModel: AppViewModel) {
    val trending by viewModel.trendingRepos.collectAsState()
    val recommended by viewModel.recommendedRepos.collectAsState()
    val bookmarked by viewModel.bookmarks.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var showDeveloperInfo by remember { mutableStateOf(false) }

    if (showDeveloperInfo) {
        val uriHandler = LocalUriHandler.current
        var selectedPayment by remember { mutableStateOf("GPay") }
        
        Dialog(onDismissRequest = { showDeveloperInfo = false }) {
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp,
                modifier = Modifier.widthIn(max = 400.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Developed By",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "TenSei てんせい",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.primary,
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Dark Mode", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
                        Switch(
                            checked = com.tensei.ui.theme.ThemeManager.isDarkMode.value,
                            onCheckedChange = { com.tensei.ui.theme.ThemeManager.isDarkMode.value = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Support the Developer",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPayment = "GPay" },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (selectedPayment == "GPay") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("GPay")
                        }
                        FilledTonalButton(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedPayment = "Binance" },
                            colors = ButtonDefaults.filledTonalButtonColors(
                                containerColor = if (selectedPayment == "Binance") MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text("Binance")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    if (selectedPayment == "GPay") {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=upi%3A%2F%2Fpay%3Fpa%3Dkoustubhgaikwad7-1%40okaxis%26pn%3DKoustubh%2520Gaikwad%26cu%3D",
                                contentDescription = "GPay QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "UPI ID: koustubhgaikwad7-1@okaxis", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    } else if (selectedPayment == "Binance") {
                        Box(
                            modifier = Modifier
                                .size(200.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                                .padding(8.dp)
                        ) {
                            AsyncImage(
                                model = "https://api.qrserver.com/v1/create-qr-code/?size=500x500&data=binance_pay_koustubhgaikwad7",
                                contentDescription = "Binance QR Code",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(text = "Scan with Binance App to pay", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = { showDeveloperInfo = false },
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Close")
                    }
                }
            }
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Trending", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                },
                actions = {
                    IconButton(onClick = { showDeveloperInfo = true }) {
                        Icon(Icons.Rounded.Info, contentDescription = "Developer Info")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        if (isLoading && trending.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(bottom = 100.dp) // space for bottom bar
            ) {
                if (recommended.isNotEmpty()) {
                    item {
                        SectionHeader("Recommended for You")
                    }
                    items(recommended.take(5), key = { "rec_" + it.id }) { repo ->
                        val isSaved = bookmarked.any { it.id == repo.id }
                        RepoCard(
                            repo = repo.copy(isBookmarked = isSaved),
                            onToggleBookmark = { viewModel.toggleBookmark(repo) },
                            onDownloadClick = { viewModel.downloadRepo(repo) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
                
                item {
                    SectionHeader("Top Repositories Today")
                }
                items(trending, key = { "trend_" + it.id }) { repo ->
                    val isSaved = bookmarked.any { it.id == repo.id }
                    RepoCard(
                        repo = repo.copy(isBookmarked = isSaved),
                        onToggleBookmark = { viewModel.toggleBookmark(repo) },
                        onDownloadClick = { viewModel.downloadRepo(repo) }
                    )
                }
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, subtitle: String? = null) {
    Column(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        if (subtitle != null) {
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
    }
}
