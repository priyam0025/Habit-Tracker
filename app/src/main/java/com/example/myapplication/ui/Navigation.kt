package com.example.myapplication.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.myapplication.ui.screens.DetailScreen
import com.example.myapplication.ui.screens.HomeScreen
import com.example.myapplication.ui.viewmodel.HitmakerViewModel

@Composable
fun HitmakerApp() {
    val navController = rememberNavController()
    val viewModel: HitmakerViewModel = viewModel()

    NavHost(navController = navController, startDestination = "home") {
        composable("home") {
            HomeScreen(
                viewModel = viewModel,
                onHitmakerClick = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.IntType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getInt("id") ?: 0
            DetailScreen(
                hitmakerId = id,
                viewModel = viewModel,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
