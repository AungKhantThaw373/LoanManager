package com.example.kotlinmulti.loanmanager.domain.repository

import com.example.kotlinmulti.loanmanager.domain.model.CustomerRecord
import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord

interface CustomerRepository {
    suspend fun fetchLoans(page: Int?, limit: Int?): Result<List<CustomerRecord>>
    suspend fun fetchRepaymentHistory(loanId: String): Result<List<RepaymentRecord>>
}