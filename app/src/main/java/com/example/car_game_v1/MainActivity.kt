package com.example.car_game_v1

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.car_game_v1.GameComponents.Obstacle
import com.example.car_game_v1.GameComponents.Player
import com.example.car_game_v1.logic.GameManager
import com.example.car_game_v1.ui.theme.UIManager
import com.example.car_game_v1.utilities.SignalManager

class MainActivity : AppCompatActivity() {

    private lateinit var player: Player
    private lateinit var obstacles: MutableList<Obstacle>
    private lateinit var gameManager: GameManager
    private lateinit var uiManager: UIManager

    private lateinit var gameArea: FrameLayout
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button
    private lateinit var scoreText: TextView
    private lateinit var main_IMG_hearts: Array<ImageView>
    private lateinit var playerImageView: ImageView

    private val matrixWidth = 3
    private val matrixHeight = 10

    private lateinit var handler: Handler
    private var tileWidth = 0
    private var tileHeight = 0

    private val FPS = 60                     // target frames per second
    private val frameTime = 1000 / FPS      // ms per frame
    private var lastUpdateTime = System.currentTimeMillis()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize the SignalManager from the correct package
        SignalManager.init(this)

        findViews()
        initGame()

        gameArea.post {
            tileWidth = gameArea.width / matrixWidth
            tileHeight = gameArea.height / matrixHeight
            playerImageView.x = (tileWidth * player.position).toFloat()
            playerImageView.y = (tileHeight * (matrixHeight - 1)).toFloat()
        }

        buttonLeft.setOnClickListener {
            if (gameManager.isGameRunning()) player.moveLeft()
        }
        buttonRight.setOnClickListener {
            if (gameManager.isGameRunning()) player.moveRight()
        }

        startGameLoop()
    }


    private fun findViews() {
        gameArea = findViewById(R.id.game_area)
        buttonLeft = findViewById(R.id.button_left)
        buttonRight = findViewById(R.id.button_right)
        scoreText = findViewById(R.id.score_text)
        main_IMG_hearts = arrayOf(
            findViewById(R.id.main_IMG_heart0),
            findViewById(R.id.main_IMG_heart1),
            findViewById(R.id.main_IMG_heart2)
        )
        playerImageView = findViewById(R.id.player)
    }

    private fun initGame() {
        player = Player(1)
        obstacles = mutableListOf()
        uiManager = UIManager(scoreText, main_IMG_hearts, this)
        gameManager = GameManager(player, obstacles, matrixWidth, matrixHeight, uiManager)
        handler = Handler(Looper.getMainLooper())
    }

    private fun startGameLoop() {
        Log.d("GameLoop", "Game loop started")
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastUpdateTime

                if (deltaTime >= frameTime) {
                    Log.d("GameLoop", "Game state updating")

                    gameManager.spawnObstacle(currentTime)
                    gameManager.moveObstacles(currentTime)
                    gameManager.updateGameMatrix()

                    // **If the game just ended, stop the loop and return immediately**
                    if (!gameManager.isGameRunning()) {
                        handler.removeCallbacksAndMessages(null)
                        Log.d("GameLoop", "Game over – loop stopped")
                        return
                    }

                    updateGameView()
                    lastUpdateTime = currentTime
                }

                handler.postDelayed(this, frameTime.toLong())
            }
        })
    }

    private fun updateGameView() {
        gameArea.removeAllViews()

        val matrix = gameManager.getGameMatrix()
        for (row in 0 until matrixHeight) {
            for (col in 0 until matrixWidth) {
                val tile = matrix[row][col]
                val x = (col * tileWidth).toFloat()
                val y = (row * tileHeight).toFloat()

                when (tile) {
                    1 -> { // obstacle
                        val obstacleView = ImageView(this)
                        obstacleView.setImageResource(R.drawable.barrier)
                        obstacleView.layoutParams = FrameLayout.LayoutParams(tileWidth, tileHeight)
                        obstacleView.x = x
                        obstacleView.y = y
                        gameArea.addView(obstacleView)
                    }
                    2 -> { // player
                        playerImageView.x = x
                        playerImageView.y = y
                        playerImageView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        Log.d("GameLifecycle", "Game Paused")
    }

    override fun onResume() {
        super.onResume()
        Log.d("GameLifecycle", "Game Resumed")
        if (gameManager.isGameRunning()) {
            lastUpdateTime = System.currentTimeMillis()
            startGameLoop()
        }
    }

    override fun onStop() {
        super.onStop()
        Log.d("GameLifecycle", "Game Stopped")
    }
}
