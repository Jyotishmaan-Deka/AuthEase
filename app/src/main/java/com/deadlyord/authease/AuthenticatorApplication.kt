package com.deadlyord.authease

import android.app.Application
import com.deadlyord.authease.di.AppModule


class AuthenticatorApplication : Application() {
    val appModule = AppModule
}