package com.next.sync.ui.folderPicker

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.next.sync.ui.theme.AppTheme

@Composable
fun LocalFolderPickerScreen(viewModel: FolderPickerLocalViewModel = hiltViewModel()) {
    FolderPickerScreen(viewModel.getState(), { viewModel.up() }, { name -> viewModel.select(name) })
}

@Composable
fun RemoteFolderPickerScreen(viewModel: FolderPickerRemoteViewModel = hiltViewModel()) {
    FolderPickerScreen(viewModel.getState(), { viewModel.up() }, { name -> viewModel.select(name) })
}

@Composable
private fun FolderPickerScreen(
    state: FolderPickerState,
    up: () -> Unit,
    open: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Path(state.path) { up() }
        HorizontalDivider()
        Folders(state, open)
    }
}

@Composable
private fun Path(path: String, up: () -> Unit) {
    Row {
        IconButton(onClick = up) {
            Icon(imageVector = Icons.Outlined.ArrowUpward, contentDescription = "Up")
        }
        Icon(
            imageVector = Icons.Outlined.Smartphone,
            contentDescription = null,
            Modifier.padding(start = 8.dp, end = 8.dp, top = 12.dp)
        )
        Text(text = path, modifier = Modifier.padding(top = 12.dp))
    }
}

@Composable
private fun Folders(state: FolderPickerState, click: (String) -> Unit) {
    LazyColumn {
        items(items = state.folders) { folder ->
            Folder(name = folder) { name ->
                click(name)
            }
        }
    }
}

@Composable
private fun Folder(name: String, click: (String) -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 8.dp)
            .clickable { click(name) }
    ) {
        Icon(
            imageVector = Icons.Outlined.Folder,
            contentDescription = null,
            modifier = Modifier.padding(start = 24.dp, end = 16.dp, top = 8.dp, bottom = 8.dp)
        )
        Text(text = name, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
@Preview
fun DashboardScreenPreview() {
    AppTheme(false) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            FolderPickerScreen(
                FolderPickerState(
                    "/storage/emulated/0/",
                    listOf("folder 1", "folder 2", "folder 3", "folder 4", "folder 5")
                ),
                {}, {}
            )
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            FolderPickerScreen(
                FolderPickerState(
                    "/storage/emulated/0/",
                    listOf("folder 1", "folder 2", "folder 3", "folder 4", "folder 5")
                ),
                {}, {}
            )
        }
    }
}