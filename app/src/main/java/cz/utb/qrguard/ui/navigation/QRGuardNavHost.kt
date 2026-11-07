package cz.utb.qrguard.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import cz.utb.qrguard.ui.screens.HistoryScreen
import cz.utb.qrguard.ui.screens.ResultScreen
import cz.utb.qrguard.ui.screens.ScannerScreen

// Položky dolní navigační lišty (Výsledek není hlavní obrazovka,
// otevírá se ze skeneru nebo z historie).
private data class BottomNavItem(val screen: Screen, val icon: ImageVector)

private val bottomNavItems = listOf(
    BottomNavItem(Screen.Scanner, Icons.Filled.QrCodeScanner),
    BottomNavItem(Screen.History, Icons.Filled.History)
)

// Kořenová komponenta aplikace – Scaffold s dolní lištou a NavHost se třemi obrazovkami.
@Composable
fun QRGuardApp(navController: NavHostController = rememberNavController()) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { QRGuardBottomBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Scanner.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Scanner.route) {
                ScannerScreen(
                    onScanned = { navController.navigate(Screen.Result.route) }
                )
            }
            composable(Screen.Result.route) {
                // Data výsledku a akce tlačítek se napojí přes ViewModel.
                ResultScreen(
                    result = null,
                    onBack = { navController.popBackStack() },
                    onOpenInBrowser = {},
                    onSave = {}
                )
            }
            composable(Screen.History.route) {
                HistoryScreen(
                    onOpenResult = { navController.navigate(Screen.Result.route) }
                )
            }
        }
    }
}

@Composable
private fun QRGuardBottomBar(navController: NavHostController) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    NavigationBar {
        bottomNavItems.forEach { item ->
            val selected = currentDestination?.hierarchy?.any {
                it.route == item.screen.route
            } == true

            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(item.screen.route) {
                        // Při přepínání záložek nevrstvíme obrazovky na back stack.
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.screen.title) },
                label = { Text(item.screen.title) }
            )
        }
    }
}
