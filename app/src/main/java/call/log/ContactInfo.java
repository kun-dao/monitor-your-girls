package call.log;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;

public class ContactInfo {
    private Context myContext;
    private static final String TAG = "CONTACTTRACKER";

    public ContactInfo(Context context){
        myContext = context;
}

    private   String[] projection = { ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER };


    //获取联系人
    public void getConnect() {
        Cursor cursor = myContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[] { "display_name", "sort_key", "contact_id",
                        "data1" }, null, null, null);
        Log.i(TAG,"cursor connect count:" + cursor.getCount());
//        moveToNext方法返回的是一个boolean类型的数据
        while (cursor.moveToNext()) {
            //读取通讯录的姓名
            String name = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            //读取通讯录的号码
            String number = cursor.getString(cursor
                    .getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            Log.i(TAG,"获取的通讯录是： " + name + "\n"
                    +  " number : " + number);
        }
        cursor.close();
    }


    //根据手机号码查询联系人姓名
    public String getConName( String[] number) {
        String displayName;
        Cursor cursor = myContext.getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, projection,
                ContactsContract.CommonDataKinds.Phone.NUMBER + "=?",
                number, null);
        Log.i(TAG,"cursor displayName count:" + cursor.getCount());
        if (cursor != null) {
            while (cursor.moveToNext()) {
                displayName = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
                if (!TextUtils.isEmpty(displayName)) {
                    Log.i(TAG,"获取的通讯录 姓名是 : " + displayName);
                    return displayName;
                }
            }
        }
        return "UNKNOWN";
    }

}
