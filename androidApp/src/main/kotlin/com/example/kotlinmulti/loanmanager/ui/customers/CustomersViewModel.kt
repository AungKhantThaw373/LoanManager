package com.example.kotlinmulti.loanmanager.ui.customers

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.kotlinmulti.loanmanager.data.remote.CustomerApiService
import com.example.kotlinmulti.loanmanager.data.repository.CustomerRepositoryImpl
import com.example.kotlinmulti.loanmanager.domain.model.CustomerRecord
import com.example.kotlinmulti.loanmanager.domain.model.RepaymentRecord
import com.example.kotlinmulti.loanmanager.domain.repository.CustomerRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CustomersViewModel(
    private val repository: CustomerRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow("All")
    private val _selectedStatus = MutableStateFlow("All")
    private val _showFilterSheet = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _records = MutableStateFlow<List<CustomerRecord>>(emptyList())
    private val _activeRepayments = MutableStateFlow<List<RepaymentRecord>>(emptyList())

    private var currentPage = 1
    private val pageLimit = 10
    private var isEndReached = false

    init {
        observeAndFetch()
    }

    @OptIn(FlowPreview::class)
    private fun observeAndFetch() {
        viewModelScope.launch {
            combine(
                _searchQuery.debounce(300.milliseconds),
                _selectedType,
                _selectedStatus
            ) { query, type, status -> Triple(query, type, status) }
                .flowOn(Dispatchers.Default)
                .collect { resetAndFetchInitialPage() }
        }
    }

    private fun resetAndFetchInitialPage() {
        currentPage = 1
        isEndReached = false
        _records.value = emptyList()
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value || isEndReached) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            repository.fetchLoans(page = currentPage, limit = pageLimit).fold(
                onSuccess = { newItems ->
                    if (newItems.isEmpty()) {
                        isEndReached = true
                    } else {
                        _records.value = _records.value + newItems
                        currentPage++
                    }
                },
                onFailure = { error ->
                    _errorMessage.value = error.localizedMessage ?: "Unable to fetch records."
                }
            )

            _isLoading.value = false
        }
    }

    fun fetchRepaymentHistoryForLoan(loanId: String) {
        viewModelScope.launch {
            _activeRepayments.value = emptyList()

            repository.fetchRepaymentHistory(loanId).fold(
                onSuccess = { history -> _activeRepayments.value = history },
                onFailure = { _activeRepayments.value = emptyList() }
            )
        }
    }

    val uiState: StateFlow<CustomersUiState> = combine(
        _searchQuery,
        _selectedType,
        _selectedStatus,
        _showFilterSheet,
        _isLoading,
        _errorMessage,
        _records,
        _activeRepayments
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        CustomersUiState(
            searchQuery = args[0] as String,
            selectedType = args[1] as String,
            selectedStatus = args[2] as String,
            showFilterSheet = args[3] as Boolean,
            isLoading = args[4] as Boolean,
            errorMessage = args[5] as String?,
            records = args[6] as List<CustomerRecord>,
            activeRepayments = args[7] as List<RepaymentRecord>
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CustomersUiState(isLoading = true)
    )

    fun onSearchQueryChanged(newQuery: String) { _searchQuery.value = newQuery }
    fun onFilterSheetToggled(show: Boolean) { _showFilterSheet.value = show }
    fun onApplyFilters(type: String, status: String) {
        _selectedType.value = type
        _selectedStatus.value = status
        _showFilterSheet.value = false
    }
    fun onResetFilters() {
        _searchQuery.value = ""
        _selectedType.value = "All"
        _selectedStatus.value = "All"
    }
    fun onRetry() { resetAndFetchInitialPage() }

    companion object {
        fun provideFactory(
            repository: CustomerRepository = CustomerRepositoryImpl(CustomerApiService.create())
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(CustomersViewModel::class.java)) {
                    return CustomersViewModel(repository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}