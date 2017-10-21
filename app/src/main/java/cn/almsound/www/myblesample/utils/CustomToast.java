package cn.almsound.www.myblesample.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.support.annotation.StringRes;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by alm on 17-6-5.
 * 自定义Toast，可实现自定义显示时间
 */

@SuppressWarnings({"WeakerAccess", "unused"})
class CustomToast {
    private Toast mToast;
    private static int mDuration;
    private Object mTN;
    private Method show;
    private Method hide;
    private Handler mHandler = new Handler();

    /**
     * 短时间
     */
    @SuppressWarnings("WeakerAccess")
    public static int LENGTH_SHORT = 2000;

    /**
     * 长时间
     */
    @SuppressWarnings("WeakerAccess")
    public static int LENGTH_LONG = 3000;

    /**
     * 总是显示，不消失
     */
    public static int LENGTH_ALWAYS = 0;

    private Runnable mHide = this::hide;

    private CustomToast(Toast toast) {
        mToast = toast;

    }

    public static CustomToast makeText(Context context, String message, int duration) {
        @SuppressLint("ShowToast") Toast toast = Toast.makeText(context, message, duration);
        mDuration = duration;
        return new CustomToast(toast);
    }



    public static CustomToast makeText(Context context, @StringRes int messageRes, int duration) {
        @SuppressLint("ShowToast") Toast toast = Toast.makeText(context, messageRes, duration);
        mDuration = duration;
        return new CustomToast(toast);
    }


    @SuppressWarnings("TryWithIdenticalCatches")
    private void hide() {
        try {
            hide.invoke(mTN);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    public void show(){
        initTN();
        try {
            show.invoke(mTN);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (mDuration > 0){
            mHandler.postDelayed(mHide,mDuration);
        }else if(mDuration < 0){
            mHandler.postDelayed(mHide,LENGTH_LONG);
        }
    }

    @SuppressWarnings("TryWithIdenticalCatches")
    private void initTN() {
        try {
            Field tnField= mToast.getClass().getDeclaredField("mTN");
            tnField.setAccessible(true);
            mTN = tnField.get(mToast);

            show = mTN.getClass().getDeclaredMethod("show");
            hide = mTN.getClass().getDeclaredMethod("hide");

            Field tnTextViewField = mTN.getClass().getDeclaredField("mNextView");
            tnTextViewField.setAccessible(true);
            tnTextViewField.set(mTN,mToast.getView());

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
