package com.euxcet.viturering

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.WindowManager
import android.widget.GridView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var ringManager: RingManager

    private var controlView: MainControlView? = null
    private val iconAdapter = HomeIconAdapter()
    private var gridView: GridView? = null
    private val density by lazy {
        resources.displayMetrics.density
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        gridView = findViewById<GridView>(R.id.grid).apply {
            this.adapter = iconAdapter
            setOnItemClickListener { _, _, position, _ ->
                onSelectIcon(position)
            }
        }
        controlView = findViewById<MainControlView>(R.id.control_view).apply {

        }
    }

    override fun onResume() {
        super.onResume()
        connectRing()
    }

    private fun onSelectIcon(position: Int) {
        val key = iconAdapter.getKey(position)
        Log.e("Nuix", "on select Key: $key")
        when (key) {
            "writing" -> {
                val intent = Intent(this@HomeActivity, HandWritingActivity::class.java)
                startActivity(intent)
            }

            "gesture" -> {
                val intent = Intent(this@HomeActivity, GestureDetectActivity::class.java)
                startActivity(intent)
            }
            "model" -> {
                val intent = Intent(this@HomeActivity, Model3DActivity::class.java)
                startActivity(intent)
            }
            "setting" -> {
                val intent = Intent(Settings.ACTION_SETTINGS)
                startActivity(intent)
            }
            "health" -> {
//                val intent = Intent(this@HomeActivity, HealthActivity::class.java)
                val intent = Intent(this@HomeActivity, VideoActivity::class.java)
                startActivity(intent)
            }

            else -> {
            }
        }
    }

    private fun selectCurPointIcon() {
        val cursorPoint = controlView?.getCursorPoint()
        gridView?.pointToPosition(cursorPoint?.x?.toInt() ?: 0, cursorPoint?.y?.toInt() ?: 0)?.let {
            onSelectIcon(it)
        }
        Log.e("Nuix", "Cursor: $cursorPoint")

    }

    private fun connectRing() {
        ringManager.registerListener {
            onConnectCallback { // Connect
                runOnUiThread {

                }
            }
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    when (it) {
                        "pinch" -> {
                        }

                        "middle_pinch" -> {
                            //overlayView?.switch()
                        }

                        "snap" -> {
//                            val intent = Intent(Settings.ACTION_SETTINGS)
//                            startActivity(intent)
                        }
                        "tap_air" -> {
                            val curPos = iconAdapter.getCurFocusedPosition()
                            if (curPos > 0) {
                                onSelectIcon(curPos)
                            }
                        }

                        "circle_clockwise" -> {
                            iconAdapter.focusNext()
//                            val intent = Intent(this@HomeActivity, ObjectActivity::class.java)
//                            startActivity(intent)
                        }

                        "touch_ring" -> {
                            //overlayView?.reset()
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    controlView?.move(it.first * (density.coerceAtLeast(2f)) / 2, it.second * (density.coerceAtLeast(2f)) / 2)
                }
            }
            onStateCallback { // State

            }
            onTouchCallback { // Touch
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
                    Log.e("Nuix", "Touch: $touchText")
//                    touchView.text = touchText
                    when (it.data) {
                        RingTouchEvent.BOTTOM_BUTTON_CLICK -> {

                        }

                        RingTouchEvent.TAP -> {
                            selectCurPointIcon()
                        }

                        else -> {}
//                    }
                    }
                }
            }
            ringManager.connect()
        }
    }
}