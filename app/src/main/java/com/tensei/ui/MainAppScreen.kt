package com.tensei.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.tensei.MainRoute
import com.tensei.ui.components.AnimatedBottomBar
import com.tensei.ui.screens.TrendingScreen
import com.tensei.ui.screens.SearchScreen
import androidx.compose.ui.unit.dp
import com.tensei.ui.screens.DownloadsScreen
import com.tensei.ui.screens.RepoViewerScreen
import com.tensei.ui.screens.BookmarksScreen
import com.tensei.ui.screens.DevelopersScreen

import io.github.fletchmckee.liquid.rememberLiquidState
import io.github.fletchmckee.liquid.LiquidState
import io.github.fletchmckee.liquid.liquefiable

val LocalLiquidState = compositionLocalOf<LiquidState> { error("No liquid state") }

@Composable
fun MainAppScreen() {
    var currentRoute by remember { mutableStateOf(MainRoute.Trending) }
    var viewingRepoId by remember { mutableStateOf<String?>(null) }
    val viewModel: AppViewModel = viewModel()
    val liquidState = rememberLiquidState()

    CompositionLocalProvider(LocalLiquidState provides liquidState) {
        // The root Box allows the bar to float directly over the scrolling content
        Box(modifier = Modifier.fillMaxSize()) {
            Box(modifier = Modifier.fillMaxSize().background(androidx.compose.material3.MaterialTheme.colorScheme.background).liquefiable(liquidState)) {
                // Decorative background for liquid refraction
                Box(modifier = Modifier.fillMaxSize()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color(0x229C27B0), 
                                        androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(200f, 400f),
                                    radius = 800f
                                )
                            )
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = androidx.compose.ui.graphics.Brush.radialGradient(
                                    colors = listOf(
                                        androidx.compose.ui.graphics.Color(0x2203DAC5), 
                                        androidx.compose.ui.graphics.Color.Transparent
                                    ),
                                    center = androidx.compose.ui.geometry.Offset(800f, 1500f),
                                    radius = 900f
                                )
                            )
                    )
                }

                Crossfade(
                    targetState = currentRoute, 
                    label = "Main Router"
                ) { screen ->
                when (screen) {
                    MainRoute.Trending -> TrendingScreen(viewModel)
                    MainRoute.Search -> SearchScreen(viewModel)
                    MainRoute.Developers -> DevelopersScreen(viewModel)
                    MainRoute.Bookmarks -> BookmarksScreen(viewModel)
                    MainRoute.Downloads -> DownloadsScreen(
                        viewModel = viewModel,
                        onViewRepoFiles = { repo -> viewingRepoId = repo.name }
                    )
                }
            }
            } // Close liquefiable box

            // The floating Liquid Navigation Bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 32.dp) // Pushes it up slightly from the absolute bottom edge
            ) {
                AnimatedBottomBar(
                    currentRoute = currentRoute,
                    liquidState = liquidState,
                    onNavigate = { route ->
                        currentRoute = route
                    }
                )
            }

            viewingRepoId?.let { repoId ->
                RepoViewerScreen(
                    repoId = repoId,
                    onBack = { viewingRepoId = null }
                )
            }
        }
    } // Close CompositionLocalProvider
}


