package com.example.kotlinmulti.loanmanager.ui.loans

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.kotlinmulti.loanmanager.ui.components.CustomBottomNavigationBar
import com.example.kotlinmulti.loanmanager.ui.components.EmptyStateView
import com.example.kotlinmulti.loanmanager.ui.components.ErrorStateView
import com.example.kotlinmulti.loanmanager.ui.components.FilterBottomSheet
import com.example.kotlinmulti.loanmanager.ui.components.LoanItemRow
import com.google.gson.Gson

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoansScreen(
    navController: NavHostController,
    authToken: String? = null,
    showRepaymentDetails: Boolean = false,
    viewModel: LoansViewModel = viewModel(
        factory = LoansViewModel.provideFactory(authToken = authToken)
    ),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItem >= totalItems - 2
        }
    }

        LaunchedEffect(shouldLoadMore.value) {
            if (shouldLoadMore.value) {
                viewModel.loadNextPage()
            }
        }

        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Column {
                            Text(
                                "Loan Manager",
                                color = MaterialTheme.colorScheme.onPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                if (showRepaymentDetails) "Customers Directory" else "Loans Directory",
                                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f),
                                fontSize = 14.sp
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.primary)
                )
            },
            bottomBar = {
                val currentRoute = navController
                    .currentBackStackEntryAsState()
                    .value
                    ?.destination
                    ?.route

                CustomBottomNavigationBar(
                    currentRoute = currentRoute ?: "loans",
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo("loans") {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            },
            containerColor = MaterialTheme.colorScheme.background
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::onSearchQueryChanged,
                        placeholder = {
                            Text(
                                if (showRepaymentDetails) "Search customers..." else "Search loans...",
                                fontSize = 16.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        leadingIcon = {
                            Icon(
                                Icons.Filled.Search,
                                contentDescription = "Search",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                            focusedBorderColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    Spacer(modifier = Modifier.width(12.dp))

                    Box(
                        modifier = Modifier
                            .size(52.dp)
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outlineVariant,
                                RoundedCornerShape(8.dp)
                            )
                            .background(
                                MaterialTheme.colorScheme.surfaceContainerLow,
                                RoundedCornerShape(8.dp)
                            )
                            .clickable { viewModel.onFilterSheetToggled(true) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Filled.Menu,
                            contentDescription = "Filter",
                            tint = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        uiState.headerLabel,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                    Text(
                        "${uiState.totalRecords}",
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant,
                    thickness = 0.5.dp
                )

                Box(modifier = Modifier.fillMaxSize()) {
                    when {
                        // Initial Loading (only when zero records exist)
                        uiState.isLoading && uiState.records.isEmpty() -> {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        uiState.errorMessage != null && uiState.records.isEmpty() -> {
                            ErrorStateView(
                                message = uiState.errorMessage!!,
                                onRetry = viewModel::onRetry
                            )
                        }

                        uiState.records.isEmpty() -> {
                            EmptyStateView(onResetFilters = viewModel::onResetFilters)
                        }

                        else -> {
                            LazyColumn(
                                state = listState, // <-- PASS LIST STATE HERE
                                modifier = Modifier.fillMaxSize()
                            ) {
                                items(uiState.records, key = { it.id }) { record ->
                                    LoanItemRow(
                                        record = record,
                                        showPaidLabel = showRepaymentDetails,
                                        onItemClick = { clickedItem ->
                                            if (showRepaymentDetails) {
                                                val encodedRecord = Uri.encode(Gson().toJson(clickedItem))
                                                navController.navigate("repaymentDetails/${Uri.encode(clickedItem.loanId)}?record=$encodedRecord")
                                            } else {
                                                navController.navigate("loanDetails/${Uri.encode(clickedItem.loanId)}")
                                            }
                                        }
                                        )
                                    HorizontalDivider(
                                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                        thickness = 0.5.dp,
                                        modifier = Modifier.padding(horizontal = 16.dp)
                                    )
                                }

                                // BOTTOM LOADER ITEM: Shows spinner while fetching subsequent pages
                                if (uiState.isLoading && uiState.records.isNotEmpty()) {
                                    item {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(16.dp),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(24.dp),
                                                color = MaterialTheme.colorScheme.primary,
                                                strokeWidth = 2.5.dp
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (uiState.showFilterSheet) {
                FilterBottomSheet(
                    currentType = uiState.selectedType,
                    currentStatus = uiState.selectedStatus,
                    onDismiss = { viewModel.onFilterSheetToggled(false) },
                    onApply = { type, status -> viewModel.onApplyFilters(type, status) }
                )
            }
        }
}
