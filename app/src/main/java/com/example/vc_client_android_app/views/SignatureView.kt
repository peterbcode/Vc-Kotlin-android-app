package com.example.vc_client_android_app.views

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class SignatureView(context: Context, attrs: AttributeSet) : View(context, attrs) {

    private var path = Path()
    private val paint = Paint().apply {
        isAntiAlias = true
        color = Color.WHITE
        style = Paint.Style.STROKE
        strokeJoin = Paint.Join.ROUND
        strokeWidth = 5f
    }

    override fun onDraw(canvas: Canvas) {
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val x = event.x
        val y = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(x, y)
            MotionEvent.ACTION_MOVE -> path.lineTo(x, y)
            else -> return false
        }

        invalidate()
        return true
    }

    fun clear() {
        path.reset()
        invalidate()
    }
    
    fun getSignatureBitmap(): Bitmap {
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        val bgPaint = Paint().apply { color = Color.BLACK }
        canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)
        draw(canvas)
        return bitmap
    }
    
    fun isSignatureEmpty(): Boolean = path.isEmpty
}
