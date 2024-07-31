package com.hcifuture.producer.sensor.external.ringV1

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.hcifuture.producer.recorder.Collector
import com.hcifuture.producer.recorder.collectors.BytesDataCollector
import com.hcifuture.producer.sensor.NuixSensor
import com.hcifuture.producer.sensor.NuixSensorSpec
import com.hcifuture.producer.sensor.NuixSensorState
import com.hcifuture.producer.sensor.data.RingV1ImuData
import com.hcifuture.producer.sensor.data.RingV1TouchData

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattService
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.nio.ByteBuffer
import java.nio.ByteOrder

@SuppressLint("MissingPermission")
class RingV1(
    val context: Context,
    private val deviceName: String,
    private val address: String,
) : NuixSensor() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var buffer = ByteArray(0)
    private lateinit var sppReadCharacteristic: ClientBleGattCharacteristic
    private lateinit var sppWriteCharacteristic: ClientBleGattCharacteristic
    private lateinit var notifyReadCharacteristic: ClientBleGattCharacteristic
    private lateinit var notifyWriteCharacteristic: ClientBleGattCharacteristic
    private lateinit var connection: ClientBleGatt
    private val _imuFlow = MutableSharedFlow<RingV1ImuData>()
    private val _touchFlow = MutableSharedFlow<RingV1TouchData>()
    override val name: String = "RING[${deviceName}|${address}]"
    override val flows = mapOf(
        RingV1Spec.imuFlowName(this) to _imuFlow.asSharedFlow(),
        RingV1Spec.touchFlowName(this) to _touchFlow.asSharedFlow(),
        NuixSensorSpec.lifecycleFlowName(this) to lifecycleFlow.asStateFlow(),
    )
    override val defaultCollectors: Map<String, Collector> = mapOf<String, Collector>(
        RingV1Spec.imuFlowName(this) to
                BytesDataCollector(listOf(this), listOf(_imuFlow.asSharedFlow()), "ring[${address}]Imu.bin"),
        RingV1Spec.touchFlowName(this) to
                BytesDataCollector(listOf(this), listOf(_touchFlow.asSharedFlow()), "ring[${address}]Touch.bin"),
    )

    override fun connect() {
        if (!connectable()) return
        status = NuixSensorState.CONNECTING
        scope.launch {
            connection = ClientBleGatt.connect(context, address, scope)
            connection.requestMtu(517)
            if (!connection.isConnected) {
                status = NuixSensorState.DISCONNECTED
                return@launch
            }
            status = NuixSensorState.CONNECTED
            connection.mtu
            Log.e("Test", connection.mtu.value.toString())
            val services = connection.discoverServices()
            lateinit var sppService: ClientBleGattService
            try {
                sppService = services.findService(RingV1Spec.SPP_SERVICE_UUID)!!
            } catch (e: Exception) {
                return@launch
            }
            val notifyService = services.findService(RingV1Spec.NOTIFY_SERVICE_UUID)!!
            sppReadCharacteristic = sppService.findCharacteristic(RingV1Spec.SPP_READ_CHARACTERISTIC_UUID)!!
            sppWriteCharacteristic = sppService.findCharacteristic(RingV1Spec.SPP_WRITE_CHARACTERISTIC_UUID)!!
            notifyReadCharacteristic = notifyService.findCharacteristic(RingV1Spec.NOTIFY_READ_CHARACTERISTIC_UUID)!!
            notifyWriteCharacteristic = notifyService.findCharacteristic(RingV1Spec.NOTIFY_WRITE_CHARACTERISTIC_UUID)!!

            sppReadCharacteristic.getNotifications().onEach {
                extractImu(it).map { data ->
                    /**
                     * TODO: use timestamps from the sensor?
                     */
                    _imuFlow.emit(RingV1ImuData(data.first, data.second))
                }
            }.launchIn(scope)

            notifyReadCharacteristic.getNotifications().onEach {
                handleNotification(it)?.let { data ->
                    _touchFlow.emit(RingV1TouchData(data, System.currentTimeMillis()))
                }
            }.launchIn(scope)

            // Enable touch, TODO: refactor
            notifyReadCharacteristic.write(
                DataByteArray.from(36.toByte(), 2, 224.toByte(), 5, 0),
                writeType = BleWriteType.NO_RESPONSE
            )

            val commandList = arrayOf("ENSPP", "ENFAST", "TPOPS=1,1,1",
                "IMUARG=0,0,0,200", "ENDB6AX")
            for (command in commandList) {
                writeSpp(command)
            }
        }
    }

    override fun disconnect() {
        if (!disconnectable()) return
        connection.disconnect()
        status = NuixSensorState.DISCONNECTED
    }

    private suspend fun writeSpp(data: String) {
        try {
            sppWriteCharacteristic.write(DataByteArray.from(data + "\r\n"))
        }
        catch (_: Exception) {
        }
        delay(100)
    }

    private fun handleNotification(data: DataByteArray): Int? {
        // TODO: check crc
        data.getByte(0).let { type ->
            when (type) {
                0x24.toByte() -> {
                    return data.getByte(4)!!.toInt()
                }
                else -> {}
            }

        }
        return null
    }

    private fun extractImu(data: DataByteArray): List<Pair<List<Float>, Long>> {
        buffer += data.value
        val imuList = mutableListOf<Pair<List<Float>, Long>>()
        for (i in 0 until buffer.size - 1) {
            if (buffer[i] == 0xAA.toByte() && buffer[i + 1] == 0x55.toByte()) {
                buffer = buffer.copyOfRange(i, buffer.size)
                break
            }
        }
        while (buffer.size > 36 && buffer[0] == 0xAA.toByte() && buffer[1] == 0x55.toByte()) {
            // TODO: check crc
            val byteBuffer = ByteBuffer.wrap(buffer, 4, 32).order(ByteOrder.LITTLE_ENDIAN)
            imuList.add(Pair(listOf(
                byteBuffer.getFloat(), byteBuffer.getFloat(),
                byteBuffer.getFloat(), byteBuffer.getFloat(),
                byteBuffer.getFloat(), byteBuffer.getFloat(),
            ), byteBuffer.getLong()))
            buffer = buffer.copyOfRange(36, buffer.size)
        }
        return imuList
    }
}