package com.euxcet.viturering

import androidx.appcompat.app.AppCompatActivity
import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import com.dmitrybrant.modelviewer.Model
import com.dmitrybrant.modelviewer.ModelSurfaceView
import com.dmitrybrant.modelviewer.stl.StlModel
import com.euxcet.viturering.databinding.ActivityModel3dBinding
import dagger.hilt.android.AndroidEntryPoint

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@AndroidEntryPoint
class Model3DActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Model3DActivity"
    }

    private lateinit var binding: ActivityModel3dBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private var modelView: ModelSurfaceView? = null
    private var currentModel: Model? = null

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        binding = ActivityModel3dBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun createNewModelView(model: Model?) {
        if (modelView != null) {
            binding.container.removeView(modelView)
        }
        modelView = ModelSurfaceView(this, model)
        binding.container.addView(modelView, 0)
    }

    private fun loadSampleModel() {
        try {
            assets.open("dragon.stl").use {
                currentModel = StlModel(it)
                createNewModelView(currentModel)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading sample model", e)
        }
    }

    override fun onStart() {
        super.onStart()
        loadSampleModel()
    }
}