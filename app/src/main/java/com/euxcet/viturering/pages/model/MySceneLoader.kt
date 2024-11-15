package com.euxcet.viturering.pages.model

import org.the3deer.android_3d_model_engine.ModelEngine
import org.the3deer.android_3d_model_engine.model.Scene
import org.the3deer.android_3d_model_engine.services.SceneLoader
import org.the3deer.util.math.Quaternion

class MySceneLoader(val engine: ModelEngine): SceneLoader() {

    override fun onLoadComplete() {
        super.onLoadComplete()
        engine.beanFactory.find(Scene::class.java)?.let { scene ->
            if ((scene.objects?.size ?: 0) > 0) {
                scene.objects.forEach { model ->
//                    val xRotate = Quaternion.getQuaternion(floatArrayOf(1f, 0f, 0f, 1f), -(Math.PI / 2).toFloat())
//                    val zRotate = Quaternion.getQuaternion(floatArrayOf(0f, 0f, 1f, 1f), Math.PI.toFloat())
                    val yRotate = Quaternion.getQuaternion(floatArrayOf(0f, 1f, 0f, 1f), Math.PI.toFloat())
                    model.setOrientation(Quaternion.multiply(model.orientation, yRotate.normalize()))
//                    model.setOrientation(Quaternion.multiply(Quaternion.multiply(model.orientation, xRotate.normalize()), zRotate.normalize()))
                    model.setLocation(model.location[0], model.location[1] - 10, model.location[2])
                }
            }
        }
    }
}