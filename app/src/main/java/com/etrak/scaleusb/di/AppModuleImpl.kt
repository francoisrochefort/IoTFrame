package com.etrak.scaleusb.di

import android.content.Context
import androidx.room.Room
import com.etrak.scaleusb.Db
import com.etrak.scaleusb.domain.customer.CustomerRepository
import com.etrak.scaleusb.domain.customer.CustomerRepositoryImpl
import com.etrak.scaleusb.api.mc.Mc
import com.etrak.scaleusb.domain.scale.Scale
import com.etrak.scaleusb.domain.scale.ScaleService

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

    override val mc: Mc by lazy {
        Mc(
            context = context,
            service = ScaleService::class.java
        ).apply {
            start()
        }
    }

    override val scale: Scale by lazy {
        Scale(mc)
    }
}