package cn.almsound.www.myblesample.utils;

import android.content.Context;

/**
 * 吐司工具类
 * Created by John on 2016/3/15.
 */
public class ToastUtil {


    /**
     * 弹出吐司
     * @param context 上下文
     * @param resId 吐司内容的资源id
     * @param duration 吐司持续时间
     */
    private static void showToast(Context context, int resId, int duration){
       CustomToast.makeText(context,resId,duration).show();
    }

    /**
     *  弹出吐司
     * @param context 上下文
     * @param msg 吐司内容
     * @param duration 吐司持续时间
     */
    private static void showToast(Context context, String msg, int duration) {
       CustomToast.makeText(context,msg,duration).show();
    }

    /**
     * 长时间的吐司
     *
     * @param context 上下文
     * @param msg     要进行吐司的String字符串
     */
    public static void L(Context context, String msg) {
        showToast(context, msg, CustomToast.LENGTH_LONG);
    }

    /**
     * 长时间的吐司
     *
     * @param context 上下文
     * @param msgId   要进行吐司的字符串id
     */
    public static void L(Context context, int msgId) {
        showToast(context, msgId, CustomToast.LENGTH_LONG);
    }

    /**
     * 短时间的吐司
     *
     * @param context 上下文
     * @param msg     要进行吐司的String字符串
     */
    public static void S(Context context, String msg) {
        showToast(context, msg, CustomToast.LENGTH_SHORT);
    }

    /**
     * 短时间的吐司
     *
     * @param context 上下文
     * @param msgId   要进行吐司的String字符串
     */
    public static void S(Context context, int msgId) {
        showToast(context, msgId, CustomToast.LENGTH_SHORT);
    }

    public static void T(Context context,String msg,int duration){
        showToast(context, msg,duration);
    }

    public static void T(Context context,int msgRes,int duration){
        showToast(context, msgRes,duration);
    }
}