package com.next.sync.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.next.sync.ui.components.bottom_bar.BottomBarScreen
import com.next.sync.ui.home.HomeScreen
import com.next.sync.ui.options.DashboardScreen
import com.next.sync.ui.tasks.NotificationScreen
import com.next.sync.ui.theme.AppTheme

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomBar(navController = navController) },
    ) { paddingValues ->

        Column(
            modifier = Modifier.fillMaxSize().statusBarsPadding().padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = BottomBarScreen.Home.route
            ) {
                composable(route = BottomBarScreen.Home.route) {
                    HomeScreen()
                }

                composable(route = BottomBarScreen.Tasks.route) {
                    DashboardScreen()
                }

                composable(route = BottomBarScreen.Options.route) {
                    NotificationScreen()
                }

                composable(route = Routes.LoginScreen.name) {
                    LoginScreen()
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun HomeTopBar() {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),

        title = { Text(text = "Home") }
    )
}

@Composable
fun AppBottomBar(
    navController: NavHostController,
) {
    val backStackEntry = navController.currentBackStackEntryAsState()

    val screens = listOf(
        BottomBarScreen.Tasks,
        BottomBarScreen.Home,
        BottomBarScreen.Options
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.surface
    ) {
        screens.forEach { screens ->
            BottomNavigationItem(
                label = { Text(text = screens.label) },
                icon = {
                    Icon(
                        imageVector = screens.icon,
                        contentDescription = screens.route + " icon",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },

                unselectedContentColor = MaterialTheme.colorScheme.onSurface,
                selectedContentColor = MaterialTheme.colorScheme.onSurface,
                selected = screens.route == backStackEntry.value?.destination?.route,

                onClick = {
                    navController.navigate(screens.route) {
                        popUpTo(navController.graph.findStartDestination().id)
                        launchSingleTop = true
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    AppTheme(false) {
        AppNavigation()
    }
}

@Preview
@Composable
fun AppDarkThemePreview() {
    AppTheme(true) {
        AppNavigation()
    }
}