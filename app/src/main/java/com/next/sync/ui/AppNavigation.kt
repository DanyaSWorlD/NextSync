package com.next.sync.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import coil.compose.rememberAsyncImagePainter
import com.next.sync.R
import com.next.sync.ui.components.bottom_bar.BottomBarScreen
import com.next.sync.ui.folderPicker.LocalFolderPickerScreen
import com.next.sync.ui.folderPicker.RemoteFolderPickerScreen
import com.next.sync.ui.home.HomeScreen
import com.next.sync.ui.home.HomeViewModel
import com.next.sync.ui.login.LoginScreen
import com.next.sync.ui.login.LoginViewModel
import com.next.sync.ui.login.LoginWebViewScreen
import com.next.sync.ui.options.OptionsScreen
import com.next.sync.ui.tasks.TasksScreen
import com.next.sync.ui.tasks_create.CreateTaskScreen
import com.next.sync.ui.theme.AppTheme

@Composable
fun AppNavigation(
    loginViewModel: LoginViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
) {
    val navController = rememberNavController()
    val navigate: (String) -> Unit = { route -> navController.navigate(route) }

    Scaffold(
        topBar = {
            if (loginViewModel.loginState.isLoggedIn)
                AccountTopBar(
                    loginViewModel.loginState.user,
                    loginViewModel.loginState.serverAddress
                )
        },
        bottomBar = {
            if (loginViewModel.loginState.isLoggedIn)
                AppBottomBar(navController = navController)
        },
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
        ) {
            NavHost(
                navController = navController,
                startDestination = if (loginViewModel.loginState.isLoggedIn) BottomBarScreen.Home.route else Routes.LoginScreen.name
            ) {
                composable(route = BottomBarScreen.Home.route) {
                    HomeScreen(
                        homeEvents = homeViewModel::onEvent,
                        onNavigate = navigate,
                        homeState = homeViewModel.homeState
                    )
                }

                composable(route = BottomBarScreen.Tasks.route) {
                    TasksScreen(navigate)
                }

                composable(route = BottomBarScreen.Options.route) {
                    OptionsScreen()
                }

                composable(route = Routes.LoginScreen.name) {
                    LoginScreen(
                        loginState = loginViewModel.loginState,
                        loginEvents = loginViewModel::onEvent,
                        navigate = navigate
                    )
                }

                composable(route = Routes.LoginWebViewScreen.name) {
                    LoginWebViewScreen(
                        loginState = loginViewModel.loginState,
                        navigate = navigate,
                        loginViewModel
                    )
                }

                composable(route = Routes.CreateTasksScreen.name) {
                    CreateTaskScreen(navigate)
                }

                composable(route = Routes.FolderPickerLocalScreen.name) {
                    LocalFolderPickerScreen()
                }

                composable(route = Routes.FolderPickerRemoteScreen.name) {
                    RemoteFolderPickerScreen()
                }
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private fun AccountTopBar(account: String, server: String) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface
        ),
        title = {
            Row {
                Column {
                    Spacer(modifier = Modifier.padding(top = 8.dp))
                    Row {
                        Icon(
                            painter = painterResource(R.drawable.baseline_https_24),
                            contentDescription = null,
                            modifier = Modifier.padding(end = 4.dp)
                        )
                        Text(
                            text = server,
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            lineHeight = 20.sp
                        )
                    }
                    Text(
                        text = account,
                        fontSize = 16.sp,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
                Box(
                    Modifier
                        .fillMaxWidth()
                        .padding(8.dp)
                        .padding(end = 8.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter("$server/index.php/avatar/$account/512?v=0"),
                        contentDescription = null,
                        modifier = Modifier
                            .height(44.dp)
                            .aspectRatio(1f)
                            .align(Alignment.CenterEnd)
                            .clip(RoundedCornerShape(100))
                            .background(MaterialTheme.colorScheme.onBackground)
                    )
                }
            }

        }
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
fun AccountTopBarPreview() {
    AppTheme(false) {
        AccountTopBar(account = "username", server = "cloud.example.com")
    }
}

@Preview
@Composable
fun AppPreview() {
    AppTheme(false) {
        AppNavigation(hiltViewModel())
    }
}

@Preview
@Composable
fun AppDarkThemePreview() {
    AppTheme(true) {
        AppNavigation(hiltViewModel())
    }
}