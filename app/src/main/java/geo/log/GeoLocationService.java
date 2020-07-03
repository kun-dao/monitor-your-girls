package geo.log;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.util.Log;

import java.util.Map;

import Beans.ResponseBean;
import utils.SetUtil;
import utils.VolleyUtils;

import static utils.TimeUtils.dateToString;
import static utils.TimeUtils.getTimeStame;

public class GeoLocationService {
    private  final long IMEI =00000000001 ;
    Context myContext = null;
    private String tag = "GPS Tracking";
    private  String LOCATIONURL = VolleyUtils.IP+ "/insertLocations";
    private Handler mHandler = new Handler();
    GPSTracker gps;
    SQLiteDatabase db;

    public GeoLocationService(Context context) {
        // TODO Auto-generated constructor stub
        myContext = context;

    }

    public void startLocationService() {
        mHandler.postDelayed(mRunnable, 1000);
    }

    private final Runnable mRunnable = new Runnable() {

        public void run() {
            getLocation();
            mHandler.postDelayed(mRunnable, 60000); // 1min后再次执行任务
        }

        private void getLocation() {
            // TODO Auto-generated method stub
            // create class object
            gps = new GPSTracker(myContext);

            // check if GPS enabled
            if (gps.canGetLocation()) {
                double latitude = (float) gps.getLatitude();
                double longitude = (float) gps.getLongitude();

                Log.i(tag, "MyLatitude : " + latitude);
                Log.i(tag, "MyLongitude : " + longitude);
                storeData(latitude, longitude);
            } else {
                // can't get location
                // GPS or Network is not enabled
                // Ask user to enable GPS/network in settings
                gps.showSettingsAlert();
            }


        }

    };

    private void storeData(double latitude, double longitude){
        final com.dk.monitoryourgilrs.DbHelper dh = com.dk.monitoryourgilrs.DbHelper.getInstance(myContext);
        db = dh.getWritableDatabase();
        final ContentValues values=new ContentValues();
        values.put("_id", System.currentTimeMillis());
        values.put("imei", IMEI);
        values.put("date",dateToString(getTimeStame()));
        values.put("latitude", latitude);
        values.put("longitude", longitude);
        values.put("updateServer",-1);
        db.insert("location", null, values);
        Log.v(tag,"data inserted");
        final Cursor data = db.rawQuery("select * from location ORDER BY date DESC", null);
        while (data.moveToNext()) {
            int updateServer = data.getInt(5 );
            if(updateServer == -1){
                final long _id =  data.getLong(0);
                values.put("_id", _id);
                values.put("imei", data.getLong(1));
                values.put("date",data.getString(2));
                values.put("latitude", data.getDouble(3));
                values.put("longitude", data.getDouble(4));
                values.put("updateServer",1);
                //to do update server
                if (utils.NetWorkUtils.isNetworkConnected(myContext)) {
                    Log.v(tag, "NetWork ok !");

                    VolleyUtils.create(myContext)
                            .post(LOCATIONURL, ResponseBean.class, new VolleyUtils.OnResponse<ResponseBean>() {
                                @Override
                                public void OnMap(Map<String, String> map) {
                                    map.put("_id", values.getAsString("_id"));
                                    map.put("imei", values.getAsString("imei"));
                                    map.put("date", values.getAsString("date"));
                                    map.put("latitude", values.getAsString("latitude"));
                                    map.put("longitude", values.getAsString("longitude"));
                                }

                                @Override
                                public void onSuccess(ResponseBean response) {
                                    Log.e(tag, "response---->" + response);
                                    db.update("location",values,"_id = ?", new String[]{String.valueOf(_id)});
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(tag, "error---->" + error);
                                }
                            });
                }else{
                    Log.v(tag, "NetWork not ok !");
                }
            }
        }
        data.close();
    }
}
