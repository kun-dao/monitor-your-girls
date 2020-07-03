package browser.log;


import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.provider.Browser;
import android.util.Log;



public class BrowserActivator
{
    private BrowserObserver observer = null;
    private String tag="BrowserTag";
    private Context mContext;

    public BrowserActivator(Context mContext)
    {
        this.mContext = mContext;
    }

    public void activateBrowserObserver()
    {
        try
        {
            if(observer  == null)
            {
                observer = new BrowserObserver(new Handler(),mContext);
//					mContext.getContentResolver().registerContentObserver(Browser.BOOKMARKS_URI,true,observer);
                mContext.getContentResolver().registerContentObserver(Uri.parse("content://browser/bookmarks"),true,observer);
            }
            System.out.println("activateBrowserObserver");

        } catch (Exception e)
        {
            Log.i(tag,"Ex : "+e.getMessage());
        }
    }
}
