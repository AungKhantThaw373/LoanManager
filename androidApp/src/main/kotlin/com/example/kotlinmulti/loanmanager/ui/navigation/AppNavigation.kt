package com.example.kotlinmulti.loanmanager.ui.navigation

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlinmulti.loanmanager.ui.customers.CustomersScreen

@Composable
fun AppNavigation() {

    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "loans"
    ) {

        composable("loans") {
            CustomersScreen(
                navController = navController
            )
        }

        composable("repayments") {
            CustomersScreen(
                navController = navController
            )
        }

        composable("profile") {
            Text("Profile")
        }
    }
}
