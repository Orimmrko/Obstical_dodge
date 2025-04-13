package com.example.car_game_v1

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var gameArea: FrameLayout
    private lateinit var player: ImageView
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button

    private lateinit var columnX: List<Float>
    private var currentColumn = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameArea = findViewById(R.id.game_area)
        player = findViewById(R.id.player)
        buttonLeft = findViewById(R.id.button_left)
        buttonRight = findViewById(R.id.button_right)

        // Wait for layout to be measured
        player.post {
            val screenWidth = gameArea.width
            val carWidth = player.width

            // Center positions of 3 lanes, adjusted for car width
            val left = (screenWidth / 6f) - (carWidth / 2f)
            val center = (screenWidth / 2f) - (carWidth / 2f)
            val right = (5 * screenWidth / 6f) - (carWidth / 2f)

            columnX = listOf(left, center, right)
            Log.d("LANES", "Lane X positions: $columnX")

            // Start in center lane
            currentColumn = 1
            player.x = columnX[currentColumn]

            startSpawningObstacles()
        }

        buttonLeft.setOnClickListener {
            if (currentColumn > 0) {
                currentColumn--
                player.x = columnX[currentColumn]
            }
        }

        buttonRight.setOnClickListener {
            if (currentColumn < 2) {
                currentColumn++
                player.x = columnX[currentColumn]
            }
        }
    }

    private fun startSpawningObstacles() {
        val handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                spawnObstacle()
                handler.postDelayed(this, 2000)
            }
        })
    }

    private fun spawnObstacle() {
        if (!::columnX.isInitialized || columnX.size < 3) return

        val obstacleSize = 100
        val obstacle = ImageView(this)
        obstacle.setImageResource(android.R.drawable.ic_delete) // change to your obstacle image
        obstacle.layoutParams = FrameLayout.LayoutParams(obstacleSize, obstacleSize)

        // Choose a random lane
        val col = Random.nextInt(0, 3)

        // Correct X alignment: center obstacle to match car
        val x = columnX[col] + (player.width / 2f) - (obstacleSize / 2f)

        obstacle.x = x
        obstacle.y = 0f

        gameArea.addView(obstacle)

        val fall = ObjectAnimator.ofFloat(obstacle, "translationY", gameArea.height.toFloat())
        fall.duration = 3000
        fall.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                gameArea.removeView(obstacle)
            }
        })
        fall.start()
    }

}
