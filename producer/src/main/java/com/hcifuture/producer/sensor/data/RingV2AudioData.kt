package com.hcifuture.producer.sensor.data

import java.nio.ByteBuffer

data class RingV2AudioData (
    val length: Int,
    val sequenceId: Int,
    val data: List<Byte>,
): BytesData {
    override fun toBytes(): ByteArray {
        val byteBuffer = ByteBuffer.allocate(data.size * 4 + 8)
        for (value in data) {
            byteBuffer.put(value)
        }
        return byteBuffer.array()
    }
}
