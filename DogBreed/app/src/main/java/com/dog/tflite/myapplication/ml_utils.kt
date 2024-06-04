package com.dog.tflite.myapplication

import android.graphics.Bitmap
import org.tensorflow.lite.support.image.TensorImage

fun preprocessFrame(frameBitmap: Bitmap): TensorImage {
    // Resize the frame to match the model input size (e.g., 224x224)
    val resizedBitmap = Bitmap.createScaledBitmap(frameBitmap, 224, 224, true)

    // Normalize pixel values
    //val normalizedBitmap = normalizeBitmap(resizedBitmap)

    // Convert the normalized bitmap to a TensorImage
    return TensorImage.fromBitmap(resizedBitmap)
}



