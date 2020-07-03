package sensor.step.log;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;

public class StepSensorEventListener implements SensorEventListener {
    private  int mStepCounter = 0;
    @Override
    public void onSensorChanged(SensorEvent event) {
        System.out.println("@@@:"+event.sensor.getType()+"--"+Sensor.TYPE_STEP_DETECTOR+"--"+Sensor.TYPE_STEP_COUNTER);
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
                mStepCounter = (int) event.values[0];
            }

        String desc = String.format("自开机以来总数为%d步", mStepCounter);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    public int getmStepCounter() {
        return mStepCounter;
    }
}
