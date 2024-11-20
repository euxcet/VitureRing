package com.euxcet.viturering.utils

import com.hcifuture.producer.detector.TouchState
import com.hcifuture.producer.sensor.NuixSensorState
import com.hcifuture.producer.sensor.data.RingTouchEvent

class LanguageUtils {
    companion object {
        fun gestureChinese(gesture: String): String {
            return when (gesture) {
                "pinch" -> { "捏合" }
                "middle_pinch" -> { "中指捏合" }
                "clap" -> { "拍手" }
                "snap" -> { "打响指" }
                "tap_plane" -> { "桌面单击" }
                "tap_air" -> { "空中单击" }
                "circle_clockwise" -> { "顺时针转" }
                "circle_counterclockwise" -> { "逆时针转" }
                "touch_ring" -> { "单击戒指" }
                "touch_up" -> { "上滑" }
                "touch_down" -> { "下滑" }
                else -> { gesture }
            }
        }

        fun touchChinese(touch: RingTouchEvent): String {
            return when (touch) {
                RingTouchEvent.UNKNOWN -> "未知"
                RingTouchEvent.BOTH_BUTTON_PRESS -> "双键按压"
                RingTouchEvent.BOTH_BUTTON_RELEASE -> "双键释放"
                RingTouchEvent.BOTTOM_BUTTON_CLICK -> "下键单击"
                RingTouchEvent.BOTTOM_BUTTON_DOUBLE_CLICK -> "下键双击"
                RingTouchEvent.BOTTOM_BUTTON_LONG_PRESS -> "下键长按"
                RingTouchEvent.BOTTOM_BUTTON_RELEASE -> "下键释放"
                RingTouchEvent.TOP_BUTTON_CLICK -> "上键单击"
                RingTouchEvent.TOP_BUTTON_DOUBLE_CLICK -> "上键双击"
                RingTouchEvent.TOP_BUTTON_LONG_PRESS -> "上键长按"
                RingTouchEvent.TOP_BUTTON_RELEASE -> "上键释放"
                RingTouchEvent.TAP -> "单击"
                RingTouchEvent.SWIPE_POSITIVE, RingTouchEvent.FLICK_POSITIVE, RingTouchEvent.UP -> "上滑"
                RingTouchEvent.SWIPE_NEGATIVE, RingTouchEvent.FLICK_NEGATIVE, RingTouchEvent.DOWN -> "下滑"
                RingTouchEvent.HOLD -> "长按"
                RingTouchEvent.DOUBLE_TAP -> "双击"
            }
        }

        fun statusChinese(status: NuixSensorState): String {
            return when(status) {
                NuixSensorState.SCANNING -> "扫描中"
                NuixSensorState.CONNECTING -> "连接中"
                NuixSensorState.CONNECTED -> "已连接"
                NuixSensorState.DISCONNECTED -> "已断连"
            }
        }

        fun planeChinese(plane: TouchState): String {
            return when(plane) {
                TouchState.DOWN -> "下"
                TouchState.UP -> "上"
                else -> "Unknown"
            }
        }
    }
}