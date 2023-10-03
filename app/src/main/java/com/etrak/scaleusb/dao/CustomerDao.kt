package com.etrak.scaleusb.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.etrak.scaleusb.data.Customer
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomerDao {

    @Query("SELECT * FROM customers ORDER BY name ASC")
    fun listAll() : Flow<List<Customer>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun add(customer: Customer)
}