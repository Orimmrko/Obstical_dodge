package dev.tomco.a25b_11345a_l05.utilities

import android.content.Context
import android.content.Context.VIBRATOR_MANAGER_SERVICE
import android.content.Context.VIBRATOR_SERVICE
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.widget.Toast
import java.lang.ref.WeakReference

class SignalManager private constructor(context: Context) {
    private val contextRef = WeakReference(context)

    companion object {
        @Volatile
        private var instance: SignalManager? = null

        fun init(context: Context): SignalManager {
            return SignalManager.instance ?: synchronized(this) {
                SignalManager.instance
                    ?: SignalManager(context).also { this.instance = it }
            }
        }

        fun getInstance(): SignalManager {
            return instance ?: throw IllegalStateException(
                "SignalManager must be initialized by calling init(context) before use."
            )
        }
    }

    fun toast(text: String) {
        contextRef.get()?.let { context ->
            Toast
                .makeText(
                    context,
                    text,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    fun vibrate() {
        contextRef.get()?.let { context: Context ->
            val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager =
                    context.getSystemService(VIBRATOR_MANAGER_SERVICE) as VibratorManager
                vibratorManager.defaultVibrator
            } else {
                context.getSystemService(VIBRATOR_SERVICE) as Vibrator
            }

            vibrator?.let {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    // Single vibration for 500 milliseconds (can adjust duration if needed)
                    val oneShotVibrationEffect = VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE)

                    // Vibrate once with the specified duration
                    it.vibrate(oneShotVibrationEffect)
                } else {
                    // Fall back for older devices: vibrate for 500ms
                    it.vibrate(500)
                }
            }
        }
    }

}