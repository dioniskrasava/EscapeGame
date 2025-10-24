package com.majo.escapegame


import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import android.view.SurfaceView
import java.util.*

class GameView(context: Context) : SurfaceView(context), SurfaceHolder.Callback {

    private lateinit var gameThread: GameThread
    private var player: Player? = null
    private val enemies = mutableListOf<Enemy>()
    private var score = 0
    private val paint = Paint()
    private val random = Random()
    private var lastEnemyTime = 0L
    private val enemyInterval = 1000L
    private var gameOver = false

    companion object {
        private const val TAG = "GameView"
    }

    init {
        holder.addCallback(this)
        isFocusable = true

        paint.color = Color.WHITE
        paint.textSize = 50f
        paint.style = Paint.Style.FILL

        Log.d(TAG, "GameView initialized")
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        Log.d(TAG, "onSizeChanged: $w x $h")

        // Инициализируем игрока после того, как размеры View известны
        player = Player(w / 2f, h / 2f)
        player!!.targetX = w / 2f
        player!!.targetY = h / 2f

        // Создаем игровой поток после инициализации игрока
        if (!this::gameThread.isInitialized) {
            gameThread = GameThread(holder, this)
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceCreated")
        if (this::gameThread.isInitialized) {
            gameThread.running = true
            gameThread.start()
        }
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        Log.d(TAG, "surfaceChanged: $width x $height")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        Log.d(TAG, "surfaceDestroyed")
        if (this::gameThread.isInitialized) {
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
    }

    fun update() {
        if (width == 0 || height == 0 || player == null) {
            Log.d(TAG, "update skipped - not ready")
            return
        }

        player!!.update()

        val currentTime = System.currentTimeMillis()
        if (currentTime - lastEnemyTime > enemyInterval) {
            val enemyX = random.nextInt(width - 100) + 50f // чтобы не спавнить у краев
            enemies.add(Enemy(enemyX, -50f))
            lastEnemyTime = currentTime
            Log.d(TAG, "New enemy created at $enemyX")
        }

        val iterator = enemies.iterator()
        while (iterator.hasNext()) {
            val enemy = iterator.next()
            enemy.update()

            if (player!!.collidesWith(enemy)) {
                Log.d(TAG, "Collision detected!")
                gameOver = true
                return
            }

            if (enemy.y > height) {
                iterator.remove()
                score++
                Log.d(TAG, "Score: $score")
            }
        }
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)

        // Очищаем экран
        canvas.drawColor(Color.BLACK)

        // Рисуем игрока если он инициализирован
        player?.let {
            it.draw(canvas)

            // Рисуем врагов
            enemies.forEach { enemy -> enemy.draw(canvas) }

            // Рисуем счет
            canvas.drawText("Score: $score", 50f, 80f, paint)

            // Если игра окончена
            if (gameOver) {
                val gameOverPaint = Paint().apply {
                    color = Color.RED
                    textSize = 80f
                    textAlign = Paint.Align.CENTER
                }
                canvas.drawText("GAME OVER", width / 2f, height / 2f, gameOverPaint)
                canvas.drawText("Score: $score", width / 2f, height / 2f + 100f, paint)
            }
        } ?: run {
            // Если игрок еще не инициализирован
            val debugPaint = Paint().apply {
                color = Color.YELLOW
                textSize = 40f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Initializing...", width / 2f, height / 2f, debugPaint)
            canvas.drawText("Size: ${width}x${height}", width / 2f, height / 2f + 60f, debugPaint)
        }
    }

    // Вспомогательный метод для отрисовки в GameThread
    fun drawGame(canvas: Canvas) {
        draw(canvas)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_DOWN -> {
                player?.let {
                    it.targetX = event.x
                    it.targetY = event.y
                    Log.d(TAG, "Touch at: ${event.x}, ${event.y}")
                }
            }
        }
        return true
    }

    fun pause() {
        if (this::gameThread.isInitialized) {
            gameThread.running = false
        }
    }

    fun resume() {
        if (this::gameThread.isInitialized && !gameThread.isAlive) {
            gameThread = GameThread(holder, this)
            gameThread.running = true
            gameThread.start()
        }
    }
}