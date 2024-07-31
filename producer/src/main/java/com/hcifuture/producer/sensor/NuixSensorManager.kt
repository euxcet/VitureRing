package com.hcifuture.producer.sensor

import android.graphics.Path
import android.util.Log
import com.hcifuture.producer.common.utils.FunctionUtils.flatten
import com.hcifuture.producer.common.utils.FunctionUtils.reifiedValue
import com.hcifuture.producer.sensor.audio.AudioProvider
import com.hcifuture.producer.sensor.audio.AudioSensor
import com.hcifuture.producer.sensor.external.BleProvider
import com.hcifuture.producer.sensor.external.ringV1.RingV1
import com.hcifuture.producer.sensor.video.VideoProvider
import com.hcifuture.producer.sensor.external.ringV1.RingV1Provider
import com.hcifuture.producer.sensor.external.ringV2.RingV2
import com.hcifuture.producer.sensor.external.ringV2.RingV2Provider
import com.hcifuture.producer.sensor.internal.InternalSensor
import com.hcifuture.producer.sensor.internal.InternalSensorProvider
import com.hcifuture.producer.sensor.location.LocationProvider
import com.hcifuture.producer.sensor.location.LocationSensor
import com.hcifuture.producer.sensor.touch.TouchSensor
import com.hcifuture.producer.sensor.touch.TouchSensorProvider
import com.hcifuture.producer.sensor.video.VideoSensor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

sealed class NuixSensorManagerEvent {
    data class CreateSensor(val sensor: NuixSensor): NuixSensorManagerEvent()
    data class DeleteSensor(val sensor: NuixSensor): NuixSensorManagerEvent()
}

@Singleton
class NuixSensorManager @Inject constructor(
    private val internalSensorProvider: InternalSensorProvider,
//    private val ringV1Provider: RingV1Provider,
//    private val ringV2Provider: RingV2Provider,
    private val bleProvider: BleProvider,
    private val videoProvider: VideoProvider,
    private val audioProvider: AudioProvider,
    private val touchSensorProvider: TouchSensorProvider,
    private val locationProvider: LocationProvider
) {
    private val scope = CoroutineScope(Dispatchers.Default)
    private val providers = listOf(
        internalSensorProvider,
//        ringV1Provider,
//        ringV2Provider,
        bleProvider,
        videoProvider,
        audioProvider,
        touchSensorProvider,
        locationProvider,
    )
    // Sensors
    private val _sensors: MutableMap<NuixSensorProvider, MutableList<NuixSensor>> = mutableMapOf()
    val sensors: Map<NuixSensorProvider, List<NuixSensor>> = _sensors

    private val _eventFlows = MutableSharedFlow<NuixSensorManagerEvent>()
    val eventFlows = _eventFlows.asSharedFlow()

    private val _scanJob: MutableMap<NuixSensorProvider, Job> = mutableMapOf()

    init {
        providers.forEach { provider ->
            addProvider(provider)
            provider.get().forEach {
                addSensor(provider, it)
            }
        }
    }

    private fun addProvider(provider: NuixSensorProvider) {
        if (provider !in _sensors) {
            _sensors[provider] = mutableListOf()
        }
    }

    /**
     * Sensor
     */
    fun internalSensors(): List<InternalSensor>
        = sensors.getValue(internalSensorProvider).map { it as InternalSensor }
    fun ringV1s(): List<RingV1>
        = sensors.getValue(bleProvider)
            .filterIsInstance<RingV1>()
            .map { it }
    //        = sensors.getValue(ringV1Provider).map { it as RingV1 }
    fun ringV2s(): List<RingV2>
        = sensors.getValue(bleProvider)
            .filterIsInstance<RingV2>()
            .map { it }
//        = sensors.getValue(ringV2Provider).map { it as RingV2 }
    fun touchSensors(): List<TouchSensor>
        = sensors.getValue(touchSensorProvider).map { it as TouchSensor }
    fun locationSensors(): List<LocationSensor>
        = sensors.getValue(locationProvider).map { it as LocationSensor }
    fun videos(): List<VideoSensor>
        = sensors.getValue(videoProvider).map { it as VideoSensor }
    fun audios(): List<AudioSensor>
        = sensors.getValue(audioProvider).map { it as AudioSensor }

    /**
     * The default ring will be switched automatically when the target ring change.
     */
    var defaultRingV1: NuixSensorProxy = NuixSensorProxy("DefaultRing", null)
    private fun defaultRingV1(): NuixSensor?
        = ringV1s().find { it.status == NuixSensorState.CONNECTED }
            ?: ringV1s().find { it.status == NuixSensorState.CONNECTING }
            ?: ringV1s().firstOrNull()
    var defaultRingV2: NuixSensorProxy = NuixSensorProxy("DefaultRingV2", null)
    private fun defaultRingV2(): NuixSensor?
        = ringV2s().find { it.status == NuixSensorState.CONNECTED }
            ?: ringV2s().find { it.status == NuixSensorState.CONNECTING }
            ?: ringV2s().firstOrNull()
    fun defaultTouchSensor(): TouchSensor? = touchSensors().firstOrNull()
    fun defaultLocationSensor(): LocationSensor? = locationSensors().firstOrNull()
    fun defaultVideo(): VideoSensor? = videos().firstOrNull()
    fun defaultAudio(): AudioSensor? = audios().firstOrNull()

    private fun addSensor(provider: NuixSensorProvider, sensor: NuixSensor) {
        _sensors[provider] ?:
            throw NoSuchElementException("Attempt to add a sensor from an unknown provider.")
        if (_sensors[provider]?.find { it.name == sensor.name } == null) {
            _sensors[provider]?.add(sensor)
            emitEvent(NuixSensorManagerEvent.CreateSensor(sensor))
        }
    }

    fun getSensor(name: String): NuixSensor? {
        return getSensor { sensor -> sensor.name == name }
    }

    fun getSensor(condition: (sensor: NuixSensor) -> Boolean): NuixSensor? {
        for (providerSensors in sensors.values) {
            for (sensor in providerSensors) {
                if (condition(sensor)) {
                    return sensor
                }
            }
        }
        return null
    }

    fun getSensors(name: String): List<NuixSensor> = getSensors { sensor -> sensor.name == name }

    fun getSensors(condition: (sensor: NuixSensor) -> Boolean): List<NuixSensor> {
        val result = mutableListOf<NuixSensor>()
        for (providerSensors in sensors.values) {
            for (sensor in providerSensors) {
                if (condition(sensor)) {
                    result.add(sensor)
                }
            }
        }
        return result
    }

    fun refreshDefaultSensors() {
        Log.e("Nuix", "refresh")
        defaultRingV1.switchTarget(defaultRingV1())
        defaultRingV2.switchTarget(defaultRingV2())
    }

    /**
     * Scan
     * Setting the timeout to 0, which means an unlimited scan time,
     * has NOT yet been fully implemented. Please refrain from using it.
    */
    private suspend fun scanProvider(provider: NuixSensorProvider, timeout: Long = 2000L) {
        if (_scanJob[provider]?.isActive == true) {
            return
        }
        _scanJob[provider] = scope.launch {
            provider.scan().collect { sensors ->
                // TODO: remove invisible sensors.
                sensors.forEach {
                    addSensor(provider, it)
                }
            }
        }
        if (timeout > 0) {
            delay(timeout)
            _scanJob[provider]?.cancel()
            _scanJob[provider]?.join()
            if (provider is BleProvider) {
                refreshDefaultSensors()
            }
        }
    }

    private suspend fun scan(providers: List<NuixSensorProvider>, timeout: Long = 5000L) {
        providers
            .filter { it.requireScan }
            .forEach { scanProvider(it, timeout) }
    }

    private suspend fun stopScanProvider(provider: NuixSensorProvider) {
        if (_scanJob[provider]?.isActive == true) {
            _scanJob[provider]?.cancel()
            _scanJob[provider]?.join()
        }
    }

    private suspend fun stopScan(providers: List<NuixSensorProvider>) {
        providers
            .filter { it.requireScan }
            .forEach { stopScanProvider(it) }
    }

    suspend fun scanAll(timeout: Long = 5000L) {
        scan(providers, timeout)
    }

//    suspend fun scanRingV1(timeout: Long = 2000L) {
//        scan(listOf(ringV1Provider), timeout)
//    }
//
//    suspend fun scanRingV2(timeout: Long = 2000L) {
//        scan(listOf(ringV2Provider), timeout)
//    }

    suspend fun stopScanAll() {
        stopScan(providers)
    }

//    suspend fun stopScanRingV1() {
//        stopScan(listOf(ringV1Provider))
//    }
//
//    suspend fun stopScanRingV2() {
//        stopScan(listOf(ringV2Provider))
//    }
//
    /**
     * Flow
     */
    fun flows(): Map<String, Flow<Any>> =
        flatten(sensors.values.flatten()
            .map { it.flows })

    inline fun<reified T> getFlow(name: String): Flow<T>? =
        flows()[name]?.map { v -> reifiedValue<T>(v) }

    /**
     * Events
     */
    private fun emitEvent(event: NuixSensorManagerEvent) {
        CoroutineScope(Dispatchers.Default).launch {
            _eventFlows.emit(event)
        }
    }

    /**
     * Injected events
     */
    fun onTouchEvent(path: Path) {
        (getSensor { sensor -> sensor is TouchSensor } as TouchSensor).onTouchEvent(path)
    }
}