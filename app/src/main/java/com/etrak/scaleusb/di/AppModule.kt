package com.etrak.scaleusb.di

import com.etrak.scaleusb.Db
import com.etrak.scaleusb.domain.customer.CustomerRepository
import com.etrak.scaleusb.domain.scale.Scale

interface AppModule {
    val db: Db
    val customerRepository: CustomerRepository
    val scale: Scale
}