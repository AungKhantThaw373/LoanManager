package com.example.kotlinmulti.loanmanager.ui.repayments

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import com.example.kotlinmulti.loanmanager.data.remote.LoanApiService
import com.example.kotlinmulti.loanmanager.data.repository.RepaymentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class RepaymentViewModel(
    private val repository: RepaymentRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RepaymentUiState())
    val uiState: StateFlow<RepaymentUiState> = _uiState.asStateFlow()
    private var selectedLoanId: String? = null
    private var activeRequest: Job? = null
    private var generation = 0L

    fun fetchForLoan(loanId: String) {
        if (selectedLoanId == loanId && (_uiState.value.isLoading || _uiState.value.records.isNotEmpty())) return
        selectedLoanId = loanId
        load(loanId)
    }

    fun retry() {
        selectedLoanId?.let(::load)
    }

    private fun load(loanId: String) {
        generation++
        val requestGeneration = generation
        activeRequest?.cancel()
        activeRequest = viewModelScope.launch {
            _uiState.value = RepaymentUiState(isLoading = true)
            repository.fetchHistory(loanId).fold(
                onSuccess = { records ->
                    if (requestGeneration == generation) _uiState.value = RepaymentUiState(records = records)
                },
                onFailure = { error ->
                    if (requestGeneration == generation) {
                        _uiState.value = RepaymentUiState(
                            errorMessage = error.localizedMessage ?: "Could not load repayment history."
                        )
                    }
                }
            )
        }
    }

    companion object {
        fun provideFactory(authToken: String?): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (!modelClass.isAssignableFrom(RepaymentViewModel::class.java)) {
                        throw IllegalArgumentException("Unknown ViewModel class")
                    }
                    val repository = RepaymentRepository(
                        LoanApiService.instance,
                        authToken
                    )
                    return RepaymentViewModel(repository) as T
                }
            }
    }
}
