package com.euxcet.viturering.input

import android.content.Context
import android.graphics.Bitmap
import android.util.Base64
import android.util.Log
import cn.xfyun.api.GeneralWordsClient
import cn.xfyun.config.OcrWordsEnum
import com.google.gson.Gson
import com.google.gson.JsonObject
import java.io.ByteArrayOutputStream

class XFHandwriting(val context: Context): HandwritingApi {

    companion object {
        const val TAG = "XFHandwriting"
    }

    private val client: GeneralWordsClient by lazy {
        GeneralWordsClient.Builder("3d73e9ca", "f9de810c05a37bb0f7a15e816ce93204", OcrWordsEnum.HANDWRITING).build()
    }

    override suspend fun recognizeHandwriting(image: Bitmap): String? {
        val base64Img = ByteArrayOutputStream().use {
            val resizeImage = Bitmap.createScaledBitmap(image, image.width/4, image.height/4, true)
            resizeImage.compress(Bitmap.CompressFormat.JPEG, 90, it)
            Base64.encodeToString(it.toByteArray(), Base64.NO_WRAP)
        }
        val res = client.generalWords(base64Img)
        val gson = Gson()
        val jsonData: JsonObject = gson.fromJson(res, JsonObject::class.java)
        val code = jsonData.get("code").asString
        if (code != "0") {
            Log.e(TAG, "Recognize handwriting failed: $res")
            return null
        }
        Log.e(TAG, "Recognize handwriting: $res")
        val line = jsonData.getAsJsonObject("data").getAsJsonArray("block")[0].asJsonObject.getAsJsonArray("line")
        if (line.size() == 0) {
            return null
        }
        return line.joinToString("\n") { l ->
            l.asJsonObject.getAsJsonArray("word").joinToString(" ") { w ->
                w.asJsonObject.get("content").asString
            }
        }
    }
}