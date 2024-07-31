package com.euxcet.commander

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceView
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.euxcet.commander.ui.theme.CommanderTheme
import com.euxcet.commander.utils.Permission
import com.hcifuture.producer.sensor.NuixSensorManager
import com.hcifuture.producer.sensor.data.RingV1TouchData
import com.hcifuture.producer.sensor.external.ringV1.RingV1Spec
import com.seveninvensun.sdk.EyeData
import com.seveninvensun.sdk.Point3D
import com.seveninvensun.sdk.api.CameraCallback
import com.seveninvensun.sdk.api.EyeDataCallback
import com.seveninvensun.sdk.api.ITrackerClient
import com.seveninvensun.sdk.api.ServerConnectCallback
import com.seveninvensun.sdk.api.TrackerCreator
import com.seveninvensun.sdk.entity.TrackerResult
import dagger.hilt.android.AndroidEntryPoint
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
import kotlinx.coroutines.flow.collectIndexed
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nuixSensorManager: NuixSensorManager

    companion object {
        const val FRAME_WIDTH = 960
        const val FRAME_HEIGHT = 720
    }

    private lateinit var eyeTracker: EyeTracker
    private lateinit var tello: Tello
    private lateinit var commander: Commander

    @RequiresApi(Build.VERSION_CODES.S)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)

        Permission.requestPermissions(this, listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.CAMERA,
            Manifest.permission.FOREGROUND_SERVICE,
            Manifest.permission.FOREGROUND_SERVICE_SPECIAL_USE,
            Manifest.permission.INTERNET,
        ))

        setContentView(R.layout.main)
        val layout = findViewById<RelativeLayout>(R.id.mainLayout)
        val surfaceLayout = findViewById<LinearLayout>(R.id.surfaceLayout)
        val surface = findViewById<SurfaceView>(R.id.surfaceView).holder.surface
        // centering
        val params = surfaceLayout.layoutParams as ViewGroup.MarginLayoutParams
        val windowWidth = windowManager.defaultDisplay.width
        val windowHeight = windowManager.defaultDisplay.height

        val frameWidth = windowHeight * 960 / 720
        val frameHeight = windowHeight
        val frameMargin = (windowWidth - frameWidth) / 2

        params.width = frameWidth
        params.height = frameHeight
        params.leftMargin = frameMargin

        val overlayView = OverlayView(this)
        overlayView.setParams(frameWidth, frameHeight, frameMargin, windowWidth, windowHeight)
        layout.addView(overlayView)

        commander = Commander(overlayView)
        commander.connect()
        eyeTracker = EyeTracker(this, commander)
        eyeTracker.connect()

        CoroutineScope(Dispatchers.Default).launch {
            Log.e("Nuix", "scan")
            nuixSensorManager.scanAll(timeout = 2000L)
            for (sensor in nuixSensorManager.ringV1s()) {
                Log.e("Nuix", sensor.name)
                sensor.connect()
            }
            val ring = nuixSensorManager.defaultRingV1
            Log.e("Nuix", "default ${ring.name}")
            ring.getFlow<RingV1TouchData>(
                RingV1Spec.touchFlowName(ring)
            )?.collect { data ->
                Log.e("Nuix", "touch ${RingV1Spec.touchEventName(data.data)}")
            }
        }

        tello = Tello()
        CoroutineScope(Dispatchers.Default).launch {
            delay(1000)
            tello.connect(surface, disableCommand = true)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        Log.e("Test", "onDestroy")
//        eyeTracker.disconnect()
    }

}
