package com.etrak.scaleusb.domain

import com.etrak.scaleusb.data.Customer
import kotlinx.coroutines.flow.Flow

interface CustomerRepository {
    fun listAll() : Flow<List<Customer>>
    suspend fun add(customer: Customer)
}