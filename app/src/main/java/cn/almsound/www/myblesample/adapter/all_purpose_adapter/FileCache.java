package cn.almsound.www.myblesample.adapter.all_purpose_adapter;

import android.content.Context;

import java.io.File;

/**
 * 文件缓存工具
 * Created by ALM on 2016/7/7.
 */
class FileCache {

    /**
     * 缓存目录
     */
    private File cacheDir;

    /**
     * 构造器
     */
    FileCache(Context context) {
        // 如果有SD卡则在SD卡中建一个LazyList的目录存放缓存的图片
        // 没有SD卡就放在系统的缓存目录中
        if (android.os.Environment.getExternalStorageState().equals(
                android.os.Environment.MEDIA_MOUNTED)) {
            /*cacheDir = new File(
                    android.os.Environment.getExternalStorageDirectory(),
                    "LazyList");*/
            cacheDir = FileUtil.App_Cache;
        } else {
            cacheDir = context.getCacheDir();
        }

        //如果目录不存在，那么创建一个缓存目录
        if (!cacheDir.exists()) {
            //noinspection ResultOfMethodCallIgnored
            cacheDir.mkdirs();
        }
    }

    /**
     * 根据url获取缓存文件
     *
     * @param url 缓存文件url
     * @return 缓存文件
     */
    File getFile(String url) {
        // 将url的hashCode作为缓存的文件名
        String filename = String.valueOf(url.hashCode());
        // Another possible solution
        // String filename = URLEncoder.encode(url);
        return new File(cacheDir, filename);

    }

    /**
     * 清除缓存
     */
    void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null) {
            return;
        }

        for (File f : files) {
            //noinspection ResultOfMethodCallIgnored
            f.delete();
        }
    }

}