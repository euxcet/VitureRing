package com.euxcet.commander

import android.media.MediaCodec
import android.media.MediaFormat
import android.util.Log
import android.view.Surface
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.openReadChannel
import io.ktor.network.sockets.openWriteChannel
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.readUTF8Line
import io.ktor.utils.io.writeStringUtf8
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class Tello {
    companion object {
        const val LOCAL_ADDRESS = "0.0.0.0"
        const val LOCAL_COMMAND_PORT = 9000
        const val LOCAL_VIDEO_PORT = 11111
        const val DEVICE_ADDRESS = "192.168.3.15"
        const val DEVICE_PORT = 8889
        const val NO_RESPONSE = "NO_RESPONSE"
        const val FRAME_WIDTH = 960
        const val FRAME_HEIGHT = 720
        const val FRAME_RATE = 24
    }
    private lateinit var telloSocket: BoundDatagramSocket
    private lateinit var videoSocket: BoundDatagramSocket
    private var connected: Boolean = false

    fun connect(surface: Surface) {
        CoroutineScope(Dispatchers.Default).launch {
            Log.e("Test", "start")
            val selectorManager = SelectorManager(Dispatchers.Default)
            telloSocket = aSocket(selectorManager).udp().bind(InetSocketAddress(LOCAL_ADDRESS, LOCAL_COMMAND_PORT))
            videoSocket = aSocket(selectorManager).udp().bind(InetSocketAddress(LOCAL_ADDRESS, LOCAL_VIDEO_PORT))
            sendAction("command")
            sendAction("battery?")
            sendAction("streamon")
            connected = true
            val mediaCodec = MediaCodec.createDecoderByType("video/avc")
            val mediaFormat = MediaFormat.createVideoFormat("video/avc", FRAME_WIDTH, FRAME_HEIGHT)
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, FRAME_RATE)
            mediaCodec.configure(mediaFormat, surface, null, 0)
            mediaCodec.start()
            videoSocket.use {
                var bytes = byteArrayOf()
                while (true) {
                    val packet = videoSocket.receive()
                    val newBytes = packet.packet.readBytes()
                    bytes += newBytes
                    val totalSize = bytes.size
                    Log.e("Test", "${bytes.size} ${newBytes.size}")
                    while (true) {
                        if (totalSize == 0) {
                            break
                        }
                        val nextFrameStart = findByFrame(bytes, 1, bytes.size)
                        if (nextFrameStart == -1) {
                            break
                        }
                        val info = MediaCodec.BufferInfo()
                        val inIndex = mediaCodec.dequeueInputBuffer(1000)
                        if (inIndex >= 0) {
                            val byteBuffer = mediaCodec.getInputBuffer(inIndex)!!
                            byteBuffer.clear()
                            byteBuffer.put(bytes, 0, nextFrameStart)
                            mediaCodec.queueInputBuffer(inIndex, 0, nextFrameStart, 0, 0)
                        } else {
                            continue
                        }
                        bytes = bytes.drop(nextFrameStart).toByteArray()
                        val outIndex = mediaCodec.dequeueOutputBuffer(info, 1000)
                        if (outIndex >= 0) {
                            mediaCodec.releaseOutputBuffer(outIndex, true)
                        }
                    }
                }
            }
        }
    }

    private fun findByFrame(bytes: ByteArray, start: Int, end: Int): Int {
        for (i in start until end - 4) {
            if (bytes[i] == (0x00).toByte() && bytes[i + 1] == (0x00).toByte() &&
                bytes[i + 2] == (0x00).toByte() && bytes[i + 3] == (0x01).toByte()) {
                return i
            }
        }
        return -1
    }

    private suspend fun sendAction(action: String, waitResponse: Boolean = true): String {
        telloSocket.send(Datagram(
            ByteReadPacket(action.encodeToByteArray()),
            InetSocketAddress(DEVICE_ADDRESS, DEVICE_PORT)
        ))
        val response = if (waitResponse) {
            telloSocket.receive().packet.readUTF8Line() ?: NO_RESPONSE
        } else {
            NO_RESPONSE
        }
        Log.e("Test", "Tello response: $response")
        return response
    }

    fun performAction(action: String) {
        if (!connected) {
            return
        }
        CoroutineScope(Dispatchers.Default).launch {
            sendAction(action)
        }
    }
}