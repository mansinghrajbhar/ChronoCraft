package com.arafat.chrono;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;

@CapacitorPlugin(name = "ProximitySensor")
public class ProximitySensorPlugin extends Plugin implements SensorEventListener {
    private SensorManager sensorManager;
    private Sensor proximitySensor;
    private boolean listening = false;

    @PluginMethod
    public void start(PluginCall call) {
        if (sensorManager == null) {
            sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        }
        if (sensorManager == null) {
            call.reject("Sensor manager unavailable");
            return;
        }

        proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        if (proximitySensor == null) {
            call.reject("This Android device does not have a proximity sensor");
            return;
        }

        if (!listening) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
            listening = true;
        }
        call.resolve();
    }

    @PluginMethod
    public void stop(PluginCall call) {
        stopListening();
        call.resolve();
    }

    private void stopListening() {
        if (sensorManager != null && listening) {
            sensorManager.unregisterListener(this);
        }
        listening = false;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() != Sensor.TYPE_PROXIMITY || event.values.length == 0) return;
        float distance = event.values[0];
        float maxRange = event.sensor.getMaximumRange();
        JSObject data = new JSObject();
        data.put("near", distance < maxRange);
        data.put("distance", distance);
        data.put("maximumRange", maxRange);
        notifyListeners("proximityChange", data);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    protected void handleOnDestroy() {
        stopListening();
        super.handleOnDestroy();
    }
}
