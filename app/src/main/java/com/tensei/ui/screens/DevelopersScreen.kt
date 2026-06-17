package com.tensei.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.graphics.Color
import coil.compose.AsyncImage
import io.github.fletchmckee.liquid.liquid
import com.tensei.network.GitHubSearchUser
import com.tensei.ui.AppViewModel
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.border

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevelopersScreen(viewModel: AppViewModel) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedUserUrl by remember { mutableStateOf<String?>(null) }
    
    val searchResults by viewModel.searchUsersResults.collectAsState()
    val trending by viewModel.trendingDevelopers.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val uriHandler = LocalUriHandler.current

    LaunchedEffect(searchQuery) {
        if (searchQuery.isNotBlank()) {
            kotlinx.coroutines.delay(800)
            viewModel.searchDevelopers(searchQuery)
        }
    }

    Scaffold(
        containerColor = androidx.compose.ui.graphics.Color.Transparent,
        topBar = {
            TopAppBar(
                title = { 
                    Text("Developers", fontWeight = FontWeight.Bold, fontSize = 28.sp)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = androidx.compose.ui.graphics.Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search developers...") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = "Search") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                shape = RoundedCornerShape(16.dp),
                singleLine = true,
                keyboardActions = androidx.compose.foundation.text.KeyboardActions(
                    onSearch = { 
                        viewModel.searchDevelopers(searchQuery)
                    }
                ),
                keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                    imeAction = androidx.compose.ui.text.input.ImeAction.Search
                ),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.surfaceVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val listToShow = if (searchQuery.isBlank()) trending else searchResults
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(bottom = 100.dp, start = 16.dp, end = 16.dp)
                ) {
                    if (searchQuery.isBlank()) {
                        item {
                            Text(
                                "Trending Developers",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(bottom = 8.dp)
                            )
                        }
                    }
                    items(listToShow, key = { it.id }) { user ->
                        DeveloperCard(user = user, onClick = { selectedUserUrl = user.htmlUrl })
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
    
    if (selectedUserUrl != null) {
        com.tensei.ui.components.WebViewDialog(
            url = selectedUserUrl!!,
            title = "Developer Profile",
            onDismissRequest = { selectedUserUrl = null }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DeveloperCard(user: GitHubSearchUser, onClick: () -> Unit) {
    val liquidState = com.tensei.ui.LocalLiquidState.current
    val isDark = androidx.compose.foundation.isSystemInDarkTheme()
    
    Box(
        modifier = Modifier
            .fillMaxWidth()
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
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AsyncImage(
                model = user.avatarUrl,
                contentDescription = "Avatar",
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = user.login,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
