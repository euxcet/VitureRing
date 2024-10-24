package com.dmitrybrant.modelviewer

import android.content.Context

class ModuleProxy(private val context: Context) {

    companion object {

        private const val TAG = "ModuleProxy"
        private lateinit var instacne: ModuleProxy

        fun getInstance(): ModuleProxy {
            return instacne
        }

        fun initModule(context: Context) {
            instacne = ModuleProxy(context)
        }
    }

    fun getContext(): Context {
        return context
    }
}