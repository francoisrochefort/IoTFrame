package com.etrak.scaleusb.di

import com.etrak.scaleusb.Db
import com.etrak.scaleusb.Scale
import com.etrak.scaleusb.domain.customer.CustomerRepository

interface AppModule {
    val db: Db
    val customerRepository: CustomerRepository
    val scale: Scale
}