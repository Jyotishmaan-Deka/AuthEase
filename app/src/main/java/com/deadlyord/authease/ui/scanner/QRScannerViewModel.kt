package com.deadlyord.authease.ui.scanner

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deadlyord.authease.db.AccountDao
import com.deadlyord.authease.utils.QRCodeParser
import com.deadlyord.authease.utils.isValidBase32
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class QRScannerViewModel @Inject constructor(
    private val accountDao: AccountDao
) : ViewModel() {

    private val _navigationEvent = MutableSharedFlow<QRScannerNavigationEvent>()
    val navigationEvent = _navigationEvent.asSharedFlow()

    fun processQRCode(qrContent: String) {
        viewModelScope.launch {
            try {
                val account = QRCodeParser.parseOtpAuthUri(qrContent)

                if (account == null) {
                    _navigationEvent.emit(
                        QRScannerNavigationEvent.ShowError("Invalid QR code format")
                    )
                    return@launch
                }

                // Validate secret key format
                if (!account.secretKey.isValidBase32()) {
                    _navigationEvent.emit(
                        QRScannerNavigationEvent.ShowError("Invalid secret key format")
                    )
                    return@launch
                }

                // Save the account
                accountDao.insertAccount(account)
                _navigationEvent.emit(QRScannerNavigationEvent.NavigateBackWithAccount)

            } catch (e: Exception) {
                _navigationEvent.emit(
                    QRScannerNavigationEvent.ShowError("Failed to process QR code: ${e.message}")
                )
            }
        }
    }
}

sealed class QRScannerNavigationEvent {
    object NavigateBackWithAccount : QRScannerNavigationEvent()
    data class ShowError(val message: String) : QRScannerNavigationEvent()
}