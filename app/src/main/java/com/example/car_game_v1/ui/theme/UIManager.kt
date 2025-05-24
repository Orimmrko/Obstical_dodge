package com.example.car_game_v1.ui.theme

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.example.car_game_v1.MenuActivity

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
     * When the game ends, go straight back to the menu.
     */
    fun showGameOverScreen(score: Int) {
        val intent = Intent(context, MenuActivity::class.java).apply {
            putExtra("EXTRA_RETURNED_SCORE", score)
        }
        context.startActivity(intent)
        if (context is Activity) {
            context.finish()
        }
    }
}
