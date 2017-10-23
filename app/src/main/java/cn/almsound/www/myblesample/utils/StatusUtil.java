package cn.almsound.www.myblesample.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.support.v4.view.ViewCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;


/**
 * @author alm
 *         Created by ALM on 2016/6/30.
 *         状态栏设置工具类(沉浸式状态栏)
 */
@SuppressWarnings({"unused", "WeakerAccess", "deprecation", "AliDeprecation"})
@TargetApi(19)
public class StatusUtil {

    private static boolean statusFlag = true;

    public static boolean isStatusFlag() {
        return statusFlag;
    }

    public static void setStatusFlag(boolean statusFlag) {
        StatusUtil.statusFlag = statusFlag;
    }

    /**
     * 获取通知栏高度
     *
     * @param activity 对应的activity
     * @return 通知栏高度
     */
    public static int getStatusBarHeight(Activity activity) {
        if (!statusFlag) {
            return 0;
        }
        int result = 0;
        int resourceId = activity.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = activity.getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    /**
     * 通知栏透明化(将整个布局填充全屏,包括通知栏)
     *
     * @param activity 对应的activity
     */
    public static void setStatusTranslucent(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            translucentStatusBarForLOLLIPOP(activity);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            translucentStatusBarForKITKAT1(activity);
        } else {
            try {
                throw (new Throwable("This tool does not support the current API version"));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * 设置通知栏颜色
     *
     * @param activity    对应的activity
     * @param statusColor 颜色
     */
    @SuppressWarnings("WeakerAccess")
    public static void setStatusByColor(Activity activity, int statusColor) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setStatusColorForLOLLIPOP(activity, statusColor);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setStatusColorForKITKAT3(activity, statusColor);
        } else {
            try {
                throw (new Throwable("This tool does not support the current API version"));
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    /**
     * 设置通知栏颜色
     *
     * @param activity   对应的activity
     * @param colorResId 颜色资源ID
     */
    public static void setStatusColorByResource(Activity activity, int colorResId) {
        if (!statusFlag) {
            return;
        }
        int color;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            color = activity.getColor(colorResId);
        } else {
            //noinspection deprecation
            color = activity.getResources().getColor(colorResId);
        }
        setStatusByColor(activity, color);
    }

    /**
     * 设置状态栏颜色（安卓4.4方法3）
     *
     * @param activity    对应的activity
     * @param statusColor int型颜色值
     */
    private static void setStatusColorForKITKAT3(Activity activity, int statusColor) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        int statusBarHeight = getStatusBarHeight(activity);

        View mTopView = mContentView.getChildAt(0);
        if (mTopView != null && mTopView.getLayoutParams() != null && mTopView.getLayoutParams().height == statusBarHeight) {
            //避免重复添加 View
            mTopView.setBackgroundColor(statusColor);
            return;
        }
        //使 ChildView 预留空间
        if (mTopView != null) {
            ViewCompat.setFitsSystemWindows(mTopView, true);
        }

        //添加假 View
        mTopView = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
        mTopView.setBackgroundColor(statusColor);
        mContentView.addView(mTopView, 0, lp);
    }

    /**
     * 设置状态栏颜色（安卓4.4方法2）
     * Using this method will have a black line, and there is no solution
     *
     * @param activity    对应的activity
     * @param statusColor int型颜色值
     */
    private static void setStatusColorForKITKAT2(Activity activity, int statusColor) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        ViewGroup mContentParent = (ViewGroup) mContentView.getParent();

        View statusBarView = mContentParent.getChildAt(0);
        if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == getStatusBarHeight(activity)) {
            //避免重复调用时多次添加 View
            statusBarView.setBackgroundColor(statusColor);
            return;
        }

        //创建一个假的 View, 并添加到 ContentParent
        statusBarView = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                getStatusBarHeight(activity));
        statusBarView.setBackgroundColor(statusColor);
        mContentParent.addView(statusBarView, 0, lp);

        //ChildView 不需要预留系统空间
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }
    }

    /**
     * 设置状态栏颜色（安卓4.4方法1）
     *
     * @param activity    对应的activity
     * @param statusColor int型颜色值
     */
    private static void setStatusColorForKITKAT1(Activity activity, int statusColor) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);

        //First translucent status bar.
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        int statusBarHeight = getStatusBarHeight(activity);

        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
            //如果已经为 ChildView 设置过了 marginTop, 再次调用时直接跳过
            if (lp != null && lp.topMargin < statusBarHeight && lp.height != statusBarHeight) {
                //不预留系统空间
                ViewCompat.setFitsSystemWindows(mChildView, false);
                lp.topMargin += statusBarHeight;
                mChildView.setLayoutParams(lp);
            }
        }

        View statusBarView = mContentView.getChildAt(0);
        if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == statusBarHeight) {
            //避免重复调用时多次添加 View
            statusBarView.setBackgroundColor(statusColor);
            return;
        }
        statusBarView = new View(activity);
        ViewGroup.LayoutParams lp = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, statusBarHeight);
        statusBarView.setBackgroundColor(statusColor);
        //向 ContentView 中添加假 View
        mContentView.addView(statusBarView, 0, lp);
    }

    /**
     * 设置状态栏颜色（安卓5.0以上）
     *
     * @param activity    对应的activity
     * @param statusColor int型颜色值
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void setStatusColorForLOLLIPOP(Activity activity, int statusColor) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        //取消设置透明状态栏,使 ContentView 内容不再覆盖状态栏
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        //设置状态栏颜色
        window.setStatusBarColor(statusColor);

        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 预留出系统 View 的空间.
            ViewCompat.setFitsSystemWindows(mChildView, true);
        }
    }

    /**
     * 将通知栏透明化(安卓5.0以上)
     *
     * @param activity 对应的activity
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private static void translucentStatusBarForLOLLIPOP(Activity activity) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        //设置透明状态栏,这样才能让 ContentView 向上
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //需要设置这个 flag 才能调用 setStatusBarColor 来设置状态栏颜色
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);

        //设置状态栏颜色
        window.setStatusBarColor(Color.TRANSPARENT);

        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            //注意不是设置 ContentView 的 FitsSystemWindows, 而是设置 ContentView 的第一个子 View . 使其不为系统 View 预留空间.
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }
    }

    /**
     * 将通知栏透明化(安卓4.4方法1)
     *
     * @param activity 对应的activity
     */
    private static void translucentStatusBarForKITKAT1(Activity activity) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);

        //首先使 ChildView 不预留空间
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }

        int statusBarHeight = getStatusBarHeight(activity);
        //需要设置这个 flag 才能设置状态栏
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //避免多次调用该方法时,多次移除了 View
        if (mChildView != null && mChildView.getLayoutParams() != null && mChildView.getLayoutParams().height == statusBarHeight) {
            //移除假的 View.
            mContentView.removeView(mChildView);
            mChildView = mContentView.getChildAt(0);
        }
        if (mChildView != null) {
            FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) mChildView.getLayoutParams();
            //清除 ChildView 的 marginTop 属性
            if (lp != null && lp.topMargin >= statusBarHeight) {
                lp.topMargin -= statusBarHeight;
                mChildView.setLayoutParams(lp);
            }
        }
    }

    /**
     * 将通知栏透明化(安卓4.4方法2)
     *
     * @param activity 对应的activity
     */
    private static void translucentStatusBarForKITKAT2(Activity activity) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        ViewGroup mContentParent = (ViewGroup) mContentView.getParent();

        View statusBarView = mContentParent.getChildAt(0);
        if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == getStatusBarHeight(activity)) {
            //移除假的 View
            mContentParent.removeView(statusBarView);
        }
        //ContentView 不预留空间
        if (mContentParent.getChildAt(0) != null) {
            ViewCompat.setFitsSystemWindows(mContentParent.getChildAt(0), false);
        }

        //ChildView 不预留空间
        View mChildView = mContentView.getChildAt(0);
        if (mChildView != null) {
            ViewCompat.setFitsSystemWindows(mChildView, false);
        }
    }

    /**
     * 将通知栏透明化(安卓4.4方法3)
     *
     * @param activity 对应的activity
     */
    private static void translucentStatusBarForKITKAT3(Activity activity) {
        if (!statusFlag) {
            return;
        }
        Window window = activity.getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        ViewGroup mContentView = activity.findViewById(Window.ID_ANDROID_CONTENT);
        View statusBarView = mContentView.getChildAt(0);
        //移除假的 View
        if (statusBarView != null && statusBarView.getLayoutParams() != null && statusBarView.getLayoutParams().height == getStatusBarHeight(activity)) {
            mContentView.removeView(statusBarView);
        }
        //不预留空间
        if (mContentView.getChildAt(0) != null) {
            ViewCompat.setFitsSystemWindows(mContentView.getChildAt(0), false);
        }
    }
}
