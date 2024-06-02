package com.next.sync.ui.tasks_create

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.NavigateNext
import androidx.compose.material.icons.outlined.CreateNewFolder
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.next.sync.ui.theme.AppTheme

@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel = hiltViewModel(), navigate: (String) -> Unit
) {
    CreateTaskScreen(
        viewModel.state,
        { n -> viewModel.setName(n) },
        { viewModel.openLocalPicker(navigate) },
        { viewModel.openRemotePicker(navigate) })
}

@Composable
fun CreateTaskScreen(
    state: CreateTaskState,
    nameUpdate: (String) -> Unit,
    navigateLocal: () -> Unit,
    navigateRemote: () -> Unit
) {
    Column(
        Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        LazyColumn {
            item { Name(state, nameUpdate) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Caption(caption = "Synchronisation type") }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SegmentedButton() }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { PathSelector(state, navigateLocal, navigateRemote) }

        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(80.dp))
            Button(onClick = { /*TODO*/ }, modifier = Modifier.weight(1f)) {
                Text(text = "Save")
            }
            Spacer(modifier = Modifier.width(80.dp))
        }

    }
}

@Composable
private fun Name(state: CreateTaskState, nameUpdate: (String) -> Unit) {
    OutlinedTextField(
        value = state.name,
        label = { Text(text = "Name") },
        onValueChange = { nameUpdate(it) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Caption(caption: String) {
    Text(text = caption)
}

@Composable
private fun SegmentedButton() {
    val state = remember { mutableStateOf(3) }
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize()
        ) {
            Segment(label = "To device", state = state, radioButtonValue = 1)
            VerticalDivider(Modifier.height(80.dp))
            Segment(label = "Other", state = state, radioButtonValue = 2)
            VerticalDivider(Modifier.height(80.dp))
            Segment(label = "To cloud", state = state, radioButtonValue = 3)
        }
    }
}

@Composable
private fun Segment(label: String, state: MutableState<Int>, radioButtonValue: Int) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RadioButton(
            selected = radioButtonValue == state.value,
            onClick = { state.value = radioButtonValue })
        Text(text = label)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PathSelector(
    state: CreateTaskState,
    navigateLocal: () -> Unit,
    navigateRemote: () -> Unit,
) {
    Row {
        Box(modifier = Modifier.weight(1f)) {
            Folder(
                "Device folder", state.localPath, navigateLocal
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            Folder(
                "Cloud folder", state.remotePath, navigateRemote
            )
        }
    }
}

@Composable
private fun Folder(title: String, path: String? = null, click: () -> Unit) {
    val pathExists = !path.isNullOrEmpty()
    val folderIcon = if (pathExists) Icons.Outlined.CreateNewFolder else Icons.Outlined.Folder

    Column(modifier = Modifier.fillMaxSize()) {
        Caption(title)
        Spacer(modifier = Modifier.height(8.dp))
        Card(
            modifier = Modifier
                .fillMaxSize()
                .clickable { click() },
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column {
                Spacer(modifier = Modifier.height(8.dp))
                Row {
                    Spacer(modifier = Modifier.width(8.dp))
                    Icon(
                        imageVector = folderIcon,
                        contentDescription = null,
                    )
                    Text(
                        text = "Select folder",
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.NavigateNext,
                        contentDescription = null
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                if (pathExists) {
                    HorizontalDivider(modifier = Modifier.padding(top = 8.dp, bottom = 8.dp))
                    Text(
                        text = path!!,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreview() {
    AppTheme(false) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            CreateTaskScreen(CreateTaskState(), {}, {}, {})
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            CreateTaskScreen(CreateTaskState(), {}, {}, {})
        }
    }
}