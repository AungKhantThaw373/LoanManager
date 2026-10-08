package com.example.kotlinmulti.loanmanager.data.remote

import com.google.gson.annotations.SerializedName

// Response from GET /api/loans
data class LoansApiResponse(
    val success: Boolean,
    val message: String?,
    val data: List<LoanDto>?,
    val pagination: PaginationDto?
)

data class LoanDto(
    val id: String,
    val loanNumber: String?,
    val loanIdNo: String?,
    val loanType: String?,
    val requestedAmount: String?,
    val status: String?,
    val loanCreatedAt: String?,
    @SerializedName("Customer") val customer: CustomerInfoDto?,
    @SerializedName("Group") val group: GroupInfoDto?,
    val summary: LoanSummaryDto?
)

data class CustomerInfoDto(
    val id: String,
    val name: String,
    val phone: String?
)

data class GroupInfoDto(
    val id: String,
    val name: String
)

data class LoanSummaryDto(
    val totalAmountToPay: Double?,
    val totalPaid: Double?,
    val remainingBalance: Double?
)

data class PaginationDto(
    val total: Int?,
    val page: Int?,
    val limit: Int?,
    val totalPages: Int?
)

// Response from GET /api/repayments/loan/:loanId
data class RepaymentHistoryApiResponse(
    val success: Boolean,
    val data: List<RepaymentHistoryDto>?
)

data class RepaymentHistoryDto(
    val id: String,
    val repaymentNo: String?,
    val amountPaid: Double?,
    val paymentMethod: String?,
    val paymentDate: String?,
    val notes: String?
)