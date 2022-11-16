package com.example.aeye.tflite

import android.content.Context
import android.graphics.Bitmap
import android.util.Size

import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

import java.io.IOException
import java.io.ObjectOutput
import kotlin.math.min

class CustomClassifier(private val modelName : String) {

    var modelInputWidth : Int = 0; var modelInputHeight : Int = 0; var modelInputChannel : Int = 0

    lateinit var context: Context
    lateinit var model: Model
    lateinit var inputImage : TensorImage
    lateinit var outputBuffer : TensorBuffer

    private val labelFile : String = "labels.text"
    private lateinit var labels : List<String>

    private var isInitialized : Boolean = false

    @Throws(IOException::class)
    fun init() {
        model = Model.createModel(context, modelName)
        initModelShape()
        labels = FileUtil.loadLabels(context, labelFile)

        isInitialized = true
    }

    fun isInitialized() = isInitialized

    private fun initModelShape(){
        val inputTensor : Tensor = model.getInputTensor(0)
        val shape : IntArray = inputTensor.shape()
        modelInputChannel = shape[0]; modelInputWidth = shape[1]; modelInputHeight = shape[2]

        inputImage = TensorImage(inputTensor.dataType())

        val outputTensor : Tensor = model.getOutputTensor(0)
        outputBuffer = TensorBuffer.createFixedSize(outputTensor.shape(), outputTensor.dataType())

    }

    fun getModelInputSize() : Size {
        if (!isInitialized) return Size(0,0)
        return Size(modelInputWidth, modelInputWidth)
    }

    private fun convertBitmapToARGB8888(bitmap: Bitmap) : Bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)

    private fun loadImage(bitmap : Bitmap, sensorOrientation: Int) : TensorImage{
        if (bitmap.config != Bitmap.Config.ARGB_8888){
            inputImage.load(convertBitmapToARGB8888(bitmap))
        }
        else {
            inputImage.load(bitmap)
        }

        val cropSize : Int = min(bitmap.width, bitmap.height)
        val numRotation : Int = sensorOrientation / 90

        val imageProcessor : ImageProcessor = ImageProcessor.Builder()
            .add(ResizeWithCropOrPadOp(cropSize, cropSize))
            .add(ResizeOp(modelInputWidth, modelInputWidth, ResizeOp.ResizeMethod.NEAREST_NEIGHBOR))
            .add(Rot90Op(numRotation))
            .add(NormalizeOp(0.0f, 255.0f))
            .build()

        return imageProcessor.process(inputImage)
    }

    fun classify(image: Bitmap, sensorOrientation: Int) : Pair<String, Float> {
        inputImage = loadImage(image, sensorOrientation)

        val inputs = Array<Any>(1) {inputImage.buffer}
        val outputs = HashMap<Int, Any>()
        outputs[0] = outputBuffer.buffer.rewind()

        model.run(inputs, outputs)

        val output : Map<String, Float> = TensorLabel(labels, outputBuffer).mapWithFloatValue
        return argmax(output)
    }

    fun classify(image: Bitmap) : Pair<String, Float> = classify(image, 0)

    private fun argmax(map : Map<String, Float>) : Pair<String, Float>{
        var maxKey = ""
        var maxVal : Float = -1f

        for (entry : Map.Entry<String, Float> in map.entries){
            val f : Float = entry.value
            if (f > maxVal){
                maxKey = entry.key
                maxVal = entry.value
            }
        }

        return Pair(maxKey, maxVal)
    }

    fun finish() {
        model.close()
        isInitialized = false
    }

}