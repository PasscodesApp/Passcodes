package com.jeeldobariya.passcodes.autofill.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "passcodes")
data class Passcode(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val value: String
)
