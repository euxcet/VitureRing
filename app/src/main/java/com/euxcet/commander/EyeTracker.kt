package com.euxcet.commander

import android.content.Context
import android.util.Log
import com.seveninvensun.sdk.EyeData
import com.seveninvensun.sdk.Point3D
import com.seveninvensun.sdk.api.CameraCallback
import com.seveninvensun.sdk.api.EyeDataCallback
import com.seveninvensun.sdk.api.ITrackerClient
import com.seveninvensun.sdk.api.ServerConnectCallback
import com.seveninvensun.sdk.api.TrackerCreator

class EyeTracker(
    private val context: Context,
    val commander: Commander,
) {
    private lateinit var trackerClient: ITrackerClient
    private val serverConnectCallback = object: ServerConnectCallback {
        override fun onServerConnected(connected: Boolean) {
            Log.e("Test", "$connected")
            if (!connected) {
                Log.e("Test", "Not connected!")
                return
            }
            var result = trackerClient.initial(3)
            Log.e("Test", "Initial: ${result.message} ${result.success}")
            result = trackerClient.userList
            Log.e("Test", "User list: ${result.data} ${result.success}")
            result = trackerClient.setUser("Guest")
            Log.e("Test", "Set user: ${result.message} ${result.success}")
            result = trackerClient.cameraState
            Log.e("Test", "Camera State: ${result.code} ${result.message} ${result.success}")
            trackerClient.startCamera(cameraCallback)
        }
    }
    private val cameraCallback = object: CameraCallback {
        override fun onState(code: Int, message: String?) {
            Log.e("Test", "$code $message")
            if (code == 0) {
                trackerClient.checkAuthState("")
                trackerClient.registerEyeDataCallback(eyeDataCallback)
            }
        }
    }

    private val eyeDataCallback = object : EyeDataCallback {
        fun logPoint(point: Point3D, prefix: String = "") {
            Log.e("Test", "$prefix ${point.x} ${point.y} ${point.z}")
        }

        override fun onEyeData(eyeData: EyeData) {
            if (eyeData.leftGaze.gazePoint.x > 0.001f) {
                logPoint(eyeData.leftGaze.gazePoint, "gaze")
                logPoint(eyeData.leftGaze.rawPoint, "raw")
                logPoint(eyeData.leftGaze.smoothPoint, "smooth")
            }
            commander.newEyeData(eyeData)
        }
    }

    fun connect() {
        trackerClient = TrackerCreator.getTracker(context, "")
        trackerClient.connect(serverConnectCallback)
    }

    fun disconnect() {
        trackerClient.disconnect()
        trackerClient.release()
    }
}