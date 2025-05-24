package com.example.car_game_v1.logic

import android.util.Log
import com.example.car_game_v1.GameComponents.Obstacle
import com.example.car_game_v1.GameComponents.Player
import com.example.car_game_v1.ui.theme.UIManager
import com.example.car_game_v1.utilities.SignalManager
import kotlin.random.Random

class GameManager(
    private val player: Player,
    private val obstacles: MutableList<Obstacle>,
    private val matrixWidth: Int,
    private val matrixHeight: Int,
    private val uiManager: UIManager
) {
    private var score = 0
    private var isGameOver = false

    // Normal vs Fast intervals (ms)
    private val normalSpawn = 1000L
    private val fastSpawn   = 500L
    private val normalMove  = 500L
    private val fastMove    = 250L

    private var spawnInterval = normalSpawn
    private var moveInterval  = normalMove

    private var timeSinceLastSpawn = 0L
    private var timeSinceLastMove  = 0L

    /** Call from MainActivity to toggle spawn/move speed */
    fun setFastModeEnabled(enabled: Boolean) {
        spawnInterval = if (enabled) fastSpawn else normalSpawn
        moveInterval  = if (enabled) fastMove  else normalMove
        Log.d("GameManager", "FastMode=$enabled spawnInt=$spawnInterval moveInt=$moveInterval")
    }

    fun spawnObstacle(currentTime: Long) {
        if (isGameOver) return
        val delta = currentTime - timeSinceLastSpawn
        if (delta < spawnInterval) return

        val col = Random.nextInt(0, matrixWidth)
        obstacles.add(Obstacle(col, 0))
        Log.d("GameManager", "Spawned obstacle at col $col")
        timeSinceLastSpawn = currentTime
    }

    fun moveObstacles(currentTime: Long) {
        if (isGameOver) return
        val delta = currentTime - timeSinceLastMove
        if (delta < moveInterval) return

        val toRemove = mutableListOf<Obstacle>()
        for (obs in obstacles) {
            obs.moveDown()

            // Collision check: remove immediately, no score bump
            if (obs.column == player.position && obs.row == matrixHeight - 1) {
                toRemove.add(obs)
                handleCollision()
                continue
            }

            // Off-screen: score++ and remove
            if (obs.row >= matrixHeight) {
                toRemove.add(obs)
                score++
                uiManager.updateScore(score)
                Log.d("GameManager", "Off-screen obstacle. Score=$score")
            }
        }

        obstacles.removeAll(toRemove)
        timeSinceLastMove = currentTime
    }

    fun handleCollision() {
        if (uiManager.reduceLife()) {
            isGameOver = true
            uiManager.showGameOverScreen(score)
            Log.d("GameManager", "GameOver! Final score=$score")
        } else {
            SignalManager.getInstance().vibrate()
            Log.d("GameManager", "Collision! Lives left.")
        }
    }

    fun updateGameMatrix() {
        // Reset
        for (r in 0 until matrixHeight)
            for (c in 0 until matrixWidth)
                gameMatrix[r][c] = 0

        // Player
        gameMatrix[matrixHeight - 1][player.position] = 2

        // Obstacles
        for (obs in obstacles) {
            if (obs.row in 0 until matrixHeight) {
                gameMatrix[obs.row][obs.column] = 1
            }
        }
    }

    private val gameMatrix: Array<Array<Int>> =
        Array(matrixHeight) { Array(matrixWidth) { 0 } }

    fun getGameMatrix(): Array<Array<Int>> = gameMatrix
    fun isGameRunning() = !isGameOver
}
