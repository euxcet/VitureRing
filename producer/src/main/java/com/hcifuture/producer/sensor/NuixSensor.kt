package com.hcifuture.producer.sensor

import com.hcifuture.producer.common.utils.FunctionUtils
import com.hcifuture.producer.recorder.Collector
import com.hcifuture.producer.sensor.data.RingV1ImuData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

enum class NuixSensorState {
    SCANNED,
    CONNECTING,
    CONNECTED,
    DISCONNECTED,
}
abstract class NuixSensor {
    abstract fun connect()
    abstract fun disconnect()

    fun toggle() {
        if (status !in listOf(NuixSensorState.CONNECTED, NuixSensorState.CONNECTING)) {
            connect()
        } else {
            disconnect()
        }
    }

    fun connectable(): Boolean {
        return status in listOf(NuixSensorState.SCANNED, NuixSensorState.DISCONNECTED)
    }

    fun disconnectable(): Boolean {
        return status == NuixSensorState.CONNECTED
    }

    inline fun<reified T> getFlow(name: String): Flow<T>? =
        flows[name]?.map { v -> FunctionUtils.reifiedValue<T>(v) }

    /**
     * The names of the sensors should be different from each other.
     */
    abstract val name: String
    var status: NuixSensorState = NuixSensorState.SCANNED
        set(state) {
            field = state
            CoroutineScope(Dispatchers.Default).launch {
                lifecycleFlow.emit(state)
            }
        }
    abstract val flows: Map<String, Flow<Any>>
    abstract val defaultCollectors: Map<String, Collector>
    protected val lifecycleFlow = MutableStateFlow(NuixSensorState.SCANNED)
}