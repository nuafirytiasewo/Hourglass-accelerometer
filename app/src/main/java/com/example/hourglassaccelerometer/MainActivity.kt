// MainActivity.kt
package com.example.hourglassaccelerometer

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.DisplayMetrics
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.example.hourglassaccelerometer.R

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor

    private lateinit var icSand: ImageView
    private var screenWidth: Int = 0
    private var screenHeight: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Получение размеров экрана
        val displayMetrics = DisplayMetrics()
        windowManager.defaultDisplay.getMetrics(displayMetrics)
        screenWidth = displayMetrics.widthPixels
        screenHeight = displayMetrics.heightPixels

        // Инициализация ImageView
        icSand = findViewById(R.id.ic_sand)

        // Настройка датчика акселерометра
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!

        // Регистрация слушателя датчика
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            // Обработка данных акселерометра
            val x = event.values[0]
            val y = event.values[1]

            // Перемещение иконки в зависимости от значений акселерометра
            moveIconWithGravity(x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Здесь можно обработать изменения точности сенсора
    }

    private fun moveIconWithGravity(x: Float, y: Float) {
        // Получение текущих координат
        val currentX = icSand.x
        val currentY = icSand.y

        // Рассчет новых координат
        val newX = currentX - x * 5
        val newY = currentY + y * 5

        // Убедитесь, что иконка остается в пределах экрана
        icSand.x = newX.coerceIn(0f, (screenWidth - icSand.width).toFloat())
        icSand.y = newY.coerceIn(0f, (screenHeight - icSand.height).toFloat())
    }

    override fun onDestroy() {
        super.onDestroy()
        // Отмена регистрации слушателя датчика
        sensorManager.unregisterListener(this)
    }
}
