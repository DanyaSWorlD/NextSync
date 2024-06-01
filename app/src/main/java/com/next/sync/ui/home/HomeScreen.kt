package com.next.sync.ui.home

import androidx.annotation.DrawableRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.next.sync.R
import com.next.sync.ui.Routes
import com.next.sync.ui.components.bottom_bar.BottomBarScreen
import com.next.sync.ui.events.HomeEvents
import com.next.sync.ui.theme.AppTheme


@Composable
fun HomeScreen(
    homeEvents: (HomeEvents) -> Unit,
    onNavigate: (String) -> Unit,
    homeState: HomeState
) {
    LazyColumn {
        item { Spacer(modifier = Modifier.height(8.dp)) }
        item {
            MainCard(
                onSynchronizeNow = { homeEvents(HomeEvents.SynchronizeNow) },
                onAddTask = { onNavigate(BottomBarScreen.Tasks.route) },
                homeState
            )
        }

        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { BatteryCard(onOpenSettings = { onNavigate(Routes.SettingsScreen.name) }) }

        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { StatusCard() }

        item { Spacer(modifier = Modifier.height(8.dp)) }
        item { SyncReportCard(onOpenReport = {}, onDismiss = {}) }

        item { Spacer(modifier = Modifier.height(8.dp)) }
    }
}

@Composable
private fun StatusCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 8.dp, end = 8.dp)
    ) {
        Column {
            Row {
                Box(modifier = Modifier.weight(1f)) {
                    Tile(R.drawable.baseline_network_wifi_24, "WI-FI")
                }

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Tile(R.drawable.baseline_battery_5_bar_24, "Not charging")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Row {

                Box(modifier = Modifier.weight(1f)) {
                    Tile(R.drawable.baseline_cloud_sync_24, "Last sync\n2 hrs ago")
                }

                Spacer(modifier = Modifier.width(8.dp))
                Box(modifier = Modifier.weight(1f)) {
                    Tile(R.drawable.baseline_timer_24, "Next sync\nin 22 hrs")
                }
            }
        }
    }
}

@Composable
private fun SyncReportCard(
    onOpenReport: () -> Unit,
    onDismiss: () -> Unit
) {
    Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp))
    {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column()
            {
                Row(Modifier.padding(8.dp)) {
                    Icon(
                        painter = painterResource(id = R.drawable.baseline_info_24),
                        contentDescription = null, // decorative element
                        modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 16.dp)
                    )
                    Column {
                        Box(
                            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                        ) {
                            Text(text = "Sync report", fontWeight = FontWeight.Bold)
                        }

                        Text(text = "10/10 files uploaded")
                        Text(text = "15MB of bandwidth used")
                        Text(text = "15 seconds runtime")

                        Spacer(Modifier.height(8.dp))
                    }
                }
                Row(horizontalArrangement = Arrangement.SpaceBetween) {
                    Button(
                        onClick = { onOpenReport() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(text = "Open report")
                    }

                    Button(
                        onClick = { onDismiss() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.inversePrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "dismiss")
                    }
                }
            }
        }
    }
}

@Composable
private fun BatteryCard(
    onOpenSettings: () -> Unit
) {
    Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Row(Modifier.padding(8.dp)) {
                Icon(
                    painter = painterResource(id = R.drawable.baseline_battery_alert_24),
                    contentDescription = null,
                    modifier = Modifier.padding(top = 16.dp, start = 8.dp, end = 16.dp)
                )
                Column {

                    Box(
                        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
                    ) {
                        Text(text = "Battery optimisation", fontWeight = FontWeight.Bold)
                    }

                    Text(
                        text = "NextSync working in background to keep your files in sync. " +
                                "To maintain app stability please disable battery optimisations"
                    )

                    Button(
                        onClick = { onOpenSettings() },
                        modifier = Modifier
                            .padding(8.dp)
                            .align(Alignment.End)
                    ) {
                        Text(text = "Open settings")
                    }
                }
            }
        }
    }
}

@Composable
fun MainCard(
    onSynchronizeNow: () -> Unit,
    onAddTask: () -> Unit,
    homeState: HomeState
) {
    Box(modifier = Modifier.padding(start = 8.dp, end = 8.dp)) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
        ) {
            Column {

                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .weight(1f)
                            .padding(16.dp)
                    ) {
                        SpaceGauge(total = homeState.storageTotal, used = homeState.storageUsed)
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        AllTimeStats()
                    }
                }

                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.SpaceBetween) {

                    Button(
                        onClick = { onSynchronizeNow() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp)
                    ) {
                        Text(text = "Synchronize now")
                    }

                    Button(
                        onClick = { onAddTask() },
                        modifier = Modifier
                            .weight(1f)
                            .padding(8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.inversePrimary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) {
                        Text(text = "Add task")
                    }
                }
            }
        }
    }
}

@Composable
fun SpaceGauge(
    used: Long,
    total: Long
) {
    val percentValue = (total / 100)
    val percent = if (percentValue == 0.toLong()) 0 else used / percentValue

    Column(
        Modifier
            .fillMaxWidth()
            .padding(start = 30.dp, end = 20.dp, top = 30.dp, bottom = 20.dp)
    ) {
        Box(
            Modifier
                .weight(1f)
                .fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Text(text = "$percent%", fontSize = 40.sp, textAlign = TextAlign.Center)
        }
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentAlignment = Alignment.TopCenter
        ) {
            val usedPair = humanify(used.toFloat())
            val totalPair = humanify(total.toFloat())

            val usedNumber = usedPair.first
            val usedMetrics = usedPair.second
            val totalNumber = totalPair.first
            val totalMetrics = totalPair.second

            Column(
                Modifier
                    .align(Alignment.Center)
                    .fillMaxWidth()
            ) {
                Text(
                    text = "$usedNumber $usedMetrics",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
                Text(
                    text = "$totalNumber $totalMetrics",
                    textAlign = TextAlign.Center,
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                )
            }
        }
    }
    CircularProgressIndicator(
        color = MaterialTheme.colorScheme.primary,
        trackColor = MaterialTheme.colorScheme.background,
        progress = percent / 100f,
        strokeWidth = 10.dp,
        strokeCap = StrokeCap.Round,
        modifier = Modifier.fillMaxSize()
    )
}

@Composable
fun AllTimeStats() {
    Column {
        Text(text = "All time stats")

        IconText(resource = R.drawable.baseline_cloud_upload_24, text = "Upload")
        Text(text = "1.1GB", Modifier.padding(start = 28.dp))

        IconText(resource = R.drawable.baseline_cloud_download_24, text = "Download")
        Text(text = "270MB", Modifier.padding(start = 28.dp))
    }
}

@Composable
fun IconText(@DrawableRes resource: Int, text: String) {
    Row {
        Icon(
            painter = painterResource(id = resource),
            contentDescription = null
        )
        Text(text = text, Modifier.padding(start = 4.dp))
    }
}

@Composable
fun Tile(@DrawableRes resource: Int, text: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {

        Row(modifier = Modifier.fillMaxHeight()) {
            Icon(
                painter = painterResource(id = resource),
                contentDescription = null, // decorative element
                Modifier.padding(16.dp)
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(text = text, textAlign = TextAlign.Center)
            }
        }
    }
}

val literals = arrayOf("B", "KB", "MB", "GB", "TB", "PB")
fun humanify(bytes: Float, divides: Int = 0): Pair<String, String> {
    if (bytes > 1024)
        return humanify(bytes / 1024, divides + 1)

    var bytesResponse = ""

    if ((bytes * 10).toInt() % 10 == 0)
        bytesResponse = bytes.toInt().toString()
    else
        bytesResponse = String.format("%.1f", bytes)

    return Pair(bytesResponse, literals[divides])
}

@Composable
@Preview
fun HomeScreenPreview() {
    AppTheme(false) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            HomeScreen(
                homeEvents = {},
                onNavigate = {},
                HomeState(
                    storageTotal = 32212254720,
                    storageUsed = 2000000000//21474836480
                ),
            )
        }
    }
}

@Composable
@Preview
fun HomeScreenPreviewDark() {
    AppTheme(true) {
        Box(Modifier.background(color = MaterialTheme.colorScheme.background)) {
            HomeScreen(
                homeEvents = {},
                onNavigate = {},
                HomeState(
                    storageTotal = 32212254720,
                    storageUsed = 2000000000//21474836480
                ),
            )
        }
    }
}