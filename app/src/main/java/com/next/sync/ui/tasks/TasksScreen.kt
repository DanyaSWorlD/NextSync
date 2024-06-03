package com.next.sync.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.Cloud
import androidx.compose.material.icons.outlined.Smartphone
import androidx.compose.material.icons.outlined.SwapVert
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.next.sync.core.db.data.TaskEntity
import com.next.sync.core.extensions.toEnum
import com.next.sync.core.model.SyncFlowDirection
import com.next.sync.ui.Routes
import com.next.sync.ui.theme.AppTheme

@Composable
fun TasksScreen(navigate: (String) -> Unit, viewModel: TasksViewModel = hiltViewModel()) {
    TasksScreen(viewModel.state, navigate)
}

@Composable
private fun TasksScreen(
    state: TaskState,
    navigate: (String) -> Unit
) {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navigate(Routes.CreateTasksScreen.name) },
            ) {
                Icon(Icons.Filled.Add, "Floating action button.")
            }
        }
    )
    { paddingValues ->
        LazyColumn(Modifier.padding(paddingValues)) {
            item { Spacer(modifier = Modifier.height(8.dp)) }
            item { MasterSwitch() }
            item { Spacer(modifier = Modifier.height(8.dp)) }
            if (state.tasks.isEmpty()) item { Empty() }
            items(state.tasks) { Task(it) }
        }
    }
}

@Composable
private fun MasterSwitch() {
    Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row {
                Icon(
                    painter = rememberVectorPainter(Icons.Rounded.CloudSync),
                    contentDescription = null, // decorative element
                    modifier = Modifier.padding(24.dp)
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(72.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "Enable auto sync")
                }

                var checked by remember { mutableStateOf(true) }

                Switch(
                    checked = checked, onCheckedChange = { checked = it },
                    modifier = Modifier.padding(
                        start = 24.dp,
                        end = 24.dp,
                        top = 12.dp,
                        bottom = 12.dp
                    )
                )
            }
        }
    }
}

@Composable
private fun Task(task: TaskEntity) {
    Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp, bottom = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column(
                modifier = Modifier
                    .padding(8.dp)
                    .fillMaxWidth()
            ) {
                Text(
                    text = task.name ?: "Task",
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(start = 16.dp, top = 8.dp)
                )
                Row {
                    Icon(
                        imageVector = iconByDirection(task.direction),
                        contentDescription = "Direction both",
                        modifier = Modifier
                            .padding(start = 16.dp, end = 8.dp)
                            .align(Alignment.CenterVertically)
                    )
                    Column {
                        PathRow(icon = Icons.Outlined.Cloud, path = task.remotePath)
                        PathRow(icon = Icons.Outlined.Smartphone, path = task.localPath)
                    }
                }
            }
        }
    }
}

@Composable
private fun PathRow(icon: ImageVector, path: String) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Icon(imageVector = icon, contentDescription = null, modifier = Modifier.padding(8.dp))
        Text(text = path, modifier = Modifier.padding(top = 8.dp))
    }
}

@Composable
private fun Empty() {
    Text(
        text = "Add new task via plus button",
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.outline,
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    )
}

@Composable
@Preview
fun DashboardScreenPreview() {
    AppTheme(false) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            TasksScreen(TaskState(getPreviewData())) {}
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            TasksScreen(TaskState(getPreviewData())) {}
        }
    }
}

private fun getPreviewData(): List<TaskEntity> {
    return listOf(
        TaskEntity(
            accountId = 0,
            name = "Photos",
            localPath = "/DCIM/Camera",
            remotePath = "/Images/Camera",
            direction = 1
        ),
        TaskEntity(
            accountId = 0,
            name = "Screenshots",
            localPath = "/DCIM/Screenshots",
            remotePath = "/Images/Screenshots",
            direction = 2
        ),
    )
}

private fun iconByDirection(direction: Int): ImageVector {
    return when (direction.toEnum<SyncFlowDirection>()) {
        SyncFlowDirection.ToCloud -> Icons.Outlined.ArrowUpward
        SyncFlowDirection.Other -> Icons.Outlined.SwapVert
        SyncFlowDirection.ToDevice -> Icons.Outlined.ArrowDownward
        null -> Icons.AutoMirrored.Outlined.Help
    }

}

