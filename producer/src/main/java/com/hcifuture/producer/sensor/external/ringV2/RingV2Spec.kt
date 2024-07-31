package com.hcifuture.producer.sensor.external.ringV2

import com.hcifuture.producer.common.utils.FunctionUtils
import com.hcifuture.producer.sensor.NuixSensor
import com.hcifuture.producer.sensor.NuixSensorSpec
import com.hcifuture.producer.sensor.data.RingV2AudioData
import com.hcifuture.producer.sensor.data.RingV2ImuData
import com.hcifuture.producer.sensor.data.RingV2StatusData
import com.hcifuture.producer.sensor.data.RingV2TouchEventData
import com.hcifuture.producer.sensor.data.RingV2TouchRawData
import com.hcifuture.producer.sensor.external.ringV1.RingV1Event
import no.nordicsemi.android.common.core.DataByteArray
import java.util.UUID

class RingV2Spec {
    companion object {
        val SERVICE_UUID: UUID = UUID.fromString("bae80001-4f05-4503-8e65-3af1f7329d1f")
        val READ_CHARACTERISTIC_UUID: UUID = UUID.fromString("BAE80011-4F05-4503-8E65-3AF1F7329D1F")
        val WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("BAE80010-4F05-4503-8E65-3AF1F7329D1F")

        val GET_TIME             = byteArrayOf(0x00, 0x00, 0x10, 0x01)
        val GET_SOFTWARE_VERSION = byteArrayOf(0x00, 0x00, 0x11, 0x00)
        val GET_HARDWARE_VERSION = byteArrayOf(0x00, 0x00, 0x11, 0x01)
        val GET_BATTERY_LEVEL    = byteArrayOf(0x00, 0x00, 0x12, 0x00)
        val GET_BATTERY_STATUS   = byteArrayOf(0x00, 0x00, 0x12, 0x01)
        val OPEN_6AXIS_IMU       = byteArrayOf(0x00, 0x00, 0x40, 0x06)
        val CLOSE_6AXIS_IMU      = byteArrayOf(0x00, 0x00, 0x40, 0x00)
        val GET_TOUCH            = byteArrayOf(0x00, 0x00, 0x61, 0x00)
        val OPEN_MIC             = byteArrayOf(0x00, 0x00, 0x71, 0x00, 0x01)
        val CLOSE_MIC            = byteArrayOf(0x00, 0x00, 0x71, 0x00, 0x00)
        val GET_NFC              = byteArrayOf(0x00, 0x00, 0x82.toByte(), 0x00)

        fun touchEventName(action: Int): String {
            return when (action) {
                4 -> RingV1Event.EVENT_BOTH_BUTTON_PRESS
                5 -> RingV1Event.EVENT_BOTH_BUTTON_RELEASE
                6 ->  RingV1Event.EVENT_BOTTOM_BUTTON_CLICK
                7 ->  RingV1Event.EVENT_BOTTOM_BUTTON_DOUBLE_CLICK
                9 -> RingV1Event.EVENT_BOTTOM_BUTTON_LONG_PRESS
                10 -> RingV1Event.EVENT_BOTTOM_BUTTON_RELEASE
                11 -> RingV1Event.EVENT_TOP_BUTTON_CLICK
                12 -> RingV1Event.EVENT_TOP_BUTTON_DOUBLE_CLICK
                14 -> RingV1Event.EVENT_TOP_BUTTON_LONG_PRESS
                15 -> RingV1Event.EVENT_TOP_BUTTON_RELEASE
                else -> "Unknown"
            }
        }

        fun imuFlowName(sensor: NuixSensor): String {
            return NuixSensorSpec.flowName(sensor, "imu")
        }

        fun touchEventFlowName(sensor: NuixSensor): String {
            return NuixSensorSpec.flowName(sensor, "touch_event")
        }

        fun touchRawFlowName(sensor: NuixSensor): String {
            return NuixSensorSpec.flowName(sensor, "touch_raw")
        }

        fun audioFlowName(sensor: NuixSensor): String {
            return NuixSensorSpec.flowName(sensor, "audio")
        }

        fun statusFlowName(sensor: NuixSensor): String {
            return NuixSensorSpec.flowName(sensor, "status")
        }

        fun refriedImuFlow(value: Any): RingV2ImuData =
            FunctionUtils.reifiedValue<RingV2ImuData>(value)

        fun refriedTouchEventFlow(value: Any): RingV2TouchEventData =
            FunctionUtils.reifiedValue<RingV2TouchEventData>(value)

        fun refriedTouchRawFlow(value: Any): RingV2TouchRawData =
            FunctionUtils.reifiedValue<RingV2TouchRawData>(value)

        fun refriedAudioFlow(value: Any): RingV2AudioData =
            FunctionUtils.reifiedValue<RingV2AudioData>(value)

        fun refriedStatusFlow(value: Any): RingV2StatusData =
            FunctionUtils.reifiedValue<RingV2StatusData>(value)
    }
}
