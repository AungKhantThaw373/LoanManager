package com.example.kotlinmulti.loanmanager.domain.model

data class CustomerRecord(
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

data class RepaymentRecord(
    val id: String,
    val paymentMethod: String,
    val date: String,
    val amount: String,
    val statusNote: String?
)