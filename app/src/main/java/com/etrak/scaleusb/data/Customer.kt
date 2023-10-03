package com.etrak.scaleusb.data

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "customers",
    indices = [
        Index(value = ["name"], unique = true)
    ]
)
data class Customer(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)
