package com.euxcet.viturering.utils

class GestureThrottle(gesture: String) {

    companion object {
        const val TAG = "GestureThrottle"
        private val gestureMap = mutableMapOf<String, GestureThrottle>()

        fun getThrottle(gesture: String): GestureThrottle {
            return gestureMap.getOrPut(gesture) { GestureThrottle(gesture) }
        }

        fun throttle(gesture: String): Boolean {
            return getThrottle(gesture).throttle()
        }
    }

    private val gesture = gesture
    private var lastGestureTime = 0L
    private val throttleTime = 500L

    fun throttle(): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime - lastGestureTime < throttleTime) {
            return true
        }
        lastGestureTime = currentTime
        return false
    }
}