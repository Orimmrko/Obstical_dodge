package com.example.car_game_v1


import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
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


class MainActivity : AppCompatActivity(), SensorEventListener {

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

    private val FPS = 30
    private val frameTime = 1000 / FPS
    private var lastUpdateTime = System.currentTimeMillis()

    // Sensor fields
    private var sensorManager: SensorManager? = null
    private var accelerometer: Sensor? = null
    private var sensorMode = false
    private var lastTiltTime = 0L
    private val tiltInterval = 200L         // ms between moves
    private val tiltThreshold = 2.0f        // adjust sensitivity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 1) init vibrator
        SignalManager.init(this)

        // 2) read menu options
        val twoButton = intent.getBooleanExtra("EXTRA_TWO_BUTTON", true)
        val fastMode  = intent.getBooleanExtra("EXTRA_FAST_MODE", false)
        sensorMode = !twoButton

        // 3) find views & init game
        findViews()
        initGame()

        // 4) UI mode config
        buttonLeft.visibility  = if (twoButton) View.VISIBLE else View.GONE
        buttonRight.visibility = if (twoButton) View.VISIBLE else View.GONE
        gameManager.setFastModeEnabled(fastMode)

        // 5) sensor setup if needed
        if (sensorMode) {
            sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
            accelerometer = sensorManager
                ?.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        }

        // 6) measure tiles & initial player place
        gameArea.post {
            tileWidth = gameArea.width / matrixWidth
            tileHeight = gameArea.height / matrixHeight
            playerImageView.x = (tileWidth * player.position).toFloat()
            playerImageView.y = (tileHeight * (matrixHeight - 1)).toFloat()
        }

        // 7) button controls
        buttonLeft.setOnClickListener {
            if (gameManager.isGameRunning()) {
                player.moveLeft()
            }
        }
        buttonRight.setOnClickListener {
            if (gameManager.isGameRunning()) {
                player.moveRight()
            }
        }

        // 8) start game loop
        startGameLoop()
    }

    override fun onResume() {
        super.onResume()
        // register sensor if in sensor mode
        if (sensorMode) {
            accelerometer?.let {
                sensorManager?.registerListener(
                    this, it, SensorManager.SENSOR_DELAY_GAME
                )
            }
        }
        lastUpdateTime = System.currentTimeMillis()
        startGameLoop()
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacksAndMessages(null)
        if (sensorMode) {
            sensorManager?.unregisterListener(this)
        }
    }

    override fun onStop() {
        super.onStop()
        handler.removeCallbacksAndMessages(null)
    }

    // SensorEventListener
    override fun onSensorChanged(event: SensorEvent) {
        val x = event.values[0]    // left/right tilt
        val now = System.currentTimeMillis()
        if (now - lastTiltTime < tiltInterval) return

        if (x > tiltThreshold && player.position > 0) {
            // tilt left → move car left
            player.moveLeft()
            lastTiltTime = now
        } else if (x < -tiltThreshold && player.position < matrixWidth - 1) {
            // tilt right → move car right
            player.moveRight()
            lastTiltTime = now
        }
    }
    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) { /* no-op */}

    // --------------------
    // GAME LOOP & RENDERING
    // --------------------
    private fun startGameLoop() {
        handler = Handler(Looper.getMainLooper())
        Log.d("GameLoop", "Game loop started")
        handler.post(object : Runnable {
            override fun run() {
                val currentTime = System.currentTimeMillis()
                val deltaTime = currentTime - lastUpdateTime

                if (deltaTime >= frameTime) {
                    gameManager.spawnObstacle(currentTime)
                    gameManager.moveObstacles(currentTime)
                    gameManager.updateGameMatrix()

                    if (!gameManager.isGameRunning()) {
                        handler.removeCallbacksAndMessages(null)
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
        for (r in 0 until matrixHeight) {
            for (c in 0 until matrixWidth) {
                val tile = matrix[r][c]
                val x = (c * tileWidth).toFloat()
                val y = (r * tileHeight).toFloat()
                when (tile) {
                    1 -> {
                        val iv = ImageView(this)
                        iv.setImageResource(R.drawable.barrier)
                        iv.layoutParams = FrameLayout.LayoutParams(tileWidth, tileHeight)
                        iv.x = x; iv.y = y
                        gameArea.addView(iv)
                    }
                    2 -> {
                        playerImageView.x = x
                        playerImageView.y = y
                        playerImageView.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    // findViews & initGame unchanged...
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
    }
}
