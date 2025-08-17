package com.deadlyord.authease.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deadlyord.authease.db.AccountDao
import com.deadlyord.authease.db.AccountEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val accountDao: AccountDao
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<AccountEntity>>(emptyList())
    val accounts: StateFlow<List<AccountEntity>> = _accounts.asStateFlow()

    init {
        loadAccounts()
    }

    private fun loadAccounts() {
        viewModelScope.launch {
            accountDao.getAllAccounts().collect { accountList ->
                _accounts.value = accountList
            }
        }
    }

    fun deleteAccount(account: AccountEntity) {
        viewModelScope.launch {
            accountDao.deleteAccount(account)
        }
    }
}