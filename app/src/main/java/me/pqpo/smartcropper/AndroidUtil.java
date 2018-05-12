package me.pqpo.smartcropper;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by Msrlin on 2017/9/14.
 */

public final class AndroidUtil {

    private AndroidUtil() {
    }


    public static void toastShort(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
    }

    public static void toastLong(Context context, String msg) {
        Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
    }

    /**
     * dp转px
     * @param dp
     */
    public static int dp2px(float dp){
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density + 0.5f);
    }

    /**
     * sp转px
     * @param sp
     * @return
     */
    public static int sp2px(float sp){
        return (int) (sp * Resources.getSystem().getDisplayMetrics().scaledDensity + 0.5f);
    }


    /**
     * px转换为dp
     * @param px
     * @return
     */
    public static int px2dp(float px) {
        float scale = Resources.getSystem().getDisplayMetrics().density;
        return (int) (px / scale + 0.5f);
    }

    /**
     * px转换为sp
     * @param px
     * @return
     */
    public static int px2sp(float px) {
        final float fontScale = Resources.getSystem().getDisplayMetrics().scaledDensity;
        return (int) (px / fontScale + 0.5f);
    }


    /**
     * 获取屏幕宽度（px）
     * @param context
     * @return
     */
    public static int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.widthPixels;
    }

    /**
     * 获取屏幕高度（px）
     * @param context
     * @return
     */
    public static int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        return metrics.heightPixels;
    }


    /**
     * 兼容方式获取颜色资源
     * @param context
     * @param colorResId
     * @return
     */
    public static int getColor(Context context, int colorResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getColor(colorResId);
        }
        return context.getResources().getColor(colorResId);
    }

    /**
     * 兼容方式获取Drawable资源
     * @param context
     * @param drawableResId
     * @return
     */
    public static Drawable getDrawable(Context context, int drawableResId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return context.getDrawable(drawableResId);
        }
        return context.getResources().getDrawable(drawableResId);
    }


    /**
     * 从assets目录读取文本信息
     *
     * @param context
     * @param fileName
     * @return
     */
    public static String getAssetsString(Context context, String fileName) {
        String string = null;
        AssetManager assetMgr = context.getAssets();
        try {
            InputStream is = assetMgr.open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            string = new String(buffer);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            return string;
        }
    }


    /**
     * 打电话
     * @param context
     * @param tel
     * @param isAutoCall 自动拨打
     */
    public static void callPhoneNumber(Context context, String tel, boolean isAutoCall) {
        if (tel == null || tel.trim().equals("")) {
            throw new IllegalArgumentException("电话号码不能为空");
        }
        Intent intent = new Intent();
        if (isAutoCall) {
            //直接拨打电话并显示正在拨打电话界面
            intent.setAction(Intent.ACTION_CALL);
        } else {
            //只跳转到拨打电话界面
            intent.setAction(Intent.ACTION_DIAL);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        intent.setData(Uri.parse("tel:" + tel));
        context.startActivity(intent);
    }

    /**
     * 获取App版本名称
     * @return
     */
    public static String getAppVersionName(Context context) {
        PackageInfo packageInfo;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return "";
        }
        return packageInfo.versionName;
    }


    /**
     * 获取App版本代码
     * @return
     */
    public static int getAppVersionCode(Context context) {
        PackageInfo packageInfo = null;
        try {
            packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return 0;
        }
        return packageInfo.versionCode;
    }


    /**
     * 安装apk文件
     */
    public static void installApk(Context context, String filePath) {
        File apkFile = new File(filePath);
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }


    /**
     * 从Manifest文件中获取配置信息
     * @param context
     * @return key
     */
    public static String getMetaData(Context context, String key) {
        String value = null;
        ApplicationInfo appInfo = null;
        try {
            appInfo = context.getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        if (appInfo != null && appInfo.metaData != null) {
            value = appInfo.metaData.getString(key);
        }
        return value;
    }

    /**
     * 获取状态栏高度(px)
     * @param context context
     * @return 状态栏高度
     */
    public static int getStatusBarHeight(Context context) {
        int resourceId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        return context.getResources().getDimensionPixelSize(resourceId);
    }


    /**
     * 打开输入法软键盘
     */
    public static void openKeybord(Context mContext, View editTextView) {
        editTextView.requestFocus();
        InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
//        imm.showSoftInput(editTextView, InputMethodManager.SHOW_FORCED);
        imm.showSoftInput(editTextView, InputMethodManager.SHOW_IMPLICIT);
    }


    /**
     * 关闭输入法软键盘
     */
    public static void closeKeybord(Context context, View view) {
        IBinder windowToken = view.getWindowToken();
        if (windowToken != null) {
            InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(windowToken, 0);
        }
    }

    /**
     * 输入法是否打开
     * @param context
     * @return
     */
    public static boolean isKeybordOpen(Context context) {
        InputMethodManager im = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        return im.isActive();
    }


    /**
     * 设置Activity的背景透明度
     * @param bgAlpha 取值0.0~1.0
     */
    public static void setActivityBackgroundAlpha(Activity context, float bgAlpha) {
        WindowManager.LayoutParams winLayoutParam = context.getWindow().getAttributes();
        winLayoutParam.alpha = bgAlpha;
        context.getWindow().addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        context.getWindow().setAttributes(winLayoutParam);
    }


    /**
     * 尝试将文件Uri转换成文件路径
     *
     * @param context
     * @param uri
     * @return the file path or null
     */
    public static String getRealFilePath(Context context, Uri uri) {
        if (null == uri) {
            return null;
        }
        String scheme = uri.getScheme();
        String realPath = null;
        if (scheme == null) {
            realPath = uri.getPath();
        } else if (ContentResolver.SCHEME_FILE.equals(scheme)) {
            realPath = uri.getPath();
        } else if (ContentResolver.SCHEME_CONTENT.equals(scheme)) {
            Cursor cursor = context.getContentResolver().query(uri, new String[] { MediaStore.Images.ImageColumns.DATA }, null, null, null);
            if (null != cursor) {
                if (cursor.moveToFirst()) {
                    int index = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
                    if (index > -1) {
                        realPath = cursor.getString(index);
                    }
                }
                cursor.close();
            }
        }
        return realPath;
    }


    public static boolean makeDirOnNotExist(String path) {
        File apkDir = new File(path);
        if (!apkDir.exists()) {
            return apkDir.mkdirs();
        }
        return false;
    }



    public static void saveBitmapAsFile(Bitmap bitmap, int quality, String fileUrl) {
        File file = new File(fileUrl);
        if (file.exists()) {
            file.delete();
        }
        if (bitmap.isRecycled()) {
            return;
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, out);
            out.flush();
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /**
     * 通过路径按比例缩放图片到指定尺寸
     *
     * @param imgPath
     * @param maxWpx
     * @param maxHpx
     * @return
     */
    public static Bitmap scaleImage(String imgPath, int maxWpx, int maxHpx) {
        BitmapFactory.Options bmapOpts = new BitmapFactory.Options();
        bmapOpts.inJustDecodeBounds = false;
        bmapOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmapSrc = BitmapFactory.decodeFile(imgPath, bmapOpts);

        Bitmap bitmap = zoomBitmapKeeepScale(bitmapSrc, maxWpx, maxHpx);
//        bitmapSrc.recycle();

        int degree = readPhotoDegree(imgPath);
        if (degree != 0) {
            bitmap = rotatePhotoDegree(bitmap, degree);
        }
        return bitmap;
    }

    /**
     * 按照尺寸比例大致缩放图片
     * @param imgPath
     * @param pixelW
     * @param pixelH
     * @return
     */
    public static Bitmap ratio(String imgPath, int pixelW, int pixelH) {
        BitmapFactory.Options bitMapOpts = new BitmapFactory.Options();
        // 开始读入图片，把options.inJustDecodeBounds设回true，即只读边不读内容
        bitMapOpts.inJustDecodeBounds = true;
        bitMapOpts.inPreferredConfig = Bitmap.Config.RGB_565;
        Bitmap bitmap = BitmapFactory.decodeFile(imgPath, bitMapOpts);

        bitMapOpts.inJustDecodeBounds = false;
        int w = bitMapOpts.outWidth;
        int h = bitMapOpts.outHeight;
        // 想要缩放的目标尺寸
        float hh = pixelH;// 设置高度为240f时，可以明显看到图片缩小了
        float ww = pixelW;// 设置宽度为120f，可以明显看到图片缩小了
        // 缩放比。由于是固定比例缩放，只用高或者宽其中一个数据进行计算即可
        int be = 1;//be=1表示不缩放
        if (w > h && w > ww) { //如果宽度大的话根据宽度固定大小缩放
            be = Math.round(w / ww);
        } else if (w < h && h > hh) {//如果高度高的话根据宽度固定大小缩放
            be = Math.round(h / hh);
        }
        if (be <= 0) be = 1;
        bitMapOpts.inSampleSize = be; //设置缩放比例
        // 开始压缩图片，注意此时已经把options.inJustDecodeBounds 设回false了
        bitmap = BitmapFactory.decodeFile(imgPath, bitMapOpts);
        return bitmap;
    }


    /**
     * 保持比例缩放到指定尺寸
     *
     * @param bitmap
     * @param width
     * @param height
     * @return
     */
    public static Bitmap zoomBitmapKeeepScale(Bitmap bitmap, int width, int height) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        Matrix matrix = new Matrix();
        float scaleWidth = ((float) width / w);
        float scaleHeight = ((float) height / h);
        float scale = Math.min(scaleWidth, scaleHeight);
        matrix.postScale(scale, scale);
        return Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
    }

    /**
     * 读取照片旋转的角度
     * @param path 图片绝对路径
     * @return degree旋转的角度
     */
    public static int readPhotoDegree(String path) {
        int degree  = 0;
        try {
            ExifInterface exifInterface = new ExifInterface(path);
            int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (orientation) {
                case ExifInterface.ORIENTATION_ROTATE_90:
                    degree = 90;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_180:
                    degree = 180;
                    break;
                case ExifInterface.ORIENTATION_ROTATE_270:
                    degree = 270;
                    break;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return degree;
    }

    /** 按一定角度旋转图片
     * @param angle
     * @param bmap
     * @return Bitmap
     */
    public static Bitmap rotatePhotoDegree(Bitmap bmap, int angle) {
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(bmap, 0, 0, bmap.getWidth(), bmap.getHeight(), matrix, true);
    }

    public static void copyFile(String srcPath, String desPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(srcPath);
            if (oldfile.exists()) { //文件存在时
                InputStream inStream = new FileInputStream(srcPath); //读入原文件
                FileOutputStream fs = new FileOutputStream(desPath);
                byte[] buffer = new byte[10*1024];
                while ( (byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread; //字节数 文件大小
                    System.out.println(bytesum);
                    fs.write(buffer, 0, byteread);
                }
                fs.flush();
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 发起添加群流程。群号：苏州职多聘服务群18群(519199185) 的 key 为： vdOTAYtkGYvaXZJrPMVErz_q3Jj04fnJ
     * 调用 joinQQGroup(vdOTAYtkGYvaXZJrPMVErz_q3Jj04fnJ) 即可发起手Q客户端申请加群 苏州职多聘服务群18群(519199185)
     *
     * @param key 由官网生成的key
     * @return 返回true表示呼起手Q成功，返回fals表示呼起失败
     ******************/
    public static boolean joinQQGroup(Context context, String key) {
        Intent intent = new Intent();
//        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.setData(Uri.parse("mqqopensdkapi://bizAgent/qm/qr?url=http%3A%2F%2Fqm.qq.com%2Fcgi-bin%2Fqm%2Fqr%3Ffrom%3Dapp%26p%3Dandroid%26k%3D" + key));
        // 此Flag可根据具体产品需要自定义，如设置，则在加群界面按返回，返回手Q主界面，不设置，按返回会返回到呼起产品界面    //intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        try {
            context.startActivity(intent);
            return true;
        } catch (Exception e) {
            // 未安装手Q或安装的版本不支持
            return false;
        }
    }



    /**
     * 判断是否安装某个应用
     */
    public static boolean hasInstalledApp(Context context, String packageName) {
        PackageManager packageManager = context.getPackageManager();
        List<PackageInfo> pkgInfoList = packageManager.getInstalledPackages(0);
        if (pkgInfoList != null) {
            for (PackageInfo pkgInfo : pkgInfoList) {
                if (packageName.equals(pkgInfo.packageName)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 屏幕指定View截图
     * @param view
     * @return
     */
    public static Bitmap takeScreenShot(View view) {
        view.setDrawingCacheEnabled(true);
        view.buildDrawingCache();
        return view.getDrawingCache();
    }

    /**
     * 设置SharedPreferences字符串值
     * @param context
     * @param key
     * @param val
     */
    public static void setPreferenceStringProperty(Context context, String key, String val) {
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, val);
        editor.apply();
    }

    /**
     * 读取SharedPreferences字符串值
     * @param context
     * @param key
     * @return 返回默认值null
     */
    public static String getPreferenceStringProperty(Context context, String key) {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(key, null);
    }

    /**
     * 调用浏览器打开网址
     * @param context
     * @param url
     */
    public static void openUrlWithBrowser(Context context, String url) {
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        context.startActivity(intent);
    }




}
