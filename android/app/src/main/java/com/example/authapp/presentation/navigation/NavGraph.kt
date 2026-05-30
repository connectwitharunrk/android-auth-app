package com.example.authapp.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.authapp.presentation.screens.auth.login.LoginScreen
import com.example.authapp.presentation.screens.auth.signup.SignupScreen
import com.example.authapp.presentation.screens.home.HomeScreen

@Composable
fun AppNavGraph() {
    val navController = rememberNavController()
    AppNavHost(navController = navController)
}

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Login.route
    ) {
        composable(Screen.Login.route) {
            LoginScreen(navController = navController)
        }
        composable(Screen.Signup.route) {
            SignupScreen(navController = navController)
        }
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }
    }
}

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Signup : Screen("signup")
    object Home : Screen("home")
}
