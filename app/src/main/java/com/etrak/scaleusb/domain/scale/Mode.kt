package com.etrak.scaleusb.domain.scale

import kotlinx.coroutines.flow.Flow

interface Mode {
    val events: Flow<Scale.Event>
    fun print(message: String)
}

