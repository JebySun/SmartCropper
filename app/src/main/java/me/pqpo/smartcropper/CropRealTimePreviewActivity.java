package me.pqpo.smartcropper;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import me.pqpo.smartcropperlib.SmartCropper;

/**
 * 自动识别并剪裁身份证
 * Created by JebySun on 2018/5/10.
 * email:jebysun@126.com
 */
public class CropRealTimePreviewActivity extends AppCompatActivity {

    private FrameLayout layoutPreviewContainer;
    private SurfaceView surfaceView;
    private CropView cropView;
    private SurfaceHolder surfaceViewHolder;
    private Camera camera;
    private int screenWidth;
    private int screenHeight;
    int surfaceWidth;
    int surfaceHeight;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_realtime_preview);

        init();


    }

    private void init() {
        layoutPreviewContainer = findViewById(R.id.layout_preview_container);
        surfaceView = findViewById(R.id.sfv);
        cropView = findViewById(R.id.crop);

        screenWidth = AndroidUtil.getScreenWidth(this);
        screenHeight = AndroidUtil.getScreenHeight(this);

        surfaceViewHolder = surfaceView.getHolder();
        surfaceViewHolder.setKeepScreenOn(true);
        surfaceViewHolder.addCallback(new SurfaceHolder.Callback() {
            //创建
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                //开始进行图片的预览
                startPreview();
            }
            //改变
            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                //停止旧的预览,开启新的预览
//                camera.stopPreview();
//                startPreview();
            }
            //释放
            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                //停止预览,释放资源
                stopCamera();
            }
        });

        camera = Camera.open();
        camera.setPreviewCallback(new Camera.PreviewCallback() {
            @Override
            public void onPreviewFrame(byte[] bytes, Camera camera) {
//                camera.stopPreview();
                //获得相机预览分辨率
                Camera.Size previewSize = camera.getParameters().getPreviewSize();
                YuvImage localYuvImage = new YuvImage(bytes, ImageFormat.NV21, previewSize.width, previewSize.height, null);
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                //把摄像头回调数据转成YUV,再按图像尺寸压缩成JPEG,从输出流中转成数组
                localYuvImage.compressToJpeg(new Rect(0, 0, previewSize.width,  previewSize.height), 100, outputStream);
                byte[] mParamArrayOfByte = outputStream.toByteArray();
                //生成Bitmap
                BitmapFactory.Options localOptions = new BitmapFactory.Options();
                localOptions.inPreferredConfig = Bitmap.Config.RGB_565; //构造位图生成的参数,必须为565。类名+enum
                Bitmap bitmap = BitmapFactory.decodeByteArray(mParamArrayOfByte, 0, mParamArrayOfByte.length, localOptions);

                //利用Matrix缩小成SurfaceView同样尺寸,然后旋转90度
                int bWidth = bitmap.getWidth();
                int bHeight = bitmap.getHeight();
                Matrix matrix = new Matrix();
                matrix.postRotate((float) 90.0);
                matrix.postScale(surfaceWidth*1.0F / bHeight, surfaceHeight*1.0F / bWidth);
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bWidth, bHeight, matrix, true);

                Point[] points = SmartCropper.scan(bitmap);
                if (points != null && points.length != 0) {
                    cropView.setRectPoints(points);
                }
//                AndroidUtil.saveBitmapAsFile(bitmap, 80, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "1024" + File.separator + System.currentTimeMillis() + ".jpg");
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

    }


    /**
     * 设置view的大小
     *
     */
    private void setViewSize(View view, int width, int height) {
        ViewGroup.MarginLayoutParams margin = new ViewGroup.MarginLayoutParams(view.getLayoutParams());
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(margin);
        layoutParams.width = width;
        layoutParams.height = height;
        view.setLayoutParams(layoutParams);
    }


    public void onPictureTaken(byte[] data, Camera camera) {
        Bitmap mBitmap = null;
        if (null != data) {
            mBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);//data是字节数据，将其解析成位图
            camera.stopPreview();
        }
        //设置FOCUS_MODE_CONTINUOUS_VIDEO)之后，myParam.set("rotation", 90)失效。图片竟然不能旋转了，故这里要旋转下
        Matrix matrix = new Matrix();
        matrix.postRotate((float) 90.0);
        Bitmap rotaBitmap = Bitmap.createBitmap(mBitmap, 0, 0, mBitmap.getWidth(), mBitmap.getHeight(), matrix, false);

        //旋转后rotaBitmap是960×1280.预览surfaview的大小是540×800
        //将960×1280缩放到540×800
        Bitmap sizeBitmap = Bitmap.createScaledBitmap(rotaBitmap, 540, 800, true);
        Bitmap rectBitmap = Bitmap.createBitmap(sizeBitmap, 100, 200, 300, 300);//截取

        //保存图片到sdcard
        if (null != rectBitmap) {

        }
    }




    @Override
    protected void onResume() {
        super.onResume();
        if (camera == null) {
            camera = Camera.open();
            if (surfaceViewHolder != null){
                startPreview();
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        if (camera != null) {
            stopCamera();
        }
    }

    /**
     * 相机预览
     */
    private void startPreview() {
        Camera.Size cameraSize = camera.getParameters().getPreviewSize();
        surfaceWidth = screenWidth;
        surfaceHeight = screenWidth * cameraSize.width/cameraSize.height;
        setViewSize(layoutPreviewContainer, surfaceWidth, surfaceHeight);
        try {
            //相机与SurfaceView进行绑定
            camera.setPreviewDisplay(surfaceViewHolder);
            //预览的图形旋转
            camera.setDisplayOrientation(90);
            camera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /**
     * 停止拍照释放资源
     */
    private void stopCamera() {
        if (camera != null) {
            camera.stopPreview();
            camera.release();
            camera = null;
        }

    }

}













