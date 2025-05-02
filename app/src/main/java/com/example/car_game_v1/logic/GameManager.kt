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
    private val gameMatrix: Array<Array<Int>> =
        Array(matrixHeight) { Array(matrixWidth) { 0 } }  // 0=empty,1=obs,2=player

    // Spawn one obstacle every second by default
    private var timeSinceLastSpawn = 0L
    private val spawnInterval = 600L

    // 20% chance to spawn two obstacles at once
    private val doubleSpawnChance = 0.2f

    // Controls how fast obstacles fall
    private var timeSinceLastMove = 0L
    private val moveInterval = 300L

    fun spawnObstacle(currentTime: Long) {
        if (isGameOver) return

        val delta = currentTime - timeSinceLastSpawn
        if (delta < spawnInterval) return

        if (Random.nextFloat() < doubleSpawnChance && matrixWidth >= 2) {
            // double-spawn: choose two distinct columns
            val cols = (0 until matrixWidth).shuffled().take(2)
            for (c in cols) {
                obstacles.add(Obstacle(c, 0))
                Log.d("GameManager", "Double-spawn at col $c")
            }
        } else {
            // single spawn
            val c = Random.nextInt(0, matrixWidth)
            obstacles.add(Obstacle(c, 0))
            Log.d("GameManager", "Spawned obstacle at col $c")
        }

        timeSinceLastSpawn = currentTime
    }

    fun moveObstacles(currentTime: Long) {
        if (isGameOver) return

        // only move when enough time has passed
        val deltaMove = currentTime - timeSinceLastMove
        if (deltaMove < moveInterval) return

        val toRemove = mutableListOf<Obstacle>()
        for (obs in obstacles) {
            obs.moveDown()

            // 1) collision: remove immediately, no score bump
            if (obs.column == player.position && obs.row == matrixHeight - 1) {
                toRemove.add(obs)
                handleCollision()
                continue
            }

            // 2) went off-screen: score and remove
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
            Log.d("GameManager", "GameOver. Final score=$score")
        } else {
            SignalManager.getInstance().vibrate()
            Log.d("GameManager", "Collision! Lives left.")
        }
    }

    fun updateGameMatrix() {
        for (r in 0 until matrixHeight) {
            for (c in 0 until matrixWidth) {
                gameMatrix[r][c] = 0
            }
        }
        gameMatrix[matrixHeight - 1][player.position] = 2
        for (obs in obstacles) {
            if (obs.row in 0 until matrixHeight) {
                gameMatrix[obs.row][obs.column] = 1
            }
        }
    }

    fun getGameMatrix(): Array<Array<Int>> = gameMatrix
    fun isGameRunning() = !isGameOver
}
