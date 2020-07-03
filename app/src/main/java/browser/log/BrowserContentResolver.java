package browser.log;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;

public class BrowserContentResolver {
    String records = null;
    StringBuilder recordBuilder = null;
    private Context mContext;
    final private String TAG = "BrowserContentResolver";

    public BrowserContentResolver(Context mContext)
    {
        this.mContext = mContext;
    }

    public void getRecords() {
         ContentResolver contentResolver = mContext.getContentResolver();
        Cursor cursor = contentResolver.query(Uri.parse("content://browser/bookmarks"),null,null,null);
        while (cursor != null && cursor.moveToNext()) {
            String url = null;
            String title = null;
            String time = null;
            String date = null;

            recordBuilder = new StringBuilder();
            title = cursor.getString(cursor.getColumnIndex("title"));
            url = cursor.getString(cursor.getColumnIndex("url"));

            date = cursor.getString(cursor.getColumnIndex("date"));

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    "yyyy-MM-dd hh:mm;ss");
            Date d = new Date(Long.parseLong(date));
            time = dateFormat.format(d);

            Log.v(TAG,title + url + time);
        }
        Log.v(TAG,"nothing");
    }
}
