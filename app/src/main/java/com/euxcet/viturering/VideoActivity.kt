package com.euxcet.viturering

import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.euxcet.viturering.databinding.ActivityVideoBinding
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    @Inject
    lateinit var ringManager: RingManager
    private lateinit var binding: ActivityVideoBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        val path = "android.resource://" + packageName + "/" + R.raw.demo
        binding.videoView.setVideoURI(Uri.parse(path))
        binding.videoView.setOnPreparedListener {
            binding.videoView.start()
        }
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
                    when (it) {
                        "pinch" -> {
                        }

                        "middle_pinch" -> {
                            //overlayView?.switch()
                        }

                        "snap" -> {
//                            val intent = Intent(Settings.ACTION_SETTINGS)
//                            startActivity(intent)
                            if (binding.videoView.isPlaying) {
                                binding.videoView.pause()
                            } else {
                                binding.videoView.start()
                            }
                        }
                        "tap_air" -> {

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