package com.deadlyord.authease.ui.addaccount

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deadlyord.authease.auth.CryptoHelper
import com.deadlyord.authease.db.AccountDao
import com.deadlyord.authease.db.AccountEntity
import com.deadlyord.authease.utils.isValidBase32
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AddAccountViewModel @Inject constructor(
    private val accountDao: AccountDao,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddAccountUiState())
    val uiState: StateFlow<AddAccountUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<NavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    private val cryptoHelper = CryptoHelper(context)

    fun updateIssuer(issuer: String) { _uiState.value = _uiState.value.copy(issuer = issuer) }
    fun updateAccountName(name: String) { _uiState.value = _uiState.value.copy(accountName = name) }
    fun updateSecret(secret: String) { _uiState.value = _uiState.value.copy(secret = secret) }
    fun updateAlgorithm(algorithm: String) { _uiState.value = _uiState.value.copy(algorithm = algorithm) }
    fun updateDigits(digits: Int) { _uiState.value = _uiState.value.copy(digits = digits) }
    fun clearError() { _uiState.value = _uiState.value.copy(error = null) }

    fun addAccount() {
        val state = _uiState.value

        if (state.issuer.isBlank() || state.accountName.isBlank() || state.secret.isBlank()) {
            _uiState.value = state.copy(error = "Please fill in all required fields")
            return
        }

        val cleanSecret = state.secret.trim().uppercase().replace(" ", "")
        if (!cleanSecret.isValidBase32()) {
            _uiState.value = state.copy(error = "Invalid Base32 secret key format")
            return
        }

        _uiState.value = state.copy(isLoading = true, error = null)

        viewModelScope.launch {
            try {
                val encryptedSecret = try {
                    cryptoHelper.encrypt(cleanSecret)
                } catch (e: Exception) {
                    cleanSecret // fallback to plain text if encryption unavailable
                }

                val account = AccountEntity(
                    issuer = state.issuer.trim(),
                    accountName = state.accountName.trim(),
                    secretKey = encryptedSecret,
                    algorithm = state.algorithm,
                    digits = state.digits
                )

                accountDao.insertAccount(account)
                _navigationEvent.emit(NavigationEvent.NavigateBack)
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    error = "Failed to add account: ${e.message}",
                    isLoading = false
                )
            }
        }
    }
}

data class AddAccountUiState(
    val issuer: String = "",
    val accountName: String = "",
    val secret: String = "",
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val error: String? = null,
    val isLoading: Boolean = false
)

sealed class NavigationEvent {
    object NavigateBack : NavigationEvent()
}
