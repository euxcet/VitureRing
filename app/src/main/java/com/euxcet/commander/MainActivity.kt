package com.euxcet.commander

import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.euxcet.commander.ui.theme.CommanderTheme
import com.seveninvensun.sdk.EyeData
import com.seveninvensun.sdk.Point3D
import com.seveninvensun.sdk.api.CameraCallback
import com.seveninvensun.sdk.api.EyeDataCallback
import com.seveninvensun.sdk.api.ITrackerClient
import com.seveninvensun.sdk.api.ServerConnectCallback
import com.seveninvensun.sdk.api.TrackerCreator
import com.seveninvensun.sdk.entity.TrackerResult
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var eyeTracker: EyeTracker
    private lateinit var tello: Tello
    private val commander = Commander()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    Log.i("Permission: ", "Granted")
                } else {
                    Log.i("Permission: ", "Denied")
                }
            }
        requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
        commander.connect()
        eyeTracker = EyeTracker(this, commander)
        eyeTracker.connect()
//        tello = Tello()
//        tello.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
        eyeTracker.disconnect()
    }
}
