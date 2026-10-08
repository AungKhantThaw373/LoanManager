package com.example.kotlinmulti.loanmanager.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlinmulti.loanmanager.ui.customers.CustomersScreen
import com.example.kotlinmulti.loanmanager.ui.auth.LoginScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    var authToken by remember { mutableStateOf<String?>(null) }

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    authToken = token
                    navController.navigate("loans") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("loans") {
            CustomersScreen(
                navController = navController,
                authToken = authToken
            )
        }

        composable("repayments") {
            CustomersScreen(
                navController = navController,
                authToken = authToken
            )
        }

        composable("profile") {
            Text("Profile")
        }
    }
}
