package com.next.sync.ui.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.next.sync.ui.theme.AppTheme

@Composable
fun TasksScreen() {
    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { /*TODO*/ },
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
        }
    }
}

@Composable
fun MasterSwitch() {
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
@Preview
fun DashboardScreenPreview() {
    AppTheme(false) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            TasksScreen()
        }
    }
}

@Composable
@Preview
fun DashboardScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            TasksScreen()
        }
    }
}

