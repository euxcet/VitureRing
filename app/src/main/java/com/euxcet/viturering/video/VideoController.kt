package com.euxcet.viturering.video

interface VideoController {
    fun play()
    fun pause()
    fun stop()
    fun seekTo(position: Long, mode: Int)
    fun setMute(isMute: Boolean)
    fun isPlaying(): Boolean
    fun getCurrentPosition(): Int
}