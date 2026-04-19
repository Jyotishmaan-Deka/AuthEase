package com.deadlyord.authease.ui.settings

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.deadlyord.authease.AuthenticatorApplication
import com.deadlyord.authease.db.AccountDao
import com.deadlyord.authease.db.AccountEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import javax.inject.Inject
import androidx.core.content.edit

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val accountDao: AccountDao
) : ViewModel() {

    private val _themeLabel = MutableLiveData<String>()
    val themeLabel: LiveData<String> = _themeLabel

    init {
        updateThemeLabel(AppCompatDelegate.getDefaultNightMode())
    }

//    fun saveThemeMode(mode: Int) {
//        AppCompatDelegate.setDefaultNightMode(mode)
//        updateThemeLabel(mode)
//    }

    fun saveThemeMode(mode: Int) {
        AppCompatDelegate.setDefaultNightMode(mode)
        updateThemeLabel(mode)
        // Persist using Application context
        AuthenticatorApplication.instance.getSharedPreferences("authease_prefs", Context.MODE_PRIVATE)
            .edit {
                putInt("theme_mode", mode)
            }
    }

    private fun updateThemeLabel(mode: Int) {
        _themeLabel.value = when (mode) {
            AppCompatDelegate.MODE_NIGHT_NO -> "Light"
            AppCompatDelegate.MODE_NIGHT_YES -> "Dark"
            else -> "Follow system"
        }
    }

    /**
     * Exports all accounts to a JSON file at the given URI.
     * Returns true on success.
     */
    suspend fun exportAccounts(uri: Uri, resolver: ContentResolver): Boolean =
        withContext(Dispatchers.IO) {
            try {
                val accounts = accountDao.getAllAccountsOnce()
                val array = JSONArray()
                for (account in accounts) {
                    val obj = JSONObject().apply {
                        put("issuer", account.issuer)
                        put("accountName", account.accountName)
                        put("secretKey", account.secretKey)
                        put("algorithm", account.algorithm)
                        put("digits", account.digits)
                        put("period", account.period)
                    }
                    array.put(obj)
                }
                resolver.openOutputStream(uri)?.use { stream ->
                    stream.write(array.toString(2).toByteArray())
                }
                true
            } catch (e: Exception) {
                e.printStackTrace()
                false
            }
        }

    /**
     * Imports accounts from a JSON file at the given URI.
     * Returns the number of accounts imported, or -1 on error.
     */
    suspend fun importAccounts(uri: Uri, resolver: ContentResolver): Int =
        withContext(Dispatchers.IO) {
            try {
                val content = resolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                    ?: return@withContext -1

                val array = JSONArray(content)
                val entities = mutableListOf<AccountEntity>()
                for (i in 0 until array.length()) {
                    val obj = array.getJSONObject(i)
                    entities.add(
                        AccountEntity(
                            issuer = obj.getString("issuer"),
                            accountName = obj.getString("accountName"),
                            secretKey = obj.getString("secretKey"),
                            algorithm = obj.optString("algorithm", "HmacSHA1"),
                            digits = obj.optInt("digits", 6),
                            period = obj.optInt("period", 30)
                        )
                    )
                }
                accountDao.insertAll(entities)
                entities.size
            } catch (e: Exception) {
                e.printStackTrace()
                -1
            }
        }
}
