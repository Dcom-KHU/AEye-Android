package com.example.aeye.env

import android.content.Context
import android.util.AttributeSet
import android.view.TextureView
import android.view.View

class AutoFitTextureView : TextureView {

    private var ratioWidth : Int = 0
    private var ratioHeight : Int = 0

    constructor(context: Context) : this(context, null)
    constructor(context: Context, attrs: AttributeSet?) : this(context, attrs, 0)
    constructor(context: Context, attrs: AttributeSet?, defStyle : Int) : super(context, attrs, defStyle)

    fun setAspectRatio(width : Int, height : Int){
        if (width < 0 || height < 0){
            throw IllegalArgumentException("Size cannot be negative.")
        }
        ratioWidth = width
        ratioHeight = height
        requestLayout()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val width : Int = View.MeasureSpec.getSize(widthMeasureSpec)
        val height : Int = View.MeasureSpec.getSize(heightMeasureSpec)

        if (0 == ratioWidth || 0 == ratioHeight){
            setMeasuredDimension(width, height)
        }
        else {
            if (width < height * ratioWidth / ratioHeight){
                setMeasuredDimension(width, width * ratioHeight / ratioWidth)
            }
            else{
                setMeasuredDimension(height * ratioWidth / ratioHeight, height)
            }
        }

    }



}