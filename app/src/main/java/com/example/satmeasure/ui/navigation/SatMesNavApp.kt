
package com.example.satmeasure.ui.navigation

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.satmeasure.ui.main.MainScreen
import com.example.satmeasure.ui.main.HistoryScreen
import com.example.satmeasure.ui.otherScreens.AboutUsScreen
import com.example.satmeasure.ui.otherScreens.HowToCoordinatesScreen
import com.example.satmeasure.ui.otherScreens.SearchScreen
import com.example.satmeasure.ui.otherScreens.SettingsScreen
import com.example.satmeasure.ui.otherScreens.TutorialScreen

const val ANIM_DURATION = 400
val ANIM_EASING = FastOutSlowInEasing

@Composable
fun SatMesNavApp() {
    // --- Bottom Sheet Configuration Variables ---
    // Portrait bottom sheet peek height
    val portraitPeekHeight = 245.dp
    // Portrait  bottom sheet expanded height ratio (percentage of screen height)
    val portraitExpandedHeightRatio = 0.5f
    // Landscape bottom sheet peek height
    val landscapePeekHeight = 95.dp
    // Landscape bottom sheet expanded height ratio (percentage of screen height)
    val landscapeExpandedHeightRatio = 0.93f

    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = (context as? Activity)

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: SatMesRoutes.MAP

    val navigateToDest = { route: String ->
        if (currentRoute != route) {
            navController.navigate(route) {
                popUpTo(SatMesRoutes.MAP) { saveState = true }
                launchSingleTop = true
                restoreState = true
            }
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = SatMesRoutes.MAP,
            modifier = Modifier.fillMaxSize(),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(ANIM_DURATION, easing = ANIM_EASING)
                )
            }
        ) {

            // 1. MAP SCREEN
            composable(route = SatMesRoutes.MAP) {
                var backPressedTime by remember { mutableLongStateOf(0L) }

                // Only intercept hardware back button on the MAP screen
                BackHandler(enabled = currentRoute == SatMesRoutes.MAP) {
                    val currentTime = System.currentTimeMillis()
                    if (currentTime - backPressedTime < 2000) {
                        activity?.finish()
                    } else {
                        backPressedTime = currentTime
                        Toast.makeText(context, "Press back again to exit", Toast.LENGTH_SHORT).show()
                    }
                }

                MainScreen(
                    currentRoute = currentRoute,
                    onNavigate = navigateToDest,
                    portraitPeekHeight = portraitPeekHeight,
                    portraitExpandedHeightRatio = portraitExpandedHeightRatio,
                    landscapePeekHeight = landscapePeekHeight,
                    landscapeExpandedHeightRatio = landscapeExpandedHeightRatio
                )
            }

            // 2. HISTORY SCREEN
            composable(route = SatMesRoutes.HISTORY) {
                HistoryScreen(onBackClick = { navController.popBackStack() })
            }

            // 3. SETTINGS SCREEN
            composable(route = SatMesRoutes.SETTINGS) {
                SettingsScreen(onBackClick = { navController.popBackStack() })
            }

            // 5. ABOUT US SCREEN
            composable(route = SatMesRoutes.ABOUT_US) {
                AboutUsScreen(onBackClick = { navController.popBackStack() })
            }

            // 6. TUTORIAL SCREEN
            composable(route = SatMesRoutes.TUTORIAL) {
                TutorialScreen(onBackClick = { navController.popBackStack() })
            }

            // 7. SEARCH SCREEN
            composable(route = SatMesRoutes.SEARCH) {
                SearchScreen(
                    onBackClick = { navController.popBackStack() },
                    onHowToClick = { navController.navigate(SatMesRoutes.HOW_TO) },
                    onCoordinateSearch = { lat, lng ->
                        navController.popBackStack()
                    },
                    onTextSearch = { query ->
                    }
                )
            }

            // 8. HOW TO GET COORDINATES SCREEN
            composable(route = SatMesRoutes.HOW_TO) {
                HowToCoordinatesScreen(onBackClick = { navController.popBackStack() })
            }
        }
    }
}