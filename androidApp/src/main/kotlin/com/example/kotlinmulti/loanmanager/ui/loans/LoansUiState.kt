package com.example.kotlinmulti.loanmanager.ui.loans

import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord

data class LoansUiState(
    val searchQuery: String = "",
    val selectedType: String = "All",
    val selectedStatus: String = "All",
    val showFilterSheet: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val records: List<LoanRecord> = emptyList(),
    val totalRecords: Int = 0
) {
    val headerLabel: String
        get() = listOf(selectedType, selectedStatus)
            .filterNot { it.equals("All", ignoreCase = true) }
            .joinToString(" / ")
            .ifBlank { "All" }
}
