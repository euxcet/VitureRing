package com.euxcet.viturering

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.euxcet.viturering.databinding.LayoutTopBarBinding
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.sensor.NuixSensorManager
import com.hcifuture.producer.sensor.NuixSensorState
import com.hcifuture.producer.sensor.data.RingV2StatusData
import com.hcifuture.producer.sensor.data.RingV2StatusType
import com.hcifuture.producer.sensor.external.ring.RingSpec
import com.hcifuture.producer.sensor.external.ring.ringV2.RingV2
import com.hcifuture.producer.sensor.external.ring.ringV2.RingV2Spec
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class TopBarView(context: Context, attrs: AttributeSet?, defStyleAttr: Int): FrameLayout(context, attrs, defStyleAttr) {

    @Inject
    lateinit var nuixSensorManager: NuixSensorManager

    constructor(context: Context) : this(context, null)

    constructor(context: Context, attrs: AttributeSet?): this(context, attrs, 0)

    private var binding: LayoutTopBarBinding = LayoutTopBarBinding.inflate(LayoutInflater.from(context), this, true)

    private var mScope: CoroutineScope? = null

    init {
        attrs?.let { ats ->
            val typedArray = context.obtainStyledAttributes(ats, R.styleable.TopBarView)
            val title = typedArray.getString(R.styleable.TopBarView_title)
            typedArray.recycle()
            title?.let { setTitle(it) }
        }
        addOnAttachStateChangeListener(object : OnAttachStateChangeListener {
            override fun onViewAttachedToWindow(v: View) {
                mScope = CoroutineScope(Dispatchers.IO).apply {
                    launch {
                        var lastConnectStatus = NuixSensorState.SCANNING
                        while (true) {
                            if (nuixSensorManager.defaultRing.status != lastConnectStatus) {
                                lastConnectStatus = nuixSensorManager.defaultRing.status
                                withContext(Dispatchers.Main) {
                                    binding.ringState.text = LanguageUtils.statusChinese(lastConnectStatus)
                                }
                                if (lastConnectStatus == NuixSensorState.CONNECTED) {
                                    mScope?.launch {
                                        nuixSensorManager.defaultRing.getProxyFlow<RingV2StatusData>(
                                            RingSpec.statusFlowName(nuixSensorManager.defaultRing)
                                        )?.collect {
                                            if (it.type == RingV2StatusType.BATTERY_LEVEL) {
                                                withContext(Dispatchers.Main) {
                                                    binding.ringBattery.text = "戒指电量：${if (it.batteryLevel > 100) 100 else it.batteryLevel }"
                                                }
                                            }
                                        }
                                    }
                                    mScope?.launch {
                                        while (nuixSensorManager.defaultRing.status == NuixSensorState.CONNECTED) {
                                            val ringV2 = nuixSensorManager.defaultRing.target as? RingV2
                                            ringV2?.write(RingV2Spec.GET_BATTERY_LEVEL)
                                            delay(1000 * 60)
                                        }
                                    }
                                } else {
                                    withContext(Dispatchers.Main) {
                                        binding.ringBattery.text = ""
                                    }
                                }
                            }
                            delay(1000)
                        }
                    }
                }
            }

            override fun onViewDetachedFromWindow(v: View) {
                mScope?.cancel()
            }
        })
    }

    fun setTitle(title: String) {
        binding.title.text = title
    }

    fun setTitleVisibility(visibility: Int) {
        binding.title.visibility = visibility
    }
}