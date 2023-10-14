package com.etrak.scaleusb.api.mc

/**********************************************************************************************
 * McService Wrapper
 *********************************************************************************************/

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import com.etrak.scaleusb.api.mc.McService.Companion.EXTRA_MESSAGE_CODE
import com.etrak.scaleusb.api.mc.McService.Companion.EXTRA_MESSAGE_PARAMS
import com.etrak.scaleusb.api.mc.McService.Companion.ON_MESSAGE
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow

class Hardware(private val context: Context) : Mode {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val messages: Flow<Mode.Message> by lazy {
        callbackFlow {

            // Start the service
            Intent(context, McService::class.java).apply {
                action = McService.Action.Start.name
                context.startService(this)
            }

            // Receiver implementation
            val receiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context?, intent: Intent) {

                    // Emit the newly received message
                    trySend(
                        Mode.Message(
                            code = intent.getStringExtra(EXTRA_MESSAGE_CODE)!!,
                            params = intent.getStringArrayExtra(EXTRA_MESSAGE_PARAMS)!!.toList()
                        )
                    )
                }
            }

            // Register the receiver
            context.registerReceiver(
                receiver,
                IntentFilter().apply {
                    addAction(ON_MESSAGE)
                }
            )

            awaitClose {

                // Unregister the receiver
                context.unregisterReceiver(receiver)
            }
        }
    }

    override fun send(msg: Mode.Message) {

        // Send the message to thew service
        Intent(context, McService::class.java).apply {
            action = McService.Action.Print.name
            putExtra(EXTRA_MESSAGE_CODE, msg.code)
            putExtra(EXTRA_MESSAGE_PARAMS, msg.params.toTypedArray())
            context.startService(this)
        }
    }
}