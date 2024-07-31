package com.hcifuture.producer.sensor.external.ringV2

import com.hcifuture.producer.common.utils.NuixEvent
import com.hcifuture.producer.sensor.data.RingV2TouchEvent

class RingV2Event (name: String, data: Map<String, Any?>?) : NuixEvent(name, data, NAMESPACE) {

    companion object {
        const val NAMESPACE = "ring_v2"
        const val EVENT_OPEN_MIC = "OPEN_MIC"
        const val EVENT_CLOSE_MIC = "CLOSE_MIC"
        const val EVENT_TOUCH_TAP = "TOUCH.TAP"
        const val EVENT_TOUCH_SWIPE_POSITIVE = "TOUCH.SWIPE_POSITIVE"
        const val EVENT_TOUCH_SWIPE_NEGATIVE = "TOUCH.SWIPE_NEGATIVE"
        const val EVENT_TOUCH_FLICK_POSITIVE = "TOUCH.FLICK_POSITIVE"
        const val EVENT_TOUCH_FLICK_NEGATIVE = "TOUCH.FLICK_NEGATIVE"
        const val EVENT_TOUCH_HOLD = "TOUCH.HOLD"

        fun createRingV2Event(name: String, data: Map<String, Any?>?): RingV2Event {
            return RingV2Event(name, data)
        }

        fun createRingV2Event(ringV2TouchEvent: RingV2TouchEvent, data: Map<String, Any?>?) : RingV2Event {
            val name = when(ringV2TouchEvent) {
                RingV2TouchEvent.TAP -> EVENT_TOUCH_TAP
                RingV2TouchEvent.SWIPE_POSITIVE -> EVENT_TOUCH_SWIPE_POSITIVE
                RingV2TouchEvent.SWIPE_NEGATIVE -> EVENT_TOUCH_SWIPE_NEGATIVE
                RingV2TouchEvent.FLICK_POSITIVE -> EVENT_TOUCH_FLICK_POSITIVE
                RingV2TouchEvent.FLICK_NEGATIVE -> EVENT_TOUCH_FLICK_NEGATIVE
                RingV2TouchEvent.HOLD -> EVENT_TOUCH_HOLD
            }
            return RingV2Event(name, data)
        }
    }
}