package com.example.aeye.tflite

import android.content.Context
import android.graphics.Bitmap
import android.util.Log
import android.util.Size
import com.example.aeye.R
import org.tensorflow.lite.DataType

import org.tensorflow.lite.Tensor
import org.tensorflow.lite.support.common.FileUtil
import org.tensorflow.lite.support.common.Processor
import org.tensorflow.lite.support.common.ops.CastOp
import org.tensorflow.lite.support.common.ops.NormalizeOp
import org.tensorflow.lite.support.image.ImageProcessor
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.support.image.ops.ResizeOp
import org.tensorflow.lite.support.image.ops.ResizeWithCropOrPadOp
import org.tensorflow.lite.support.image.ops.Rot90Op
import org.tensorflow.lite.support.label.Category
import org.tensorflow.lite.support.label.TensorLabel
import org.tensorflow.lite.support.model.Model
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer

import java.io.IOException
import kotlin.math.min

class CustomClassifier(private val context: Context, private val mode_det : Int) {

    var modelInputWidth : Int = 0; var modelInputHeight : Int = 0; var modelInputChannel : Int = 0

    lateinit var model: Model
    lateinit var inputImage : TensorImage
    lateinit var outputBuffer : TensorBuffer

    private lateinit var modelName : String
    private lateinit var labelFile : String
    private lateinit var labels : List<String>
    private lateinit var outputLabels : List<String>

    private var isInitialized : Boolean = false

    @Throws(IOException::class)
    fun init() {
        modelName = when(mode_det){
            R.drawable.drink_icon -> "drinkmodel_final_metadata.tflite" //임시 nms 처리 파일
            else -> "medicine_model_metadata.tflite" //예시 파일
        }
        labelFile = when(mode_det){
            R.drawable.drink_icon -> "classes_drink.txt"
            else -> "classes_medicine.txt" // 예시 파일
        }
        outputLabels = when(mode_det){
            R.drawable.drink_icon -> listOf("soda", "pepsi", "water", "coffee", "zerosoda", "zeropepsi", "hot6", "milkis", "pocari", "cocacola", "zerococacola", "undefined")
            else -> listOf("tylenol", "fucidin", "Brufen", "easyend", "Geworin", "Geworinsoft", "Gas", "undefined")
        }

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
            //.add(CastOp(DataType.UINT8))
            .build()

        return imageProcessor.process(inputImage)
    }

    fun classify(image: Bitmap, sensorOrientation: Int) : Pair<String, Float> {
        inputImage = loadImage(image, sensorOrientation)

        val outputSize = model.getOutputTensor(0).shape()
        var outputLabelId : Int = 0

        val inputs = Array<Any>(1) {inputImage.buffer}
        val outputs = HashMap<Int, Any>()
        outputs[0] = outputBuffer.buffer.rewind()

        model.run(inputs, outputs)

        var recognitionArray = outputBuffer.floatArray

        var maxLabelScores = recognitionArray[4]
        for (idx_i in 0 until 10647){

            val gridStride : Int = idx_i * outputSize[2]
            val confidence : Float = recognitionArray[4 + gridStride]
            val classScores : FloatArray = recognitionArray.copyOfRange(5 + gridStride, outputSize[2] + gridStride)

            if (confidence > maxLabelScores) {
                maxLabelScores = confidence
                outputLabelId = idx_i
            }
        }

        Log.d("Classifier", "index : ".plus(maxLabelScores.toString()))
        outputLabelId = if (maxLabelScores > 0.5f){
            if (mode_det == R.drawable.drink_icon)
                argmax(recognitionArray, outputLabelId, 16)
            else
                argmax(recognitionArray, outputLabelId, 12)
        } else{
            outputLabels.size - 1
        }

        Log.d("Classifier", outputLabels[outputLabelId])
        //val output : Map<String, Float> = TensorLabel(labels, outputBuffer).mapWithFloatValue
        return Pair(outputLabels[outputLabelId], 95f)
    }

    fun classify(image: Bitmap) : Pair<String, Float> = classify(image, 0)

    private fun argmax(floatArray: FloatArray, initId : Int, targetIdx : Int) : Int{
        var maxIdx = 0 ; var maxValue = 0f
        val initIndex = initId * model.getOutputTensor(0).shape()[2]
        val testArray = floatArray.copyOfRange(initIndex + 5, initIndex + targetIdx)

        for (idx in testArray.indices){
            if (testArray[idx] > maxValue){
                maxValue = testArray[idx]
                maxIdx = idx
            }
        }
        return maxIdx
    }

    fun finish() {
        model.close()
        isInitialized = false
    }

}