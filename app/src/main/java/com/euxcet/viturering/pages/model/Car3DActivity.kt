package com.euxcet.viturering.pages.model

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.euxcet.viturering.R
import com.euxcet.viturering.RingManager
import com.euxcet.viturering.databinding.ActivityCar3DactivityBinding
import com.euxcet.viturering.utils.GestureThrottle
import com.euxcet.viturering.utils.LanguageUtils
import com.hcifuture.producer.detector.TouchState
import com.hcifuture.producer.sensor.data.RingTouchEvent
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Job
import org.the3deer.android_3d_model_engine.ModelEngine
import org.the3deer.android_3d_model_engine.camera.CameraController
import org.the3deer.android_3d_model_engine.drawer.BoundingBoxRenderer
import org.the3deer.android_3d_model_engine.model.Camera
import org.the3deer.android_3d_model_engine.model.Constants
import org.the3deer.android_3d_model_engine.model.Light
import org.the3deer.android_3d_model_engine.model.Scene
import org.the3deer.android_3d_model_engine.view.GLFragment
import org.the3deer.android_3d_model_engine.view.GLSurfaceView
import org.the3deer.util.android.ContentUtils
import javax.inject.Inject

@AndroidEntryPoint
class Car3DActivity : AppCompatActivity() {
    companion object {
        const val TAG = "Car3DActivity"
    }

    @Inject
    lateinit var ringManager: RingManager
    private lateinit var binding: ActivityCar3DactivityBinding
    private val mainHandler = Handler(Looper.getMainLooper())
    private lateinit var modelEngine: ModelEngine

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        binding = ActivityCar3DactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        modelEngine = ModelEngine.newInstance(this, savedInstanceState, null)
        modelEngine.init()
        loadModel()
    }


    private fun loadModel() {
        val args = Bundle()
        var hasExternalModel = false
        val modelDir = getExternalFilesDir("models")
        if (modelDir != null && modelDir.exists()) {
            val modelFile = modelDir.listFiles()?.firstOrNull { it.isFile && (it.extension == "obj" || it.extension == "glb" || it.extension == "gltf" || it.extension == "stl") }
            if (modelFile != null) {
                hasExternalModel = true
                ContentUtils.setCurrentDir(modelDir) // 不调用找不到模型所需文件
                args.putString("uri", Uri.fromFile(modelFile).toString())
            }
        }
        if (!hasExternalModel) {
            ContentUtils.provideAssets(this) // 不调用找不到模型所需文件
            args.putString("uri", "android://${packageName}/assets/models/Paimon.obj")
        }
        // args.putString("type", "0") //obj不用设置？
        args.putBoolean("demo", false)
        modelEngine.beanFactory.addOrReplace("extras", args)
        modelEngine.beanFactory.addOrReplace("surface", GLSurfaceView(this))
        modelEngine.beanFactory.addOrReplace("fragment_gl", GLFragment())
        modelEngine.beanFactory.addOrReplace("scene_0.loader", MySceneLoader(modelEngine))
        modelEngine.beanFactory.addOrReplace("20.scene_0.camera", Camera(1f))
//        modelEngine.beanFactory.addOrReplace("20.scene_0.light", Light(floatArrayOf(0f, Constants.UNIT * 10f, Constants.UNIT * 10f)))
        // modelEngine.beanFactory.addOrReplace("80.gui.renderer", GUISystem().apply { isEnabled = false })
        modelEngine.beanFactory.addOrReplace("50.renderer4.boundingBoxDrawer", BoundingBoxRenderer().apply { isEnabled = false })
        modelEngine.refresh()
        supportFragmentManager.beginTransaction().replace(binding.mainContainer.id, modelEngine.glFragment).commit()
        scene = modelEngine.beanFactory.find(Scene::class.java)
        cameraController = modelEngine.beanFactory.find(CameraController::class.java)
    }

    override fun onStart() {
        super.onStart()
        connectRing()
    }

    private var isTouchDown = false

    private var isPlaying = false
    private fun playVideo(resId: Int) {
        val path = "android.resource://" + packageName + "/" + resId
        playVideo(Uri.parse(path))
    }

    private fun playVideo(name: String) {
        val resDir = getExternalFilesDir("res")
        if (resDir?.exists() == false) {
            resDir.mkdirs()
        }
        val targetFile = resDir?.listFiles()?.firstOrNull {
            it.nameWithoutExtension == name
        }
        if (targetFile != null) {
            playVideo(Uri.fromFile(targetFile))
        }
    }

    private fun playVideo(uri: Uri) {
        binding.videoContainer.visibility = View.VISIBLE
        binding.videoView.visibility = View.VISIBLE
        binding.videoView.setOnPreparedListener { mp ->
            binding.videoView.start()
            isPlaying = true
        }
        binding.videoView.setOnCompletionListener {
            binding.videoContainer.visibility = View.GONE
            binding.videoView.visibility = View.GONE
            isPlaying = false
        }
        binding.videoView.setOnErrorListener { mp, what, extra ->
            binding.videoContainer.visibility = View.GONE
            binding.videoView.visibility = View.GONE
            isPlaying = false
            true
        }
        binding.videoView.setVideoURI(uri)
    }

    private var rotateJob: Job? = null
    private var isPinchDown = false
    private var scene: Scene? = null
    private var cameraController: CameraController? = null
    private var rotateMode: Int = 0 // 0: y轴旋转 1: x轴旋转
    private fun connectRing() {
        ringManager.registerListener {
            onConnectCallback { // Connect
                runOnUiThread {

                }
            }
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    if (GestureThrottle.throttle(it)) {
                        return@runOnUiThread
                    }
                    when (it) {
                        "pinch" -> {
                            // modelEngine.rotate(1.57f)
                            if (rotateJob != null) {
                                rotateJob?.cancel()
                                rotateJob = null
                                return@runOnUiThread
                            }
                            // 操作模型
//                            scene?.objects?.let { objects ->
//                                if (objects.size > 0) {
//                                    val camera = scene?.camera
//                                    val model = objects[0]
//                                    val delayTime = 1000f / 60
//                                    val speed = Math.PI.toFloat() / 0.2f
//                                    rotateJob = CoroutineScope(Dispatchers.Main).launch {
//                                        while(true) {
//                                            val q = Quaternion.getQuaternion(floatArrayOf(0f, 1f, 0f, 1f), speed / 60)
//                                            model.setOrientation(Quaternion.multiply(model.orientation, q.normalize()))
//                                            delay(delayTime.toLong())
//                                        }
//                                    }
//                                }
//                            }
                        }
                        "pinch_down" -> {
                            isPinchDown = true
                        }
                        "pinch_up" -> {
                            isPinchDown = false
                        }
                        "snap" -> {
                            finish()
                        }
                        "circle_clockwise" -> {
                            playVideo("car_window_open")
                        }
                        "circle_counterclockwise" -> {
                            playVideo("car_window_close")
                        }
                        "wave_up" -> {
                            playVideo("car_screen_up")
                        }
                        "wave_down" -> {
                            playVideo("car_screen_down")
                        }
                        "push_forward" -> {
                            playVideo("car_view_back")
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    if (isPinchDown) {
                        if (rotateMode == 0) {
                            cameraController?.move(it.first, 0f)
                        } else {
                            cameraController?.move(0f, it.second)
                        }
                    }
                }
            }
            onStateCallback { // State
            }
            onTouchCallback { // Touch
                runOnUiThread {
                    val touchText = "触摸: ${(it.data)}"
                    Log.e("Nuix", "Touch: ${it.data}")
                    when (it.data) {
                        RingTouchEvent.HOLD -> {
                        }
                        RingTouchEvent.TAP -> {
                            rotateMode = if (rotateMode == 0) 1 else 0
                        }
                        else -> {}
                    }
                }
            }
            onPlaneEventCallback {
                runOnUiThread {
                    if (it == TouchState.DOWN) {
                        Log.e("Nuix", "Plane down")
                        mainHandler.postDelayed({
                            isTouchDown = true
                        }, 500)
                    } else {
                        Log.e("Nuix", "Plane up")
                        isTouchDown = false
                    }
                }
            }
            onPlaneMoveCallback {
                runOnUiThread {
                    if (isTouchDown) {

                    }
                }
            }
        }
        ringManager.connect()
    }
}