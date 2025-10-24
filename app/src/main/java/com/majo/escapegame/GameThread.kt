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
                canvas?.let {
                    synchronized(surfaceHolder) {
                        gameView.update()
                        gameView.drawGame(it) // Используем вспомогательный метод
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                canvas?.let { surfaceHolder.unlockCanvasAndPost(it) }
            }

            try {
                sleep(16) // ~60 FPS
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}