package com.example.hourglassaccelerometer

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
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
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.random.Random

class MainActivity : AppCompatActivity(), SensorEventListener {

    // Инициализация сенсорного менеджера и акселерометра
    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    // Списки для хранения иконок и их скоростей
    private val icons = mutableListOf<ImageView>()
    private val velocities = mutableListOf<Pair<Float, Float>>() // Пары (vx, vy) для каждой иконки

    // Константы для количества иконок, их размера и масштаба гравитации
    private val numIcons = 300
    private val scaleIcons = 20
    private val gravityScale = 1f

    // Переменные для хранения размеров экрана
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    // Переменные для обработки обновления физики
    private val handler = Handler(Looper.getMainLooper())
    private val delayMillis: Long = 16 // примерно 60 кадров в секунду

    // Переменные для определения области спавна
    private var spawnAreaStartX: Int = 0
    private var spawnAreaStartY: Int = 0
    private var spawnAreaWidth: Int = 0
    private var spawnAreaHeight: Int = 0

    // Объект для отображения песочных часов
    private lateinit var hourglassView: HourglassView

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
        val rootView = findViewById<FrameLayout>(R.id.root_view)
        for (i in 0 until numIcons) {
            val icon = ImageView(this)
            icon.setImageResource(R.drawable.ic_sand)
            icon.layoutParams = ViewGroup.LayoutParams(scaleIcons, scaleIcons)
            rootView.addView(icon)

            // Спавн только в центральной области
            do {
                icon.x = Random.nextInt(spawnAreaStartX, spawnAreaStartX + spawnAreaWidth - scaleIcons).toFloat()
                icon.y = Random.nextInt(spawnAreaStartY, spawnAreaStartY + spawnAreaHeight - scaleIcons).toFloat()
            } while (!isPointInsideHourglass(icon.x, icon.y))

            icons.add(icon)
            velocities.add(Pair(0f, 0f)) // начальные скорости (0, 0)
        }

        // Добавление визуализации области спавна
        hourglassView = HourglassView(this)
        rootView.addView(hourglassView)

        handler.post(runnable)
    }

    // Runnable для обновления физики с определенной задержкой
    private val runnable = object : Runnable {
        override fun run() {
            updatePhysics()
            hourglassView.invalidate() // Перерисовываем область
            handler.postDelayed(this, delayMillis)
        }
    }

    // Обновление физики для каждого кадра
    private fun updatePhysics() {
        for (i in icons.indices) {
            val icon = icons[i]
            val (vx, vy) = velocities[i]

            // Обновление координат на основе скоростей
            var newX = icon.x + vx
            var newY = icon.y + vy

            // Обработка столкновений с границами области песочных часов
            if (!isPointInsideHourglass(newX, newY)) {
                newX = icon.x - vx
                newY = icon.y - vy
                velocities[i] = Pair(-vx * 0.5f, -vy * 0.5f) // Уменьшаем скорость при столкновении с границей
            }

            icon.x = newX
            icon.y = newY
        }
    }

    // Проверка столкновений между иконками
    private fun checkCollision(icon1: ImageView, icon2: ImageView): Boolean {
        val dx = icon1.x - icon2.x
        val dy = icon1.y - icon2.y
        val distance = sqrt(dx * dx + dy * dy)
        return distance < (icon1.width + icon2.width) / 2
    }

    // Обработка столкновений между иконками
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

    // Обработка изменений сенсора
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

    // Обработка изменений точности сенсора
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Здесь можно обработать изменения точности сенсора
    }

    // Отмена регистрации слушателя сенсора при уничтожении активности
    override fun onDestroy() {
        super.onDestroy()
        sensorManager.unregisterListener(this)
        handler.removeCallbacks(runnable) // Остановка обновления физики
    }

    // Проверка, находится ли точка внутри песочных часов
    private fun isPointInsideHourglass(px: Float, py: Float): Boolean {
        val topTrapezoid = isPointInsideTrapezoid(px, py,
            spawnAreaStartX.toFloat(), spawnAreaStartY.toFloat(),
            spawnAreaStartX + spawnAreaWidth / 3f, spawnAreaStartY + spawnAreaHeight / 2f,
            spawnAreaStartX + 2 * spawnAreaWidth / 3f, spawnAreaStartY + spawnAreaHeight / 2f,
            spawnAreaStartX + spawnAreaWidth.toFloat(), spawnAreaStartY.toFloat()
        )

        val bottomTrapezoid = isPointInsideTrapezoid(px, py,
            spawnAreaStartX.toFloat(), spawnAreaStartY + spawnAreaHeight.toFloat(),
            spawnAreaStartX + spawnAreaWidth / 3f, spawnAreaStartY + spawnAreaHeight / 2f,
            spawnAreaStartX + 2 * spawnAreaWidth / 3f, spawnAreaStartY + spawnAreaHeight / 2f,
            spawnAreaStartX + spawnAreaWidth.toFloat(), spawnAreaStartY + spawnAreaHeight.toFloat()
        )

        return topTrapezoid || bottomTrapezoid
    }

    // Проверка, находится ли точка внутри трапеции
    private fun isPointInsideTrapezoid(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float, x3: Float, y3: Float, x4: Float, y4: Float): Boolean {
        val d1 = sign(px, py, x1, y1, x2, y2)
        val d2 = sign(px, py, x2, y2, x3, y3)
        val d3 = sign(px, py, x3, y3, x4, y4)
        val d4 = sign(px, py, x4, y4, x1, y1)

        val hasNeg = (d1 < 0) || (d2 < 0) || (d3 < 0) || (d4 < 0)
        val hasPos = (d1 > 0) || (d2 > 0) || (d3 > 0) || (d4 > 0)

        return !(hasNeg && hasPos)
    }

    // Вычисление знака для определения положения точки относительно линии
    private fun sign(px: Float, py: Float, x1: Float, y1: Float, x2: Float, y2: Float): Float {
        return (px - x2) * (y1 - y2) - (x1 - x2) * (py - y2)
    }

    // Класс для отображения области песочных часов
    inner class HourglassView(context: MainActivity) : View(context) {
        private val paint = Paint()
        private val path = Path()

        init {
            paint.color = 0x66FF0000 // Прозрачный красный для визуализации
            paint.style = Paint.Style.FILL
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            path.reset()
            // Верхняя трапеция
            path.moveTo(spawnAreaStartX.toFloat(), spawnAreaStartY.toFloat())
            path.lineTo((spawnAreaStartX + spawnAreaWidth / 3).toFloat(), (spawnAreaStartY + spawnAreaHeight / 2).toFloat())
            path.lineTo((spawnAreaStartX + 2 * spawnAreaWidth / 3).toFloat(), (spawnAreaStartY + spawnAreaHeight / 2).toFloat())
            path.lineTo((spawnAreaStartX + spawnAreaWidth).toFloat(), spawnAreaStartY.toFloat())
            path.close()

            // Нижняя трапеция
            path.moveTo(spawnAreaStartX.toFloat(), (spawnAreaStartY + spawnAreaHeight).toFloat())
            path.lineTo((spawnAreaStartX + spawnAreaWidth / 3).toFloat(), (spawnAreaStartY + spawnAreaHeight / 2).toFloat())
            path.lineTo((spawnAreaStartX + 2 * spawnAreaWidth / 3).toFloat(), (spawnAreaStartY + spawnAreaHeight / 2).toFloat())
            path.lineTo((spawnAreaStartX + spawnAreaWidth).toFloat(), (spawnAreaStartY + spawnAreaHeight).toFloat())
            path.close()

            canvas.drawPath(path, paint)
        }
    }
}
