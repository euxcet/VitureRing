package com.euxcet.viturering.video

import android.annotation.SuppressLint
import android.content.Context
import android.media.MediaPlayer
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import com.euxcet.viturering.databinding.LayoutVideoControlBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs

@SuppressLint("ClickableViewAccessibility")
class VideoControlView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): FrameLayout(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val binding: LayoutVideoControlBinding = LayoutVideoControlBinding.inflate(LayoutInflater.from(context), this, true)

    private var videoController: VideoController? = null

    private var touchDownTime: Long = 0
    private var touchDownX: Float = 0f
    private var touchDownY: Float = 0f
    private var lastX: Float = 0f
    private var lastY: Float = 0f
    private var isBeginSeek = false
    private var isPlaying = false
    private var duration: Int = 0
    private var progressJob: Job? = null

    init {
        binding.progressBar.max = 1000
        binding.progressBar.min = 0
        binding.seekProgressBar.max = 1000
        binding.seekProgressBar.min = 0
        binding.mask.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    touchDownTime = System.currentTimeMillis()
                    touchDownX = event.x
                    touchDownY = event.y
                    lastX = event.x
                    lastY = event.y
                    isBeginSeek = false
                }
                MotionEvent.ACTION_MOVE -> {
                    val dx = event.x - lastX
                    if (abs(dx) > 1) {
                        isBeginSeek = true
                        beginSeek()
                    }
                    if (isBeginSeek) {
                        val maskWith = binding.mask.width
                        val curPlayTime = videoController?.getCurrentPosition() ?: 0
                        setCurrentTime(curPlayTime + (dx / maskWith / 3f * duration).toInt())
                    }
                    lastX = event.x
                    lastY = event.y
                }
                MotionEvent.ACTION_UP -> {
                    if (isBeginSeek) {
                        isBeginSeek = false
                        endSeek()
                    } else {
                        switchPlay()
                    }
                }
            }
            true
        }
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                progressJob = CoroutineScope(Dispatchers.Main).launch {
                    while (true) {
                        if (isPlaying && !isBeginSeek) {
                            val currentPosition = videoController?.getCurrentPosition() ?: 0
                            updateProgressView(currentPosition / duration.toFloat())
                        }
                        delay(500)
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                progressJob?.cancel()
            }
        })
    }

    private var isPlayingWhenBeginSeek = false
    private var seekMode = MediaPlayer.SEEK_CLOSEST_SYNC
    fun beginSeek() {
        if (isPlaying) {
            isPlayingWhenBeginSeek = true
        } else {
            binding.pausedToast.visibility = View.GONE
        }
        videoController?.pause()
        binding.seekToast.visibility = View.VISIBLE
        seekMode = MediaPlayer.SEEK_PREVIOUS_SYNC
    }

    fun endSeek() {
        if (isPlayingWhenBeginSeek) {
            isPlayingWhenBeginSeek = false
            videoController?.play()
        } else {
            binding.pausedToast.visibility = View.VISIBLE
        }
        binding.seekToast.visibility = View.GONE
        seekMode = MediaPlayer.SEEK_CLOSEST_SYNC
        videoController?.seekTo((binding.seekProgressBar.progress / 1000f * duration).toLong(), seekMode)
    }

    fun seek(delta: Int) {
        val curPlayTime = videoController?.getCurrentPosition() ?: 0
        setCurrentTime(curPlayTime + delta)
    }

    fun setCurrentTime(currentTime: Int) {
        setProgress(currentTime / duration.toFloat())
    }

    private var lastSeekPosition = 0L
    private var lastSeekMode = MediaPlayer.SEEK_CLOSEST_SYNC
    fun setProgress(progress: Float) {
        var fixedProgress = progress
        if (progress < 0) {
            fixedProgress = 0f
        } else if (progress > 1) {
            fixedProgress = 1f
        }
        val position = (fixedProgress * duration).toLong()
        if (lastSeekPosition == position && lastSeekMode == seekMode) {
            return
        }
        videoController?.seekTo(position, seekMode)
        updateProgressView(fixedProgress)
    }

    private fun updateProgressView(progress: Float) {
        binding.progressBar.progress = (binding.progressBar.max * progress).toInt()
        binding.seekProgressBar.progress = (binding.seekProgressBar.max * progress).toInt()
        val curTimeDisplay = getDisplayTime(videoController?.getCurrentPosition() ?: 0)
        binding.seekTime.text = curTimeDisplay
        binding.playTime.text = curTimeDisplay
    }

    fun switchPlay() {
        if (isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun setVideoController(videoController: VideoController) {
        this.videoController = videoController
    }

    fun pause() {
        this.videoController?.pause()
        isPlaying = false
        binding.play.setImageResource(android.R.drawable.ic_media_play)
        binding.pausedToast.visibility = View.VISIBLE
    }

    fun play() {
        this.videoController?.play()
        isPlaying = true
        binding.play.setImageResource(android.R.drawable.ic_media_pause)
        binding.pausedToast.visibility = View.GONE
    }

    fun setDuration(duration: Int) {
        this.duration = duration
        binding.totalTime.text = getDisplayTime(duration)
        binding.seekDuration.text = getDisplayTime(duration)
    }

    private fun getDisplayTime(time: Int): String {
        val second = time / 1000
        val minute = second / 60
        val hour = minute / 60
        if (hour > 0) {
            return String.format("%02d:%02d:%02d", hour, minute % 60, second % 60)
        } else {
            return String.format("%02d:%02d", minute % 60, second % 60)
        }
    }
}