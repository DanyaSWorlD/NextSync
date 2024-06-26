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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.next.sync.core.model.SyncFlowDirection
import com.next.sync.ui.events.CreateTaskEvent
import com.next.sync.ui.theme.AppTheme

@Composable
fun CreateTaskScreen(
    navigate: (String) -> Unit,
    viewModel: CreateTaskViewModel = hiltViewModel(),
) {
    CreateTaskScreen(viewModel.state, viewModel::onEvent, navigate)
}

@Composable
fun CreateTaskScreen(
    state: CreateTaskState,
    event: (CreateTaskEvent) -> Unit,
    navigate: (String) -> Unit
) {
    Column(
        Modifier
            .padding(8.dp)
            .fillMaxSize()
    ) {
        LazyColumn {
            item { Name(state, event) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { Caption(caption = "Synchronisation type") }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { SegmentedButton(state, event) }
            item { Spacer(modifier = Modifier.height(16.dp)) }
            item { PathSelector(state, event, navigate) }

        }
        Spacer(modifier = Modifier.weight(1f))
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.width(80.dp))
            Button(
                onClick = { event(CreateTaskEvent.Save(navigate)) },
                modifier = Modifier.weight(1f)
            ) {
                Text(text = "Save")
            }
            Spacer(modifier = Modifier.width(80.dp))
        }

    }
}

@Composable
private fun Name(state: CreateTaskState, event: (CreateTaskEvent) -> Unit) {
    OutlinedTextField(
        value = state.name,
        label = { Text(text = "Name") },
        onValueChange = { event(CreateTaskEvent.SetName(it)) },
        modifier = Modifier.fillMaxWidth(),
    )
}

@Composable
private fun Caption(caption: String) {
    Text(text = caption, fontWeight = FontWeight.Bold)
}

@Composable
private fun SegmentedButton(state: CreateTaskState, event: (CreateTaskEvent) -> Unit) {
    OutlinedCard(
        colors = CardDefaults.cardColors(
            containerColor = Color.Transparent
        ), modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxSize()
        ) {
            Segment(label = "To device", SyncFlowDirection.ToDevice, state.direction, event)
            VerticalDivider(Modifier.height(80.dp))
            Segment(label = "Other", SyncFlowDirection.Other, state.direction, event)
            VerticalDivider(Modifier.height(80.dp))
            Segment(label = "To cloud", SyncFlowDirection.ToCloud, state.direction, event)
        }
    }
}

@Composable
private fun Segment(
    label: String,
    radioButtonValue: SyncFlowDirection,
    direction: SyncFlowDirection,
    event: (CreateTaskEvent) -> Unit
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        RadioButton(
            selected = radioButtonValue == direction,
            onClick = { event(CreateTaskEvent.SetDirection(radioButtonValue)) })
        Text(text = label)
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun PathSelector(
    state: CreateTaskState,
    event: (CreateTaskEvent) -> Unit,
    navigate: (String) -> Unit
) {
    Row {
        Box(modifier = Modifier.weight(1f)) {
            Folder(
                "Device folder",
                state.localPath
            ) { event(CreateTaskEvent.OpenLocalPicker(navigate)) }
        }
        Spacer(modifier = Modifier.width(8.dp))
        Box(modifier = Modifier.weight(1f)) {
            Folder(
                "Cloud folder", state.remotePath
            ) { event(CreateTaskEvent.OpenRemotePicker(navigate)) }
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
            CreateTaskScreen(CreateTaskState(), {}, {})
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            CreateTaskScreen(CreateTaskState(), {}, {})
        }
    }
}