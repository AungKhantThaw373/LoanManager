package com.example.kotlinmulti.loanmanager.ui.navigation

import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord

/** Holds the tapped row while its detail destination is being opened. */
object LoanRecordStore {
    var repaymentRecord: LoanRecord? = null
}
