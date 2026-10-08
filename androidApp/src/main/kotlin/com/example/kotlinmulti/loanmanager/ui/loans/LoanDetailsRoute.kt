package com.example.kotlinmulti.loanmanager.ui.loans

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.kotlinmulti.loanmanager.ui.demo.OfflineDemoMode

@Composable
fun LoanDetailsRoute(
    loanId: String,
    authToken: String?,
    onBackClick: () -> Unit
) {
    val context = LocalContext.current
    var exportText by remember { mutableStateOf<String?>(null) }
    val createDocument = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/plain")
    ) { uri ->
        val content = exportText
        if (uri != null && content != null) {
            val saved = runCatching {
                context.contentResolver.openOutputStream(uri)?.bufferedWriter()?.use { writer ->
                    writer.write(content)
                } ?: error("Unable to open the selected file.")
            }.isSuccess
            Toast.makeText(
                context,
                if (saved) "Loan details saved." else "Could not save loan details.",
                Toast.LENGTH_SHORT
            ).show()
        }
        exportText = null
    }

    val onDownload: (Loan) -> Unit = { loan ->
            exportText = loan.toExportText()
            val safeName = loan.loanIdNo.ifBlank { loan.loanNumber }
                .replace(Regex("[^A-Za-z0-9_-]"), "_")
            createDocument.launch("${safeName.ifBlank { "loan_details" }}_details.txt")
        }

    if (OfflineDemoMode.isSession(authToken)) {
        val demoLoan = remember(loanId) { OfflineDemoMode.loanDetails(loanId) }
        if (demoLoan != null) {
            LoanDetailsScreen(
                loan = demoLoan,
                onBackClick = onBackClick,
                onDownloadClick = { onDownload(demoLoan) }
            )
        } else {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Demo loan not found.", color = MaterialTheme.colorScheme.error)
            }
        }
    } else {
        ConnectedLoanDetailsScreen(
            loanId = loanId,
            authToken = authToken,
            onBackClick = onBackClick,
            onDownloadClick = onDownload
        )
    }
}

private fun Loan.toExportText(): String = buildString {
    appendLine("Loan details")
    appendLine("Loan ID: ${loanIdNo.ifBlank { id }}")
    appendLine("Loan number: $loanNumber")
    appendLine("Borrower: ${customer.name}")
    appendLine("Phone: ${customer.phone}")
    appendLine("Loan type: ${loanType.displayValue}")
    appendLine("Status: ${status.displayValue}")
    appendLine("Branch: $branch")
    appendLine("Principal: $requestedAmount MMK")
    appendLine("Interest rate: $interestRate% per month")
    appendLine("Term: $durationMonths months")
    appendLine("Total repayable: ${summary.totalAmount} MMK")
    appendLine("Paid: ${summary.totalPaid} MMK")
    appendLine("Remaining: ${summary.remainingBalance} MMK")
    appendLine("Disbursed: ${formatDate(loanCreatedAt)}")
    group?.let { appendLine("Group: ${it.name}") }
    businessLicense?.let { appendLine("Business license: $it") }
    colateralName?.let { appendLine("Collateral: $it") }
    if (groupMembers.isNotEmpty()) {
        appendLine()
        appendLine("Group members")
        groupMembers.forEach { member ->
            appendLine("- ${member.name} | ${member.nationalId} | ${member.phone}")
        }
    }
}
