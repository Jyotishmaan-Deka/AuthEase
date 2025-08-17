package com.deadlyord.authease.ui.addaccount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deadlyord.authease.db.AccountDao
import com.deadlyord.authease.db.AccountEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountDao: AccountDao
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun updateIssuer(issuer: String) {
        _uiState.value = _uiState.value.copy(issuer = issuer)
    }

    fun updateAccountName(accountName: String) {
        _uiState.value = _uiState.value.copy(accountName = accountName)
    }

    fun updateSecret(secret: String) {
        _uiState.value = _uiState.value.copy(secret = secret)
    }

    fun addAccount() {
        val state = _uiState.value

        if (state.issuer.isBlank() || state.accountName.isBlank() || state.secret.isBlank()) {
            _uiState.value = state.copy(error = "Please fill all fields")
            return
        }

        viewModelScope.launch {
            try {
                val account = AccountEntity(
                    issuer = state.issuer.trim(),
                    accountName = state.accountName.trim(),
                    secretKey = state.secret.trim()
                )

                accountDao.insertAccount(account)
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.value = state.copy(error = "Failed to add account: ${e.message}")
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class AddAccountUiState(
    val issuer: String = "",
    val accountName: String = "",
    val secret: String = "",
    val error: String? = null,
    val isLoading: Boolean = false
)

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}