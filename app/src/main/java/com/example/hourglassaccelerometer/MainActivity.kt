package com.example.hourglassaccelerometer;

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var sandClockImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Инициализация датчика акселерометра и изображения песочных часов
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sandClockImageView = findViewById(R.id.sandClockImageView)

        val containerLayout = findViewById<ViewGroup>(R.id.containerLayout)

        // Добавляем тысячи песчинок в верхний треугольник
        for (i in 0 until 1000) {
            val sandImageView = ImageView(this)
            sandImageView.setImageResource(R.drawable.sand)
            sandImageView.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)

            // Настраиваем позицию песчинок случайным образом внутри верхнего треугольника
            val randomX = (Math.random() * 400).toInt()  // Размер экрана
            val randomY = (Math.random() * 200).toInt()  // Высота верхнего треугольника
            val layoutParams = sandImageView.layoutParams
            if (layoutParams is ViewGroup.MarginLayoutParams) {
                layoutParams.setMargins(randomX, randomY, 0, 0)
                sandImageView.layoutParams = layoutParams
            }


            // Запускаем анимацию падения для каждой песчинки
            val fallingAnimation = AnimationUtils.loadAnimation(this, R.anim.fall_animation)
            sandImageView.startAnimation(fallingAnimation)

            containerLayout.addView(sandImageView)
        }
    }

    override fun onResume() {
        super.onResume()
        // Регистрация слушателя датчика акселерометра
        accelerometer?.let {
            sensorManager.registerListener(this, it, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    override fun onPause() {
        super.onPause()
        // Отмена регистрации слушателя датчика акселерометра при приостановке активности
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Обработка изменений данных с акселерометра
        event?.let {
            if (it.sensor.type == Sensor.TYPE_ACCELEROMETER) {
                val x = it.values[0]
                val y = it.values[1]
                // Вызываем функцию обновления угла поворота песочных часов
                updateSandClockRotation(x, y)
            }
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // Не используется
    }

    private fun updateSandClockRotation(x: Float, y: Float) {
        // Вычисляем угол поворота на основе данных с акселерометра
        val rotationAngle = Math.atan2(y.toDouble(), x.toDouble())

        // Преобразуем угол из радиан в градусы
        var degrees = Math.toDegrees(rotationAngle).toFloat()

        // Компенсируем начальный поворот изображения песочных часов (90 градусов)
        degrees -= 90f

        // Применяем угол поворота к изображению песочных часов
        sandClockImageView.rotation = -degrees
    }

}
