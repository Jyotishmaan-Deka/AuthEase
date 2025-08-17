package com.deadlyord.authease.utils

import android.content.Context
import android.widget.Toast

fun Context.showToast(message: String, duration: Int = Toast.LENGTH_SHORT) {
    Toast.makeText(this, message, duration).show()
}

fun String.isValidBase32(): Boolean {
    return this.matches(Regex("[A-Z2-7]+"))
}