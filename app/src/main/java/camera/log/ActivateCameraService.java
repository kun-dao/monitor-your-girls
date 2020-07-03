package camera.log;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;



public class ActivateCameraService  {

    private  final String tag = "CameraService";
    private static Handler mHandler;
    private Context myContext;
    private Intent face;
    private static ActivateCameraService mInstance;
    private static int RUN = 200;
    private static int STOP = -200;
    private static int STATUS;

    private ActivateCameraService(Context context) {
        // TODO Auto-generated constructor stub
        myContext = context;
        face = new Intent(context, CameraService.class);
        mHandler = new Handler();
        STATUS = STOP;
    }

    /**
     * 2.提供一个静态方法，返回一个当前类
     * @param context
     * @return
     */
    public static ActivateCameraService create(Context context) {
        if (mInstance == null) {
            synchronized (ActivateCameraService.class) {
                if (mInstance == null) {
                    mInstance = new ActivateCameraService(context);
                }
            }
        }
        return mInstance;
    }


    public void start() {
        Log.v(tag, "start");
        if(STATUS == STOP){
        mHandler.postDelayed(startCameraServiceRunnable, 1000);
            STATUS = RUN;
        }
    }

    private final Runnable startCameraServiceRunnable = new Runnable() {

        public void run() {
            Log.v(tag, "startCameraServiceRunnable");
            startCameraService();
            mHandler.postDelayed(closeCameraServiceRunnable, 10000); // 10秒后关闭相机服务
        }
    };


    private void startCameraService() {
        myContext.startService(face);
    }

    private final Runnable closeCameraServiceRunnable = new Runnable() {

        public void run() {
            Log.v(tag, "closeCameraServiceRunnable");
            stopCameraService();
            mHandler.postDelayed(startCameraServiceRunnable, 20000); // 4min后再次打开相机服务
        }
    };

    private void stopCameraService() {
        myContext.stopService(face);
    }

    public  void stop(){
        Log.v(tag, "stop");
        if(STATUS == RUN) {
            stopCameraService();
            mHandler.removeCallbacks(closeCameraServiceRunnable);
            mHandler.removeCallbacks(startCameraServiceRunnable);
            STATUS = STOP;
        }
    }

}
