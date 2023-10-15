package com.etrak.scaleusb.api.mc

import kotlinx.coroutines.flow.Flow

interface Device {

    data class Message(val code: String, val params: List<String>)

    val messages: Flow<Message>
    fun send(msg: Message)
    fun connect()
}

