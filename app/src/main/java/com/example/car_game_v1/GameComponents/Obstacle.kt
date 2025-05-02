package com.example.car_game_v1.GameComponents




class Obstacle(var column: Int, var row: Int) {
    // Move the obstacle down by one step (1 row per move)
    fun moveDown() {
        row++  // Move the obstacle down by 1 row
    }
}




