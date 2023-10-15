package com.etrak.scaleusb.api.mc

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.etrak.scaleusb.api.mc.McService.Companion.EXTRA_MESSAGE_CODE
import com.etrak.scaleusb.api.mc.McService.Companion.EXTRA_MESSAGE_PARAMS
import com.etrak.scaleusb.api.mc.McService.Companion.ON_MESSAGE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.*

class Mc(

    private val context: Context,
    private val service: Class<*>?

) {
    fun send(msg: Device.Message) {

        // Send the message to the service
        Intent(context, service).apply {
            action = McService.Action.Send.name
            putExtra(EXTRA_MESSAGE_CODE, msg.code)
            putExtra(EXTRA_MESSAGE_PARAMS, msg.params.toTypedArray())
            context.startService(this)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val messages = callbackFlow {

        // Receiver
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {

                // Send message
                trySend(
                    Device.Message(
                        code = intent.getStringExtra(EXTRA_MESSAGE_CODE)!!,
                        params = intent.getStringArrayExtra(EXTRA_MESSAGE_PARAMS)!!.toList()
                    )
                )
            }
        }

        // Register the receiver
        IntentFilter().apply {
            addAction(ON_MESSAGE)
            context.registerReceiver(receiver, this)
        }

        awaitClose {

            // Unregister the receiver
            context.unregisterReceiver(receiver)
        }
    }

    fun start() {

        // Start the service
        Intent(context, service).apply {
            action = McService.Action.Start.name
            context.startService(this)
        }
    }
}
















