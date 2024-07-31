package com.hcifuture.producer.sensor.external.ringV1

import com.hcifuture.producer.common.utils.NuixEvent

class RingV1Event(name: String, data: Map<String, Any?>?) : NuixEvent(name, data, NAMESPACE) {

    companion object {
        const val NAMESPACE = "ring"
        const val EVENT_BOTH_BUTTON_PRESS = "BothButton.Press"
        const val EVENT_BOTH_BUTTON_RELEASE = "BothButton.Release"
        const val EVENT_BOTTOM_BUTTON_CLICK = "BottomButton.Click"
        const val EVENT_BOTTOM_BUTTON_DOUBLE_CLICK = "BottomButton.DoubleClick"
        const val EVENT_BOTTOM_BUTTON_LONG_PRESS = "BottomButton.LongPress"
        const val EVENT_BOTTOM_BUTTON_RELEASE = "BottomButton.Release"
        const val EVENT_TOP_BUTTON_CLICK = "TopButton.Click"
        const val EVENT_TOP_BUTTON_DOUBLE_CLICK = "TopButton.DoubleClick"
        const val EVENT_TOP_BUTTON_LONG_PRESS = "TopButton.LongPress"
        const val EVENT_TOP_BUTTON_RELEASE = "TopButton.Release"
    }
}