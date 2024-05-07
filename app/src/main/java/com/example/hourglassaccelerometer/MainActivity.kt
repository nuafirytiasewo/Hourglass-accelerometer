package com.example.hourglassaccelerometer

import android.content.res.Resources
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var containerLayout: ViewGroup

    private val sandList = mutableListOf<ImageView>()

    private var lastTime: Long = 0
    private var deltaTime: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        containerLayout = findViewById(R.id.containerLayout)

        // Получаем менеджер датчиков и датчик акселерометра
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        // Создаем и добавляем песчинки на экран
        createSand()
    }

    private fun createSand() {
        val sandCount = 100 // Количество песчинок
        val sandWidth = 50 // Ширина песчинки в пикселях
        val sandHeight = 50 // Высота песчинки в пикселях

        for (i in 0 until sandCount) {
            val sandImageView = ImageView(this)
            sandImageView.setImageResource(R.drawable.ic_sand)
            sandImageView.layoutParams = ViewGroup.LayoutParams(sandWidth, sandHeight)

            // Генерируем случайные координаты для размещения песчинки в пределах экрана
            val randomX = (0 until screenWidth - sandWidth).random()
            val randomY = (0 until screenHeight - sandHeight).random()

            // Устанавливаем координаты песчинки
            val layoutParams = ViewGroup.MarginLayoutParams(sandWidth, sandHeight)
            layoutParams.setMargins(randomX, randomY, 0, 0)
            sandImageView.layoutParams = layoutParams

            // Добавляем песчинку на экран
            containerLayout.addView(sandImageView)
            sandList.add(sandImageView)
        }
    }

    override fun onResume() {
        super.onResume()
        // Регистрируем слушатель для датчика акселерометра
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Отменяем регистрацию слушателя при приостановке активности
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val xAcceleration = -it.values[0] // Инвертируем значение для соответствия ориентации экрана
                val yAcceleration = it.values[1]
                updateSandPositions(xAcceleration, yAcceleration)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не используется
    }

    private fun updateSandPositions(xAcceleration: Float, yAcceleration: Float) {
        val currentTime = System.currentTimeMillis()
        deltaTime = if (lastTime == 0L) 0 else currentTime - lastTime
        lastTime = currentTime

        for (sand in sandList) {
            val layoutParams = sand.layoutParams as ViewGroup.MarginLayoutParams

            // Вычисляем новую вертикальную позицию песчинки
            var deltaY = yAcceleration * GRAVITY * SAND_SPEED * deltaTime
            layoutParams.topMargin += deltaY.toInt()

            // Обновляем позицию песчинки
            sand.layoutParams = layoutParams
        }
    }

    companion object {
        // Константы для имитации физики
        private const val GRAVITY = 9.8f // Ускорение свободного падения
        private const val SAND_SPEED = 0.1f // Скорость падения песчинок

        private val screenHeight = Resources.getSystem().displayMetrics.heightPixels
        private val screenWidth = Resources.getSystem().displayMetrics.widthPixels
    }
}
