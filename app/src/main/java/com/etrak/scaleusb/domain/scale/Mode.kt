package com.etrak.scaleusb.domain.scale

import android.content.Context
import kotlinx.coroutines.flow.Flow

interface Mode {

    class Builder(private val context: Context) {

        private lateinit var connectionStatus: Scale.ConnectionStatus

        fun connectionStatus(connectionStatus: Scale.ConnectionStatus): Builder {
            this.connectionStatus = connectionStatus
            return this
        }

        fun build(): Mode {
            return if (connectionStatus == Scale.ConnectionStatus.Connected)
                NormalMode(context = context)
            else
                DemoMode()
        }
    }

    val events: Flow<Scale.Event>
    fun print(message: String)
}

//val mode = ModeBuilder(context)
//    .connectionStatus(Scale.ConnectionStatus.Connected)
//    .build()
