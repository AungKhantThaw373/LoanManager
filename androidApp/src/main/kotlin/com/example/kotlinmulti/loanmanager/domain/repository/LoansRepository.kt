package com.example.kotlinmulti.loanmanager.domain.repository

import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord
import com.example.kotlinmulti.loanmanager.domain.model.LoanPage

interface LoansRepository {
    suspend fun fetchLoans(
        page: Int?,
        limit: Int?,
        search: String? = null,
        type: String? = null,
        status: String? = null
    ): Result<LoanPage>
}
