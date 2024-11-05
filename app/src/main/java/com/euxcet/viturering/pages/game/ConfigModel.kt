package com.euxcet.viturering.pages.game

import android.text.TextUtils
import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader


class ConfigModel {

    companion object {

        private const val TAG = "ConfigModel"

        /**
         * 解析资源包，内部目录结构应如下：
         * ./
         * config.json
         * xxx.mp4
         * xxx.mp4
         * @param resourcePath
         * @return
         */
        fun parseConfigModel(resourcePath: String): ConfigModel? {
            if (TextUtils.isEmpty(resourcePath)) {
                return null
            }
            if (!(File(resourcePath).exists())) {
                return null
            }

            val configFilePath =
                if (resourcePath.endsWith(File.separator)) resourcePath + "config.json"
                else resourcePath + File.separator + "config.json"
            if (!(File(configFilePath).exists())) {
                return null
            }

            var fis: FileInputStream? = null
            var isr: InputStreamReader? = null
            var input: CharArray? = null
            try {
                fis = FileInputStream(configFilePath)
                isr = InputStreamReader(fis, "UTF-8")
                input = CharArray(fis.available())
                isr.read(input)
                isr.close()
                fis.close()
            } catch (e: IOException) {
                Log.e(TAG, "parse: $e")
            } finally {
                try {
                    isr?.close()
                    fis?.close()
                } catch (e: IOException) {
                    Log.e(TAG, "parse: $e")
                }
            }

            if (input == null) {
                return null
            }

            val configStr = String(input)
            val gson = Gson()
            return gson.fromJson(configStr, ConfigModel::class.java)
        }
    }

    @SerializedName("landscape")
    var landscapeItem: Item? = null

    @SerializedName("portrait")
    var portraitItem: Item? = null

    class Item {
        @SerializedName("path")
        var path: String? = null

        @SerializedName("align")
        var alignMode: Int = 0
    }
}