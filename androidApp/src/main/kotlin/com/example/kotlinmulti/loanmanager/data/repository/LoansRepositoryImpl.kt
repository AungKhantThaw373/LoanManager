package com.example.kotlinmulti.loanmanager.data.repository

import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord
import com.example.kotlinmulti.loanmanager.domain.model.LoanPage
import com.example.kotlinmulti.loanmanager.domain.repository.LoansRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext

class LoansRepositoryImpl(
    private val apiService: LoanApiService,
    private val authToken: String? = null
) : LoansRepository {

    override suspend fun fetchLoans(
        page: Int?,
        limit: Int?,
        search: String?,
        type: String?,
        status: String?
    ): Result<LoanPage> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLoans(
                token = authToken?.let { "Bearer $it" },
                page = page,
                limit = limit,
                search = search?.trim()?.takeIf { it.isNotBlank() },
                loanType = type.toApiLoanType(),
                status = status?.takeIf { it.isNotBlank() && !it.equals("All", ignoreCase = true) }
                    ?.uppercase()
            )

            if (!response.success) {
                throw IllegalStateException(response.message ?: "The API could not fetch loans.")
            }

            val records = response.data?.map { dto ->
                LoanRecord(
                    loanId = dto.id,
                    id = dto.loanIdNo ?: dto.id,
                    date = dto.loanCreatedAt?.take(10) ?: "N/A",
                    name = dto.customer?.name ?: dto.group?.name ?: "Unknown",
                    amount = "${dto.requestedAmount ?: "0"} MMK",
                    totalToPay = "${dto.summary?.totalAmountToPay?.toLong() ?: 0} MMK",
                    paidAmount = "${dto.summary?.totalPaid?.toLong() ?: 0} MMK",
                    remainingBalance = "${dto.summary?.remainingBalance?.toLong() ?: 0} MMK",
                    type = dto.loanType ?: "INDIVIDUAL",
                    status = dto.status ?: "PENDING"
                )
            } ?: emptyList()

            Result.success(
                LoanPage(
                    records = records,
                    totalRecords = response.pagination?.total ?: records.size
                )
            )
        } catch (e: Exception) {
            if (e is CancellationException) throw e
            Result.failure(e)
        }
    }

    private fun String?.toApiLoanType(): String? = when (this?.trim()?.lowercase()) {
        null, "", "all" -> null
        "personal", "individual" -> "INDIVIDUAL"
        "group" -> "GROUP"
        "business" -> "BUSINESS"
        "special" -> "SPECIAL"
        else -> this.uppercase()
    }
}
