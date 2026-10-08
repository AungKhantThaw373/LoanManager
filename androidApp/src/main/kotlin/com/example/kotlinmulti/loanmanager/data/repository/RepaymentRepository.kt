package com.example.kotlinmulti.loanmanager.data.repository

import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import com.example.kotlinmulti.loanmanager.ui.demo.OfflineDemoMode

class RepaymentRepository(
    private val apiService: LoanApiService,
    private val authToken: String?
) {
    suspend fun fetchHistory(loanId: String): Result<List<RepaymentRecord>> =
        withContext(Dispatchers.IO) {
            if (OfflineDemoMode.isSession(authToken)) {
                return@withContext Result.success(OfflineDemoMode.repaymentHistory(loanId))
            }
            try {
                val response = apiService.getRepaymentHistory(
                    loanId = loanId,
                    token = authToken?.let { "Bearer $it" }
                )
                if (!response.success) {
                    throw IllegalStateException("The API could not fetch repayment history.")
                }
                val history = response.data.orEmpty().map { dto ->
                    RepaymentRecord(
                        id = dto.id,
                        paymentMethod = formatPaymentMethod(dto.paymentMethod),
                        date = dto.paymentDate?.take(10) ?: "N/A",
                        amount = "${dto.amountPaid?.toLong() ?: 0} MMK",
                        statusNote = dto.notes
                    )
                }
                Result.success(history)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                Result.failure(error)
            }
        }

    private fun formatPaymentMethod(rawMethod: String?): String = when (rawMethod?.uppercase()?.trim()) {
        "CASH" -> "Cash"
        "KBZ_PAY", "KBZPAY" -> "KBZ Pay"
        "WAVE_PAY", "WAVEPAY" -> "Wave Pay"
        "CB_PAY", "CBPAY" -> "CB Pay"
        "AYA_PAY", "AYAPAY" -> "AYA Pay"
        else -> rawMethod ?: "Cash"
    }
}
