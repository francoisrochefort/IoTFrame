package com.etrak.scaleusb.domain.scale

import com.etrak.scaleusb.api.mc.Mc
import com.etrak.scaleusb.api.mc.Mode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Scale(

    private val mc: Mc

) {
    sealed class Event {
        data class OnCabAngle(val angle: Int) : Event()
        data class OnError(val msg: Mode.Message) : Event()
    }

    val events: Flow<Event> by lazy {
        mc.messages.map {
            when (it.code) {

                "AD38" -> Event.OnCabAngle(it.params[0].toInt())

                else -> Event.OnError(it)
            }
        }
    }
}