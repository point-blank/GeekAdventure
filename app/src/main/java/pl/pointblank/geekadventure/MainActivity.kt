package pl.pointblank.geekadventure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.animation.core.EaseInOutQuart
import androidx.compose.animation.core.tween
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.pointblank.geekadventure.ui.screens.GameScreen
import pl.pointblank.geekadventure.ui.screens.LobbyScreen
import pl.pointblank.geekadventure.viewmodel.GameViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            val isTablet = windowSizeClass.widthSizeClass != WindowWidthSizeClass.Compact
            
            val navController = rememberNavController()
            val gameViewModel: GameViewModel = viewModel()

            NavHost(
                navController = navController, 
                startDestination = "lobby",
                // GLOBALNE ANIMACJE PRZEJŚĆ
                enterTransition = {
                    fadeIn(animationSpec = tween(500, easing = EaseInOutQuart)) +
                    scaleIn(initialScale = 0.92f, animationSpec = tween(500, easing = EaseInOutQuart))
                },
                exitTransition = {
                    fadeOut(animationSpec = tween(400)) +
                    scaleOut(targetScale = 1.05f, animationSpec = tween(400))
                },
                popEnterTransition = {
                    fadeIn(animationSpec = tween(500)) +
                    scaleIn(initialScale = 1.05f, animationSpec = tween(500))
                },
                popExitTransition = {
                    fadeOut(animationSpec = tween(400)) +
                    scaleOut(targetScale = 0.92f, animationSpec = tween(400))
                }
            ) {
                composable("lobby") {
                    LobbyScreen(
                        viewModel = gameViewModel,
                        isTablet = isTablet,
                        onScenarioSelected = { scenario, enableImages, resume ->
                            gameViewModel.initGame(scenario, enableImages, resume)
                            navController.navigate("game")
                        }
                    )
                }
                composable("game") {
                    GameScreen(
                        viewModel = gameViewModel,
                        isTablet = isTablet,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
