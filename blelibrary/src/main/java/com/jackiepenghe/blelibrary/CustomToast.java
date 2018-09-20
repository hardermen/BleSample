package com.jackiepenghe.blelibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Message;
import android.support.annotation.StringRes;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 自定义Toast，可实现自定义显示时间,兼容至安卓N及以上版本
 */
class CustomToast {

    /**
     * 长时间的吐司持续时间
     */
    static final int LENGTH_LONG = 3500;
    /**
     * 短时间的吐司持续时间
     */
    static final int LENGTH_SHORT = 2000;

    /*--------------------------------静态变量--------------------------------*/

    /**
     * 自定义吐司本类单例
     */
    @SuppressLint("StaticFieldLeak")
    private static CustomToast customToast;

    /**
     * 吐司专用的handler(使用Handler可以避免定时器在非主线程中导致的线程问题)
     */
    @SuppressLint("StaticFieldLeak")
    private static ToastHandler toastHandler;
    /**
     * 是否重用上次未消失的Toast的标志（缓存标志），实际标志在handler中
     */
    private static boolean reuse = false;
    /**
     * showToast的定时任务
     */
    private static ScheduledExecutorService SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE;
    /**
     * hideToast的定时任务
     */
    private static ScheduledExecutorService HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE;

    /*--------------------------------成员变量--------------------------------*/

    /**
     * 上下文
     */
    private Context context;
    /**
     * Toast文本内容
     */
    private String messageText;
    /**
     * Toast持续时长
     */
    private int duration;

    /*--------------------------------构造方法--------------------------------*/

    /**
     * 构造方法
     *
     * @param context     上下文
     * @param messageText Toast文本内容
     * @param duration    Toast持续时间（单位：毫秒）
     */
    private CustomToast(Context context, String messageText, int duration) {
        this.context = context;
        this.messageText = messageText;
        this.duration = duration;
    }

    /*--------------------------------私有静态方法--------------------------------*/

    /**
     * 显示Toast
     *
     * @param context     上下文
     * @param messageText Toast文本内容
     * @param duration    Toast持续时间（单位：毫秒）
     */
    @SuppressLint("ShowToast")
    private static void showMyToast(final Context context, final String messageText, int duration) {
        if (toastHandler == null) {
            toastHandler = new ToastHandler(context.getApplicationContext());
        }
        setHandlerReuse();
        if (SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE != null) {
            SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
        }

        if (HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE != null) {
            HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
            HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
        }
        SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = BleManager.newScheduledExecutorService();
        HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = BleManager.newScheduledExecutorService();
        final boolean[] first = {true};
        SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (first[0]) {
                    handlerShowToast(messageText, ToastHandler.FIRST_SEND);
                    first[0] = false;
                } else {
                    handlerShowToast(messageText, ToastHandler.KEEP_TOAST);
                }
            }
        }, 0, 3000, TimeUnit.MILLISECONDS);

        HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.schedule(new Runnable() {
            @Override
            public void run() {
                SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
                SHOW_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
                handlerCancelToast();
                HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE.shutdownNow();
                HIDE_TOAST_KEEP_SCHEDULED_EXECUTOR_SERVICE = null;
            }
        }, duration, TimeUnit.MILLISECONDS);
    }

    /**
     * 使用Handler显示Toast
     *
     * @param messageText Toast文本内容
     * @param arg         是否为定时器保持消息显示
     */
    private static void handlerShowToast(String messageText, int arg) {
        Message message = new Message();
        message.obj = messageText;
        message.what = ToastHandler.MESSAGE;
        message.arg1 = arg;
        toastHandler.sendMessage(message);
    }

    /**
     * 使用Handler取消Toast
     */
    private static void handlerCancelToast() {
        Message message = new Message();
        message.what = ToastHandler.CANCEL;
        toastHandler.sendMessage(message);
    }

    /**
     * 设置Handler是否重用未消失的Toast
     */
    private static void setHandlerReuse() {
        Message message = new Message();
        message.what = ToastHandler.SET_RE_USE;
        message.obj = reuse;
        toastHandler.sendMessage(message);
    }

    /*--------------------------------公开方法--------------------------------*/

    /**
     * 显示吐司
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    public void show() {
        showMyToast(context, messageText, duration);
    }

    /**
     * 设置是否重用（缓存位，每次在显示Toast前会将其设置到Handler中）
     *
     * @param reuse true表示开启重用
     */
    @SuppressWarnings("WeakerAccess")
    static void setReuse(boolean reuse) {
        CustomToast.reuse = reuse;
    }

    /*--------------------------------公开静态方法--------------------------------*/

    /**
     * 获取CustomToast本类
     *
     * @param context  上下文
     * @param message  吐司显示信息
     * @param duration 吐司显示时长
     * @return CustomToast本类
     */
    public static CustomToast makeText(Context context, String message, int duration) {
        if (customToast == null) {
            synchronized (CustomToast.class) {
                if (customToast == null) {
                    customToast = new CustomToast(context.getApplicationContext(), message, duration);
                } else {
                    customToast.messageText = message;
                    customToast.duration = duration;
                }
            }
        } else {
            customToast.messageText = message;
            customToast.duration = duration;
        }
        return customToast;
    }

    /**
     * CustomToast本类
     *
     * @param context    上下文
     * @param messageRes 吐司显示信息
     * @param duration   吐司显示时长
     * @return CustomToast本类
     */
    public static CustomToast makeText(Context context, @StringRes int messageRes, int duration) {
        String message = context.getString(messageRes);
        if (customToast == null) {
            synchronized (CustomToast.class) {
                if (customToast == null) {
                    customToast = new CustomToast(context.getApplicationContext(), message, duration);
                } else {
                    customToast.messageText = message;
                    customToast.duration = duration;
                }
            }
        } else {
            customToast.messageText = message;
            customToast.duration = duration;
        }
        return customToast;
    }
}
