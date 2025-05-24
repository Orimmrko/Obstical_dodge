package com.example.car_game_v1

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Switch
import androidx.appcompat.app.AppCompatActivity

class MenuActivity : AppCompatActivity() {
    @SuppressLint("UseSwitchCompatOrMaterialCode")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_menu)

        // Speed toggle
        val speedSwitch = findViewById<Switch>(R.id.switch_speed)

        // Buttons mode
        findViewById<Button>(R.id.btn_use_buttons).setOnClickListener {
            startGame(twoButton = true, fastMode = speedSwitch.isChecked)
        }

        // Sensor mode
        findViewById<Button>(R.id.btn_use_sensors).setOnClickListener {
            startGame(twoButton = false, fastMode = speedSwitch.isChecked)
        }
    }

    private fun startGame(twoButton: Boolean, fastMode: Boolean) {
        Intent(this, MainActivity::class.java).also { intent ->
            intent.putExtra("EXTRA_TWO_BUTTON", twoButton)
            intent.putExtra("EXTRA_FAST_MODE", fastMode)
            startActivity(intent)
            finish()
        }
    }
}
