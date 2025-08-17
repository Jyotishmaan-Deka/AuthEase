package com.deadlyord.authease.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class AccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val issuer: String,
    val accountName: String,
    val secretKey: String,
    val algorithm: String = "SHA1",
    val digits: Int = 6,
    val period: Int = 30,
    val counter: Int =0,    // For HOTP
    @ColumnInfo(name = "created_at") val createdAt: Long = System.currentTimeMillis()
)

