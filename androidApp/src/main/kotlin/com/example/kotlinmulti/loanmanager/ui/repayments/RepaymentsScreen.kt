package com.example.kotlinmulti.loanmanager.ui.repayments

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.example.kotlinmulti.loanmanager.ui.loans.LoansScreen

/** Keeps the former customer directory layout while showing repayment history on selection. */
@Composable
fun RepaymentsScreen(
    navController: NavHostController,
    authToken: String?,
    showAdmin: Boolean = false
) {
    LoansScreen(
        navController = navController,
        authToken = authToken,
        showRepaymentDetails = true,
        showAdmin = showAdmin
    )
}
