package com.etrak.scaleusb.api.mc

import kotlinx.coroutines.flow.Flow

interface Mode {

    data class Message(val code: String, val params: List<String>)

    val messages: Flow<Message>
    fun print(message: String)
}

