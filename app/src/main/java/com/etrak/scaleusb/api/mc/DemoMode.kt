package com.etrak.scaleusb.api.mc

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DemoMode : Mode {
    override val messages: Flow<Mode.Message> by lazy {
        flow {
            var param = 0
            while (true) {
                emit(Mode.Message(
                        code = "AD38",
                        params = listOf(param.toString())
                    )
                )
                delay(1000)
                param++
            }
        }
    }

    override fun print(message: String) {
        TODO("Not yet implemented")
    }
}