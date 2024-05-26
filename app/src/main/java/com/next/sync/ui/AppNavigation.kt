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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.next.sync.ui.components.bottom_bar.BottomBarScreen
import com.next.sync.ui.home.HomeScreen
import com.next.sync.ui.home.HomeViewModel
import com.next.sync.ui.login.LoginScreen
import com.next.sync.ui.login.LoginWebViewScreen
import com.next.sync.ui.login.LoginViewModel
import com.next.sync.ui.options.DashboardScreen
import com.next.sync.ui.tasks.NotificationScreen
import com.next.sync.ui.theme.AppTheme

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = viewModel(),
    homeViewModel: HomeViewModel = viewModel()
) {
    val navController = rememberNavController()
    val navigate : (String) -> Unit = { route -> navController.navigate(route) }

    LaunchedEffect(loginViewModel.loginState.isLoggedIn) {
        //navController.navigate(BottomBarScreen.Home.route)
    }

    Scaffold(
        topBar = { HomeTopBar() },
        bottomBar = { AppBottomBar(navController = navController) },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.LoginScreen.name
            ) {
                composable(route = BottomBarScreen.Home.route) {
                    HomeScreen(onNavigate = { _ -> navController.navigate(BottomBarScreen.Tasks.route) })
                }

                composable(route = BottomBarScreen.Tasks.route) {
                    DashboardScreen()
                }

                composable(route = BottomBarScreen.Options.route) {
                    NotificationScreen()
                }

                composable(route = Routes.LoginScreen.name) {
                    LoginScreen(
                        loginState = loginViewModel.loginState,
                        loginEvents = loginViewModel::onEvent,
                        navigate = navigate)
                }

                composable(route = Routes.LoginWebViewScreen.name){
                    LoginWebViewScreen(loginState = loginViewModel.loginState)
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
                label = { Text(text = screens.label, color = MaterialTheme.colorScheme.onSurface) },
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