package com.euxcet.viturering.pages.game

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.euxcet.viturering.R
import com.euxcet.viturering.RingManager
import com.euxcet.viturering.databinding.ActivityGameBinding
import com.euxcet.viturering.utils.GestureThrottle
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

    private var pageWidth = 0
    private var pageHeight = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        binding = ActivityGameBinding.inflate(layoutInflater)
        setContentView(binding?.root)
        initVideoGiftView()
        binding?.testBtn?.setOnClickListener {
            playGift()
        }
        binding?.root?.let { root ->
            root.viewTreeObserver.addOnGlobalLayoutListener {
                if (pageWidth == root.width && pageHeight == root.height) {
                    return@addOnGlobalLayoutListener
                }
                pageWidth = root.width
                pageHeight = root.height
                binding?.cursor?.let { cursor ->
                    cursor.translationX = (pageWidth - cursor.width) / 2f
                    cursor.translationY = (pageHeight - cursor.height) / 2f
                }
            }
        }
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
            binding?.cursor?.visibility = View.INVISIBLE
            isPlaying = true
        }

        override fun endAction() {
            Log.i(TAG, "call endAction")
            binding?.cursor?.visibility = View.VISIBLE
            isPlaying = false
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

    private var isPlaying = false
    fun playGift() {
        if (isPlaying) {
            return
        }
        binding?.cursor?.let { cursor ->
            val x = cursor.translationX + cursor.width / 2
            val y = cursor.translationY + cursor.height / 2
            binding?.gifView?.let { gifView ->
                gifView.translationX = x - gifView.width / 2
                gifView.translationY = y - gifView.height / 2
            }
        }
        val file = getResourceFile()
        val config = ConfigModel().apply {
            landscapeItem = ConfigModel.Item().apply {
                path = file?.name
                alignMode = ScaleType.ScaleToFill.ordinal
            }
            portraitItem = ConfigModel.Item().apply {
                path = file?.name
                alignMode = ScaleType.ScaleToFill.ordinal
            }
        }
        binding?.gifView?.startVideoGift(config, file?.parent ?: "")

    }

    private fun getResourceFile(): File? {
        val dirFile: File = getExternalFilesDir("alphaVideoGift") ?: return null
        if (!dirFile.exists()) {
            dirFile.mkdirs()
        }
        val targetFile = File(dirFile, "game_wave_video.mp4")
        if (!targetFile.exists()) {
            // copy raw resource to dirPath
            val rawResource = resources.openRawResource(R.raw.game_wave_video)
            targetFile.outputStream().use { rawResource.copyTo(it) }
        }
        return targetFile
    }

    private fun connectRing() {
        ringManager.registerListener {
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    if (GestureThrottle.throttle(it)) {
                        return@runOnUiThread
                    }
                    when (it) {
                        "index_flick" -> {
                            playGift()
                        }
                        "snap" -> {
                            finish()
                        }
                    }
                }
            }
            onMoveCallback {
                runOnUiThread {
                    if (isPlaying) {
                        return@runOnUiThread
                    }
                    Log.e("Nuix", "Move: $it")
                    binding?.cursor?.apply {
                        if (translationX + it.first < 0 || translationX + it.first > pageWidth - width) {
                            return@apply
                        }
                        if (translationY + it.second < 0 || translationY + it.second > pageHeight - height) {
                            return@apply
                        }
                        translationX += it.first
                        translationY += it.second
                    }
                }
            }
            ringManager.connect()
        }
    }
}