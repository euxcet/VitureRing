package com.euxcet.viturering

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.euxcet.viturering.databinding.ActivityHealthBinding
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class HealthActivity : AppCompatActivity() {

    companion object {
        const val MODE_HEART_RATE = 1
        const val MODE_BLOOD_OXYGEN = 2
    }

    @Inject
    lateinit var ringManager: RingManager
    lateinit var binding: ActivityHealthBinding

    private var mode: Int = 0 // 1: heart rate, 2: blood oxygen
    private var focusedPosition = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityHealthBinding.inflate(layoutInflater)
        setContentView(binding.root)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        binding.heartRate.setOnClickListener {
            switchMode(MODE_HEART_RATE)
        }
        binding.bloodOxygen.setOnClickListener {
            switchMode(MODE_BLOOD_OXYGEN)
        }
        binding.cancel.setOnClickListener {
            switchMode(0)
        }
        focusNext()
    }

    private fun switchMode(value: Int) {
        if (mode == value) {
            return
        }
        mode = value
        if (mode == MODE_HEART_RATE) {
            binding.heartRate.visibility = View.GONE
            binding.bloodOxygen.visibility = View.GONE
            binding.cancel.visibility = View.VISIBLE
            beginHeartRateDetect()
        } else if (mode == MODE_BLOOD_OXYGEN) {
            binding.heartRate.visibility = View.GONE
            binding.bloodOxygen.visibility = View.GONE
            binding.cancel.visibility = View.VISIBLE
            beginBloodOxygenDetect()
        } else {
            binding.heartRate.visibility = View.VISIBLE
            binding.bloodOxygen.visibility = View.VISIBLE
            binding.cancel.visibility = View.GONE
            cancelDetect()
        }
    }

    private fun focusNext() {
        focusedPosition = (focusedPosition + 1) % 2
        if (focusedPosition == 0) {
            binding.heartRate.alpha = 1f
            binding.bloodOxygen.alpha = 0.5f
        } else {
            binding.heartRate.alpha = 0.5f
            binding.bloodOxygen.alpha = 1f
        }
    }

    var mockDataJob: Job? = null
    private fun startMockJob() {
        mockDataJob?.cancel()
        mockDataJob = CoroutineScope(Dispatchers.IO).launch {
            withContext(Dispatchers.Main) {
                binding.waveView.setWaveLineWidth(30)
                binding.waveView.showLine(0f)
            }
            repeat(100) {
                launch(Dispatchers.Main) {
                    onReceiveData((Math.random() * 100).toInt())
                }
                delay(1000)
            }
        }
    }

    private fun beginHeartRateDetect() {
        // TODO
        startMockJob()
    }

    private fun beginBloodOxygenDetect() {
        // TODO
        startMockJob()
    }

    private fun cancelDetect() {
        mockDataJob?.cancel()
    }

    override fun onStart() {
        super.onStart()
        connectRing()
    }

    override fun onDestroy() {
        super.onDestroy()
        mockDataJob?.cancel()
    }

    private fun onReceiveData(value: Int) {
        binding.waveView.showLine(value.toFloat())
        binding.waveValue.text = value.toString()
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

                        }

                        "circle_clockwise" -> {
                            focusNext()
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
                    Log.e("Nuix", "Touch: $touchText")
//                    touchView.text = touchText
                    when (it.data) {
                        RingTouchEvent.BOTTOM_BUTTON_CLICK -> {

                        }

                        RingTouchEvent.TAP -> {
                            if (mode > 0) {
                                switchMode(0)
                            } else {
                                when (focusedPosition) {
                                    0 -> switchMode(MODE_HEART_RATE)
                                    1 -> switchMode(MODE_BLOOD_OXYGEN)
                                }
                            }
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