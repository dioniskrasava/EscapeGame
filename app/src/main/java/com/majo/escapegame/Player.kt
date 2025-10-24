package com.majo.escapegame


import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint

class Player(var x: Float, var y: Float) {
    private val radius = 50f
    var targetX = x
    var targetY = y
    private val speed = 10f

    fun update() {
        val dx = targetX - x
        val dy = targetY - y
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        if (distance > speed) {
            x += (dx / distance) * speed
            y += (dy / distance) * speed
        } else {
            x = targetX
            y = targetY
        }
    }

    fun draw(canvas: Canvas) {
        val paint = Paint().apply {
            color = Color.BLUE
            style = Paint.Style.FILL
        }
        canvas.drawCircle(x, y, radius, paint)
    }

    fun collidesWith(enemy: Enemy): Boolean {
        val dx = x - enemy.x
        val dy = y - enemy.y
        val distance = Math.sqrt((dx * dx + dy * dy).toDouble())
        return distance < (radius + enemy.radius)
    }
}