package com.example.kotlinmulti.loanmanager.ui.repayments

import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord

data class RepaymentUiState(
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val records: List<RepaymentRecord> = emptyList()
)
