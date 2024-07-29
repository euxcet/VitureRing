package com.euxcet.commander

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    private lateinit var eyeTracker: EyeTracker
    private lateinit var tello: Tello
    private lateinit var commander: Commander

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        requestPermissions()
        Log.e("Test", "${windowManager.defaultDisplay.width} ${windowManager.defaultDisplay.height}")

        setContentView(R.layout.main)
        val layout = findViewById<RelativeLayout>(R.id.mainLayout)
        val surface = findViewById<SurfaceView>(R.id.surfaceView).holder.surface
        val overlayView = OverlayView(this)
        layout.addView(overlayView)

        commander = Commander(overlayView)
        commander.connect()
        eyeTracker = EyeTracker(this, commander)
        eyeTracker.connect()

//        tello = Tello()
//        tello.connect(surface, disableCommand = false)
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                overlayView.resetEye()
                delay(100)
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e("Test", "onDestroy")
//        eyeTracker.disconnect()
    }

    private fun requestPermissions() {
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
    }
}
