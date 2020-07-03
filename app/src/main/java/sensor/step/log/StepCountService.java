package sensor.step.log;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class StepCountService {
    private final Context mContext;
    private final String tag ="StepCountService";
    private final SensorManager mSensorManager;
    private StepSensorEventListener mListener;
    private Handler mHandler = new Handler();;

    public StepCountService(Context context){
        mContext = context;
        mListener = new StepSensorEventListener();
        mSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        if (mSensorManager != null) {
            mSensorManager.registerListener(mListener, mSensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    private final Runnable mRunnable = new Runnable() {

        public void run() {
            getStepCount();
            mHandler.postDelayed(mRunnable, 6000); // 1min后再次执行任务
        }
        private int  getStepCount(){
            int step = mListener.getmStepCounter();
            Log.v(tag, String.valueOf(step));
            return step;
        }
    };

    public void startStepCountService() {
        mHandler.postDelayed(mRunnable, 6000);
    }

}
