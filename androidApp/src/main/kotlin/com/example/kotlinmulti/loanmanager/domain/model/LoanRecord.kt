package com.example.kotlinmulti.loanmanager.domain.model

data class LoanRecord(
    val loanId: String,
    val id: String,
    val date: String,
    val name: String,
    val amount: String,
    val totalToPay: String,
    val paidAmount: String,
    val remainingBalance: String,
    val type: String,
    val status: String
)

data class LoanPage(
    val records: List<LoanRecord>,
    val totalRecords: Int
)

data class RepaymentRecord(
    val id: String,
    val paymentMethod: String,
    val date: String,
    val amount: String,
    val statusNote: String?
)
