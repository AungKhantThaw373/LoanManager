package com.example.kotlinmulti.loanmanager.ui.loans

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.data.repository.LoansRepositoryImpl
import com.example.kotlinmulti.loanmanager.domain.model.LoanRecord
import com.example.kotlinmulti.loanmanager.domain.repository.LoansRepository
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

class LoansViewModel(
    private val repository: LoansRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _selectedType = MutableStateFlow("All")
    private val _selectedStatus = MutableStateFlow("All")
    private val _showFilterSheet = MutableStateFlow(false)
    private val _isLoading = MutableStateFlow(false)
    private val _errorMessage = MutableStateFlow<String?>(null)
    private val _records = MutableStateFlow<List<LoanRecord>>(emptyList())
    private val _totalRecords = MutableStateFlow(0)

    private var currentPage = 1
    private val pageLimit = 10
    private var isEndReached = false
    private var requestGeneration = 0L
    private var activeFetch: Job? = null

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
        requestGeneration++
        activeFetch?.cancel()
        activeFetch = null
        _isLoading.value = false
        currentPage = 1
        isEndReached = false
        _records.value = emptyList()
        _totalRecords.value = 0
        loadNextPage()
    }

    fun loadNextPage() {
        if (_isLoading.value || isEndReached) return

        val generation = requestGeneration
        val requestedPage = currentPage
        _isLoading.value = true
        activeFetch = viewModelScope.launch {
            try {
                repository.fetchLoans(
                    page = requestedPage,
                    limit = pageLimit,
                    search = _searchQuery.value,
                    type = _selectedType.value,
                    status = _selectedStatus.value
                ).fold(
                    onSuccess = { page ->
                        if (generation == requestGeneration) {
                            _totalRecords.value = page.totalRecords
                            if (page.records.isEmpty()) {
                                isEndReached = true
                            } else {
                                _records.value = _records.value + page.records
                                currentPage++
                            }
                        }
                    },
                    onFailure = { error ->
                        if (generation == requestGeneration) {
                            _errorMessage.value = error.localizedMessage ?: "Unable to fetch records."
                        }
                    }
                )
            } finally {
                if (generation == requestGeneration) _isLoading.value = false
            }
        }
    }

    val uiState: StateFlow<LoansUiState> = combine(
        _searchQuery,
        _selectedType,
        _selectedStatus,
        _showFilterSheet,
        _isLoading,
        _errorMessage,
        _records,
        _totalRecords
    ) { args: Array<Any?> ->
        @Suppress("UNCHECKED_CAST")
        LoansUiState(
            searchQuery = args[0] as String,
            selectedType = args[1] as String,
            selectedStatus = args[2] as String,
            showFilterSheet = args[3] as Boolean,
            isLoading = args[4] as Boolean,
            errorMessage = args[5] as String?,
            records = args[6] as List<LoanRecord>,
            totalRecords = args[7] as Int
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = LoansUiState(isLoading = true)
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
            repository: LoansRepository? = null,
            authToken: String? = null
        ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                if (modelClass.isAssignableFrom(LoansViewModel::class.java)) {
                    val authenticatedRepository = repository
                        ?: LoansRepositoryImpl(LoanApiService.create(), authToken)
                    return LoansViewModel(authenticatedRepository) as T
                }
                throw IllegalArgumentException("Unknown ViewModel class")
            }
        }
    }
}
