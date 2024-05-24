// MainActivity.kt
package com.example.hourglassaccelerometer

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
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
                moveIconRandomly(icSand, screenWidth, screenHeight)
                handler.postDelayed(this, delayMillis)
            }
        }

        // Запуск авто-перемещения
        handler.post(runnable)
    }

    private fun moveIconRandomly(icSand: ImageView, screenWidth: Int, screenHeight: Int) {
        // Размеры иконки
        val iconWidth = icSand.width
        val iconHeight = icSand.height

        // Генерация случайных координат
        val randomX = Random.nextInt(0, screenWidth - iconWidth).toFloat()
        val randomY = Random.nextInt(0, screenHeight - iconHeight).toFloat()

        // Установка новых координат для иконки
        icSand.x = randomX
        icSand.y = randomY
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable) // Остановить авто-перемещение при уничтожении активности
    }
}
