package com.example.car_game_v1


import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import androidx.appcompat.widget.AppCompatImageView
import android.view.View
import com.example.car_game_v1.logic.GameManager

class MainActivity : AppCompatActivity() {

    private lateinit var gameArea: FrameLayout
    private lateinit var player: ImageView
    private lateinit var buttonLeft: Button
    private lateinit var buttonRight: Button
    private lateinit var main_IMG_hearts: Array<AppCompatImageView> // Hearts for player lives
    private lateinit var scoreText: TextView // TextView for displaying score
    private lateinit var gameManager: GameManager

    // Matrix size (width and height)
    private val matrixWidth = 3  // 3 columns for the game area
    private val matrixHeight = 10 // Number of rows

    // Matrix representation (2D array)
    private var gameMatrix: Array<Array<Int>> = Array(matrixHeight) { Array(matrixWidth) { 0 } } // 0 = empty, 1 = obstacle

    private var carPosition = 1  // Start the car in the middle column (index 1)

    // Column width and height for each tile
    private var tileWidth = 0
    private var tileHeight = 0

    // Score variable
    private var score = 0

    // Flag to pause the game
    private var isGamePaused = false

    // Handler for obstacle spawning and movement
    private lateinit var handler: Handler

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViews()  // Initialize the views

        // Initialize the GameManager with the number of hearts
        gameManager = GameManager(main_IMG_hearts.size)

        // Set up the matrix (empty the game area)
        initializeGameMatrix()

        // Wait for layout to be measured (post method used to access layout measurements)
        player.post {
            // Calculate the tile width based on the screen width divided by 3 columns
            tileWidth = gameArea.width / matrixWidth
            tileHeight = gameArea.height / matrixHeight

            // Place the car in the bottom row (matrixHeight - 1) and middle column (carPosition)
            gameMatrix[matrixHeight - 1][carPosition] = 2  // Place the car in the bottom row
            updateGameView()

            startSpawningObstacles()
        }

        // Button listeners for moving the player left and right
        buttonLeft.setOnClickListener {
            if (carPosition > 0 && !isGamePaused) {
                moveCarLeft()
            }
        }

        buttonRight.setOnClickListener {
            if (carPosition < matrixWidth - 1 && !isGamePaused) {
                moveCarRight()
            }
        }
    }

    private fun findViews() {
        gameArea = findViewById(R.id.game_area)
        player = findViewById(R.id.player)
        buttonLeft = findViewById(R.id.button_left)
        buttonRight = findViewById(R.id.button_right)
        main_IMG_hearts = arrayOf(
            findViewById(R.id.main_IMG_heart0),
            findViewById(R.id.main_IMG_heart1),
            findViewById(R.id.main_IMG_heart2)
        )
        scoreText = findViewById(R.id.score_text) // TextView for score
    }

    private fun initializeGameMatrix() {
        // Initialize the game matrix to be empty (0 represents empty, 1 represents obstacle)
        for (row in gameMatrix) {
            row.fill(0)
        }
    }

    private fun startSpawningObstacles() {
        if (isGamePaused) return // Do nothing if the game is paused

        handler = Handler(Looper.getMainLooper())
        handler.post(object : Runnable {
            override fun run() {
                spawnObstacle() // Spawn a new obstacle
                moveObstaclesDown() // Move all obstacles down
                updateGameView() // Redraw the game area
                handler.postDelayed(this, 500) // Adjust the delay for faster falling
            }
        })
    }

    private fun spawnObstacle() {
        val randomColumn = Random.nextInt(0, matrixWidth) // Random column in the 3-column grid

        // Place the new obstacle at the top-most row (row 0) in the random column
        gameMatrix[0][randomColumn] = 1
    }

    private fun moveObstaclesDown() {
        var scoreIncremented = false

        // Move each row's obstacles down by one row
        for (row in matrixHeight - 1 downTo 1) {
            for (col in 0 until matrixWidth) {
                gameMatrix[row][col] = gameMatrix[row - 1][col] // Move obstacle down
                gameMatrix[row - 1][col] = 0 // Clear the row above
            }
        }

        // Check for obstacles passing through the bottom row (car's row) without hitting the car
        for (col in 0 until matrixWidth) {
            if (gameMatrix[matrixHeight - 1][col] == 1) {
                // Obstacle has reached the car's row
                if (col != carPosition) {
                    score++  // Increment score when an obstacle is avoided
                    scoreIncremented = true
                    updateScore()  // Update the score display
                } else {
                    // Collision detected
                    gameManager.reduceLife()  // Reduce life when the obstacle hits the car
                    updateHearts()  // Update hearts after collision
                    gameMatrix[matrixHeight - 1][col] = 0 // Remove the obstacle after collision

                    // If the game is over and no lives are left, stop the game and show Game Over screen
                    if (gameManager.lives == 0 && !isGamePaused) {
                        isGamePaused = true
                        showGameOverScreen() // Show the end screen when lives are exhausted
                    }
                }
            }
        }

        // If the game is over and no lives are left, stop the game and show Game Over screen
        if (gameManager.lives == 0 && !isGamePaused) {
            isGamePaused = true
            showGameOverScreen() // Show the end screen when lives are exhausted
        }
    }


    private fun moveCarLeft() {
        // Clear the current car position in the game matrix
        gameMatrix[matrixHeight - 1][carPosition] = 0
        carPosition--  // Move the car left
        gameMatrix[matrixHeight - 1][carPosition] = 2  // Place the car in the new position
        updateGameView()  // Redraw the game matrix
    }

    private fun moveCarRight() {
        // Clear the current car position in the game matrix
        gameMatrix[matrixHeight - 1][carPosition] = 0
        carPosition++  // Move the car right
        gameMatrix[matrixHeight - 1][carPosition] = 2  // Place the car in the new position
        updateGameView()  // Redraw the game matrix
    }

    private fun updateGameView() {
        // Clear the previous views from the game area to avoid overlaps
        gameArea.removeAllViews()

        // Loop through the game matrix and update the UI based on the matrix
        for (row in 0 until matrixHeight) {
            for (col in 0 until matrixWidth) {
                val tile = gameMatrix[row][col]
                val x = col * tileWidth // Adjust x for the tile spacing
                val y = row * tileHeight // Adjust y for the tile spacing

                when (tile) {
                    1 -> { // Draw an obstacle
                        val obstacle = ImageView(this)
                        obstacle.setImageResource(android.R.drawable.ic_delete) // Obstacle image
                        obstacle.layoutParams = FrameLayout.LayoutParams(tileWidth, tileHeight)
                        obstacle.x = x.toFloat()
                        obstacle.y = y.toFloat()
                        gameArea.addView(obstacle)
                    }
                    2 -> { // Draw the car
                        player.x = (col * tileWidth).toFloat()
                        player.y = (row * tileHeight).toFloat()
                        // Ensure car visibility
                        player.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun updateHearts() {
        val heartsVisibility = gameManager.getHeartsVisibility()
        for (i in 0 until 3) {
            if (heartsVisibility[i]) {
                main_IMG_hearts[i].visibility = View.VISIBLE // Make the heart visible
            } else {
                main_IMG_hearts[i].visibility = View.INVISIBLE // Make the heart invisible
            }
        }
    }


    @SuppressLint("SetTextI18n")
    private fun updateScore() {
        scoreText.text = "Score: $score" // Update the score TextView
    }

    private fun showGameOverScreen() {
        // Pause the game when Game Over screen is shown
        isGamePaused = true

        // Transition to the GameOverActivity and pass the score
        val intent = Intent(this, GameOverActivity::class.java)
        intent.putExtra("SCORE", score)  // Pass the score to the Game Over screen
        startActivity(intent)
        finish() // Finish MainActivity so the user can't go back to the game screen
    }




}
