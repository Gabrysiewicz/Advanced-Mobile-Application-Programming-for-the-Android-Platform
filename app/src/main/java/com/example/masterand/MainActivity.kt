package com.example.masterand

import GameView
import HighScoreView
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.masterand.ui.theme.MasterAndTheme
import com.example.masterand.view.CreateProfileView
import com.example.masterand.view.ProfileListView
import com.example.masterand.view.ProfileView
import com.example.masterand.dao.PlayerDao
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject
import com.example.masterand.view.EditProfileView
import androidx.compose.animation.*
import androidx.compose.ui.Alignment
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable

class MainActivity : ComponentActivity() {
    // Inject PlayerDao using Koin
    private val playerDao: PlayerDao by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Clear the database on start, TODO: delete for production
        lifecycleScope.launch {
            playerDao.clearPlayers()
            playerDao.clearGameResults()
        }

        setContent {
            MasterAndTheme {
                AppNavigation(playerDao)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavigation(playerDao: PlayerDao) {
    val navController = rememberNavController()

    val allGameColors = remember {
        listOf(
            Color.Red,
            Color.Green,
            Color.Blue,
            Color.Yellow,
            Color.Magenta,
            Color.Cyan,
            Color.DarkGray,
            Color(0xFF8B4513),
            Color(0xFF4B0082),
            Color(0xFFFF8C00)
        )
    }

    AnimatedNavHost(
        navController = navController,
        startDestination = "ProfileList"
    ) {
        // Profile List Screen (Horizontal Transition)
        composable(
            route = "ProfileList",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() }
        ) {
            ProfileListView(navController, playerDao)
        }

        // Create Profile Screen (Vertical Transition)
        composable(
            route = "CreateProfile",
            enterTransition = { slideInVertically(initialOffsetY = { 2000 }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { -2000 }) + fadeOut() }
        ) {
            CreateProfileView(navController, playerDao)
        }

        // Profile View Screen (Horizontal Transition)
        composable(
            route = "ProfileView/{playerId}",
            arguments = listOf(navArgument("playerId") { type = NavType.IntType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() }
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: 0
            ProfileView(navController, playerId, playerDao)
        }

        // Edit Profile Screen (Vertical Transition)
        composable(
            route = "EditProfile/{playerId}",
            arguments = listOf(navArgument("playerId") { type = NavType.IntType }),
            enterTransition = { slideInVertically(initialOffsetY = { 2000 }) + fadeIn() },
            exitTransition = { slideOutVertically(targetOffsetY = { -2000 }) + fadeOut() }
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: 0
            EditProfileView(navController, playerId, playerDao)
        }

        // Game Screen (Horizontal Transition)
        composable(
            route = "GameView/{colorCount}/{playerId}",
            arguments = listOf(
                navArgument("colorCount") { type = NavType.IntType },
                navArgument("playerId") { type = NavType.IntType }
            ),
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() }
        ) { backStackEntry ->
            val colorCount = backStackEntry.arguments?.getInt("colorCount") ?: 4
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: 0
            val selectedColors = allGameColors.take(colorCount)

            GameView(
                navController = navController,
                availableColors = selectedColors,
                playerId = playerId,
                playerDao = playerDao
            )
        }

        // High Score Screen (Horizontal Transition)
        composable(
            route = "HighScoreView/{playerId}",
            arguments = listOf(navArgument("playerId") { type = NavType.IntType }),
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() }
        ) { backStackEntry ->
            val playerId = backStackEntry.arguments?.getInt("playerId") ?: 0
            HighScoreView(navController, playerDao, playerId)
        }
    }
}
