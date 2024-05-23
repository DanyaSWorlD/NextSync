package com.next.sync

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.BottomNavigation
import androidx.compose.material.BottomNavigationItem
import androidx.compose.material.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
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

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    MainScreen()
                }
            }
        }
    }

//    private lateinit var binding: ActivityMainBinding

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//
//        binding = ActivityMainBinding.inflate(layoutInflater)
//        setContentView(binding.root)
//
//        val navView: BottomNavigationView = binding.navView
//
//        val navController = findNavController(R.id.nav_host_fragment_activity_main)
//        // Passing each menu ID as a set of Ids because each
//        // menu should be considered as top level destinations.
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                R.id.navigation_home, R.id.navigation_dashboard, R.id.navigation_notifications
//            )
//        )
//        setupActionBarWithNavController(navController, appBarConfiguration)
//        navView.setupWithNavController(navController)
//        promptUserForPermissions()
//    }

//    private fun promptUserForPermissions() {
//        val permissionsToRequest = arrayOf(
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.INTERNET,
//            Manifest.permission.WAKE_LOCK,
//            Manifest.permission.BATTERY_STATS,
//            Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
//        )
//
//        val permissionsGranted = permissionsToRequest.all { permission ->
//            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
//        }
//
//        if (permissionsGranted) {
//            // All permissions are already granted, proceed with your operation.
//        } else {
//            ActivityCompat.requestPermissions(
//                this, permissionsToRequest, REQUEST_PERMISSIONS
//            )
//        }
//    }
//
//
//    companion object {
//        private const val REQUEST_PERMISSIONS = 101
//    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val navController = rememberNavController()
    Scaffold(
        topBar = {
            TopAppBar(
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                title = { androidx.compose.material3.Text(text = "Home") }
            )
        },
        bottomBar = { AppBottomBar(navController = navController) },
    ) //content:
    { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(paddingValues)
        ) {
            BottomNavigationGraph(
                navController = navController,
                paddingModifier = Modifier.padding(paddingValues)
            )
        }

    }
}

@Composable
fun AppBottomBar(navController: NavHostController) {
    val screens = listOf(
        BottomBarScreen.Tasks,
        BottomBarScreen.Home,
        BottomBarScreen.Options
    )
    BottomNavigation(
        backgroundColor = MaterialTheme.colorScheme.primary
    ) {
        screens.forEach { screen ->
            AddItem(
                screen = screen,
                navController = navController,
            )
        }
    }
}

@Composable
fun RowScope.AddItem(
    screen: BottomBarScreen,
    navController: NavHostController
) {
    val backStackEntry = navController.currentBackStackEntryAsState()
    BottomNavigationItem(
        label = {
            Text(text = screen.label)
        },
        icon = {
            Icon(
                imageVector = screen.icon,
                contentDescription = screen.route + " icon",
                tint = MaterialTheme.colorScheme.onPrimary
            )
        },
        unselectedContentColor = MaterialTheme.colorScheme.onPrimary,
        selectedContentColor = MaterialTheme.colorScheme.onSecondary,
        selected = screen.route == backStackEntry.value?.destination?.route,
        onClick = {
            navController.navigate(screen.route) {
                popUpTo(navController.graph.findStartDestination().id)
                launchSingleTop = true
            }
        }
    )
}

@Composable
fun BottomNavigationGraph(
    navController: NavHostController,
    paddingModifier: Modifier
) {
    NavHost(
        navController = navController,
        startDestination = BottomBarScreen.Home.route
    ) {
        composable(route = BottomBarScreen.Home.route) {
            HomeScreen(paddingModifier)
        }
        composable(route = BottomBarScreen.Tasks.route) {
            DashboardScreen(paddingModifier)
        }
        composable(route = BottomBarScreen.Options.route) {
            NotificationScreen(paddingModifier)
        }
    }
}

@Preview
@Composable
fun AppPreview() {
    AppTheme(false) {
        MainScreen()
    }
}

@Preview
@Composable
fun AppDarkThemePreview() {
    AppTheme(true) {
        MainScreen()
    }
}