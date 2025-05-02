package com.example.car_game_v1.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.car_game_v1.GameOverActivity

class UIManager(
    private val scoreText: TextView,
    private val hearts: Array<ImageView>,
    private val context: Context
) {
    private var lives = hearts.size

    @SuppressLint("SetTextI18n")
    fun updateScore(score: Int) {
        scoreText.text = "Score: $score"
    }

    /**
     * Reduce a life. Returns true if that was the last life (game over).
     */
    fun reduceLife(): Boolean {
        lives--
        updateHearts()
        return lives <= 0
    }

    private fun updateHearts() {
        for (i in hearts.indices) {
            hearts[i].visibility = if (i < lives) View.VISIBLE else View.INVISIBLE
        }
    }

    /**
     * Launches the GameOverActivity (passing the final score)
     * and finishes the current activity.
     */
    fun showGameOverScreen(score: Int) {
        val intent = Intent(context, GameOverActivity::class.java).apply {
            putExtra("SCORE", score)
        }
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }
}
