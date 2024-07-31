package com.hcifuture.producer.sensor.data

import java.nio.ByteBuffer

data class RingV1TouchData(
    val data: Int,
    val timestamp: Long,
): BytesData {
    override fun toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(4 + 8)
        byteBuffer.putInt(data)
        byteBuffer.putLong(timestamp)
        return byteBuffer.array()
    }
}