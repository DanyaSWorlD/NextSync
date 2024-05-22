package com.next.sync.ui.components.bottom_bar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.ui.graphics.vector.ImageVector

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