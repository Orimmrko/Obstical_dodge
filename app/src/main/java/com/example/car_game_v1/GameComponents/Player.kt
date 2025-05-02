package com.example.car_game_v1.GameComponents

class Player(var position: Int) {
    // Assuming the player is always on the last row (matrixHeight - 1)


    fun moveLeft() {
        if (position > 0) {
            position--
        }
    }

    fun moveRight() {
        if (position < 2) {
            position++
        }
    }

}
