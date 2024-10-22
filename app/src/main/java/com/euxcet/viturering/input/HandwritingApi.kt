package com.euxcet.viturering.input

import android.graphics.Bitmap

interface HandwritingApi {
    suspend fun recognizeHandwriting(image: Bitmap): String?
}