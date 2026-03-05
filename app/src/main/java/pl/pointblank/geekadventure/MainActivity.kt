package pl.pointblank.geekadventure

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import pl.pointblank.geekadventure.ui.screens.GameScreen
import pl.pointblank.geekadventure.ui.screens.LobbyScreen
import pl.pointblank.geekadventure.viewmodel.GameViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            val navController = rememberNavController()
            val gameViewModel: GameViewModel = viewModel()

            NavHost(navController = navController, startDestination = "lobby") {
                composable("lobby") {
                    LobbyScreen(
                        viewModel = gameViewModel,
                        onScenarioSelected = { scenario, enableImages, resume ->
                            gameViewModel.initGame(scenario, enableImages, resume)
                            navController.navigate("game")
                        }
                    )
                }
                composable("game") {
                    GameScreen(
                        viewModel = gameViewModel,
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}
