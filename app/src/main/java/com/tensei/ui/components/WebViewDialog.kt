package com.tensei.ui.components

import android.content.Intent
import android.net.Uri
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewDialog(url: String, title: String, onDismissRequest: () -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    var desktopMode by remember { mutableStateOf(false) }
    val context = LocalContext.current

    val desktopUserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/114.0.0.0 Safari/537.36"
    val defaultUserAgent = remember { android.webkit.WebSettings.getDefaultUserAgent(context) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                TopAppBar(
                    title = { Text(title) },
                    navigationIcon = {
                        IconButton(onClick = onDismissRequest) {
                            Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                        }
                    },
                    actions = {
                        IconButton(onClick = { expanded = true }) {
                            Icon(Icons.Rounded.MoreVert, contentDescription = "More")
                        }
                        DropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("Open in Browser") },
                                onClick = {
                                    expanded = false
                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                    context.startActivity(intent)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text(if (desktopMode) "Mobile View" else "Desktop View") },
                                onClick = {
                                    expanded = false
                                    desktopMode = !desktopMode
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Share") },
                                onClick = {
                                    expanded = false
                                    val intent = Intent(Intent.ACTION_SEND).apply {
                                        type = "text/plain"
                                        putExtra(Intent.EXTRA_TEXT, url)
                                    }
                                    context.startActivity(Intent.createChooser(intent, "Share"))
                                }
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)
                    )
                )
                var webViewLoading by remember { mutableStateOf(true) }
                
                Box(modifier = Modifier.fillMaxSize()) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { context ->
                            WebView(context).apply {
                                settings.javaScriptEnabled = true
                                settings.domStorageEnabled = true
                                settings.setSupportZoom(true)
                                settings.builtInZoomControls = true
                                settings.displayZoomControls = false
                                webViewClient = object : WebViewClient() {
                                    override fun onPageFinished(view: WebView?, url: String?) {
                                        super.onPageFinished(view, url)
                                        if (desktopMode) {
                                            view?.evaluateJavascript("var meta = document.querySelector('meta[name=\"viewport\"]'); if(meta) { meta.setAttribute('content', 'width=1024, initial-scale=1'); } else { meta = document.createElement('meta'); meta.name = 'viewport'; meta.content = 'width=1024, initial-scale=1'; document.head.appendChild(meta); }", null)
                                        }
                                        webViewLoading = false
                                    }
                                }
                                loadUrl(url)
                            }
                        },
                        update = { webView ->
                            // Update user agent based on desktopMode
                            val isDesktopNow = webView.settings.userAgentString == desktopUserAgent
                            if (desktopMode != isDesktopNow) {
                                if (desktopMode) {
                                    webView.settings.userAgentString = desktopUserAgent
                                    webView.settings.useWideViewPort = true
                                    webView.settings.loadWithOverviewMode = true
                                } else {
                                    webView.settings.userAgentString = defaultUserAgent
                                    webView.settings.useWideViewPort = false
                                    webView.settings.loadWithOverviewMode = false
                                }
                                webViewLoading = true
                                webView.reload()
                            }
                            // Only load URL if it's different and not already loading that URL
                            if (webView.url != url && webView.originalUrl != url) {
                                webViewLoading = true
                                webView.loadUrl(url)
                            }
                        }
                    )
                    if (webViewLoading) {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                    }
                }
            }
        }
    }
}
