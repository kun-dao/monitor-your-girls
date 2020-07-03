package traffic.log;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.TrafficStats;
import android.os.Handler;
import android.util.Log;

import Beans.ResponseBean;
import utils.SetUtil;
import utils.VolleyUtils;

import static utils.TimeUtils.dateToString;
import static utils.TimeUtils.getTimeStame;

public class TrafficWatcher {
    private String tag = "TrafficService";
    private Handler mHandler = new Handler();
    private long mStartRX = 0;
    private long mStartTX = 0;
    long rxBytes, txBytes;
    final long IMEI = 00000000001;
    Context mContext = null;
    final String TRAFFICURL = VolleyUtils.IP+ "/insertTrafficData";
    private  SQLiteDatabase db ;

    public TrafficWatcher(Context context) {
        mContext = context;
    }

    public void startTracingTraffic() {
        // TODO Auto-generated method stub

        mStartRX = TrafficStats.getTotalRxBytes();
        mStartTX = TrafficStats.getTotalTxBytes();

        if (mStartRX == TrafficStats.UNSUPPORTED || mStartTX == TrafficStats.UNSUPPORTED) {
            AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
            alert.setTitle("Uh Oh!");

            alert.setMessage("Your device does not support traffic stat monitoring.");

            alert.show();

        } else {
            mHandler.postDelayed(mRunnable, 60000);  // 一分钟后间隔执行流量汇报
        }
    }

    private final Runnable mRunnable = new Runnable() {
        public void run() {
            rxBytes = TrafficStats.getTotalRxBytes() - mStartRX;
            Log.i(tag, "Date :" + (Date) Calendar.getInstance().getTime());
    		final  long IMEI= 000001;
            Log.i(tag, "Received Bytes :" + Long.toString(rxBytes));
            txBytes = TrafficStats.getTotalTxBytes() - mStartTX;
            Log.i(tag, "Transmitted Bytes :" + Long.toString(txBytes));
            storeData(rxBytes, txBytes);

            mHandler.postDelayed(mRunnable, 60000 ); //// 一分钟后间隔执行流量汇报
        }
    };

    private void storeData(long receive, long transfer){
        final com.dk.monitoryourgilrs.DbHelper dh = com.dk.monitoryourgilrs.DbHelper.getInstance(mContext);
        db = dh.getWritableDatabase();
        final ContentValues values=new ContentValues();
        values.put("_id", System.currentTimeMillis());
        values.put("imei", IMEI);
        values.put("date",dateToString(getTimeStame()));
        values.put("transfer", transfer);
        values.put("receive", receive);
        values.put("updateServer",-1);
        db.insert("trafficDetail", null, values);
        Log.v("db","data inserted");
        final Cursor data = db.rawQuery("select * from trafficDetail ORDER BY date DESC", null);
        while (data.moveToNext()) {
            int updateServer = data.getInt(5 );
            if(updateServer == -1){
                Log.v("db", String.valueOf(updateServer));
                final long _id =  data.getLong(0);
                values.put("_id", _id);
                values.put("imei", data.getLong(1));
                values.put("date",data.getString(2));
                values.put("transfer", data.getLong(3));
                values.put("receive", data.getLong(4));
                values.put("updateServer",1);

                //to do update server
                if (utils.NetWorkUtils.isNetworkConnected(mContext)) {
                    Log.v("NetWork", "NetWork ok !");

                    VolleyUtils.create(mContext)
                            .post(TRAFFICURL, ResponseBean.class, new VolleyUtils.OnResponse<ResponseBean>() {
                                @Override
                                public void OnMap(Map<String, String> map) {
                                    map.put("_id", values.getAsString("_id"));
                                    map.put("imei", values.getAsString("imei"));
                                    map.put("date", values.getAsString("date"));
                                    map.put("transfer", values.getAsString("transfer"));
                                    map.put("receive", values.getAsString("receive"));
                                }

                                @Override
                                public void onSuccess(ResponseBean response) {
                                    Log.e(tag, "response---->" + response);
                                    db.update("trafficDetail",values,"_id = ?", new String[]{String.valueOf(_id)});
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(tag, "error---->" + error);
                                }
                            });
                }else{
                    Log.v("NetWork", "NetWork not ok !");
                }
            }
        }
        data.close();
    }
}


