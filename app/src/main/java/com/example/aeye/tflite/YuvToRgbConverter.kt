package com.example.aeye.tflite

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageFormat
import android.graphics.Rect
import android.media.Image
import android.renderscript.*
import java.nio.ByteBuffer

class YuvToRgbConverter {
    companion object {
        @JvmStatic
        @Suppress("DEPRECATION")
        fun yuvToRgb(context: Context, image : Image, output : Bitmap){
            val rs : RenderScript = RenderScript.create(context)
            val scriptYuvToRgb = ScriptIntrinsicYuvToRGB.create(rs, Element.U8_4(rs))

            val pixelCount : Int = image.cropRect.width() * image.cropRect.height()
            val pixelSizeBits : Int = ImageFormat.getBitsPerPixel(ImageFormat.YUV_420_888)
            val yuvBuffer = ByteArray(pixelCount * pixelSizeBits / 8)

            imageToByteArray(image, yuvBuffer, pixelCount)

            val elemType : Type = Type.Builder(rs, Element.YUV(rs)).setYuvFormat(ImageFormat.NV21).create()
            val inputAllocation : Allocation = Allocation.createSized(rs, elemType.element, yuvBuffer.size)
            val outputAllocation : Allocation = Allocation.createFromBitmap(rs, output)

            inputAllocation.copyFrom(yuvBuffer)
            scriptYuvToRgb.setInput(inputAllocation)
            scriptYuvToRgb.forEach(outputAllocation)
            outputAllocation.copyTo(output)
        }

        @JvmStatic
        fun imageToByteArray(image: Image, outputBuffer : ByteArray, pixelCount : Int){
            assert(image.format == ImageFormat.YUV_420_888)

            val imageCrop : Rect = image.cropRect
            val imagePlanes = image.planes

            imagePlanes.forEachIndexed { planeIndex, plane ->
                var outputStride : Int
                var outputOffset : Int

                when(planeIndex) {
                    0 -> { outputStride = 1; outputOffset = 0 }
                    1 -> { outputStride = 2; outputOffset = pixelCount + 1 }
                    2 -> { outputStride = 2; outputOffset = pixelCount }
                    else -> { return@forEachIndexed }
                }

                val planeBuffer : ByteBuffer = plane.buffer
                val rowStride : Int = plane.rowStride
                val pixelStride : Int = plane.pixelStride

                val planeCrop : Rect = if (planeIndex == 0){
                    imageCrop
                } else {
                    Rect(
                        imageCrop.left / 2,
                        imageCrop.top / 2,
                        imageCrop.right / 2,
                        imageCrop.bottom /2
                    )
                }

                val planeWidth : Int = planeCrop.width()
                val planeHeight : Int = planeCrop.height()
                val rowBuffer = ByteArray(plane.rowStride)

                val rowLength : Int = if(pixelStride == 1 && outputStride == 1){
                    planeWidth
                } else {
                    (planeWidth - 1) * pixelStride + 1
                }

                for (row in 0 until planeHeight) {
                    planeBuffer.position(
                        (row + planeCrop.top) * rowStride + planeCrop.left * pixelStride
                    )
                    if (pixelStride == 1 && outputStride == 1){
                        planeBuffer.get(outputBuffer, outputOffset, rowLength)
                        outputOffset += rowLength
                    }
                    else {
                        planeBuffer.get(rowBuffer, 0, rowLength)
                        for (col in 0 until planeWidth){
                            outputBuffer[outputOffset] = rowBuffer[col * pixelStride]
                            outputOffset += outputStride
                        }
                    }
                }

            }
        }
    }

}
