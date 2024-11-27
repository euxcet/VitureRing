package com.euxcet.viturering

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi
import androidx.core.content.edit
import androidx.core.view.children
import com.euxcet.viturering.utils.LanguageUtils
import com.euxcet.viturering.utils.Permission
import com.hcifuture.producer.common.network.http.HttpService
import com.hcifuture.producer.sensor.NuixSensor
import com.hcifuture.producer.sensor.data.RingTouchEvent
import com.hcifuture.producer.sensor.external.ring.ringV2.RingV2
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var ringManager: RingManager

    private var overlayView: OverlayView? = null

    private var ipEditText: EditText? = null
    private var portEditText: EditText? = null

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
        ipEditText = findViewById(R.id.ip)
        portEditText = findViewById(R.id.port)
        findViewById<TextView>(R.id.toHome).setOnClickListener {
            val intent = Intent(this@MainActivity, HomeActivity::class.java)
            startActivity(intent)
            ringManager.sendMessage("测试")
        }
        findViewById<TextView>(R.id.calibrate).setOnClickListener {
            ringManager.calibrate()
            Toast.makeText(this, "校准成功", Toast.LENGTH_SHORT).show()
        }
        findViewById<Button>(R.id.connect_socket).setOnClickListener {
            try {
                val ip = ipEditText?.text.toString()
                val port = portEditText?.text?.toString()?.toInt() ?: 12345
                ringManager.startSocketClient(ip, port)
                val sharedPreferences = getSharedPreferences("RingManager", Context.MODE_PRIVATE)
                sharedPreferences.edit {
                    putString("ip", ip)
                    putString("port", port.toString())
                }
                findViewById<Button>(R.id.connect_socket).isEnabled = false
            } catch (e: Exception) {
                Log.e("MainActivity", "socket连接失败", e)
            }
        }
        findViewById<Button>(R.id.disconnect_socket).setOnClickListener {
            ringManager.stopSocketClient()
        }
        val sharedPreferences = getSharedPreferences("RingManager", Context.MODE_PRIVATE)
        ipEditText?.setText(sharedPreferences.getString("ip", ""))
        portEditText?.setText(sharedPreferences.getString("port", ""))
        ringManager.setSocketConnectCallback { connected ->
            runOnUiThread {
                findViewById<Button>(R.id.connect_socket).isEnabled = true
                if (connected) {
                    findViewById<Button>(R.id.connect_socket).visibility = View.GONE
                    findViewById<Button>(R.id.disconnect_socket).visibility = View.VISIBLE
                }
            }
        }
        ringManager.setSocketDisconnectCallback {
            runOnUiThread {
                findViewById<Button>(R.id.connect_socket).visibility = View.VISIBLE
                findViewById<Button>(R.id.disconnect_socket).visibility = View.GONE
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectRing()
    }

    private fun createRingButton(ring: NuixSensor): Button {
        val ringLayout = findViewById<LinearLayout>(R.id.ringLayout)
        val button = Button(this@MainActivity)
        button.text = ring.name
        if (ringManager.isActive(ring)) {
            button.setBackgroundColor(Color.rgb(30, 150, 30))
        } else {
            button.setBackgroundColor(Color.rgb(220, 220, 220))
        }
        button.setOnClickListener {
            if (!ringManager.isActive(ring)) {
                for (child in ringLayout.children) {
                    if ((child as Button).text == ring.name) {
                        child.setBackgroundColor(Color.rgb(30, 150, 30))
                    } else {
                        child.setBackgroundColor(Color.rgb(220, 220, 220))
                    }
                }
                ringManager.selectRing(ring)
            } else {
                for (child in ringLayout.children) {
                    child.setBackgroundColor(Color.rgb(220, 220, 220))
                }
                ringManager.deselect()
            }
        }
        return button
    }

    private fun connectRing() {
        val touchView = findViewById<TextView>(R.id.touchView)
        val gestureView = findViewById<TextView>(R.id.gestureView)
        val statusView = findViewById<TextView>(R.id.statusView)
        val ringLayout = findViewById<LinearLayout>(R.id.ringLayout)
        ringManager.registerListener {
            onConnectCallback { // Connect
                runOnUiThread {
                    ringLayout.removeAllViews()
                    for (ring in ringManager.rings()) {
                        ringLayout.addView(createRingButton(ring))
                    }
                }
            }
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    gestureView.text = gestureText
                    when (it) {
                        "pinch" -> {
                            overlayView?.select()
                        }
                        "middle_pinch" -> {
                            overlayView?.switch()
                        }
                        "snap" -> {
                            val intent = Intent(Settings.ACTION_SETTINGS)
                            startActivity(intent)
                        }
                        "circle_clockwise" -> {
                            val intent = Intent(this@MainActivity, HomeActivity::class.java)
                            startActivity(intent)
                        }
                        "touch_ring" -> {
                            overlayView?.reset()
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    overlayView?.move(it.first, it.second)
                }
            }
            onStateCallback { // State
                runOnUiThread {
                    val statusText = "连接状态: ${LanguageUtils.statusChinese(it)}"
                    statusView.text = statusText
                }
            }
            onTouchCallback { // Touch
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
                    touchView.text = touchText
                    when (it.data) {
                        RingTouchEvent.BOTTOM_BUTTON_CLICK -> {
                            overlayView?.reset()
                        }
                        RingTouchEvent.TAP -> {
                            overlayView?.reset()
                        }
                        else -> {}
                    }
                }
            }
        }
        ringManager.connect()
    }

    override fun onDestroy() {
        super.onDestroy()
    }

}
