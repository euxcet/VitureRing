package com.euxcet.viturering

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.Settings
import android.view.WindowManager
import android.widget.RelativeLayout
import androidx.activity.ComponentActivity
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.detector.GestureDetector
import com.hcifuture.producer.detector.OrientationDetector
import com.hcifuture.producer.sensor.NuixSensorManager
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ObjectActivity : ComponentActivity() {

    @Inject
    lateinit var nuixSensorManager: NuixSensorManager
    @Inject
    lateinit var gestureDetector: GestureDetector
    @Inject
    lateinit var orientationDetector: OrientationDetector
    @Inject
    lateinit var ringManager: RingManager

    private lateinit var overlayView: OverlayView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
        setContentView(R.layout.activity_object)
        overlayView = OverlayView(this)
        val layout = findViewById<RelativeLayout>(R.id.objectLayout)
        layout.addView(overlayView)
        connectRing()
    }

    private fun connectRing() {
        ringManager.registerListener {
            onGestureCallback { // Gesture
                runOnUiThread {
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
                        "circle_clockwise" -> {
//                            val intent = Intent(this@ObjectActivity, MainActivity::class.java)
//                            startActivity(intent)
//                            val intent = Intent(Intent.ACTION_MAIN)
//                            intent.addCategory(Intent.CATEGORY_HOME)
//                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//                            startActivity(intent)
                        }
                        "circle_counterclockwise" -> {
                            finish()
                        }
                        "touch_ring" -> {
                            overlayView.reset()
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    overlayView.move(it.first, it.second)
                }
            }
            onTouchCallback { // Touch
                overlayView.reset()
            }
        }

    }
}