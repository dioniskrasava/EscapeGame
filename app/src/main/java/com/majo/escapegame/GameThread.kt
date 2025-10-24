package com.majo.escapegame


import android.graphics.Canvas
import android.view.SurfaceHolder

class GameThread(
    private val surfaceHolder: SurfaceHolder,
    private val gameView: GameView
) : Thread() {

    var running = false

    override fun run() {
        while (running) {
            var canvas: Canvas? = null
            try {
                canvas = surfaceHolder.lockCanvas()
                if (canvas != null) {
                    synchronized(surfaceHolder) {
                        gameView.update()
                        gameView.drawGame(canvas)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                try {
                    canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            try {
                sleep(16) // ~60 FPS
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}