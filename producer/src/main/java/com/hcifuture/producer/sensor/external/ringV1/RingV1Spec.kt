package com.hcifuture.producer.sensor.external.ringV1

import com.hcifuture.producer.common.utils.FunctionUtils.reifiedValue
import com.hcifuture.producer.sensor.NuixSensor
import com.hcifuture.producer.sensor.NuixSensorSpec
import com.hcifuture.producer.sensor.data.RingV1ImuData
import com.hcifuture.producer.sensor.data.RingV1TouchData
import java.util.UUID

class RingV1Spec {
    companion object {
        val SPP_SERVICE_UUID: UUID = UUID.fromString("a6ed0201-d344-460a-8075-b9e8ec90d71b")
        val SPP_READ_CHARACTERISTIC_UUID: UUID = UUID.fromString("a6ed0202-d344-460a-8075-b9e8ec90d71b")
        val SPP_WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("a6ed0203-d344-460a-8075-b9e8ec90d71b")
        val NOTIFY_SERVICE_UUID: UUID = UUID.fromString("0000FF10-0000-1000-8000-00805F9B34FB")
        val NOTIFY_READ_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FF11-0000-1000-8000-00805F9B34FB")
        val NOTIFY_WRITE_CHARACTERISTIC_UUID: UUID = UUID.fromString("0000FF11-0000-1000-8000-00805F9B34FB")

        fun touchEventName(action: Int): String {
            return when (action) {
                4 -> RingV1Event.EVENT_BOTH_BUTTON_PRESS
                5 -> RingV1Event.EVENT_BOTH_BUTTON_RELEASE
                6 -> RingV1Event.EVENT_BOTTOM_BUTTON_CLICK
                7 -> RingV1Event.EVENT_BOTTOM_BUTTON_DOUBLE_CLICK
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
            return NuixSensorSpec.flowName(sensor, "imu_shared")
        }

        fun touchFlowName(sensor: NuixSensor): String {
            return NuixSensorSpec.flowName(sensor, "touch_shared")
        }

        fun refriedImuFlow(value: Any): RingV1ImuData =
            reifiedValue<RingV1ImuData>(value)

        fun refriedTouchFlow(value: Any): RingV1TouchData =
            reifiedValue<RingV1TouchData>(value)
    }
}