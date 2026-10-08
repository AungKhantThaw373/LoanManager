package com.example.kotlinmulti.loanmanager.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.material3.CircularProgressIndicator
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.kotlinmulti.loanmanager.ui.auth.LoginScreen
import com.example.kotlinmulti.loanmanager.ui.loans.LoansScreen
import com.example.kotlinmulti.loanmanager.ui.repayments.RepaymentsScreen
import com.example.kotlinmulti.loanmanager.ui.profile.ProfileScreen
import com.example.kotlinmulti.loanmanager.ui.auth.AuthSessionViewModel
import com.example.kotlinmulti.loanmanager.ui.loans.LoanDetailsRoute
import com.example.kotlinmulti.loanmanager.ui.repayments.RepaymentDetailsScreen
import com.example.kotlinmulti.loanmanager.ui.repayments.RepaymentViewModel
import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord
import com.google.gson.Gson

@Composable
fun AppNavigation() {

    val navController = rememberNavController()
    val session: AuthSessionViewModel = viewModel()
    val authToken by session.token.collectAsStateWithLifecycle()

    NavHost(
        navController = navController,
        startDestination = if (authToken == null) "login" else "loans",
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {

        composable("login") {
            LoginScreen(
                onLoginSuccess = { token ->
                    session.setSignedIn(token)
                    navController.navigate("loans") {
                        popUpTo("login") { inclusive = true }
                        launchSingleTop = true
                    }
                }
            )
        }

        composable("loans") {
            LoansScreen(
                navController = navController,
                authToken = authToken
            )
        }

        composable("repayments") {
            RepaymentsScreen(
                navController = navController,
                authToken = authToken
            )
        }

        composable(
            route = "loanDetails/{loanId}",
            arguments = listOf(navArgument("loanId") { type = NavType.StringType })
        ) { entry ->
            LoanDetailsRoute(
                loanId = entry.arguments?.getString("loanId").orEmpty(),
                authToken = authToken,
                onBackClick = { navController.popBackStack() }
            )
        }

        composable(
            route = "repaymentDetails/{loanId}?record={record}",
            arguments = listOf(
                navArgument("loanId") { type = NavType.StringType },
                navArgument("record") { type = NavType.StringType; defaultValue = "" }
            )
        ) { entry ->
            val recordJson = entry.arguments?.getString("record").orEmpty()
            val record = remember(recordJson) {
                runCatching { Gson().fromJson(recordJson, LoanRecord::class.java) }.getOrNull()
            }
            val repaymentViewModel: RepaymentViewModel = viewModel(
                key = "repaymentDetails-${entry.arguments?.getString("loanId")}",
                factory = RepaymentViewModel.provideFactory(authToken)
            )
            val repaymentState by repaymentViewModel.uiState.collectAsStateWithLifecycle()
            val loanId = entry.arguments?.getString("loanId").orEmpty()
            LaunchedEffect(loanId) { repaymentViewModel.fetchForLoan(loanId) }
            if (record == null) {
                LaunchedEffect(entry.id) { navController.popBackStack() }
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                RepaymentDetailsScreen(
                    record = record,
                    repaymentHistory = repaymentState.records,
                    isLoading = repaymentState.isLoading,
                    errorMessage = repaymentState.errorMessage,
                    onRetry = repaymentViewModel::retry,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }

        composable("profile") {
            val token = authToken
            if (token == null) {
                navController.navigate("login") {
                    popUpTo("profile") { inclusive = true }
                    launchSingleTop = true
                }
            } else {
                ProfileScreen(
                    authToken = token,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("loans") { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onLogout = {
                        session.signOut()
                        navController.navigate("login") {
                            popUpTo("loans") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}
