package com.example.kotlinmulti.loanmanager.ui.customers

import com.example.kotlinmulti.loanmanager.domain.model.CustomerRecord
import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord

data class CustomersUiState(
    val searchQuery: String = "",
    val selectedType: String = "All",
    val selectedStatus: String = "All",
    val showFilterSheet: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val records: List<CustomerRecord> = emptyList(),
    val activeRepayments: List<RepaymentRecord> = emptyList()
) {
    val headerLabel: String
        get() = when {
            selectedType == "All" && selectedStatus == "All" -> "All - All"
            selectedType == "All" -> "All Types - $selectedStatus"
            selectedStatus == "All" -> "$selectedType - All Statuses"
            else -> "$selectedType - $selectedStatus"
        }
}