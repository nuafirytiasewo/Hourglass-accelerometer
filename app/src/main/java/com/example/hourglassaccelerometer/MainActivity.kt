// MainActivity.kt
package com.example.hourglassaccelerometer

import android.animation.ObjectAnimator
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hourglassaccelerometer.R
import kotlin.random.Random

class MainActivity : AppCompatActivity() {

    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private val delayMillis: Long = 3000 // 3 секунды

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение размеров экрана
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        val screenWidth = displayMetrics.widthPixels
        val screenHeight = displayMetrics.heightPixels

        // Инициализация ImageView
        val icSand: ImageView = findViewById(R.id.ic_sand)

        handler = Handler(Looper.getMainLooper())
        runnable = object : Runnable {
            override fun run() {
                moveIconWithAnimation(icSand, screenWidth, screenHeight)
                handler.postDelayed(this, delayMillis)
            }
        }

        // Запуск авто-перемещения
        handler.post(runnable)
    }

    private fun moveIconWithAnimation(icSand: ImageView, screenWidth: Int, screenHeight: Int) {
        // Размеры иконки
        val iconWidth = icSand.width
        val iconHeight = icSand.height

        // Генерация случайных координат
        val randomX = Random.nextInt(0, screenWidth - iconWidth).toFloat()
        val randomY = Random.nextInt(0, screenHeight - iconHeight).toFloat()

        // Анимация перемещения по X
        val animatorX = ObjectAnimator.ofFloat(icSand, "x", icSand.x, randomX)
        animatorX.duration = 1000 // Продолжительность анимации 1 секунда

        // Анимация перемещения по Y
        val animatorY = ObjectAnimator.ofFloat(icSand, "y", icSand.y, randomY)
        animatorY.duration = 1000 // Продолжительность анимации 1 секунда

        // Запуск анимаций
        animatorX.start()
        animatorY.start()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Остановить авто-перемещение при уничтожении активности
    }
}
