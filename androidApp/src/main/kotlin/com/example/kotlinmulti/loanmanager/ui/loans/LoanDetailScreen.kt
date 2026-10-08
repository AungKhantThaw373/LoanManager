package com.example.kotlinmulti.loanmanager.ui.loans

import androidx.compose.foundation.BorderStroke
import androidx.activity.compose.BackHandler
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
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.withContext
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.data.remote.CustomerInfoDto
import com.example.kotlinmulti.loanmanager.data.remote.LoanDto
import androidx.compose.ui.platform.LocalContext
import android.content.Intent
import android.net.Uri
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

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
    ACTIVE("ACTIVE", "Active"),
    PENDING("PENDING", "Pending"),
    PAID("PAID", "Paid"),
    OVERDUE("OVERDUE", "Overdue");

    companion object {
        fun fromApi(value: String): LoanStatus = when (value.uppercase()) {
            "ACTIVE" -> ACTIVE
            "PENDING" -> PENDING
            "PAID" -> PAID
            "OVERDUE" -> OVERDUE
            else -> throw IllegalArgumentException("Invalid loan status value: $value")
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
    val summary: Summary = Summary(0, 0, 0),
    val groupMembers: List<Customer> = emptyList()
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

// 3. MAIN CONNECTED COMPOSABLE (ENTRY POINT)
// ============================================================================

@Composable
fun ConnectedLoanDetailsScreen(
    loanId: String,
    authToken: String?,
    onBackClick: () -> Unit = {},
    onMemberClick: (Customer) -> Unit = {},
    onDownloadClick: (Loan) -> Unit = {}
) {
    var loan by remember(loanId) { mutableStateOf<Loan?>(null) }
    var isLoading by remember(loanId) { mutableStateOf(true) }
    var errorMessage by remember(loanId) { mutableStateOf<String?>(null) }
    var selectedMember by remember { mutableStateOf<Customer?>(null) }
    val scope = rememberCoroutineScope()

    fun loadData() {
        scope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = withContext(Dispatchers.IO) {
                    LoanApiService.instance.getLoanDetails(
                        loanId = loanId,
                        token = authToken?.let { "Bearer $it" }
                    )
                }
                loan = response.data?.toDetailLoan()
                if (loan == null) errorMessage = response.message ?: "Loan not found."
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Failed to fetch loan details."
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(loanId) { loadData() }

    when {
        isLoading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }
        errorMessage != null -> Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.Center
        ) {
            Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
            Button(onClick = { loadData() }) { Text("Retry") }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(onClick = onBackClick) { Text("Back") }
        }
        loan != null -> LoanDetailsScreen(
            loan = loan!!,
            onBackClick = onBackClick,
            onMemberClick = { member ->
                selectedMember = member
                onMemberClick(member)
            },
            onDownloadClick = { onDownloadClick(loan!!) }
        )
    }

    selectedMember?.let { member ->
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { selectedMember = null },
            title = { Text(member.name) },
            text = {
                Column {
                    InfoRow("NRC Number", member.nationalId.ifBlank { "-" })
                    InfoRow("Phone Number", member.phone.ifBlank { "-" })
                    InfoRow("Father Name", member.fatherName.ifBlank { "-" })
                    InfoRow("Work", member.work.ifBlank { "-" })
                    InfoRow("Address", member.address.ifBlank { "-" })
                }
            },
            confirmButton = {
                androidx.compose.material3.TextButton(onClick = { selectedMember = null }) {
                    Text("Close")
                }
            }
        )
    }
}

private fun LoanDto.toDetailLoan(): Loan {
    val borrower = customer?.toDetailCustomer() ?: Customer()
    val type = runCatching { LoanType.fromApi(loanType ?: "INDIVIDUAL") }
        .getOrDefault(LoanType.INDIVIDUAL)
    val parsedStatus = runCatching { LoanStatus.fromApi(status ?: "PENDING") }
        .getOrDefault(LoanStatus.PENDING)
    return Loan(
        id = id,
        loanNumber = loanNumber.orEmpty(),
        loanIdNo = loanIdNo.orEmpty(),
        branch = branch.orEmpty(),
        loanCreatedAt = parseIsoDate(loanCreatedAt ?: createdAt.orEmpty()),
        loanType = type,
        customerId = customerId ?: borrower.id,
        groupId = groupId,
        businessLicense = businessLicence,
        colateralName = collateralName,
        colateralImage = collateralImage,
        requestedAmount = requestedAmount ?: "0",
        interestRate = interestRate ?: "0",
        durationMonths = durationMonths ?: 0,
        status = parsedStatus,
        createdAt = parseIsoDate(createdAt ?: loanCreatedAt.orEmpty()),
        updatedAt = parseIsoDate(updatedAt ?: createdAt ?: loanCreatedAt.orEmpty()),
        customer = borrower,
        group = group?.let { Group(it.id, it.name) },
        repayments = repayments.orEmpty().map { Repayment(it.amountPaid?.toString() ?: "0") },
        summary = Summary(
            totalAmount = summary?.totalAmountToPay?.toInt() ?: 0,
            totalPaid = summary?.totalPaid?.toInt() ?: 0,
            remainingBalance = summary?.remainingBalance?.toInt() ?: 0
        ),
        groupMembers = groupMembers.orEmpty().mapNotNull { it.customer?.toDetailCustomer() }
    )
}

private fun CustomerInfoDto.toDetailCustomer() = Customer(
    id = id,
    name = name,
    phone = phone.orEmpty(),
    nationalId = nationalId.orEmpty(),
    fatherName = fatherName.orEmpty(),
    spouseName = spouseName,
    work = work.orEmpty(),
    address = address.orEmpty(),
    frontNRCUrl = frontNRCUrl.orEmpty(),
    backNRCUrl = backNRCUrl.orEmpty(),
    frontHouseholdListUrl = frontHouseholdListUrl.orEmpty(),
    backHouseholdListUrl = backHouseholdListUrl.orEmpty()
)

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
    BackHandler(onBack = onBackClick)

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
        InfoRow(label = "Loan Status", value = loan.status.displayValue)

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
                label = "${loan.status.displayValue} • ${loan.groupMembers.size} Members",
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
            value = loan.group?.name ?: "-"
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

    val totalRepayable = loan.summary.totalAmount.takeIf { it > 0 }?.toDouble()
        ?: (amount + amount * (rate / 100.0) * duration)
    val totalInterest = (totalRepayable - amount).coerceAtLeast(0.0)
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
        InfoRow(label = "Business Category", value = loan.customer.work.ifBlank { "-" })

        Spacer(modifier = Modifier.height(14.dp))
        SectionLabel(text = "LICENSE CERTIFICATION DOCUMENTS")
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DocumentBox(
                label = "Business licence document",
                url = loan.businessLicense ?: "",
                modifier = Modifier.fillMaxWidth()
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
            text = loan.colateralName?.takeIf { it.isNotBlank() } ?: "-",
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(14.dp))
        SectionLabel(text = "COLLATERAL & GUARANTEE DOCUMENTS")
        Spacer(modifier = Modifier.height(8.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            DocumentBox(
                label = "Collateral document",
                url = loan.colateralImage ?: "",
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
fun MembersListCard(
    loan: Loan,
    onMemberClick: (Customer) -> Unit
) {
    val members = loan.groupMembers

    DetailCard(
        title = "Members List",
        icon = Icons.Outlined.Group
    ) {
        if (members.isEmpty()) {
            Text(
                text = "No group member details are available.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        members.forEach { customer ->
            MemberRow(
                customer = customer,
                isLeader = customer.id == loan.customerId,
                onClick = { onMemberClick(customer) }
            )
            Spacer(modifier = Modifier.height(12.dp))
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
    val context = LocalContext.current
    val documentUrl = when {
        url.isBlank() -> null
        url.startsWith("http://") || url.startsWith("https://") -> url
        else -> LoanApiService.BASE_URL.trimEnd('/') + "/" + url.trimStart('/')
    }
    Surface(
        modifier = modifier.clickable(enabled = documentUrl != null) {
            documentUrl?.let { target ->
                runCatching {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(target)))
                }
            }
        },
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
                text = if (documentUrl == null) "Unavailable" else "Tap to open",
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
