package com.example.kotlinmulti.loanmanager.ui.demo

import com.example.kotlinmulti.loanmanager.data.remote.CurrentUserDto
import com.example.kotlinmulti.loanmanager.domain.model.LoanPage
import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord
import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord
import com.example.kotlinmulti.loanmanager.domain.repository.LoansRepository
import com.example.kotlinmulti.loanmanager.ui.loans.Customer
import com.example.kotlinmulti.loanmanager.ui.loans.Group
import com.example.kotlinmulti.loanmanager.ui.loans.Loan
import com.example.kotlinmulti.loanmanager.ui.loans.LoanStatus
import com.example.kotlinmulti.loanmanager.ui.loans.LoanType
import com.example.kotlinmulti.loanmanager.ui.loans.Summary
import java.util.Date

object OfflineDemoMode {
    const val EMAIL = "demo@offline.local"
    const val PASSWORD = "Preview123!"
    const val TOKEN = "local-offline-preview-session"
    const val OWNER_EMAIL = "owner@offline.local"
    const val OWNER_PASSWORD = "OwnerPreview123!"
    const val OWNER_TOKEN = "local-offline-owner-preview-session"

    fun isSession(token: String?): Boolean = token == TOKEN || token == OWNER_TOKEN

    fun tokenForCredentials(email: String, password: String): String? = when {
        email.trim().equals(EMAIL, ignoreCase = true) && password == PASSWORD -> TOKEN
        email.trim().equals(OWNER_EMAIL, ignoreCase = true) && password == OWNER_PASSWORD -> OWNER_TOKEN
        else -> null
    }

    fun roleForToken(token: String?): String? = when (token) {
        TOKEN -> "STAFF"
        OWNER_TOKEN -> "OWNER"
        else -> null
    }

    fun userForToken(token: String?): CurrentUserDto = if (token == OWNER_TOKEN) owner else user

    val user = CurrentUserDto(
        id = "offline-demo-user",
        name = "Demo Staff",
        email = EMAIL,
        role = "STAFF"
    )

    val owner = CurrentUserDto(
        id = "offline-demo-owner",
        name = "Demo Owner",
        email = OWNER_EMAIL,
        role = "OWNER"
    )

    val users: List<com.example.kotlinmulti.loanmanager.data.remote.UserDto> = listOf(
        com.example.kotlinmulti.loanmanager.data.remote.UserDto("offline-demo-owner", "Demo Owner", OWNER_EMAIL, "OWNER", createdAt = "2026-01-12T09:30:00Z"),
        com.example.kotlinmulti.loanmanager.data.remote.UserDto("offline-demo-manager", "May Thazin", "may.manager@offline.local", "MANAGER", createdAt = "2026-02-03T08:15:00Z"),
        com.example.kotlinmulti.loanmanager.data.remote.UserDto("offline-demo-staff-1", "Aung Kyaw", "aung.staff@offline.local", "STAFF", createdAt = "2026-03-17T11:20:00Z"),
        com.example.kotlinmulti.loanmanager.data.remote.UserDto("offline-demo-staff-2", "Hnin Ei", "hnin.staff@offline.local", "STAFF", createdAt = "2026-04-02T06:40:00Z")
    )

    val loans: List<LoanRecord> = listOf(
        LoanRecord("demo-loan-1", "KM-MGY 00001", "2026-10-01", "Aung Aung", "10,000,000 MMK", "12,500,000 MMK", "2,500,000 MMK", "10,000,000 MMK", "INDIVIDUAL", "ACTIVE"),
        LoanRecord("demo-loan-2", "KM-MGY 00002", "2026-09-26", "Mya Thandar", "8,000,000 MMK", "10,000,000 MMK", "10,000,000 MMK", "0 MMK", "BUSINESS", "PAID"),
        LoanRecord("demo-loan-3", "KM-MGY 00003", "2026-09-19", "Ko Min Thu", "6,500,000 MMK", "8,450,000 MMK", "1,000,000 MMK", "7,450,000 MMK", "GROUP", "OVERDUE"),
        LoanRecord("demo-loan-4", "KM-MGY 00004", "2026-09-12", "Hla Hla Win", "5,000,000 MMK", "6,250,000 MMK", "1,250,000 MMK", "5,000,000 MMK", "INDIVIDUAL", "ACTIVE"),
        LoanRecord("demo-loan-5", "KM-MGY 00005", "2026-09-05", "Thant Zin", "4,500,000 MMK", "5,625,000 MMK", "0 MMK", "5,625,000 MMK", "SPECIAL", "PENDING"),
        LoanRecord("demo-loan-6", "KM-MGY 00006", "2026-08-29", "Nilar Aye", "3,000,000 MMK", "3,900,000 MMK", "900,000 MMK", "3,000,000 MMK", "BUSINESS", "ACTIVE"),
        LoanRecord("demo-loan-7", "KM-MGY 00007", "2026-08-21", "Kyaw Kyaw", "2,500,000 MMK", "3,125,000 MMK", "1,000,000 MMK", "2,125,000 MMK", "INDIVIDUAL", "ACTIVE"),
        LoanRecord("demo-loan-8", "KM-MGY 00008", "2026-08-14", "Aye Chan Group", "12,000,000 MMK", "15,600,000 MMK", "3,000,000 MMK", "12,600,000 MMK", "GROUP", "OVERDUE"),
        LoanRecord("demo-loan-9", "KM-MGY 00009", "2026-08-07", "Su Mon", "1,800,000 MMK", "2,250,000 MMK", "500,000 MMK", "1,750,000 MMK", "INDIVIDUAL", "ACTIVE"),
        LoanRecord("demo-loan-10", "KM-MGY 00010", "2026-07-30", "Zaw Lin", "7,000,000 MMK", "8,750,000 MMK", "2,000,000 MMK", "6,750,000 MMK", "BUSINESS", "ACTIVE")
    )

    fun repaymentHistory(loanId: String): List<RepaymentRecord> = when (loanId) {
        "demo-loan-1" -> listOf(
            RepaymentRecord("demo-payment-1", "Cash", "2026-09-15", "1,500,000 MMK", "Received at Magway branch"),
            RepaymentRecord("demo-payment-2", "KBZ Pay", "2026-10-01", "1,000,000 MMK", "Mobile payment")
        )
        "demo-loan-2" -> listOf(
            RepaymentRecord("demo-payment-3", "Cash", "2026-09-25", "5,000,000 MMK", null),
            RepaymentRecord("demo-payment-4", "Wave Pay", "2026-09-26", "5,000,000 MMK", "Final payment")
        )
        "demo-loan-7" -> listOf(RepaymentRecord("demo-payment-5", "AYA Pay", "2026-09-02", "1,000,000 MMK", null))
        else -> emptyList()
    }

    fun loanDetails(loanId: String): Loan? {
        val record = loans.firstOrNull { it.loanId == loanId } ?: return null
        val loanType = runCatching { LoanType.valueOf(record.type) }.getOrDefault(LoanType.INDIVIDUAL)
        val status = runCatching { LoanStatus.valueOf(record.status) }.getOrDefault(LoanStatus.ACTIVE)
        val customerName = record.name
        return Loan(
            id = record.loanId,
            loanNumber = "LN-${record.loanId.substringAfterLast('-').padStart(5, '0')}",
            loanIdNo = record.id,
            branch = "Magway",
            loanCreatedAt = Date(),
            loanType = loanType,
            customerId = "demo-customer-${record.loanId}",
            groupId = if (loanType == LoanType.GROUP) "demo-group-1" else null,
            businessLicense = if (loanType == LoanType.BUSINESS) "DEMO-BIZ-2026" else null,
            colateralName = if (loanType == LoanType.INDIVIDUAL) "Household goods" else null,
            requestedAmount = record.amount.filter { it.isDigit() },
            interestRate = "2.5",
            durationMonths = 10,
            status = status,
            createdAt = Date(),
            updatedAt = Date(),
            customer = Customer(
                id = "demo-customer-${record.loanId}",
                name = customerName,
                phone = "09 555 123 456",
                nationalId = "12/MGNT(N)123456",
                fatherName = "U Demo",
                spouseName = "Daw Preview",
                work = if (loanType == LoanType.BUSINESS) "Small business owner" else "Shop owner",
                address = "Magway, Myanmar"
            ),
            group = if (loanType == LoanType.GROUP) Group("demo-group-1", customerName) else null,
            summary = Summary(
                totalAmount = record.totalToPay.filter { it.isDigit() }.toIntOrNull() ?: 0,
                totalPaid = record.paidAmount.filter { it.isDigit() }.toIntOrNull() ?: 0,
                remainingBalance = record.remainingBalance.filter { it.isDigit() }.toIntOrNull() ?: 0
            )
        )
    }
}

class OfflineDemoLoansRepository : LoansRepository {
    override suspend fun fetchLoans(
        page: Int?,
        limit: Int?,
        search: String?,
        type: String?,
        status: String?
    ): Result<LoanPage> = runCatching {
        val filtered = OfflineDemoMode.loans.filter { loan ->
            val matchesSearch = search.isNullOrBlank() || loan.name.contains(search, true) || loan.id.contains(search, true)
            val matchesType = type.isNullOrBlank() || type.equals("All", true) || loan.type.equals(type, true)
            val matchesStatus = status.isNullOrBlank() || status.equals("All", true) || loan.status.equals(status, true)
            matchesSearch && matchesType && matchesStatus
        }
        val pageNumber = (page ?: 1).coerceAtLeast(1)
        val pageSize = (limit ?: 10).coerceAtLeast(1)
        val start = (pageNumber - 1) * pageSize
        LoanPage(filtered.drop(start).take(pageSize), filtered.size)
    }
}
