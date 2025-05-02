package com.example.car_game_v1.utilities

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import java.lang.ref.WeakReference


class SignalManager private constructor(context: Context) {
    // always updated on init(...)
    private var contextRef = WeakReference(context)

    companion object {
        @Volatile
        private var instance: SignalManager? = null

        /** Always re-create with fresh context so we can vibrate after a restart. */
        fun init(context: Context): SignalManager = synchronized(this) {
            SignalManager(context).also { instance = it }
        }

        fun getInstance(): SignalManager = instance
            ?: throw IllegalStateException(
                "SignalManager must be initialized by calling init(context) before use."
            )
    }

    fun toast(text: String) {
        contextRef.get()?.let { ctx ->
            Toast.makeText(ctx, text, Toast.LENGTH_SHORT).show()
        }
    }

    fun vibrate() {
        contextRef.get()?.let { ctx ->
            val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                (ctx.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager)
                    .defaultVibrator
            } else {
                ctx.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val pattern = longArrayOf(0, 200)
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                vibrator.vibrate(200)
            }
        }
    }
}