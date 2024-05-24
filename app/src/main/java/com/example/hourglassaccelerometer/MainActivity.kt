// MainActivity.kt
package com.example.hourglassaccelerometer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.DisplayMetrics
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random
import kotlin.math.abs
import kotlin.math.sqrt

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private val icons = mutableListOf<ImageView>()
    private val velocities = mutableListOf<Pair<Float, Float>>() // Пары (vx, vy) для каждой иконки
    private val numIcons = 300
    private val scaleIcons = 20
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0
    private val gravityScale = 5f
    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis: Long = 16 // примерно 60 кадров в секунду
    private var spawnAreaStartX: Int = 0
    private var spawnAreaStartY: Int = 0
    private var spawnAreaWidth: Int = 0
    private var spawnAreaHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение размеров экрана
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        // Определение области спавна
        spawnAreaWidth = screenWidth / 2
        spawnAreaHeight = screenHeight / 2
        spawnAreaStartX = (screenWidth - spawnAreaWidth) / 2
        spawnAreaStartY = (screenHeight - spawnAreaHeight) / 2

        // Инициализация датчика акселерометра
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
            ?: throw IllegalStateException("No accelerometer sensor found")
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_GAME)

        // Создание иконок только в области спавна
        val rootView = findViewById<View>(android.R.id.content) as ViewGroup
        for (i in 0 until numIcons) {
            val icon = ImageView(this)
            icon.setImageResource(R.drawable.ic_sand)
            icon.layoutParams = ViewGroup.LayoutParams(scaleIcons, scaleIcons)
            rootView.addView(icon)

            // Спавн только в центральной области
            icon.x = Random.nextInt(spawnAreaStartX, spawnAreaStartX + spawnAreaWidth - scaleIcons).toFloat()
            icon.y = Random.nextInt(spawnAreaStartY, spawnAreaStartY + spawnAreaHeight - scaleIcons).toFloat()

            icons.add(icon)
            velocities.add(Pair(0f, 0f)) // начальные скорости (0, 0)
        }

        handler.post(runnable)
    }

    private val runnable = object : Runnable {
        override fun run() {
            updatePhysics()
            handler.postDelayed(this, delayMillis)
        }
    }

    private fun updatePhysics() {
        for (i in icons.indices) {
            val icon = icons[i]
            val (vx, vy) = velocities[i]

            // Обновление координат на основе скоростей
            var newX = icon.x + vx
            var newY = icon.y + vy

            // Обработка столкновений с границами экрана
            if (newX <= spawnAreaStartX) {
                newX = spawnAreaStartX.toFloat()
                velocities[i] = Pair(-vx, vy)
            } else if (newX >= spawnAreaStartX + spawnAreaWidth - scaleIcons) {
                newX = (spawnAreaStartX + spawnAreaWidth - scaleIcons).toFloat()
                velocities[i] = Pair(-vx, vy)
            }
            if (newY <= spawnAreaStartY) {
                newY = spawnAreaStartY.toFloat()
                velocities[i] = Pair(vx, -vy)
            } else if (newY >= spawnAreaStartY + spawnAreaHeight - scaleIcons) {
                newY = (spawnAreaStartY + spawnAreaHeight - scaleIcons).toFloat()
                velocities[i] = Pair(vx, -vy)
            }

            icon.x = newX
            icon.y = newY

            // Обработка столкновений между иконками
            for (j in i + 1 until icons.size) {
                val otherIcon = icons[j]
                if (checkCollision(icon, otherIcon)) {
                    resolveCollision(i, j)
                }
            }
        }
    }

    private fun checkCollision(icon1: ImageView, icon2: ImageView): Boolean {
        val dx = icon1.x - icon2.x
        val dy = icon1.y - icon2.y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < (icon1.width + icon2.width) / 2
    }

    private val bounceFactor = 0.1f // Уменьшаем коэффициент отскока

    private fun resolveCollision(i: Int, j: Int) {
        val icon1 = icons[i]
        val icon2 = icons[j]

        // Разделение иконок
        val overlapX = (icon1.width + icon2.width) / 2 - abs(icon1.x - icon2.x)
        val overlapY = (icon1.height + icon2.height) / 2 - abs(icon1.y - icon2.y)
        if (icon1.x < icon2.x) {
            icon1.x -= overlapX / 2
            icon2.x += overlapX / 2
        } else {
            icon1.x += overlapX / 2
            icon2.x -= overlapX / 2
        }
        if (icon1.y < icon2.y) {
            icon1.y -= overlapY / 2
            icon2.y += overlapY / 2
        } else {
            icon1.y += overlapY / 2
            icon2.y -= overlapY / 2
        }

        // Обновление скоростей с учетом коэффициента отскока
        val (vx1, vy1) = velocities[i]
        val (vx2, vy2) = velocities[j]
        velocities[i] = Pair(vx2 * bounceFactor, vy2 * bounceFactor)
        velocities[j] = Pair(vx1 * bounceFactor, vy1 * bounceFactor)
    }


    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Обработка данных акселерометра
            val x = event.values[0]
            val y = event.values[1]

            // Обновление скоростей иконок в зависимости от значений акселерометра
            for (i in velocities.indices) {
                val (vx, vy) = velocities[i]
                velocities[i] = Pair(vx - x * gravityScale, vy + y * gravityScale)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Здесь можно обработать изменения точности сенсора
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации слушателя датчика
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(runnable) // Остановка обновления физики
    }
}
