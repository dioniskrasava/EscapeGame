package com.majo.escapegame

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Enemy(var x: Float, var y: Float) {
    val radius = 30f
    private val speed = 5f

    fun update() {
        y += speed
    }

    fun draw(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.RED
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y, radius, paint)
    }
}