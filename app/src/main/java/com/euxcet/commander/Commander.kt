package com.euxcet.commander

import android.util.Log
import com.seveninvensun.sdk.EyeData
import io.ktor.network.selector.SelectorManager
import io.ktor.network.sockets.BoundDatagramSocket
import io.ktor.network.sockets.Datagram
import io.ktor.network.sockets.InetSocketAddress
import io.ktor.network.sockets.SocketAddress
import io.ktor.network.sockets.aSocket
import io.ktor.network.sockets.toJavaAddress
import io.ktor.utils.io.core.ByteReadPacket
import io.ktor.utils.io.core.readBytes
import io.ktor.utils.io.core.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import java.nio.ByteBuffer
import java.nio.ByteOrder

data class ObjectBox(
    val id: Int,
    val category: Int,
    val confidence: Float,
    val x0: Float,
    val y0: Float,
    val x1: Float,
    val y1: Float,
)

class Commander(val overlayView: OverlayView) {
    private lateinit var commandSocket: BoundDatagramSocket
    private var commandAddress: SocketAddress? = null
    val commanderAddressFlow = MutableSharedFlow<SocketAddress>()

    fun connect() {
        CoroutineScope(Dispatchers.Default).launch {
            Log.e("Test", "Command start")
            val selectorManager = SelectorManager(Dispatchers.Default)
            commandSocket = aSocket(selectorManager).udp().bind(InetSocketAddress("0.0.0.0", 9001))

            CoroutineScope(Dispatchers.Default).launch {
                while (true) {
                    val packet = commandSocket.receive()
                    val address = packet.address
                    val message = packet.packet.readBytes()
                    commandAddress = address
                    unwrap(message)
                }
            }

            while (true) {
                commandAddress?.let {
                    commandSocket.send(
                        Datagram(ByteReadPacket("alive".encodeToByteArray()), it)
                    )
                }
                delay(1000)
            }

        }
    }

    private fun getInt(data: ByteArray, offset: Int): Int {
        val buffer = ByteBuffer.wrap(data, offset, 4)
        return buffer.getInt()
    }

    private fun getFloat(data: ByteArray, offset: Int): Float {
        val buffer = ByteBuffer.wrap(data, offset, 4)
        return buffer.getFloat()
    }

    private fun unwrap(message: ByteArray) {
        val buffer = ByteBuffer.wrap(message).order(ByteOrder.LITTLE_ENDIAN)
        if (buffer.get().toInt() == 0x10 && buffer.get().toInt() == 0x16) {
            val type = buffer.get().toInt()
            if (type == 2) { // objects
                val number = buffer.getShort()
                val boxes = mutableListOf<ObjectBox>()
                for (i in 0 until number) {
                    boxes.add(ObjectBox(
                        id = buffer.getInt(),
                        category = buffer.getInt(),
                        confidence = buffer.getFloat(),
                        x0 = buffer.getFloat(),
                        y0 = buffer.getFloat(),
                        x1 = buffer.getFloat(),
                        y1 = buffer.getFloat(),
                    ))
                }
                overlayView.resetBoxes(boxes)
            }
        }
    }

    fun disconnect() {

    }

    fun newEyeData(eyeData: EyeData) {
        overlayView.resetEye(eyeData)
    }
}