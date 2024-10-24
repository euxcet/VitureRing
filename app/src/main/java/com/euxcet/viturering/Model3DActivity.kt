package com.euxcet.viturering

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import com.dmitrybrant.modelviewer.Model
import com.dmitrybrant.modelviewer.ModelSurfaceView
import com.dmitrybrant.modelviewer.stl.StlModel
import com.euxcet.viturering.databinding.ActivityModel3dBinding
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.detector.TouchState
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class Model3DActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Model3DActivity"
    }

    @Inject
    lateinit var ringManager: RingManager
    private lateinit var binding: ActivityModel3dBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private var modelView: ModelSurfaceView? = null
    private var currentModel: Model? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        binding = ActivityModel3dBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun createNewModelView(model: Model?) {
        if (modelView != null) {
            binding.container.removeView(modelView)
        }
        modelView = ModelSurfaceView(this, model)
        binding.container.addView(modelView, 0)
    }

    private fun loadSampleModel() {
        try {
            assets.open("dragon.stl").use {
                currentModel = StlModel(it)
                createNewModelView(currentModel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sample model", e)
        }
    }

    override fun onStart() {
        super.onStart()
        loadSampleModel()
        connectRing()
    }

    private var isTouchDown = false

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
                        "circle_clockwise" -> {

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
            onMoveCallback { // Move
                runOnUiThread {

                }
            }
            onStateCallback { // State
                runOnUiThread {
//                    val statusText = "连接状态: ${LanguageUtils.statusChinese(it)}"
                    // statusView.text = statusText
                    findViewById<TextView>(R.id.ring_state).text = LanguageUtils.statusChinese(it)
                }
            }
            onTouchCallback { // Touch
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
                    Log.e("Nuix", "Touch: ${it.data}")
                    when (it.data) {
                        RingTouchEvent.HOLD -> {
                        }
                        RingTouchEvent.TAP -> {
                        }
                        else -> {}
                    }
                }
            }
            onPlaneEventCallback {
                runOnUiThread {
                    if (it == TouchState.DOWN) {
                        Log.e("Nuix", "Plane down")
                        mainHandler.postDelayed({
                            isTouchDown = true
                        }, 500)
                    } else {
                        Log.e("Nuix", "Plane up")
                        isTouchDown = false
                    }
                }
            }
            onPlaneMoveCallback {
                runOnUiThread {
                    if (isTouchDown) {
                        modelView?.rotate(it.second, it.first)
                    }
                }
            }
        }
        ringManager.connect()
    }
}