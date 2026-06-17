package com.tensei.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Article
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

sealed class FileNode {
    abstract val name: String
    data class Directory(override val name: String, val children: List<FileNode>) : FileNode()
    data class File(override val name: String, val content: String, val absolutePath: String = "") : FileNode()
}

val sampleRepoTree = FileNode.Directory("root", listOf(
    FileNode.Directory("app", listOf(
        FileNode.Directory("src", listOf(
            FileNode.Directory("main", listOf(
                FileNode.Directory("java", listOf(
                    FileNode.Directory("com", listOf(
                        FileNode.File("MainActivity.kt", "package com.tensei\n\nimport android.os.Bundle\n\nclass MainActivity {\n}")
                    ))
                )),
                FileNode.File("AndroidManifest.xml", "<manifest>\n  <application/>\n</manifest>")
            ))
        )),
        FileNode.File("build.gradle", "plugins {\n  id 'com.android.application'\n}")
    )),
    FileNode.File("README.md", "# Super Repository\nThis is an awesome repo."),
    FileNode.File(".gitignore", "/build\n*.local"),
    FileNode.File("build.gradle.kts", "plugins {\n    kotlin(\"jvm\") version \"1.9.0\"\n}\n")
))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RepoViewerScreen(
    repoId: String,
    onBack: () -> Unit
) {
    var isLoaded by remember { mutableStateOf(false) }
    var rootTree by remember { mutableStateOf<FileNode>(sampleRepoTree) }
    var currentNode by remember { mutableStateOf<FileNode?>(null) }
    var pathStack by remember { mutableStateOf(listOf<FileNode.Directory>()) }
    var showSearchDialog by remember { mutableStateOf(false) }

    val context = LocalContext.current
    LaunchedEffect(repoId) {
        withContext(Dispatchers.IO) {
            val rootDir = java.io.File(context.getExternalFilesDir(null), "GitHub_Downloads/$repoId")
            if (rootDir.exists() && rootDir.isDirectory) {
                var actualRoot = rootDir
                val rootFiles = rootDir.listFiles()
                if (rootFiles?.size == 1 && rootFiles[0].isDirectory) {
                    actualRoot = rootFiles[0]
                }
                
                fun buildNode(dir: java.io.File, name: String = dir.name): FileNode {
                    if (dir.isFile) {
                        val isMedia = dir.extension.lowercase() in listOf("jpg", "jpeg", "png", "gif", "mp4", "mkv", "webp")
                        val content = if (isMedia) "Media file" else try {
                            if (dir.length() > 500 * 1024) "File too large to preview" else dir.readText()
                        } catch (e: Exception) {
                            "Binary or unreadable file"
                        }
                        return FileNode.File(name, content, dir.absolutePath)
                    }
                    val children = dir.listFiles()?.map { file ->
                        buildNode(file, file.name)
                    }?.sortedBy { it is FileNode.File } ?: emptyList()
                    return FileNode.Directory(name, children)
                }

                val tree = buildNode(actualRoot, "Repository")
                rootTree = tree
                currentNode = tree
                isLoaded = true
            } else {
                rootTree = sampleRepoTree
                currentNode = rootTree
                isLoaded = true
            }
        }
    }

    if (!isLoaded || currentNode == null) {
        Scaffold(containerColor = MaterialTheme.colorScheme.background) {}
        return
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (currentNode is FileNode.Directory) (currentNode as FileNode.Directory).name else (currentNode as FileNode.File).name, fontSize = 20.sp, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = {
                        if (pathStack.isNotEmpty()) {
                            val newStack = pathStack.dropLast(1)
                            currentNode = if (newStack.isNotEmpty()) newStack.last() else rootTree
                            pathStack = newStack
                        } else {
                            onBack()
                        }
                    }) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            if (currentNode is FileNode.Directory && pathStack.isEmpty()) {
                ExtendedFloatingActionButton(
                    onClick = { showSearchDialog = true },
                    icon = { Icon(Icons.Filled.Search, contentDescription = "Search Files") },
                    text = { Text("Search Files") },
                    containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.9f)
                )
            }
        }
    ) { paddingValues ->
        when (val node = currentNode) {
            is FileNode.Directory -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    items(node.children) { child ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    if (child is FileNode.Directory) {
                                        pathStack = pathStack + child
                                        currentNode = child
                                    } else {
                                        val fileNode = child as FileNode.File
                                        val isMedia = fileNode.name.lowercase().let { n -> n.endsWith(".jpg") || n.endsWith(".png") || n.endsWith(".mp4") || n.endsWith(".gif") || n.endsWith(".webp") || n.endsWith(".jpeg") }
                                        if (isMedia && fileNode.absolutePath.isNotEmpty()) {
                                            try {
                                                val javaFile = java.io.File(fileNode.absolutePath)
                                                val uri = androidx.core.content.FileProvider.getUriForFile(
                                                    context,
                                                    "com.tensei.gitatlas.fileprovider",
                                                    javaFile
                                                )
                                                val intent = android.content.Intent(android.content.Intent.ACTION_VIEW).apply {
                                                    setDataAndType(uri, if (fileNode.name.lowercase().endsWith(".mp4")) "video/*" else "image/*")
                                                    addFlags(android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION)
                                                }
                                                context.startActivity(android.content.Intent.createChooser(intent, "Open with"))
                                            } catch (e: Exception) {
                                                android.widget.Toast.makeText(context, "Error: ${e.message}", android.widget.Toast.LENGTH_LONG).show()
                                            }
                                        } else {
                                            pathStack = pathStack + node
                                            currentNode = child
                                        }
                                    }
                                }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (child is FileNode.Directory) Icons.Filled.Folder else Icons.Filled.Article,
                                contentDescription = if (child is FileNode.Directory) "Folder" else "File",
                                tint = if (child is FileNode.Directory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = child.name,
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                    }
                }
            }
            is FileNode.File -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f), shape = MaterialTheme.shapes.medium)
                ) {
                    val lines = remember(node.content) { node.content.split('\n') }
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        items(lines) { line ->
                            Text(
                                text = line,
                                color = MaterialTheme.colorScheme.onSurface,
                                fontFamily = FontFamily.Monospace,
                                fontSize = 14.sp,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }
            null -> {
                Box(modifier = Modifier.fillMaxSize())
            }
        }
        
        if (showSearchDialog) {
            var searchQuery by remember { mutableStateOf("") }
            val allFiles = remember(rootTree) {
                val list = mutableListOf<FileNode.File>()
                fun traverse(node: FileNode) {
                    if (node is FileNode.File) list.add(node)
                    else if (node is FileNode.Directory) node.children.forEach { traverse(it) }
                }
                traverse(rootTree)
                list
            }
            val filteredFiles = remember(searchQuery) {
                if (searchQuery.isBlank()) emptyList()
                else allFiles.filter { it.name.contains(searchQuery, ignoreCase = true) }.take(50)
            }

            AlertDialog(
                onDismissRequest = { showSearchDialog = false },
                title = { Text("Search Repository", fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth().fillMaxHeight(0.8f)) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("Enter file name...") },
                            singleLine = true,
                            leadingIcon = { Icon(Icons.Filled.Search, contentDescription = "Search") }
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        LazyColumn(modifier = Modifier.fillMaxWidth().weight(1f)) {
                            items(filteredFiles) { fileNode ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            currentNode = fileNode
                                            showSearchDialog = false
                                        }
                                        .padding(vertical = 12.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(Icons.Filled.Article, contentDescription = "File", tint = MaterialTheme.colorScheme.primary)
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Text(fileNode.name, fontSize = 16.sp, maxLines = 1, color = MaterialTheme.colorScheme.onSurface)
                                }
                                HorizontalDivider()
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showSearchDialog = false }) { Text("Close") }
                }
            )
        }
    }
}
