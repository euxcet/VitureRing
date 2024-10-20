package com.hcifuture.producer.detector

import android.content.res.AssetManager
import android.util.Log
import com.hcifuture.producer.sensor.NuixSensorManager
import com.hcifuture.producer.sensor.data.RingImuData
import com.hcifuture.producer.sensor.external.ring.RingSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.pytorch.IValue
import org.pytorch.LiteModuleLoader
import org.pytorch.Tensor
import javax.inject.Inject
import kotlin.math.exp

class WordDetector @Inject constructor(
    private val assetManager: AssetManager,
    private val nuixSensorManager: NuixSensorManager,
) {

    enum class TouchState {
        UP, DOWN
    }

    private val scope = CoroutineScope(Dispatchers.Default)
    val eventFlow = MutableSharedFlow<String>(replay = 0)
    private val labels = arrayOf("ALWAYS_CONTACT", "ALWAYS_NO_CONTACT", "UP", "DOWN")
    private val calculateFrequency: Float = 50.0f
    private val data = Array(6) { FloatArray(20) { 0.0f } }
    private var touchState = TouchState.UP

    fun getTouchEvent(state: TouchState): TouchState? {
        val event: TouchState? =
            if (touchState == TouchState.UP && state == TouchState.DOWN) {
                TouchState.DOWN
            } else if (touchState == TouchState.DOWN && state == TouchState.UP) {
                TouchState.UP
            } else {
                null
            }
        touchState = state
        return event
    }

    fun start() {
        scope.launch {
            nuixSensorManager.defaultRing.getProxyFlow<RingImuData>(
                RingSpec.imuFlowName(nuixSensorManager.defaultRing)
            )?.collect { imu ->
                for (i in 0 until 6) {
                    for (j in 0 until 19) {
                        data[i][j] = data[i][j + 1]
                    }
                    data[i][19] = imu.data[i]
                }
            }
        }

        scope.launch {
            val model = LiteModuleLoader.loadModuleFromAsset(assetManager, "touch_event.ptl")
            while (true) {
                delay((1000.0f / calculateFrequency).toLong())
                val tensor = Tensor.fromBlob(
                    data.flatMap { it.toList() }.toFloatArray(),
                    longArrayOf(1, 6, 20)
                )
                val output = model.forward(IValue.from(tensor)).toTensor().dataAsFloatArray
                val expSum = output.map { exp(it) }.sum()
                val softmax = output.map { exp(it) / expSum }
                val result = softmax.withIndex().maxByOrNull { it.value }?.index!!
                val event = if (result >= 2 && softmax[result] > 0.8) {
                    getTouchEvent(if (result == 2) TouchState.UP else TouchState.DOWN)
                } else {
                    null
                }
                Log.e("Nuix", event.toString())
                if (touchState == TouchState.DOWN) {
                    // record trajectory
                } else if (event == TouchState.UP) {
                    // upload
                }
            }
        }
    }
}
