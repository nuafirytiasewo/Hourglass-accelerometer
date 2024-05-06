package com.example.hourglassaccelerometer;

import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), SensorEventListener {

    private lateinit var sensorManager: SensorManager
    private lateinit var accelerometer: Sensor
    private lateinit var sandClockImageView: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)!!
        sandClockImageView = findViewById(R.id.sandClockImageView)
    }

    override fun onResume() {
        super.onResume()
        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(this)
    }

    override fun onSensorChanged(event: SensorEvent?) {
        // Обработка изменений данных с акселерометра
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            // Получаем данные с акселерометра
            val x = event.values[0]
            val y = event.values[1]
            val z = event.values[2]

            // Обновляем положение песочных часов на основе данных акселерометра
            updateSandClockPosition(x, y)
        }
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {
        // пока что не используется
    }

    private fun updateSandClockPosition(x: Float, y: Float) {
        // тут наверное будет код для изменения положения песочных часов на основе данных акселерометра
        // можно будет изменить положение изображения песочных часов на экране
        // в соответствии с данными x и y
    }
}
