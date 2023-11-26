package com.example.dashcam;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

public class ShakeDetector implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor accelerometer;
    private static final float SHAKE_THRESHOLD = 2.5f;
    private ShakeListener shakeListener;

    public ShakeDetector(Context context, ShakeListener shakeListener) {
        this.shakeListener = shakeListener;

        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float x = event.values[0];
        float y = event.values[1];
        float z = event.values[2];

        // 충격 감지 로직
        float acceleration = (float) Math.sqrt(x * x + y * y + z * z);
        if (acceleration > SHAKE_THRESHOLD) {
            // 충격이 감지되었을 때 동작 수행
            if (shakeListener != null) {
                shakeListener.onShakeDetected();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // 센서 정확도 변경 시 호출되는 메서드
    }


    //센서 리스너 등록
    public void startListening() {
        if (accelerometer != null) {
            sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }


    //센서 리스너 해제
    public void stopListening() {
        sensorManager.unregisterListener(this);
    }

    public interface ShakeListener{
        void onShakeDetected();
    }
}
