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
import io.ktor.utils.io.core.readUTF8Line
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

class Commander(overlayView: OverlayView) {
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
                    val message = packet.packet.readUTF8Line()

                    // unwrap the packet, look up in notion
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

    fun disconnect() {

    }

    fun newEyeData(eyeData: EyeData) {
    }
}