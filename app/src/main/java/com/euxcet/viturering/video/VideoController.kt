package com.euxcet.viturering.video

interface VideoController {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Long)
    fun setSpeed(speed: Float)
    fun setMute(isMute: Boolean)
    fun setControllerVisibility(isVisible: Boolean)
}