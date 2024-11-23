package com.euxcet.viturering.pages.model

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log

class TestReceiver(private val context: Context) : BroadcastReceiver() {


    private var isRegister = false

    private var onReceiverCallback: (action: String) -> Unit = {}

    fun register(callback: (action: String) -> Unit) {
        if (isRegister) {
            return
        }
        isRegister = true
        onReceiverCallback = callback
        val intentFilter = IntentFilter()
        intentFilter.addAction("com.euxcet.viturering.OPEN_WINDOW")
        context.registerReceiver(this, intentFilter, Context.RECEIVER_EXPORTED)
    }

    fun unRegister() {
        if (isRegister) {
            context.unregisterReceiver(this)
            isRegister = false
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        Log.d("TestReceiver", "onReceive")
        intent.action?.let {
            onReceiverCallback.invoke(it)
        }
    }
}