package com.family.order

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.family.order.ui.components.GuideDialog
import com.family.order.ui.screen.AdminScreen
import com.family.order.ui.screen.CategoryScreen
import com.family.order.ui.screen.ConfirmScreen
import com.family.order.ui.screen.DishEditScreen
import com.family.order.ui.screen.HomeScreen
import com.family.order.ui.screen.MineScreen
import com.family.order.ui.screen.OrderAdminScreen
import com.family.order.ui.screen.OrdersScreen
import com.family.order.ui.theme.FamilyOrderTheme
import kotlinx.coroutines.launch

/** 底部导航的三个主标签 */
private val TAB_ROUTES = listOf("home", "orders", "mine")

private data class TabItem(val route: String, val label: String, val icon: androidx.compose.ui.graphics.vector.ImageVector)

private val TABS = listOf(
    TabItem("home", "点菜", Icons.Filled.Home),
    TabItem("orders", "订单", Icons.Filled.ReceiptLong),
    TabItem("mine", "我的", Icons.Filled.Person)
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            FamilyOrderTheme {
                val navController = rememberNavController()
                val app = applicationContext as FamilyOrderApp
                val scope = rememberCoroutineScope()

                // 首次打开引导
                var showGuide by remember { mutableStateOf(false) }
                val guideShown by app.container.settingsRepository.guideShown
                    .collectAsStateWithLifecycle(initialValue = false)
                LaunchedEffect(guideShown) {
                    if (!guideShown) showGuide = true
                }

                val navBackStackEntry by navController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route
                val showBottomBar = TAB_ROUTES.any { it == currentRoute }

                Scaffold(
                    bottomBar = {
                        if (showBottomBar) BottomNavBar(navController, currentRoute)
                    }
                ) { padding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        composable("home") { HomeScreen(navController) }
                        composable("orders") { OrdersScreen() }
                        composable("mine") { MineScreen(navController) }
                        composable("confirm") { ConfirmScreen(navController) }
                        composable("admin") { AdminScreen(navController) }
                        composable("dishEdit?dishId={dishId}") { DishEditScreen(navController) }
                        composable("category") { CategoryScreen(navController) }
                        composable("orderAdmin") { OrderAdminScreen(navController) }
                    }
                }

                if (showGuide) {
                    GuideDialog(
                        onDismiss = {
                            showGuide = false
                            scope.launch { app.container.settingsRepository.setGuideShown(true) }
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun BottomNavBar(navController: NavHostController, currentRoute: String?) {
    NavigationBar {
        val items = TABS
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        items.forEach { tab ->
            val selected = navBackStackEntry?.destination?.hierarchy?.any { it.route == tab.route } == true
            NavigationBarItem(
                selected = selected,
                onClick = {
                    navController.navigate(tab.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
                alwaysShowLabel = true
            )
        }
    }
}
