package com.fypsolutions.tflite.myapplication

import android.content.Context
import android.graphics.Bitmap
import android.util.Size
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import kotlin.math.min

class VideoClassifier(
    private val context: Context,
    private val modelName: String,
    private val labelFile: String,
    private val options: VideoClassifierOptions
) {
    private lateinit var model: Model
    private lateinit var inputImage: TensorImage
    private lateinit var outputBuffer: TensorBuffer
    private var modelInputWidth = 0
    private var modelInputHeight = 0
    private val labels = mutableListOf<String>()

    fun init() {
        val modelOptions = Model.Options.Builder()
            .setNumThreads(options.numThreads)
            .build()
        model = Model.createModel(context, modelName, modelOptions)
        initModelShape()
        labels.addAll(FileUtil.loadLabels(context, labelFile))
    }

    private fun initModelShape() {
        val inputTensor = model.getInputTensor(0)
        val inputShape = inputTensor.shape()

        // Log the shape of the input tensor for debugging
        println("Input Tensor Shape: ${inputShape.joinToString(", ")}")

        if (inputShape.size < 5) {
            throw IllegalArgumentException("Unexpected input tensor shape. Expected a shape with at least 5 dimensions.")
        }

        var modelInputFrames = inputShape[1]
        modelInputHeight = inputShape[2]
        modelInputWidth = inputShape[3]
        var modelInputChannel = inputShape[4]
        inputImage = TensorImage(inputTensor.dataType())

        val outputTensor = model.getOutputTensor(0)
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())
    }


    fun classify(inputBitmap: Bitmap): List<Pair<String, Float>> {
        inputImage = preprocessInputImage(inputBitmap)
        val inputs = arrayOf<Any>(inputImage.buffer)
        val outputs = mutableMapOf<Int, Any>()
        outputs[0] = outputBuffer.buffer.rewind()

        model.run(inputs, outputs)
        val output = TensorLabel(labels, outputBuffer).mapWithFloatValue
        return output.toList().sortedByDescending { it.second }
            .take(options.maxResults)
    }

    private fun preprocessInputImage(bitmap: Bitmap): TensorImage {
        val size = min(bitmap.width, bitmap.height)
        val imageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(size, size))
            .add(ResizeOp(modelInputHeight, modelInputWidth, ResizeOp.ResizeMethod.BILINEAR))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()
        inputImage.load(bitmap)
        return imageProcessor.process(inputImage)
    }

    fun getInputSize(): Size {
        return Size(modelInputWidth, modelInputHeight)
    }

    fun close() {
        if (::model.isInitialized) {
            model.close()
        }
    }

    class VideoClassifierOptions private constructor(
        val numThreads: Int,
        val maxResults: Int
    ) {
        companion object {
            fun builder() = Builder()
        }

        class Builder {
            private var numThreads: Int = -1
            private var maxResults: Int = -1

            fun setNumThreads(numThreads: Int): Builder {
                this.numThreads = numThreads
                return this
            }

            fun setMaxResults(maxResults: Int): Builder {
                if (maxResults <= 0 && maxResults != -1) {
                    throw IllegalArgumentException("maxResults must be positive or -1.")
                }
                this.maxResults = maxResults
                return this
            }

            fun build(): VideoClassifierOptions {
                return VideoClassifierOptions(this.numThreads, this.maxResults)
            }
        }
    }
}
