package com.example.kotlinmulti.loanmanager.data.repository

import com.example.kotlinmulti.loanmanager.data.remote.CustomerApiService
import com.example.kotlinmulti.loanmanager.domain.model.CustomerRecord
import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord
import com.example.kotlinmulti.loanmanager.domain.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CustomerRepositoryImpl(
    private val apiService: CustomerApiService,
    private val authToken: String? = null
) : CustomerRepository {

    override suspend fun fetchLoans(
        page: Int?,
        limit: Int?
    ): Result<List<CustomerRecord>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getLoans(
                token = authToken?.let { "Bearer $it" },
                page = page,
                limit = limit
            )

            val records = response.data?.map { dto ->
                CustomerRecord(
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

            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchRepaymentHistory(
        loanId: String
    ): Result<List<RepaymentRecord>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getRepaymentHistory(
                loanId = loanId,
                token = authToken?.let { "Bearer $it" }
            )

            val records = response.data?.map { dto ->
                RepaymentRecord(
                    id = dto.id,
                    paymentMethod = formatPaymentMethod(dto.paymentMethod),
                    date = dto.paymentDate?.take(10) ?: "N/A",
                    amount = "${dto.amountPaid?.toLong() ?: 0} MMK",
                    statusNote = dto.notes
                )
            } ?: emptyList()

            Result.success(records)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun formatPaymentMethod(rawMethod: String?): String {
        return when (rawMethod?.uppercase()?.trim()) {
            "CASH" -> "Cash"
            "KBZ_PAY", "KBZPAY" -> "KBZ Pay"
            "WAVE_PAY", "WAVEPAY" -> "Wave Pay"
            "CB_PAY", "CBPAY" -> "CB Pay"
            "AYA_PAY", "AYAPAY" -> "AYA Pay"
            else -> rawMethod ?: "Cash"
        }
    }
}