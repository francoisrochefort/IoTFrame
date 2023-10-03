package com.etrak.scaleusb.domain.scale

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class DemoMode : Mode {
    override val events: Flow<Scale.Event> by lazy {
        flow {
            var angle = 0
            while (true) {
                emit(Scale.Event.OnCabAngle(angle))
                delay(1000)
                angle++
            }
        }
    }

    override fun print(message: String) {
        TODO("Not yet implemented")
    }
}