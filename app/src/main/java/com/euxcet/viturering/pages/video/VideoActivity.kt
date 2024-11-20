package com.euxcet.viturering.pages.video

import android.content.pm.ActivityInfo
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.euxcet.viturering.RingManager
import com.euxcet.viturering.databinding.ActivityVideoBinding
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class VideoActivity : AppCompatActivity() {

    @Inject
    lateinit var ringManager: RingManager
    private lateinit var binding: ActivityVideoBinding
    private var mediaPlayer: MediaPlayer? = null
    private val videoController: VideoController by lazy {
        object: VideoController {
            override fun play() {
                binding.videoView.start()
            }

            override fun pause() {
                binding.videoView.pause()
            }

            override fun stop() {
                binding.videoView.stopPlayback()
            }

            override fun seekTo(position: Long, mode: Int) {
                binding.videoView.seekTo(position.toInt())
                mediaPlayer?.seekTo(position, mode)
            }

            override fun setMute(isMute: Boolean) {

            }

            override fun isPlaying(): Boolean {
                return binding.videoView.isPlaying
            }

            override fun getCurrentPosition(): Int {
                return binding.videoView.currentPosition
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityVideoBinding.inflate(layoutInflater)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        setContentView(binding.root)
        val demoDir = getExternalFilesDir("res")
        if (demoDir != null && demoDir.exists()) {
            val file = File(demoDir, "demo.mp4")
//            val path = "android.resource://" + packageName + "/" + R.raw.demo
            binding.videoView.setVideoURI(Uri.fromFile(file))
            binding.videoView.setOnPreparedListener {
                mediaPlayer = it
                binding.videoControl.setVideoController(videoController)
                binding.videoControl.setDuration(it.duration)
                binding.videoControl.play()
            }
        }
    }

    override fun onStart() {
        super.onStart()
        connectRing()
        binding.videoControl.play()
    }

    override fun onStop() {
        super.onStop()
        ringManager.openIMU()
        binding.videoControl.pause()
    }

    private var dismissToastJob: Job? = null
    private fun connectRing() {
        ringManager.closeIMU()
        ringManager.registerListener {
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    when (it) {
                        "pinch" -> {
                        }
                        "snap" -> {
                            finish()
                        }
                    }
                }
            }
            onTouchCallback { // Touch
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
                    Log.e("Nuix", "Touch: $touchText")
//                    touchView.text = touchText
                    when (it.data) {
                        RingTouchEvent.HOLD -> {
                            finish()
                        }
                        RingTouchEvent.TAP -> {
                            binding.videoControl.switchPlay()
                        }
                        RingTouchEvent.SWIPE_POSITIVE,
                        RingTouchEvent.FLICK_POSITIVE,
                        RingTouchEvent.UP -> {
                            binding.videoControl.beginSeek()
                            binding.videoControl.seek(10000)
                            dismissToastJob?.cancel()
                            dismissToastJob = lifecycleScope.launch {
                                delay(1000)
                                binding.videoControl.endSeek()
                            }
                        }
                        RingTouchEvent.SWIPE_NEGATIVE,
                        RingTouchEvent.FLICK_NEGATIVE,
                        RingTouchEvent.DOWN, -> {
                            binding.videoControl.beginSeek()
                            binding.videoControl.seek(-10000)
                            dismissToastJob = lifecycleScope.launch {
                                delay(1000)
                                binding.videoControl.endSeek()
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