package com.etrak.scaleusb.domain.scale

import com.etrak.scaleusb.api.mc.Device
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DeviceEmulator : Device {
    override val messages: Flow<Device.Message> by lazy {
        flow {
            var param = 0
            while (true) {
                emit(
                    Device.Message(
                        code = "AD38",
                        params = listOf(param.toString())
                    )
                )
                delay(1000)
                param++
            }
        }
    }

    override fun send(msg: Device.Message) {
        TODO("Not yet implemented")
    }

    override fun connect() {
        TODO("Not yet implemented")
    }
}