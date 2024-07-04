package com.example.metal;

import static java.lang.Math.sqrt;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements SensorEventListener {

    private SensorManager sensorManager;
    private Sensor magnetometer;
    private TextView textView;
    private ProgressBar progressBar;
    private TextView magneticFieldStrengthTextView;
    private TextView maxTextView;
    private TextView minTextView;
    private TextView avgTextView;
    private TextView xTextView;
    private TextView yTextView;
    private TextView zTextView;
    private ArrayList<Double> magneticFieldValues = new ArrayList<>();

    private float[] gravity = new float[3];
    private static final float ALPHA = 0.25f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.progressbar);
        magneticFieldStrengthTextView = findViewById(R.id.magneticFieldStrengthTextView);
        maxTextView = findViewById(R.id.maxTextView);
        minTextView = findViewById(R.id.minTextView);
        avgTextView = findViewById(R.id.avgTextView);
        xTextView = findViewById(R.id.xTextView);
        yTextView = findViewById(R.id.yTextView);
        zTextView = findViewById(R.id.zTextView);

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        if (magnetometer == null) {
            Log.e("MainActivity", "No Magnetometer found!");
            finish();
        } else {
            sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, magnetometer, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // Không cần xử lý thay đổi độ chính xác
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float[] filteredValues = lowPassFilter(event.values.clone(), gravity);
        float x = filteredValues[0];
        float y = filteredValues[1];
        float z = filteredValues[2];
        double magnitude = sqrt(x * x + y * y + z * z);
        magneticFieldValues.add(magnitude);

        String magneticFieldStrengthText = String.format("%.0f", magnitude);
        textView.setText(magneticFieldStrengthText + "μT");
        progressBar.setProgress((int) magnitude);

        xTextView.setText(String.format("X: %.2fμT", x));
        yTextView.setText(String.format("Y: %.2fμT", y));
        zTextView.setText(String.format("Z: %.2fμT", z));

        updateStats();
    }

    private void updateStats() {
        if (magneticFieldValues.isEmpty()) {
            return;
        }

        double max = Double.MIN_VALUE;
        double min = Double.MAX_VALUE;
        double sum = 0;

        for (double value : magneticFieldValues) {
            if (value > max) {
                max = value;
            }
            if (value < min) {
                min = value;
            }
            sum += value;
        }

        double avg = sum / magneticFieldValues.size();

        maxTextView.setText(String.format("Max: %.0fμT", max));
        minTextView.setText(String.format("Min: %.0fμT", min));
        avgTextView.setText(String.format("Avg: %.0fμT", avg));
    }

    private float[] lowPassFilter(float[] input, float[] output) {
        if (output == null) return input;
        for (int i = 0; i < input.length; i++) {
            output[i] = output[i] + ALPHA * (input[i] - output[i]);
        }
        return output;
    }
}
