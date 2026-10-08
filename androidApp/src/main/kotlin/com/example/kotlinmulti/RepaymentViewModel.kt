package com.example.loanmobile.ui.loans

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Download
import androidx.compose.material.icons.outlined.Group
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Payments
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Phone
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Storefront
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import kotlin.math.ceil

// ============================================================================
// 1. ENUMS & DATA MODELS
// ============================================================================

enum class GroupRole(val apiValue: String) {
    MEMBER("MEMBER"),
    LEADER("LEADER");

    companion object {
        fun fromApi(value: String): GroupRole = when (value.uppercase()) {
            "MEMBER" -> MEMBER
            "LEADER" -> LEADER
            else -> throw IllegalArgumentException("Invalid group role value: $value")
        }
    }
}

enum class LoanType(val apiValue: String, val displayValue: String) {
    GROUP("GROUP", "Group"),
    INDIVIDUAL("INDIVIDUAL", "Individual"),
    BUSINESS("BUSINESS", "Business"),
    SPECIAL("SPECIAL", "Special");

    companion object {
        fun fromApi(value: String): LoanType = when (value.uppercase()) {
            "GROUP" -> GROUP
            "INDIVIDUAL" -> INDIVIDUAL
            "BUSINESS" -> BUSINESS
            "SPECIAL" -> SPECIAL
            else -> throw IllegalArgumentException("Invalid loan type value: $value")
        }
    }
}

enum class LoanStatus(val apiValue: String, val displayValue: String) {
    PENDING("PENDING", "Pending"),
    PAID("PAID", "Paid"),
    OVERDUE("OVERDUE", "Overdue");

    companion object {
        fun fromApi(value: String): LoanStatus = when (value.uppercase()) {
            "PENDING" -> PENDING
            "PAID" -> PAID
            "OVERDUE" -> OVERDUE
            else -> throw IllegalArgumentException("Invalid loan status value: $value")
        }
    }
}

data class Pagination(
    val total: Int,
    val page: Int,
    val limit: Int,
    val totalPages: Int
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Pagination {
            return Pagination(
                total = (json["total"] as Number).toInt(),
                page = (json["page"] as Number).toInt(),
                limit = (json["limit"] as Number).toInt(),
                totalPages = (json["totalPages"] as Number).toInt()
            )
        }
    }
}

data class Summary(
    val totalAmount: Int,
    val totalPaid: Int,
    val remainingBalance: Int
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Summary {
            return Summary(
                totalAmount = (json["totalAmountToPay"] as Number).toInt(),
                totalPaid = (json["totalPaid"] as Number).toInt(),
                remainingBalance = (json["remainingBalance"] as Number).toInt()
            )
        }
    }
}

data class Repayment(
    val amountPaid: String
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Repayment {
            return Repayment(
                amountPaid = json["amountPaid"] as String
            )
        }
    }
}

data class Group(
    val id: String = "",
    val name: String = ""
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Group {
            return Group(
                id = json["id"] as String,
                name = json["name"] as String
            )
        }
    }
}

data class Customer(
    val id: String = "",
    val name: String = "",
    val phone: String = "",
    val nationalId: String = "",
    val fatherName: String = "",
    val spouseName: String? = null,
    val work: String = "",
    val address: String = "",
    val frontNRCUrl: String = "",
    val backNRCUrl: String = "",
    val frontHouseholdListUrl: String = "",
    val backHouseholdListUrl: String = ""
) {
    companion object {
        fun fromJson(json: Map<String, Any?>): Customer {
            return Customer(
                id = json["id"] as String,
                name = json["name"] as String,
                phone = json["phone"] as String,
                nationalId = json["nationalId"] as String,
                fatherName = json["fatherName"] as String,
                spouseName = json["spouseName"] as? String,
                work = json["work"] as String,
                address = json["address"] as String,
                frontNRCUrl = json["frontNRCUrl"] as String,
                backNRCUrl = json["backNRCUrl"] as String,
                frontHouseholdListUrl = json["frontHouseholdListUrl"] as String,
                backHouseholdListUrl = json["backHouseholdListUrl"] as String
            )
        }
    }
}

data class Loan(
    val id: String = "",
    val loanNumber: String = "",
    val loanIdNo: String = "",
    val branch: String = "",
    val loanCreatedAt: Date = Date(),
    val loanType: LoanType = LoanType.INDIVIDUAL,
    val customerId: String = "",
    val groupId: String? = null,
    val businessLicense: String? = null,
    val colateralName: String? = null,
    val colateralImage: String? = null,
    val requestedAmount: String = "0",
    val interestRate: String = "0",
    val durationMonths: Int = 0,
    val status: LoanStatus = LoanStatus.PENDING,
    val createdAt: Date = Date(),
    val updatedAt: Date = Date(),
    val customer: Customer = Customer(),
    val group: Group? = null,
    val repayments: List<Repayment> = emptyList(),
    val summary: Summary = Summary(0, 0, 0)
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: Map<String, Any?>): Loan {
            val customerMap = (json["Customer"] as? Map<String, Any?>)
                ?: (json["customer"] as? Map<String, Any?>)
                ?: emptyMap()

            val groupMap = (json["Group"] as? Map<String, Any?>)
                ?: (json["group"] as? Map<String, Any?>)

            val repaymentsList = (json["repayments"] as? List<Map<String, Any?>>) ?: emptyList()

            return Loan(
                id = json["id"] as String,
                loanNumber = json["loanNumber"] as String,
                loanIdNo = json["loanIdNo"] as String,
                branch = json["branch"] as String,
                loanCreatedAt = parseIsoDate(json["loanCreatedAt"] as String),
                loanType = LoanType.fromApi(json["loanType"] as String),
                customerId = json["customerId"] as String,
                groupId = json["groupId"] as? String,
                businessLicense = json["businessLicense"] as? String,
                colateralName = json["colateralName"] as? String,
                colateralImage = json["colateralImage"] as? String,
                requestedAmount = json["requestedAmount"] as String,
                interestRate = json["interestRate"] as String,
                durationMonths = (json["durationMonths"] as Number).toInt(),
                status = LoanStatus.fromApi(json["status"] as String),
                createdAt = parseIsoDate(json["createdAt"] as String),
                updatedAt = parseIsoDate(json["updatedAt"] as String),
                customer = Customer.fromJson(customerMap),
                group = groupMap?.let { Group.fromJson(it) },
                repayments = repaymentsList.map { Repayment.fromJson(it) },
                summary = Summary.fromJson((json["summary"] as? Map<String, Any?>) ?: emptyMap())
            )
        }
    }
}

data class LoansResponse(
    val success: Boolean,
    val message: String,
    val data: List<Loan>,
    val pagination: Pagination
) {
    companion object {
        @Suppress("UNCHECKED_CAST")
        fun fromJson(json: Map<String, Any?>): LoansResponse {
            val dataList = (json["data"] as? List<Map<String, Any?>>) ?: emptyList()
            return LoansResponse(
                success = json["success"] as Boolean,
                message = json["message"] as String,
                data = dataList.map { Loan.fromJson(it) },
                pagination = Pagination.fromJson((json["pagination"] as? Map<String, Any?>) ?: emptyMap())
            )
        }
    }
}

data class LoanFilter(
    val loanType: LoanType? = null,
    val loanStatus: LoanStatus? = null,
    val search: String? = null
) {
    fun toQueryParameters(): Map<String, Any> {
        val params = mutableMapOf<String, Any>()
        loanType?.let { params["loanType"] = it.apiValue }
        loanStatus?.let { params["status"] = it.apiValue }
        if (!search.isNullOrEmpty()) {
            params["search"] = search
        }
        return params
    }
}

// ============================================================================
// 2. NETWORK SERVICE LAYER
// ============================================================================

data class ApiResponse(val data: Map<String, Any?>)

interface ApiClient {
    suspend fun request(
        path: String,
        method: String,
        queryParameters: Map<String, Any>
    ): ApiResponse
}

object DummyLoansData {
    val dummyLoansResponse: Map<String, Any?> = mapOf(
        "success" to true,
        "message" to "Dummy loans fetched",
        "data" to listOf<Map<String, Any?>>(),
        "pagination" to mapOf(
            "total" to 0,
            "page" to 1,
            "limit" to 20,
            "totalPages" to 1
        )
    )
}

class LoansService(
    private val apiClient: ApiClient
) {
    companion object {
        var USE_DUMMY_DATA = false
    }

    suspend fun fetchLoans(
        filter: LoanFilter,
        page: Int = 1,
        limit: Int = 20
    ): LoansResponse {
        if (USE_DUMMY_DATA) {
            delay(500)

            @Suppress("UNCHECKED_CAST")
            val allLoans = (DummyLoansData.dummyLoansResponse["data"] as? List<Map<String, Any?>>)
                ?.map { it.toMap() } ?: emptyList()

            var filtered = allLoans

            val typeValue = filter.loanType?.apiValue
            if (!typeValue.isNullOrEmpty()) {
                filtered = filtered.filter { it["loanType"] == typeValue }
            }

            val statusValue = filter.loanStatus?.apiValue
            if (!statusValue.isNullOrEmpty()) {
                filtered = filtered.filter { it["status"] == statusValue }
            }

            val q = filter.search?.trim()?.lowercase() ?: ""
            if (q.isNotEmpty()) {
                filtered = filtered.filter { l ->
                    @Suppress("UNCHECKED_CAST")
                    val customer = l["Customer"] as? Map<String, Any?>
                    val name = (customer?.get("name") as? String ?: "").lowercase()
                    val nrc = (customer?.get("nationalId") as? String ?: "").lowercase()
                    val loanNo = (l["loanNumber"] as? String ?: "").lowercase()
                    val loanId = (l["loanIdNo"] as? String ?: "").lowercase()

                    name.contains(q) || nrc.contains(q) || loanNo.contains(q) || loanId.contains(q)
                }
            }

            val start = (page - 1) * limit
            val end = (start + limit).coerceAtMost(filtered.size)
            val paged = if (start >= filtered.size) emptyList() else filtered.subList(start, end)

            val totalPages = if (limit > 0) {
                ceil(filtered.size.toDouble() / limit).toInt().coerceIn(1, 999)
            } else 1

            val dummyResponse = mapOf(
                "success" to true,
                "message" to "Dummy loans fetched",
                "data" to paged,
                "pagination" to mapOf(
                    "total" to filtered.size,
                    "page" to page,
                    "limit" to limit,
                    "totalPages" to totalPages
                )
            )

            return LoansResponse.fromJson(dummyResponse)
        }

        val queryParameters = filter.toQueryParameters().toMutableMap().apply {
            put("page", page)
            put("limit", limit)
        }

        val response = apiClient.request(
            path = "/api/loans",
            method = "GET",
            queryParameters = queryParameters
        )

        return LoansResponse.fromJson(response.data)
    }
}

// ============================================================================
// 3. MAIN CONNECTED COMPOSABLE (ENTRY POINT)
// ============================================================================

@Composable
fun ConnectedLoanDetailsScreen(
    loanId: String,
    loansService: LoansService,
    onBackClick: () -> Unit = {},
    onMemberClick: (Customer) -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    var loan by remember { mutableStateOf<Loan?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = loansService.fetchLoans(
                    filter = LoanFilter(search = loanId),
                    page = 1,
                    limit = 10
                )
                loan = response.data.firstOrNull { it.id == loanId || it.loanNumber == loanId }
                    ?: response.data.firstOrNull()

                if (loan == null) {
                    errorMessage = "Loan not found."
                }
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to fetch loan details."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(loanId) {
        loadData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        when {
            isLoading -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.primary
                )
            }
            errorMessage != null -> {
                Column(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = errorMessage!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyLarge
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(onClick = { loadData() }) {
                        Text("Retry")
                    }
                }
            }
            loan != null -> {
                LoanDetailsScreen(
                    loan = loan!!,
                    onBackClick = onBackClick,
                    onMemberClick = onMemberClick,
                    onDownloadClick = onDownloadClick
                )
            }
        }
    }
}

// ============================================================================
// 4. LOAN DETAILS SCREEN UI
// ============================================================================

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoanDetailsScreen(
    loan: Loan,
    onBackClick: () -> Unit = {},
    onMemberClick: (Customer) -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    Scaffold(
        containerColor = MaterialTheme.colorScheme.primaryContainer,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "${loan.loanType.displayValue} Loan Details",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            if (loan.loanType == LoanType.GROUP) {
                GroupIdentificationCard(loan = loan)
            } else {
                BorrowerCard(loan = loan)
            }

            Spacer(modifier = Modifier.height(12.dp))

            LoanRepaymentCard(loan = loan)

            Spacer(modifier = Modifier.height(12.dp))

            when (loan.loanType) {
                LoanType.BUSINESS -> BusinessVerificationCard(loan = loan)
                LoanType.INDIVIDUAL -> CollateralCard(loan = loan)
                LoanType.GROUP -> MembersListCard(loan = loan, onMemberClick = onMemberClick)
                LoanType.SPECIAL -> { /* No extra card */ }
            }

            Spacer(modifier = Modifier.height(12.dp))

            DownloadButton(onClick = onDownloadClick)
        }
    }
}

// ============================================================================
// 5. CARDS & UI COMPONENTS
// ============================================================================

@Composable
fun BorrowerCard(loan: Loan) {
    DetailCard(
        title = "Borrower Identification",
        icon = Icons.Outlined.Person,
        trailing = {
            Text(
                text = loan.loanNumber,
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    ) {
        if (loan.loanType == LoanType.SPECIAL) {
            HighlightAmount(amount = loan.requestedAmount)
            Spacer(modifier = Modifier.height(12.dp))
        }

        InfoRow(label = "Full Name", value = loan.customer.name)
        InfoRow(label = "NRC Number", value = loan.customer.nationalId)
        InfoRow(label = "Phone Number", value = loan.customer.phone, showPhoneIcon = true)
        InfoRow(label = "Address", value = loan.customer.address)

        Spacer(modifier = Modifier.height(14.dp))

        SectionLabel(text = "NRC DOCUMENT ATTACHMENTS")
        Spacer(modifier = Modifier.height(8.dp))
        Row(modifier = Modifier.fillMaxWidth()) {
            DocumentBox(
                label = "NRC Front - img",
                url = loan.customer.frontNRCUrl,
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DocumentBox(
                label = "NRC Back - img",
                url = loan.customer.backNRCUrl,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun GroupIdentificationCard(loan: Loan) {
    DetailCard(
        title = "Group Identification",
        icon = Icons.Outlined.Groups,
        trailing = {
            AppBadge(
                label = "Active • 5 Members",
                backgroundColor = Color(0xFFE8F5E9),
                contentColor = Color(0xFF2E7D32)
            )
        }
    ) {
        Text(
            text = loan.loanNumber,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
        )
        Spacer(modifier = Modifier.height(12.dp))
        InfoRow(
            label = "Group Name",
            value = loan.group?.name ?: "Thitsar Solidarity Group"
        )
        InfoRow(label = "Designated Leader", value = loan.customer.name)
        InfoRow(label = "Leader NRC Number", value = loan.customer.nationalId)
        InfoRow(label = "Contact Phone", value = loan.customer.phone, showPhoneIcon = true)
    }
}

@Composable
fun LoanRepaymentCard(loan: Loan) {
    val amount = loan.requestedAmount.toDoubleOrNull() ?: 0.0
    val rate = loan.interestRate.toDoubleOrNull() ?: 0.0
    val duration = loan.durationMonths

    val totalInterest = amount * (rate / 100.0) * duration
    val totalRepayable = amount + totalInterest
    val monthlyInstallment = if (duration > 0) totalRepayable / duration else 0.0

    val disbursedDate = loan.loanCreatedAt
    val maturityDate = Calendar.getInstance().apply {
        time = disbursedDate
        add(Calendar.MONTH, duration)
    }.time

    val nextDueDate = Calendar.getInstance().apply {
        time = disbursedDate
        add(Calendar.MONTH, 1)
    }.time

    DetailCard(
        title = "Loan & Repayment Details",
        icon = Icons.Outlined.Payments,
        trailing = {
            AppBadge(
                label = "$duration Months Tenure",
                backgroundColor = Color(0xFFE3F2FD),
                contentColor = Color(0xFF1565C0)
            )
        }
    ) {
        HighlightAmount(amount = loan.requestedAmount)
        Spacer(modifier = Modifier.height(12.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            InfoTile(
                icon = Icons.Outlined.CalendarToday,
                label = "Disbursed Date",
                value = formatDate(disbursedDate),
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            InfoTile(
                icon = Icons.Outlined.CalendarMonth,
                label = "Maturity Date",
                value = formatDate(maturityDate),
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            BorderedRow(
                label = "Interest Rate",
                value = "${String.format(Locale.US, "%.2f", rate)}% / mo",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            BorderedRow(
                label = "Total Interest",
                value = "${fmtAmount(totalInterest)} MMK",
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(8.dp))

        BorderedRow(
            label = "Total Repayable",
            value = "${fmtAmount(totalRepayable)} MMK",
            bold = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        BorderedRow(
            label = "Monthly Installment",
            value = "${fmtAmount(monthlyInstallment)} MMK / mo",
            bold = true
        )
        Spacer(modifier = Modifier.height(8.dp))

        BorderedRow(
            icon = Icons.Outlined.Schedule,
            label = "Next Due Date",
            value = formatDate(nextDueDate),
            valueColor = MaterialTheme.colorScheme.error
        )
    }
}

@Composable
fun BusinessVerificationCard(loan: Loan) {
    DetailCard(
        title = "Business Verification",
        icon = Icons.Outlined.Storefront
    ) {
        InfoRow(label = "Business Name", value = loan.customer.name)
        InfoRow(label = "Business License No", value = loan.businessLicense ?: "-")
        InfoRow(label = "Business Category", value = "Retail / Wholesale Trading")

        Spacer(modifier = Modifier.height(14.dp))
        SectionLabel(text = "LICENSE CERTIFICATION DOCUMENTS")
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DocumentBox(
                label = "Front - img",
                url = loan.colateralImage ?: "",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DocumentBox(
                label = "Back - img",
                url = loan.colateralImage ?: "",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun CollateralCard(loan: Loan) {
    DetailCard(
        title = "Collateral & Guarantee",
        icon = Icons.Outlined.Shield
    ) {
        SectionLabel(text = "COLLATERAL TYPE")
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = loan.colateralName ?: "Gold Jewelry & Household Title Deed",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        SectionLabel(text = "COLLATERAL & GUARANTEE DOCUMENTS")
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DocumentBox(
                label = "Front - img",
                url = loan.colateralImage ?: "",
                modifier = Modifier.weight(1f)
            )
            Spacer(modifier = Modifier.width(8.dp))
            DocumentBox(
                label = "Back - img",
                url = loan.colateralImage ?: "",
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun MembersListCard(
    loan: Loan,
    onMemberClick: (Customer) -> Unit
) {
    val members = listOf(
        loan.customer,
        Customer(
            id = "dummy-002",
            name = "U Myint Soe",
            phone = "09-770112233",
            nationalId = "09/KAKATA(N)001234",
            fatherName = "U Aye Myint",
            spouseName = "Daw Mya Mya",
            work = "Farmer",
            address = "No. 22, Bogyoke Road, Magway"
        ),
        Customer(
            id = "dummy-003",
            name = "Daw San San Nu",
            phone = "09-770223344",
            nationalId = "09/KAKATA(N)002345",
            fatherName = "U Hla Myint",
            spouseName = "U Myint Oo",
            work = "Shop Owner",
            address = "No. 45, Cherry Street, Magway"
        ),
        Customer(
            id = "dummy-004",
            name = "Daw Aye Aye Thin",
            phone = "09-770334455",
            nationalId = "09/KAPATA(N)003456",
            fatherName = "U Win Maung",
            spouseName = "U Thein Zaw",
            work = "Tailor",
            address = "No. 78, Thitsar Street, Magway"
        ),
        Customer(
            id = "dummy-005",
            name = "U Zaw Min Lwin",
            phone = "09-770445566",
            nationalId = "09/KAPATA(N)004567",
            fatherName = "U Kyaw San",
            spouseName = "Daw Hla Hla",
            work = "Driver",
            address = "No. 90, Aung Mingalar, Magway"
        )
    )

    DetailCard(
        title = "Members List",
        icon = Icons.Outlined.Group
    ) {
        members.forEachIndexed { index, customer ->
            MemberRow(
                customer = customer,
                isLeader = index == 0,
                onClick = { onMemberClick(customer) }
            )
            if (index < members.size - 1) {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun MemberRow(
    customer: Customer,
    isLeader: Boolean,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() },
        color = Color.Transparent
    ) {
        Row(
            modifier = Modifier.padding(vertical = 4.dp, horizontal = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = getInitials(customer.name),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = customer.name,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    if (isLeader) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                        ) {
                            Text(
                                text = "Leader",
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = customer.nationalId,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }

            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                modifier = Modifier.size(18.dp),
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)
            )
        }
    }
}

@Composable
fun DetailCard(
    title: String,
    icon: ImageVector,
    trailing: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                trailing?.invoke()
            }
            HorizontalDivider(
                modifier = Modifier.padding(vertical = 10.dp),
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
            )
            content()
        }
    }
}

@Composable
fun InfoRow(
    label: String,
    value: String,
    showPhoneIcon: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
            modifier = Modifier.width(120.dp)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            if (showPhoneIcon) {
                Icon(
                    imageVector = Icons.Outlined.Phone,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
        letterSpacing = 0.5.sp
    )
}

@Composable
fun DocumentBox(
    label: String,
    url: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Outlined.Image,
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "IMG",
                fontSize = 9.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.outline
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = label,
                fontSize = 9.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
            )
        }
    }
}

@Composable
fun InfoTile(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = label,
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = value,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
fun HighlightAmount(amount: String) {
    val numericAmount = amount.toDoubleOrNull() ?: 0.0
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(8.dp),
        color = MaterialTheme.colorScheme.primaryContainer
    ) {
        Column(
            modifier = Modifier.padding(vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "TOTAL PRINCIPAL LOAN AMOUNT",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "${fmtAmount(numericAmount)} MMK",
                fontSize = 22.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
fun BorderedRow(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    bold: Boolean = false,
    valueColor: Color = Color.Unspecified
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(6.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.weight(1f)
            )
            Text(
                text = value,
                fontSize = 12.sp,
                fontWeight = if (bold) FontWeight.ExtraBold else FontWeight.Bold,
                color = valueColor
            )
        }
    }
}

@Composable
fun AppBadge(
    label: String,
    backgroundColor: Color,
    contentColor: Color
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = backgroundColor
    ) {
        Text(
            text = label,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            color = contentColor,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
        )
    }
}

@Composable
fun DownloadButton(onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(10.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        contentPadding = PaddingValues(vertical = 14.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Download,
            contentDescription = null,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Download Loan Details File",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold
        )
    }
}

// ============================================================================
// 6. HELPER FUNCTIONS
// ============================================================================

fun fmtAmount(value: Number): String {
    return NumberFormat.getNumberInstance(Locale.US).format(value)
}

fun formatDate(date: Date): String {
    return SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(date)
}

fun getInitials(name: String): String {
    return name.split(" ")
        .filter { it.isNotEmpty() }
        .take(2)
        .map { it.first().uppercaseChar() }
        .joinToString("")
}

private fun parseIsoDate(dateStr: String): Date {
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(dateStr) ?: Date()
    } catch (_: Exception) {
        Date()
    }
}