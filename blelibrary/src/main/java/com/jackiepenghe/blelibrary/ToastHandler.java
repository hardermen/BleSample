package com.jackiepenghe.blelibrary;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.widget.Toast;

/**
 * 自定义Toast专用的Handler
 */
class ToastHandler extends Handler {

    /*--------------------------------私有静态常量--------------------------------*/

    private static final String TAG = ToastHandler.class.getSimpleName();

    /*--------------------------------库内静态常量--------------------------------*/

    /**
     * 显示Toast
     */
    static final int MESSAGE = 1;
    /**
     * 取消toast显示
     */
    static final int CANCEL = 2;
    /**
     * 设置是否重用上次未消失的Toast直接进行显示
     */
    static final int SET_RE_USE = 3;
    /**
     * 当前是否是用于保持Toast显示（超过3000秒时长的Toast）
     */
    static final int KEEP_TOAST = 4;
    /**
     * 当前是否为第一次弹出Toast
     */
    static final int FIRST_SEND = 5;

    /*--------------------------------成员常量--------------------------------*/

    /**
     * 上下文
     */
    private Context context;
    /**
     * Toast实例
     */
    private Toast toast;
    /**
     * 是否重用上次还未消失的Toast
     */
    private static boolean reuse = false;

    /*--------------------------------构造方法--------------------------------*/

    /**
     * Default constructor associates this handler with the {@link Looper} for the
     * current thread.
     * <p>
     * If this thread does not have a looper, this handler won't be able to receive messages
     * so an exception is thrown.
     */
    ToastHandler(Context context) {
        this.context = context;
    }

    /*--------------------------------重写父类方法--------------------------------*/

    /**
     * Subclasses must implement this to receive messages.
     *
     * @param msg 信息
     */
    @Override
    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        int what = msg.what;
        switch (what) {
            case MESSAGE:
                showToast(msg);
                break;
            case CANCEL:
                hideToast();
                break;
            case SET_RE_USE:
                setReuse(msg);
                break;
            default:
                break;
        }
    }

    /*--------------------------------私有方法--------------------------------*/

    /**
     * 设置是否重用未消失的Toast
     *
     * @param msg Message消息
     */
    private void setReuse(Message msg) {
        Object obj = msg.obj;
        if (obj == null) {
            return;
        }
        if (!(obj instanceof Boolean)) {
            return;
        }
        reuse = (boolean) obj;
    }

    /**
     * 隐藏Toast
     */
    private void hideToast() {
        if (toast != null) {
            Tool.warnOut(TAG, "hideToast");
            toast.cancel();
            toast = null;
        }
    }

    /**
     * 显示Toast
     *
     * @param msg Message消息
     */
    @SuppressLint("ShowToast")
    private void showToast(Message msg) {
        Object obj = msg.obj;
        int arg1 = msg.arg1;
        if (obj == null) {
            return;
        }
        if (!(obj instanceof String)) {
            return;
        }
        String messageText = (String) obj;
        if (toast == null) {
            toast = Toast.makeText(context, messageText, Toast.LENGTH_LONG);
        } else {
            if (reuse) {
                toast.setText(messageText);
            } else {
                if (arg1 != KEEP_TOAST) {
                    hideToast();
                }
                toast = Toast.makeText(context, messageText, Toast.LENGTH_LONG);
            }
        }
        Tool.warnOut(TAG, "showToast");
        toast.show();
    }

    static boolean isReuse() {
        return reuse;
    }
}
