package com.next.sync.ui.components.bottom_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.next.sync.ui.dashboard.DashboardScreen
import com.next.sync.ui.home.HomeScreen
import com.next.sync.ui.notifications.NotificationScreen

sealed class BottomBarScreen(
    val route: String,
    val label: String,
    val icon: ImageVector
) {
    data object Home : BottomBarScreen(
        route = "home",
        label = "Home",
        icon = Icons.Rounded.Home
    )

    data object Tasks : BottomBarScreen(
        route = "tasks",
        label = "Tasks",
        icon = Icons.Rounded.CheckCircle
    )

    data object Options : BottomBarScreen(
        route = "options",
        label = "Options",
        icon = Icons.Rounded.Settings
    )
}