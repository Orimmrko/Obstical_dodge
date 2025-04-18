package com.example.car_game_v1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class GameOverActivity : AppCompatActivity() {

    private lateinit var scoreText: TextView
    private lateinit var restartButton: Button

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_game_over)

        scoreText = findViewById(R.id.score_text)
        restartButton = findViewById(R.id.restart_button)

        // Get the score from the previous activity
        val score = intent.getIntExtra("SCORE", 0)
        scoreText.text = "Your Score: $score"

        // Restart the game when the button is clicked
        restartButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Finish GameOverActivity to return to the main game
        }
    }
}
