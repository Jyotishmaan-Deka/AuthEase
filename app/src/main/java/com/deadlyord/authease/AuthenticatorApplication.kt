package com.deadlyord.authease

import android.app.Application
import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class AuthenticatorApplication : Application() {

    companion object {
        lateinit var instance: AuthenticatorApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        // Restore saved theme on each app start
        val prefs = getSharedPreferences("authease_prefs", MODE_PRIVATE)
        val savedMode = prefs.getInt(
            "theme_mode",
            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        )
        AppCompatDelegate.setDefaultNightMode(savedMode)
    }
}