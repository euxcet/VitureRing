package com.euxcet.viturering

import android.Manifest
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import com.euxcet.viturering.utils.Permission
import com.hcifuture.producer.detector.DynamicGestureDetector
import com.hcifuture.producer.detector.GestureDetector
import com.hcifuture.producer.detector.OrientationDetector
import com.hcifuture.producer.sensor.NuixSensorManager
import com.hcifuture.producer.sensor.NuixSensorState
import com.hcifuture.producer.sensor.data.RingTouchData
import com.hcifuture.producer.sensor.data.RingTouchEvent
import com.hcifuture.producer.sensor.external.ring.RingSpec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var nuixSensorManager: NuixSensorManager
    @Inject
    lateinit var gestureDetector: GestureDetector
    @Inject
    lateinit var orientationDetector: OrientationDetector

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
            Manifest.permission.INTERNET,
        ))

        setContentView(R.layout.main)

        val touchView = findViewById<TextView>(R.id.touchView)
        val gestureView = findViewById<TextView>(R.id.gestureView)
        val statusView = findViewById<TextView>(R.id.statusView)

        val layout = findViewById<RelativeLayout>(R.id.mainLayout)
        val overlayView = OverlayView(this)
        layout.addView(overlayView)

        // Connect
        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                if (!nuixSensorManager.defaultRing.disconnectable()) {
                    nuixSensorManager.scanAll(timeout = 3000L)
                    for (ring in nuixSensorManager.ringV1s()) {
                        ring.connect()
                        while (ring.status == NuixSensorState.CONNECTING) {
                            delay(100)
                        }
                        break
                    }
                } else {
                    delay(6000)
                }
            }
        }

        // Touch
        CoroutineScope(Dispatchers.Default).launch {
            val ring = nuixSensorManager.defaultRing
            ring.getProxyFlow<RingTouchData>(
                RingSpec.touchEventFlowName(ring)
            )?.collect { data ->
                runOnUiThread {
                    val event = data.data
                    touchView.text = "触摸: ${touchChinese(event)}"
                    when (event) {
                        RingTouchEvent.BOTTOM_BUTTON_CLICK -> {
                            overlayView.reset()
                        }
                        RingTouchEvent.TAP -> {
                            overlayView.reset()
                        }
                        else -> {}
                    }
                }
            }
        }

        // Move cursor
        orientationDetector.start()
        CoroutineScope(Dispatchers.Default).launch {
            orientationDetector.eventFlow.collect {
                runOnUiThread {
                    overlayView.move(it.first, it.second)
                }
            }
        }

        // Gesture
        gestureDetector.start()
        CoroutineScope(Dispatchers.Default).launch {
            gestureDetector.eventFlow.collect {
                runOnUiThread {
                    gestureView.text = "手势: ${gestureChinese(it)}"
                    when (it) {
                        "pinch" -> {
                            overlayView.select()
                        }
                        "middle_pinch" -> {
                            overlayView.switch()
                        }
                        "snap" -> {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            startActivity(intent)
                        }
                        "clap" -> {
                            val intent = packageManager.getLaunchIntentForPackage(packageName)
                            intent?.let {
                                it.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(it)
                            }
                        }
                        "circle_clockwise" -> {
                            val intent = Intent(Intent.ACTION_MAIN)
                            intent.addCategory(Intent.CATEGORY_HOME)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                            startActivity(intent)
                        }
                        "touch_ring" -> {
                            overlayView.reset()
                        }
                    }
                }
            }
        }


        CoroutineScope(Dispatchers.Default).launch {
            val ring = nuixSensorManager.defaultRing
            while (true) {
                runOnUiThread {
                    statusView.text = "连接状态: ${statusChinese(ring.status)}"
                }
                delay(1000)
            }
        }

        CoroutineScope(Dispatchers.Default).launch {
            while (true) {
                overlayView.postInvalidate()
                delay(30)
            }
        }
    }

    fun statusChinese(status: NuixSensorState): String {
        return when(status) {
            NuixSensorState.SCANNING -> "扫描中"
            NuixSensorState.CONNECTING -> "连接中"
            NuixSensorState.CONNECTED -> "已连接"
            NuixSensorState.DISCONNECTED -> "已断连"
        }
    }

    fun gestureChinese(gesture: String): String {
        return when (gesture) {
            "pinch" -> { "捏合" }
            "middle_pinch" -> { "中指捏合" }
            "clap" -> { "拍手" }
            "snap" -> { "打响指" }
            "tap_plane" -> { "桌面单击" }
            "tap_air" -> { "空中单击" }
            "circle_clockwise" -> { "顺时针转" }
            "circle_counterclockwise" -> { "逆时针转" }
            "touch_ring" -> { "单击戒指" }
            "touch_up" -> { "上滑" }
            "touch_down" -> { "下滑" }
            else -> { gesture }
        }
    }

    fun touchChinese(touch: RingTouchEvent): String {
        return when (touch) {
            RingTouchEvent.UNKNOWN -> "未知"
            RingTouchEvent.BOTH_BUTTON_PRESS -> "双键按压"
            RingTouchEvent.BOTH_BUTTON_RELEASE -> "双键释放"
            RingTouchEvent.BOTTOM_BUTTON_CLICK -> "下键单击"
            RingTouchEvent.BOTTOM_BUTTON_DOUBLE_CLICK -> "下键双击"
            RingTouchEvent.BOTTOM_BUTTON_LONG_PRESS -> "下键长按"
            RingTouchEvent.BOTTOM_BUTTON_RELEASE -> "下键释放"
            RingTouchEvent.TOP_BUTTON_CLICK -> "上键单击"
            RingTouchEvent.TOP_BUTTON_DOUBLE_CLICK -> "上键双击"
            RingTouchEvent.TOP_BUTTON_LONG_PRESS -> "上键长按"
            RingTouchEvent.TOP_BUTTON_RELEASE -> "上键释放"
            RingTouchEvent.TAP -> "单击"
            RingTouchEvent.SWIPE_POSITIVE -> "上滑"
            RingTouchEvent.SWIPE_NEGATIVE -> "下滑"
            RingTouchEvent.FLICK_POSITIVE -> "上滑"
            RingTouchEvent.FLICK_NEGATIVE -> "下滑"
            RingTouchEvent.HOLD -> "长按"
        }
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
