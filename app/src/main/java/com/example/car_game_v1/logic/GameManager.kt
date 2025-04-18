package com.example.car_game_v1.logic

class GameManager(private val totalHearts: Int) {

    var lives: Int = totalHearts
    private var heartsVisibility: Array<Boolean> = Array(totalHearts) { true }  // Initialize hearts as visible

    // This method reduces a life and hides the corresponding heart
    fun reduceLife() {
        if (lives > 0) {
            lives--  // Decrease the number of lives
            heartsVisibility[lives] = false  // Mark the last heart as invisible
        }
    }

    // This method returns the current visibility status of all hearts
    fun getHeartsVisibility(): Array<Boolean> {
        return heartsVisibility
    }

}
