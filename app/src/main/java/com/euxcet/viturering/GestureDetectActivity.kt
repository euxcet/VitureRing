package com.euxcet.viturering

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.detector.TouchState
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class GestureDetectActivity : AppCompatActivity() {

    @Inject
    lateinit var ringManager: RingManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        setContentView(R.layout.activity_gesture_detect)
        connectRing()
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
                    findViewById<TextView>(R.id.gesture_info).text = gestureText
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
                        "circle_clockwise" -> {
//                            val intent = Intent(this@HomeActivity, ObjectActivity::class.java)
//                            startActivity(intent)
                        }
                        "circle_counterclockwise" -> {
                            finish()
                        }
                        "touch_ring" -> {
                            //overlayView?.reset()
                        }
                    }
                }
            }
            onStateCallback { // State

            }
            onTouchCallback { // Touch
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
                    findViewById<TextView>(R.id.gesture_info).text = touchText
//                    touchView.text = touchText
//                    when (it.data) {
//                        RingTouchEvent.BOTTOM_BUTTON_CLICK -> {
//                            overlayView?.reset()
//                        }
//                        RingTouchEvent.TAP -> {
//                            overlayView?.reset()
//                        }
//                        else -> {}
//                    }
                }
            }
            onPlaneEventCallback {
                runOnUiThread {
                    val text = "桌面手势: ${LanguageUtils.planeChinese(it)}"
                    if (it == TouchState.DOWN) {
                        Log.e("Nuix", "Plane down")

                    } else {
                        Log.e("Nuix", "Plane up")

                    }
                    findViewById<TextView>(R.id.gesture_info).text = text
                }
            }
        }
        ringManager.connect()
    }
}