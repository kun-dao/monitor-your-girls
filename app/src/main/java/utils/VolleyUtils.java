package utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;
import android.widget.ImageView;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.dk.monitoryourgilrs.R;
import com.google.gson.Gson;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

public class VolleyUtils {
    private static RequestQueue mQueue;
    private ImageLoader mLoader;
    private ImageLoader.ImageCache mCache;
    private static VolleyUtils mInstance;
    public  static final String IP = "http://47.113.83.2:9090";

    /**
     * 1.构造方法私有化
     * @param context
     */
    private VolleyUtils(Context context) {
        //做一些事情
        mQueue = Volley.newRequestQueue(context);
        mCache = new MyImageCache();
        mLoader = new ImageLoader(mQueue, mCache);
    }

    public RequestQueue getQueue() {
        return mQueue;
    }

    public ImageLoader getLoader() {
        return mLoader;
    }


    /**
     * 2.提供一个静态方法，返回一个当前类
     * @param context
     * @return
     */
    public static VolleyUtils create(Context context) {
        if (mInstance == null) {
            synchronized (VolleyUtils.class) {
                if (mInstance == null) {
                    mInstance = new VolleyUtils(context);
                }
            }
        }
        return mInstance;
    }


    public <T> void get(String url, final Class<T> clazz, final OnResponse<T> listener) {

        HashMap<String, String> map = new HashMap<>();
        listener.OnMap(map);
        String param = prepareParam(map);
        if (param.trim().length() >= 1) {
            url += "?" + param;
        }
        Log.e("Volley", "urlResult---->" + url);
        StringRequest stringRequest = new StringRequest(StringRequest.Method.GET, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Volley", "response-->" + response);
                Gson gson = new Gson();
                listener.onSuccess(gson.fromJson(response, clazz));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", "response-->" + error.getMessage());
                listener.onError(error.getMessage());
            }
        });

        mQueue.add(stringRequest);
    }


    private static String prepareParam(Map<String, String> paramMap) {
        StringBuilder sb = new StringBuilder();
        if (paramMap.isEmpty()) {
            return "";
        } else {
            for (String key : paramMap.keySet()) {
                String value =  paramMap.get(key);
                if (sb.length() < 1) {
                    sb.append(key).append("=").append(value);
                } else {
                    sb.append("&").append(key).append("=").append(value);
                }
            }
            return sb.toString();
        }
    }


    public <T> void post(String url, final Class<T> clazz, final OnResponse<T> listener) {
        StringRequest stringRequest = new StringRequest(StringRequest.Method.POST, url, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                Log.e("Volley", "response-->" + response);
                Gson gson = new Gson();
                listener.onSuccess(gson.fromJson(response, clazz));
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.e("Volley", "response-->" + error.getMessage());
                listener.onError(error.getMessage());
            }
        }) {
            /**
             * Post请求和Get请求的使用步骤上的区别在于请求条件的指定
             * 必须在StringRequest对象的后面添加{}，并且
             * 在{}内重写getParams方法，该方法的返回值就是所有的请求条件
             * */
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                //将请求条件封装到map对象中
                Map<String, String> map = new HashMap<>();
                listener.OnMap(map);
                return map;
            }
        };
        mQueue.add(stringRequest);
    }


    public void loadImg(String url, ImageView view, int maxWidth, int maxHeight, int defaultImageResId, int errorImageResId) {
        mLoader.get(url, //图片的下载路径
                /**
                 * 通过getImageListener方法获取ImageListener接口对象
                 * 参数1： 图片下载完成后，由哪个控件显示图片
                 * 参数2： 设置图片下载过程中显示的默认图片
                 * 参数3： 设置一旦图片下载出错，就显示出错提示图片
                 * */
                ImageLoader.getImageListener(view, defaultImageResId, errorImageResId),
                maxWidth, maxHeight, //图片的最大宽高 指定成0的话就表示不管图片有多大
                ImageView.ScaleType.FIT_XY //图片的缩放模式
        );
    }

    public void loadImg(String url, ImageView view) {
        mLoader.get(url, //图片的下载路径
                ImageLoader.getImageListener(view, R.mipmap.ic_launcher, R.mipmap.ic_launcher),
                0, 0, //图片的最大宽高 指定成0的话就表示不管图片有多大
                ImageView.ScaleType.FIT_XY //图片的缩放模式
        );
    }

    public void loadImg(String url, ImageView view, int defaultImageResId, int errorImageResId) {
        mLoader.get(url, //图片的下载路径
                ImageLoader.getImageListener(view, defaultImageResId, errorImageResId),
                0, 0, //图片的最大宽高 指定成0的话就表示不管图片有多大
                ImageView.ScaleType.FIT_XY //图片的缩放模式
        );
    }

    /**
     * 分配一定内存空间，专门存取图片，一般为内存大小的1/8
     */
    private class MyImageCache implements ImageLoader.ImageCache {

        private LruCache<String, Bitmap> mCache;

        private MyImageCache() {
            //分配最大内存空间的1/8
            long maxMemory = Runtime.getRuntime().maxMemory() / 8;
            mCache = new LruCache<String, Bitmap>((int) maxMemory) {
                @Override
                protected int sizeOf(String key, Bitmap value) {
                    //得到当前图片的大小
                    return value.getByteCount();
                }
            };
        }

        @Override
        public Bitmap getBitmap(String url) {
            return mCache.get(url);
        }

        @Override
        public void putBitmap(String url, Bitmap bitmap) {
            if (getBitmap(url) == null)
                mCache.put(url, bitmap);
        }
    }

    /**
     * 自定义的类型
     * @param <T>
     */
    public class GsonRequest<T> extends Request<T> {
        private Response.Listener<T> mListener;
        private Gson mGson;
        private Class<T> mClazz;

        private GsonRequest(int method, String url, Response.Listener<T> listener, Response.ErrorListener errorListenerlistener, Class<T> clazz) {
            super(method, url, errorListenerlistener);
            this.mListener = listener;
            mGson = new Gson();
            this.mClazz = clazz;
        }

        @Override
        protected Response<T> parseNetworkResponse(NetworkResponse response) {
            String parsed;
            try {
                parsed = new String(response.data, HttpHeaderParser.parseCharset(response.headers));
                Log.e("Volley", "服务器返回JSON------>" + parsed);
                return Response.success(mGson.fromJson(parsed, mClazz), HttpHeaderParser.parseCacheHeaders(response));
            } catch (UnsupportedEncodingException e) {
                return Response.error(new VolleyError(e));
            }
        }

        @Override
        protected void deliverResponse(T response) {
            if (mListener != null) {
                mListener.onResponse(response);
            }
        }

//        @Override
        protected void onFinish() {
//            super.onFinish();
            mListener = null;
        }
    }

    public interface OnResponse<T> {

        void OnMap(Map<String, String> map);

        void onSuccess(T response);

        void onError(String error);
    }
}