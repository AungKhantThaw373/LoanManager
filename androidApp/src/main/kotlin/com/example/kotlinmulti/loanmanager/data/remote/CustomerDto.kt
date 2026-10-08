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
    val branch: String? = null,
    val loanType: String?,
    val requestedAmount: String?,
    val status: String?,
    val loanCreatedAt: String?,
    val customerId: String? = null,
    val groupId: String? = null,
    val businessLicence: String? = null,
    val collateralName: String? = null,
    val collateralImage: String? = null,
    val interestRate: String? = null,
    val durationMonths: Int? = null,
    val createdAt: String? = null,
    val updatedAt: String? = null,
    @SerializedName("Customer") val customer: CustomerInfoDto?,
    @SerializedName("Group") val group: GroupInfoDto?,
    val summary: LoanSummaryDto?,
    val repayments: List<RepaymentHistoryDto>? = null,
    val groupMembers: List<GroupMemberDto>? = null
)

data class CustomerInfoDto(
    val id: String,
    val name: String,
    val phone: String?,
    val nationalId: String? = null,
    val fatherName: String? = null,
    val spouseName: String? = null,
    val work: String? = null,
    val address: String? = null,
    val frontNRCUrl: String? = null,
    val backNRCUrl: String? = null,
    val frontHouseholdListUrl: String? = null,
    val backHouseholdListUrl: String? = null
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

data class GroupMemberDto(
    val id: String? = null,
    val roleInGroup: String? = null,
    @SerializedName("Customer") val customer: CustomerInfoDto? = null
)

data class LoanDetailApiResponse(
    val success: Boolean,
    val message: String?,
    val data: LoanDto?
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

data class RepaymentsApiResponse(
    val success: Boolean,
    val data: List<RepaymentListItemDto>?,
    val pagination: PaginationDto?
)

data class RepaymentListItemDto(
    val id: String,
    val repaymentNo: String?,
    val amountPaid: Double?,
    val paymentMethod: String?,
    val paymentDate: String?,
    val notes: String?,
    @SerializedName("Loan") val loan: RepaymentLoanDto?
)

data class RepaymentLoanDto(
    val id: String?,
    val loanNumber: String?,
    val status: String?,
    val loanType: String?,
    @SerializedName("Customer") val customer: CustomerInfoDto?
)
