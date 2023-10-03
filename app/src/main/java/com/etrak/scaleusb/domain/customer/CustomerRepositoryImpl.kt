package com.etrak.scaleusb.domain.customer

import com.etrak.scaleusb.dao.CustomerDao
import com.etrak.scaleusb.data.Customer

class CustomerRepositoryImpl(

    private val customerDao: CustomerDao

) : CustomerRepository {
    override fun listAll() = customerDao.listAll()
    override suspend fun add(customer: Customer) = customerDao.add(customer)
}