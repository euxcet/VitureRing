package com.hcifuture.producer.sensor.external.ringV2

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import com.hcifuture.producer.recorder.Collector
import com.hcifuture.producer.recorder.collectors.BytesDataCollector
import com.hcifuture.producer.sensor.NuixSensor
import com.hcifuture.producer.sensor.NuixSensorSpec
import com.hcifuture.producer.sensor.NuixSensorState
import com.hcifuture.producer.sensor.data.RingV2AudioData
import com.hcifuture.producer.sensor.data.RingV2ImuData
import com.hcifuture.producer.sensor.data.RingV2StatusData
import com.hcifuture.producer.sensor.data.RingV2StatusType
import com.hcifuture.producer.sensor.data.RingV2TouchEvent
import com.hcifuture.producer.sensor.data.RingV2TouchEventData
import com.hcifuture.producer.sensor.data.RingV2TouchRawData
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
import no.nordicsemi.android.common.core.toDisplayString
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattService
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import no.nordicsemi.android.kotlin.ble.core.data.BleWriteType
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Arrays
import kotlin.experimental.and
import kotlin.math.PI

@SuppressLint("MissingPermission")
class RingV2(
    val context: Context,
    private val deviceName: String,
    private val address: String,
) : NuixSensor() {
    private val scope = CoroutineScope(Dispatchers.IO)
    private var buffer = ByteArray(0)
    private lateinit var readCharacteristic: ClientBleGattCharacteristic
    private lateinit var writeCharacteristic: ClientBleGattCharacteristic
    private lateinit var connection: ClientBleGatt
    private val _imuFlow = MutableSharedFlow<RingV2ImuData>()
    private val _touchEventFlow = MutableSharedFlow<RingV2TouchEventData>()
    private val _touchRawFlow = MutableSharedFlow<RingV2TouchRawData>()
    private val _statusFlow = MutableSharedFlow<RingV2StatusData>()
    private val _audioFlow = MutableSharedFlow<RingV2AudioData>()
    override val name: String = "RING[${deviceName}|${address}]"
    override val flows = mapOf(
        RingV2Spec.imuFlowName(this) to _imuFlow.asSharedFlow(),
        RingV2Spec.touchEventFlowName(this) to _touchEventFlow.asSharedFlow(),
        RingV2Spec.touchRawFlowName(this) to _touchRawFlow.asSharedFlow(),
        RingV2Spec.statusFlowName(this) to _statusFlow.asSharedFlow(),
        RingV2Spec.audioFlowName(this) to _audioFlow.asSharedFlow(),
        NuixSensorSpec.lifecycleFlowName(this) to lifecycleFlow.asStateFlow(),
    )
    override val defaultCollectors: Map<String, Collector> = mapOf<String, Collector>(
        RingV2Spec.imuFlowName(this) to
                BytesDataCollector(listOf(this), listOf(_imuFlow.asSharedFlow()), "ringV2[${address}]Imu.bin"),
        RingV2Spec.touchEventFlowName(this) to
                BytesDataCollector(listOf(this), listOf(_touchEventFlow.asSharedFlow()), "ringV2[${address}]TouchEvent.bin"),
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
            val service = connection.discoverServices().findService(RingV2Spec.SERVICE_UUID)!!
            readCharacteristic = service.findCharacteristic(RingV2Spec.READ_CHARACTERISTIC_UUID)!!
            writeCharacteristic = service.findCharacteristic(RingV2Spec.WRITE_CHARACTERISTIC_UUID)!!
            readCharacteristic.getNotifications().onEach {
                val cmd = it.value[2]
                val subCmd = it.value[3]
                when {
                    cmd == 0x11.toByte() && subCmd == 0x0.toByte() -> {
                        // software version
                        _statusFlow.emit(
                            RingV2StatusData(
                            type = RingV2StatusType.SOFTWARE_VERSION,
                            softwareVersion = it.value.slice(4 until it.value.size).toString(),
                        ))
                    }
                    cmd == 0x11.toByte() && subCmd == 0x1.toByte() -> {
                        // hardware version
                        _statusFlow.emit(
                            RingV2StatusData(
                                type = RingV2StatusType.HARDWARE_VERSION,
                                softwareVersion = it.value.slice(4 until it.value.size).toString(),
                            ))
                    }
                    cmd == 0x12.toByte() && subCmd == 0x0.toByte() -> {
                        // battery level
                        _statusFlow.emit(
                            RingV2StatusData(
                                type = RingV2StatusType.BATTERY_LEVEL,
                                batteryLevel = it.value[4].toInt(),
                            ))
                    }
                    cmd == 0x12.toByte() && subCmd == 0x1.toByte() -> {
                        // battery status
                        _statusFlow.emit(
                            RingV2StatusData(
                                type = RingV2StatusType.BATTERY_STATUS,
                                batteryStatus = it.value[4].toInt(),
                            ))
                    }
                    cmd == 0x40.toByte() && subCmd == 0x06.toByte() -> {
                        // imu
                        val data = it.value.slice(5 until it.value.size)
                            .chunked(2)
                            .map { (l, h) -> (l.toInt().and(0xFF) or h.toInt().shl(8)).toFloat() }
                        for (i in data.indices step 6) {
                            val imu = data.slice(i until i + 6).toMutableList()
                            imu[0] *= 9.8f / 1000.0f
                            imu[1] *= 9.8f / 1000.0f
                            imu[2] *= 9.8f / 1000.0f
                            imu[3] *= 3.14f / 180.0f
                            imu[4] *= 3.14f / 180.0f
                            imu[5] *= 3.14f / 180.0f
                            imu[0] = imu[1].also { imu[1] = imu[0] }
                            imu[1] = imu[2].also { imu[2] = imu[1] }
                            imu[3] = imu[4].also { imu[4] = imu[3] }
                            imu[4] = imu[5].also { imu[5] = imu[4] }
                            _imuFlow.emit(
                                RingV2ImuData(
                                    data = imu,
                                    timestamp = 0,
                                )
                            )
                        }
                    }
                    cmd == 0x61.toByte() && subCmd == 0x00.toByte() -> {
                        // touch events, disabled
                    }
                    cmd == 0x61.toByte() && subCmd == 0x01.toByte() -> {
                        var event: RingV2TouchEvent? = null
                        if (it.value[7].and(0x01) > 0) {
                            event = RingV2TouchEvent.TAP
                        } else if (it.value[7].and(0x02) > 0) {
                            event = RingV2TouchEvent.SWIPE_POSITIVE
                        } else if (it.value[7].and(0x04) > 0) {
                            event = RingV2TouchEvent.SWIPE_NEGATIVE
                        } else if (it.value[7].and(0x08) > 0) {
                            event = RingV2TouchEvent.FLICK_POSITIVE
                        } else if (it.value[7].and(0x10) > 0) {
                            event = RingV2TouchEvent.FLICK_NEGATIVE
                        } else if (it.value[7].and(0x20) > 0) {
                            event = RingV2TouchEvent.HOLD
                        }
                        if (event != null) {
                            Log.e("RingV2", event.name)
                            _touchEventFlow.emit(
                                RingV2TouchEventData(
                                    data = event,
                                    timestamp = System.currentTimeMillis(),
                                )
                            )
                        }
                        // touch raw data
                        _touchRawFlow.emit(
                            RingV2TouchRawData(
                                data = it.value.slice(5 until it.value.size),
                                timestamp = System.currentTimeMillis(),
                            )
                        )
                    }
                    cmd == 0x71.toByte() && subCmd == 0x00.toByte() -> {
                        // microphone
                        val length = it.value[4].toInt().and(0xFF) or it.value[5].toInt().shl(8)
                        val sequenceId = it.value[6].toInt().and(0xFF) or it.value[7].toInt().and(0xFF).shl(8) or
                                         it.value[8].toInt().and(0xFF).shl(16) or it.value[9].toInt().shl(24)
                        Log.e("RingV2", "length: ${length}, sequenceId: ${sequenceId}")
                        _audioFlow.emit(
                            RingV2AudioData(
                                length = length,
                                sequenceId = sequenceId,
                                data = it.value.slice(10 until it.value.size),
                            )
                        )
                    }
                }
            }.launchIn(scope)
            write(RingV2Spec.GET_BATTERY_LEVEL)
            write(RingV2Spec.GET_HARDWARE_VERSION)
            write(RingV2Spec.GET_SOFTWARE_VERSION)
            status = NuixSensorState.CONNECTED
        }
    }

    override fun disconnect() {
        if (!disconnectable()) return
        connection.disconnect()
        status = NuixSensorState.DISCONNECTED
    }

    suspend fun write(data: ByteArray) {
        try {
            writeCharacteristic.write(DataByteArray(data), writeType = BleWriteType.NO_RESPONSE)
        }
        catch (e: Exception) {
            Log.e("Test", e.toString())
        }
        delay(50)
    }

    suspend  fun openMic() {
        Log.e("RingV2", "Open mic")
        write(RingV2Spec.OPEN_MIC)
    }

    suspend fun closeMic() {
        write(RingV2Spec.CLOSE_MIC)
    }
}
