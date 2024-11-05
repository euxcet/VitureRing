package com.euxcet.viturering.pages.game

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.euxcet.viturering.R
import com.euxcet.viturering.RingManager
import com.euxcet.viturering.databinding.ActivityGameBinding
import com.euxcet.viturering.utils.LanguageUtils
import com.ss.ugc.android.alpha_player.IMonitor
import com.ss.ugc.android.alpha_player.IPlayerAction
import com.ss.ugc.android.alpha_player.model.ScaleType
import dagger.hilt.android.AndroidEntryPoint
import java.io.File
import javax.inject.Inject

@AndroidEntryPoint
class GameActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "GameActivity"
    }

    @Inject
    lateinit var ringManager: RingManager
    private var binding: ActivityGameBinding? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initVideoGiftView()
    }

    override fun onStart() {
        super.onStart()
        connectRing()
        attachView()
    }

    override fun onStop() {
        super.onStop()
        detachView()
    }

    override fun onDestroy() {
        super.onDestroy()
        binding?.gifView?.releasePlayerController()
    }

    private fun initVideoGiftView() {
        binding?.gifView?.initPlayerController(this, this, playerAction, monitor)
    }

    private val playerAction = object : IPlayerAction {
        override fun onVideoSizeChanged(videoWidth: Int, videoHeight: Int, scaleType: ScaleType) {
            Log.i(
                TAG,
                "call onVideoSizeChanged(), videoWidth = $videoWidth, videoHeight = $videoHeight, scaleType = $scaleType"
            )
        }

        override fun startAction() {
            Log.i(TAG, "call startAction()")
        }

        override fun endAction() {
            Log.i(TAG, "call endAction")
        }
    }

    private val monitor = object : IMonitor {
        override fun monitor(state: Boolean, playType: String, what: Int, extra: Int, errorInfo: String) {
            Log.i(
                TAG,
                "call monitor(), state: $state, playType = $playType, what = $what, extra = $extra, errorInfo = $errorInfo"
            )
        }
    }

    fun attachView() {
        binding?.gifView?.attachView()
    }

    fun detachView() {
        binding?.gifView?.detachView()
    }

    fun playGift() {
        val testPath = getResourcePath()
        binding?.gifView?.startVideoGift(testPath)
    }

    private fun getResourcePath(): String {
        val basePath = Environment.getExternalStorageDirectory().absolutePath
        val dirPath = basePath + File.separator + "alphaVideoGift" + File.separator
        val dirFile = File(dirPath)
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        if (dirFile.exists() && dirFile.listFiles().isNullOrEmpty()) {
            // copy raw resource to dirPath
            val rawResource = resources.openRawResource(R.raw.game_wave_video)
            val targetFile = File(dirFile, "game_wave_video.mp4")
            targetFile.outputStream().use { rawResource.copyTo(it) }
        }
        if (dirFile.exists() && dirFile.listFiles() != null && dirFile.listFiles().isNotEmpty()) {
            return dirFile.listFiles()[0].absolutePath
        }
        return ""
    }

    private fun connectRing() {
        ringManager.registerListener {
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    when (it) {
                        "pinch" -> {
                            playGift()
                        }
                        "snap" -> {
                            finish()
                        }
                    }
                }
            }
            ringManager.connect()
        }
    }
}