package cn.almsound.www.myblesample.adapter.all_purpose_adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.widget.ImageView;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.utils.LogUtil;


/**
 * 图片加载类
 * Created by ALM on 2016/7/7.
 */
@SuppressWarnings({"WeakerAccess", "unused"})
public class ImageLoader {

    /**
     * 内存缓存工具
     */
    private MemoryCache memoryCache = new MemoryCache();

    /**
     * 文件缓存工具
     */
    private FileCache fileCache;

    /**
     * 存储图片空间的集合
     */
    private Map<ImageView, String> imageViews = Collections
            .synchronizedMap(new WeakHashMap<ImageView, String>());

    /**
     * 线程池
     */
    private ExecutorService executorService;

    /**
     * 默认图片
     */
    private final int defaultDrawable = R.mipmap.ic_launcher;

    /**
     * 图片是否是圆形
     */
    private boolean isCircle;

    /**
     * 图片加载器单例
     */
    private static ImageLoader instance;

    /**
     * 是否压缩图片
     */
    private boolean compress;

    /**
     * 构造器
     *
     * @param context 上下文
     */
    private ImageLoader(Context context) {
        fileCache = new FileCache(context);
        executorService = Executors.newFixedThreadPool(10);
    }

    /**
     * 获取ImageLoader单例
     *
     * @param context 上下文对象
     * @return 当前ImageLoader对象
     */
    public static ImageLoader getInstance(Context context) {
        if (instance == null) {
            synchronized (ImageLoader.class) {
                if (instance == null) {
                    instance = new ImageLoader(context);
                }
            }
        }
        return instance;
    }

    /**
     * 显示图片
     *
     * @param url       文件url
     * @param imageView ImageView
     * @param circle    是否显示圆形图片
     */
    public void displayImage(String url, ImageView imageView, boolean circle) {
        isCircle = circle;
        imageViews.put(imageView, url);
        // 先从内存缓存中查找
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null) {
            imageView.setImageBitmap(bitmap);
        } else {
            imageView.setImageResource(defaultDrawable);
            // 若没有的话则开启新线程加载图片
            loadBitmapFromNet(url, imageView);
        }
    }

    /**
     * 清除缓存
     */
    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

    /**
     * 防止图片错位
     *
     * @param holder BitmapHolder
     * @return false表示错位
     */
    private boolean imageViewReused(BitmapHolder holder) {
        String tag = imageViews.get(holder.imageView);

        return tag == null || !tag.equals(holder.url);
    }

    /**
     * 下载图片操作
     *
     * @param is InputStream
     * @param os OutputStream
     */
    private void copyStream(InputStream is, OutputStream os) {
        final int buffer_size = 1024;
        try {
            byte[] bytes = new byte[buffer_size];
            while (true) {
                int count = is.read(bytes, 0, buffer_size);
                if (count == -1) {
                    break;
                }
                os.write(bytes, 0, count);
            }
        } catch (Exception e) {
            LogUtil.e("ImageLoader", "ImageLoader 保存文件失败...");
            // e.printStackTrace();
        }
    }

    /**
     * 加载网络图片
     *
     * @param url       网络url
     * @param imageView ImageView
     */
    private void loadBitmapFromNet(String url, ImageView imageView) {
        BitmapHolder p = new BitmapHolder(url, imageView);
        executorService.submit(new BitmapLoader(p));
    }

    /**
     * 获取图片 先从缓存中去查找，如果没有再从网络下载
     *
     * @param url 图片地址
     * @return 位图图片
     */
    @SuppressWarnings("WeakerAccess")
    public Bitmap getBitmap(String url) {
        File f = fileCache.getFile(url);

        // 先从文件缓存中查找是否有
        Bitmap b = decodeFile(f);
        if (b != null) {
            return b;
        }

        // 最后从指定的url中下载图片
        try {
            Bitmap bitmap;
            URL imageUrl = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) imageUrl
                    .openConnection();
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setInstanceFollowRedirects(true);
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(f);
            copyStream(is, os);
            os.close();
            bitmap = decodeFile(f);
            return bitmap;
        } catch (Exception e) {
            // e.printStackTrace();
            LogUtil.d("ImageLoader getBitmap Exception");
            return null;
        }
    }

    /**
     * 压缩图片
     *
     * @param f 图片的本地路径
     * @return 位图图片
     */
    private Bitmap decodeFile(File f) {
        try {
            Bitmap bitmap;
            if (compress) {
                // 不加载图片的情况下获得图片的宽高
                BitmapFactory.Options o = new BitmapFactory.Options();
                o.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(new FileInputStream(f), null, o);

                final int REQUIRED_SIZE = 70;
                int width_tmp = o.outWidth, height_tmp = o.outHeight;
                int scale = 1;
                // 如果长或宽大于70，那么把图片的高宽缩小一半
                while (true) {
                    if (width_tmp / 2 < REQUIRED_SIZE
                            || height_tmp / 2 < REQUIRED_SIZE) {
                        break;
                    }
                    width_tmp /= 2;
                    height_tmp /= 2;
                    scale *= 2;
                }

                BitmapFactory.Options o2 = new BitmapFactory.Options();
                o2.inSampleSize = scale;
                // 把图片的高宽缩小一半
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f),
                        null, o2);
            } else {
                BitmapFactory.Options o = new BitmapFactory.Options();
                bitmap = BitmapFactory.decodeStream(new FileInputStream(f),
                        null, o);
            }

            return isCircle ? createCircleBitmap(bitmap) : bitmap;
        } catch (FileNotFoundException e) {
            LogUtil.e("ImageLoader", e.getMessage());
            // e.printStackTrace();
        }
        return null;
    }

    /**
     * 设置是否压缩图片
     *
     * @param isCompress 要设置是否压缩图片的标志
     */
    public void setCompress(boolean isCompress) {
        compress = isCompress;
    }

    /**
     * 用来封装Url和ImageView，防止图片错位里用
     */
    private class BitmapHolder {
        String url;

        ImageView imageView;

        BitmapHolder(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    /**
     * 图片下载器
     */
    private class BitmapLoader implements Runnable {
        BitmapHolder holder;

        BitmapLoader(BitmapHolder photoToLoad) {
            this.holder = photoToLoad;
        }

        @Override
        public void run() {
            // 如果没有错位那么不做任何处理，如果错位那么需要再做一次加载处理
            if (imageViewReused(holder)) {
                return;
            }
            Bitmap bmp = getBitmap(holder.url);
            memoryCache.put(holder.url, bmp);

            if (imageViewReused(holder)) {
                return;
            }
            BitmapDisplayer displayer = new BitmapDisplayer(bmp, holder);
            // 更新的操作放在UI线程中
            holder.imageView.post(displayer);

        }
    }

    /**
     * 图片显示器
     */
    private class BitmapDisplayer implements Runnable {
        Bitmap bitmap;

        BitmapHolder holder;

        BitmapDisplayer(Bitmap b, BitmapHolder bh) {
            bitmap = b;
            holder = bh;
        }

        @Override
        public void run() {
            if (imageViewReused(holder)) {
                return;
            }

            if (bitmap != null) {
                holder.imageView.setImageBitmap(bitmap);
            } else {
                holder.imageView.setImageResource(defaultDrawable);
            }
        }
    }

    /**
     * 把 源圆片 加工成 圆形图片
     *
     * @param resource 源圆片
     * @return 位图图片
     */
    private Bitmap createCircleBitmap(Bitmap resource) {
        int width = resource.getWidth();
        Paint paint = new Paint();
        // 画圆或者弧形图，需要抗锯齿
        paint.setAntiAlias(true);

        // 创建一张空图片, 这张图片只有宽高，没有内容
        //noinspection SuspiciousNameCombination
        Bitmap target = Bitmap.createBitmap(width, width, Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(target);

        //图片边线宽度
        int strokeWidth = 0;
        // 画一个和原图片宽高一样的内切圆
        canvas.drawCircle(width / 2, width / 2, (width - strokeWidth) / 2,
                paint);

        // 取两图的交集(也就是重合的部分)
        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));

        // 把源图覆盖上去
        canvas.drawBitmap(resource, 0, 0, paint);

        return target;
    }
}
