package com.majo.escapegame

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var gameThread: GameThread
    private val player: Player
    private val enemies = mutableListOf<Enemy>()
    private var score = 0
    private val paint = Paint()
    private val random = java.util.Random()
    private var lastEnemyTime = 0L
    private val enemyInterval = 1000L

    init {
        holder.addCallback(this)
        player = Player(0f, 0f) // временные координаты
        gameThread = GameThread(holder, this)
        isFocusable = true

        paint.color = Color.WHITE
        paint.textSize = 50f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        // Инициализируем игрока после того, как размеры View известны
        player.x = w / 2f
        player.y = h / 2f
        player.targetX = w / 2f
        player.targetY = h / 2f
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        gameThread.running = true
        gameThread.start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        var retry = true
        gameThread.running = false
        while (retry) {
            try {
                gameThread.join()
                retry = false
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun update() {
        if (width == 0 || height == 0) return

        player.update()

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEnemyTime > enemyInterval) {
            enemies.add(Enemy(random.nextInt(width).toFloat(), -50f))
            lastEnemyTime = currentTime
        }

        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.update()

            if (player.collidesWith(enemy)) {
                gameOver()
                return
            }

            if (enemy.y > height) {
                iterator.remove()
                score++
            }
        }
    }

    // Исправленный метод draw
    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        canvas.drawColor(Color.BLACK)
        player.draw(canvas)
        enemies.forEach { enemy -> enemy.draw(canvas) }
        canvas.drawText("Score: $score", 50f, 80f, paint)
    }

    // Вспомогательный метод для отрисовки в GameThread
    fun drawGame(canvas: Canvas) {
        draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                player.targetX = event.x
                player.targetY = event.y
            }
        }
        return true
    }

    private fun gameOver() {
        gameThread.running = false
        val canvas = holder.lockCanvas()
        try {
            canvas?.let {
                it.drawColor(Color.BLACK)
                paint.color = Color.RED
                paint.textAlign = Paint.Align.CENTER
                it.drawText("GAME OVER", width / 2f, height / 2f, paint)
                it.drawText("Score: $score", width / 2f, height / 2f + 80f, paint)
                paint.textAlign = Paint.Align.LEFT
                paint.color = Color.WHITE
            }
        } finally {
            canvas?.let { holder.unlockCanvasAndPost(it) }
        }
    }

    fun pause() {
        gameThread.running = false
    }

    fun resume() {
        if (!gameThread.isAlive) {
            gameThread = GameThread(holder, this)
            gameThread.running = true
            gameThread.start()
        }
    }
}