package com.jackiepenghe.blelibrary;

import android.content.Context;
import android.os.Build;
import android.support.annotation.StringRes;
import android.util.Log;
import android.widget.Toast;

/**
 * 工具类
 *
 * @author alm
 */

class Tool {
    //log部分

    /**
     * 是否打印日志信息的标志
     */
    private static boolean mDebug = false;

    /**
     * 获取当前日志打印标志
     *
     * @return 日志打印标志
     */
    @SuppressWarnings("unused")
    public static boolean isDebug() {
        return mDebug;
    }

    /**
     * 设置日志打印标志
     *
     * @param debug 日志打印标志
     */
    public static void setDebugFlag(@SuppressWarnings("SameParameterValue") boolean debug) {
        mDebug = debug;
    }

    /**
     * 等同于Log.e
     *
     * @param tag     tag
     * @param message 日志信息
     */
    @SuppressWarnings("WeakerAccess")
    public static void errorOut(String tag, String message) {
        if (!mDebug) {
            return;
        }
        Log.e(tag, message);
    }

    /**
     * 等同于Log.w
     *
     * @param tag     tag
     * @param message 日志信息
     */
    @SuppressWarnings("WeakerAccess")
    public static void warnOut(String tag, String message) {
        if (!mDebug) {
            return;
        }
        Log.w(tag, message);
    }

    //Toast部分

    /**
     * 弹出Toast
     *
     * @param context    上下文
     * @param messageRes 信息
     * @param duration   持续时间
     */
    private static void showToast(Context context, @StringRes int messageRes, int duration) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
            CustomToast.makeText(context, messageRes, duration).show();
            return;
        }
        switch (duration) {
            case CustomToast.LENGTH_LONG:
                Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show();
                break;
            case CustomToast.LENGTH_SHORT:
                Toast.makeText(context, messageRes, Toast.LENGTH_SHORT).show();
                break;
            default:
                Toast.makeText(context, messageRes, Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * 长时间的吐司
     *
     * @param context    上下文
     * @param messageRes 信息
     */
    public static void toastL(Context context, @StringRes int messageRes) {
        showToast(context, messageRes, CustomToast.LENGTH_LONG);
    }

    //数据转换部分

    /**
     * bytes转换成十六进制字符串
     *
     * @param bytes byte数组
     * @return String 每个Byte值之间空格分隔
     */
    @SuppressWarnings("WeakerAccess")
    public static String bytesToHexStr(byte[] bytes) {

        if (bytes == null) {
            return "";
        }

        String stmp;
        StringBuilder sb = new StringBuilder("");
        for (byte aByte : bytes) {
            stmp = Integer.toHexString(aByte & 0xFF);
            sb.append((stmp.length() == 1) ? "0" + stmp : stmp);
            sb.append(" ");
        }
        return sb.toString().toUpperCase().trim();
    }

    /**
     * bytes字符串转换为Byte值
     *
     * @param src Byte字符串，每个Byte之间没有分隔符
     * @return byte[]
     */
    @SuppressWarnings("unused")
    public static byte[] hexStrToBytes(String src) {
        int m, n;
        int l = src.length() / 2;
        System.out.println(l);
        byte[] ret = new byte[l];
        for (int i = 0; i < l; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int integer = Integer.decode("0x" + src.substring(i * 2, m) + src.substring(m, n));
            ret[i] = (byte) integer;
        }
        return ret;
    }

    /**
     * 十六进制转换字符串
     *
     * @param hexStr 字符串
     *
     * @return String 对应的字符串
     */
    public static String hexStrToStr(String hexStr) {
        String str = "0123456789ABCDEF";
        char[] hexs = hexStr.toCharArray();
        byte[] bytes = new byte[hexStr.length() / 2];
        int n;
        for (int i = 0; i < bytes.length; i++) {
            n = str.indexOf(hexs[2 * i]) * 16;
            n += str.indexOf(hexs[2 * i + 1]);
            bytes[i] = (byte) (n & 0xff);
        }
        return new String(bytes);
    }

    /**
     * 使用死循环使当前线程延迟一段时间执行下一个指令
     *
     * @param sleepTime 延迟时间
     */
    public static void sleep(int sleepTime) {
        long currentTimeMillis = System.currentTimeMillis();
        while (true) {
            if (System.currentTimeMillis() - currentTimeMillis >= sleepTime) {
                break;
            }
        }
    }
}
