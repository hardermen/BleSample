package cn.almsound.www.myblesample.adapter.base;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author alm
 *         Created by ALM on 2016/7/7.
 *         文件工具类
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class FileUtil {
    /**
     * 应用程序名(在文件管理器中的程序文件夹名)
     */
    private static final String APP_NAME = Application.class.getName();

    /**
     * 项目数据存储目录
     */
    public static final File DATA_DIR = getDataDir();

    /**
     * apk存储目录
     */
    public static final File APK_DIR = getApkDir();

    /**
     * 项目文件目录
     */
    public static final File APP_DIR = getAppDir();

    /**
     * 项目缓存目录
     */
    public static final File APP_CACHE = getCache();

    /**
     * 项目图片缓存目录
     */
    public static final File IMAGE_DIR = getImageDir();

    /**
     * 获取SD卡文件目录
     *
     * @return 获取SD卡路径
     */
    private static File getSDCardDir() {
        //获取挂载状态
        String state = Environment.getExternalStorageState();

        //如果是已挂载,说明了有内存卡
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return Environment.getExternalStorageDirectory();
        }
        throw new NullPointerException("没有sd卡哦...");
    }

    /**
     * 获取本项目文件目录
     *
     * @return 本项目文件目录
     */
    private static File getAppDir() {
        return new File(getSDCardDir(), APP_NAME);
    }

    /**
     * 获取项目缓存文件目录
     *
     * @return 项目缓存文件目录
     */
    private static File getCache() {
        return new File(getAppDir(), "cache");
    }

    /**
     * 获取apk存储目录
     *
     * @return apk存储目录
     */
    private static File getApkDir() {
        return new File(getAppDir(), "apk");
    }

    /**
     * 获取图片目录
     *
     * @return 图片目录
     */
    private static File getImageDir() {
        File imageDir = new File(getAppDir(), "image");
        boolean mkdirs;
        if (!imageDir.exists()) {
            mkdirs = imageDir.mkdirs();
            if (mkdirs) {
                return imageDir;
            } else {
                return null;
            }
        }
        return imageDir;
    }

    /**
     * 安装apk
     *
     * @param context 上下文
     * @param file    文件
     */
    public static void installApk(Context context, File file) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Intent.ACTION_VIEW);
        String type = "application/vnd.android.package-archive";
        intent.setDataAndType(Uri.fromFile(file), type);
        context.startActivity(intent);
    }

    /**
     * 获取项目数据目录
     *
     * @return 项目数据目录
     */
    private static File getDataDir() {
        return new File(getAppDir(), "data");
    }

    /**
     * 删除文件
     *
     * @param fileName 文件名
     */
    public static boolean delFile(String fileName) {
        File file = new File(getAppDir(), fileName);
        if (file.isFile()) {
            //noinspection ResultOfMethodCallIgnored
            file.delete();
            return true;
        }
        //noinspection ResultOfMethodCallIgnored
        file.exists();
        return false;
    }

    /**
     * 保存图片
     *
     * @param bm      位图图片
     * @param picName 文件名
     */
    public static void saveBitmap(Bitmap bm, String picName) {
        Log.e("", "保存图片");
        try {

            File f = new File(getCache(), picName + ".JPEG");
            if (f.exists()) {
                //noinspection ResultOfMethodCallIgnored
                f.delete();
            }

            FileOutputStream out = new FileOutputStream(f);
            bm.compress(Bitmap.CompressFormat.JPEG, 90, out);
            out.flush();
            out.close();
            Log.e("", "已经保存");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 检查文件是否存在
     *
     * @param fileName 文件名
     * @return true表示文件存在
     */
    public static boolean isFileExist(String fileName) {
        File file = new File(getAppDir(), fileName);
        //noinspection ResultOfMethodCallIgnored
        file.isFile();
        return file.exists();
    }

    /**
     * 检查文件是否存在
     *
     * @param path 文件路径
     * @return true表示文件存在
     */
    public static boolean fileIsExists(String path) {
        try {
            File f = new File(path);
            if (!f.exists()) {
                return false;
            }
        } catch (Exception e) {

            return false;
        }
        return true;
    }
}
