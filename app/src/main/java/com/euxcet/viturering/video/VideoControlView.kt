package com.euxcet.viturering.video

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.FrameLayout
import com.euxcet.viturering.databinding.LayoutVideoControlBinding

class VideoControlView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): FrameLayout(context, attrs, defStyleAttr) {
    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)

    private val binding: LayoutVideoControlBinding = LayoutVideoControlBinding.inflate(LayoutInflater.from(context), this, true)

    private var videoController: VideoController? = null

    init {

    }

    fun setVideoController(videoController: VideoController) {
        this.videoController = videoController
    }

    fun pause() {

    }

    fun play() {

    }

    fun beginSeek() {

    }

    fun endSeek() {

    }
}