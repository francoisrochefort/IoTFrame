package com.etrak.scaleusb.di

import android.content.Context
import android.util.Log
import androidx.room.Room
import com.etrak.scaleusb.Db
import com.etrak.scaleusb.Scale
import com.etrak.scaleusb.TAG
import com.etrak.scaleusb.UsbDeviceApi
import com.etrak.scaleusb.domain.CustomerRepository
import com.etrak.scaleusb.domain.CustomerRepositoryImpl
import com.hoho.android.usbserial.driver.UsbSerialPort

// https://github.com/philipplackner/ManualDependencyInjection
// https://www.youtube.com/watch?v=eX-y0IEHJjM&t=647s

class AppModuleImpl(private val context: Context) : AppModule {

    override val db: Db by lazy {
        Room.databaseBuilder(
            context,
            Db::class.java,
            "scale_pro"
        ).build()
    }

    override val customerRepository: CustomerRepository by lazy {
        CustomerRepositoryImpl(
            db.customerDao
        )
    }

//    override val usbDeviceApi: UsbDeviceApi by lazy {
//        UsbDeviceApi.Builder()
//            .baudRate(115200)
//            .dataBits(8)
//            .stopBits(UsbSerialPort.STOPBITS_1)
//            .parity(UsbSerialPort.PARITY_NONE)
//            .build(context = context)
//    }

    override val scale: Scale by lazy {
//        Scale(usbDeviceApi)
        Scale()
    }
}