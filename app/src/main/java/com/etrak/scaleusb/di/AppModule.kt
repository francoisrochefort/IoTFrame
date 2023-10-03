package com.etrak.scaleusb.di

import com.etrak.scaleusb.Db
import com.etrak.scaleusb.Scale
import com.etrak.scaleusb.UsbDeviceApi
import com.etrak.scaleusb.domain.CustomerRepository

interface AppModule {

    val db: Db
    val customerRepository: CustomerRepository
//    val usbDeviceApi: UsbDeviceApi
    val scale: Scale

}