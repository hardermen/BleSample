package com.jackiepenghe.blelibrary;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;

/**
 * BlE管理类
 *
 * @author alm
 */

public class BleManager {

    /*------------------------静态常量----------------------------*/

    /**
     * Handler
     */
    private static final Handler HANDLER = new Handler();

    /*------------------------静态变量----------------------------*/

    /**
     * Ble连接实例
     */
    @SuppressLint("StaticFieldLeak")
    private static BleConnector bleConnector;
    /**
     * Ble扫描实例
     */
    @SuppressLint("StaticFieldLeak")
    private static BleScanner bleScanner;
    /**
     * Ble多连接实例
     */
    @SuppressLint("StaticFieldLeak")
    private static BleMultiConnector bleMultiConnector;
    /**
     * Ble广播实例
     */
    @SuppressLint("StaticFieldLeak")
    private static BleAdvertiser bleAdvertiser;
    /**
     * 重置Ble广播实例的标志（避免无限循环调用）
     */
    private static boolean resetBleAdvertiserFlag;
    /**
     * 上下文
     */
    @SuppressLint("StaticFieldLeak")
    private static Context context;
    /*------------------------库内静态函数----------------------------*/

    /**
     * 重置bleMultiConnector避免内存泄漏
     */
    static void resetBleMultiConnector() {
        if (bleMultiConnector != null) {
            bleMultiConnector.closeAll();
        }
        bleMultiConnector = null;
    }

    /**
     * 重置Ble广播实例
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    static void resetBleAdvertiser() {
        if (resetBleAdvertiserFlag) {
            return;
        }
        resetBleAdvertiserFlag = true;

        if (bleAdvertiser != null) {
            bleAdvertiser.close();
        }
        bleAdvertiser = null;
        resetBleAdvertiserFlag = false;
    }

    /**
     * @return Handler
     */
    static Handler getHandler() {
        return HANDLER;
    }

    /*------------------------公开静态函数----------------------------*/

    /**
     * 判断手机是否支持BLE
     *
     * @return true表示支持BLE
     */
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isSupportBle() {
        checkInitStatus();
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 初始化
     *
     * @param context 上下文
     */
    public static void init(@NonNull Context context) {
        BleManager.context = context.getApplicationContext();
    }

    /**
     * 创建一个新的BLE连接实例
     *
     * @return BleConnector
     */
    @Deprecated
    public static BleConnector newBleConnector() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }
        return new BleConnector(context);
    }

    /**
     * 获取BLE连接实例的单例
     *
     * @return BleConnector单例
     */
    public static BleConnector getBleConnectorInstance() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }

        if (bleConnector == null) {
            synchronized (BleManager.class) {
                if (bleConnector == null) {
                    bleConnector = new BleConnector(context);
                }
            }
        } else {
            if (bleConnector.getContext() == null) {
                bleConnector = null;
            }

            if (bleConnector == null) {
                synchronized (BleManager.class) {
                    if (bleConnector == null) {
                        bleConnector = new BleConnector(context);
                    }
                }
            }
        }
        return bleConnector;
    }

    /**
     * 创建一个新的BLE扫描实例
     *
     * @return BleScanner
     */
    @Deprecated
    public static BleScanner newBleScanner() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }
        return new BleScanner(context);
    }

    /**
     * 获取BLE扫描实例的单例
     *
     * @return BleScanner单例
     */
    public static BleScanner getBleScannerInstance() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }
        if (bleScanner == null) {
            synchronized (BleManager.class) {
                if (bleScanner == null) {
                    bleScanner = new BleScanner(context);
                }
            }
        } else {
            if (bleScanner.getContext() == null) {
                bleScanner = null;
            }

            if (bleScanner == null) {
                synchronized (BleManager.class) {
                    if (bleScanner == null) {
                        bleScanner = new BleScanner(context);
                    }
                }
            }
        }
        return bleScanner;
    }

    /**
     * 获取蓝牙广播单例实例
     *
     * @return BleAdvertiser
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static BleAdvertiser getBleAdvertiserInstance() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }
        if (bleAdvertiser == null) {
            synchronized (BleManager.class) {
                if (bleAdvertiser == null) {
                    bleAdvertiser = new BleAdvertiser(context);
                }
            }
        }
        return bleAdvertiser;
    }

    /**
     * 获取蓝牙广播单例实例
     *
     * @return BleAdvertiser
     */
    @Deprecated
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static BleAdvertiser newBleAdvertiser() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }

        return new BleAdvertiser(context);
    }


    /**
     * 获取BLE多连接单例
     *
     * @return BleMultiConnector
     */
    public static BleMultiConnector getBleMultiConnectorInstance() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }
        if (bleMultiConnector == null) {
            synchronized (BleManager.class) {
                if (bleMultiConnector == null) {
                    bleMultiConnector = new BleMultiConnector(context.getApplicationContext());
                }
            }
        }
        return bleMultiConnector;
    }


    /**
     * 获取BLE多连接单例
     *
     * @return BleMultiConnector
     */
    @Deprecated
    public static BleMultiConnector newBleMultiConnector() {
        checkInitStatus();
        if (!isSupportBle()) {
            return null;
        }
        return new BleMultiConnector(context);
    }

    /**
     * 判断当前手机蓝牙是否打开
     *
     * @return true表示蓝牙已打开
     */
    @SuppressWarnings("unused")
    public static boolean isBluetoothOpened() {
        checkInitStatus();
        if (!isSupportBle()) {
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();
        return adapter != null && adapter.isEnabled();
    }

    /**
     * 请求打开蓝牙
     *
     * @return true表示请求发起成功（只是发起打开蓝牙的请求成功，并不是开启蓝牙成功）
     */
    @SuppressWarnings("unused")
    public static boolean openBluetooth() {
        checkInitStatus();
        if (!isSupportBle()) {
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        return adapter != null && adapter.enable();

    }

    /**
     * 请求关闭蓝牙
     *
     * @param context 上下文
     * @return true表示请求发起成功（只是发起关闭蓝牙的请求成功，并不是关闭蓝牙成功）
     */
    @SuppressWarnings("unused")
    public static boolean closeBluetooth(Context context) {
        checkInitStatus();
        if (!isSupportBle()) {
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        return adapter != null && adapter.disable();
    }

    /**
     * 释放BLE连接工具的资源
     */
    @SuppressWarnings("WeakerAccess")
    public static void releaseBleConnector() {
        checkInitStatus();
        if (bleConnector != null) {
            if (bleConnector.getContext() != null) {
                bleConnector.close();
            }
            bleConnector = null;
        }
    }

    /**
     * 释放BLE扫描器的资源
     */
    @SuppressWarnings("WeakerAccess")
    public static void releaseBleScanner() {
        checkInitStatus();
        if (bleScanner != null) {
            bleScanner.close();
            bleScanner = null;
        }
    }

    /**
     * 释放BLE多连接器的资源
     */
    @SuppressWarnings("WeakerAccess")
    public static void releaseBleMultiConnector() {
        checkInitStatus();
        if (bleMultiConnector != null) {
            bleMultiConnector.closeAll();
            bleConnector = null;
        }
    }

    /**
     * 释放BLE广播实例的资源
     */
    @SuppressWarnings("WeakerAccess")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static void releaseBleAdvertiser() {
        checkInitStatus();
        if (bleAdvertiser != null) {
            bleAdvertiser.close();
            bleAdvertiser = null;
        }
    }

    /**
     * 释放全部实例的资源
     */
    public static void releaseAll() {
        checkInitStatus();
        releaseBleConnector();
        releaseBleScanner();
        releaseBleMultiConnector();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            releaseBleAdvertiser();
        }
    }

    /**
     * 设置是否要打印出调式信息
     *
     * @param debugFlag true表示要打印
     */
    public static void setDebugFlag(boolean debugFlag) {
        Tool.setDebugFlag(debugFlag);
    }

    /*------------------------私有静态函数----------------------------*/

    /**
     * 校验是否初始化成功
     */
    private static void checkInitStatus(){
        if (context == null){
            throw new IllegalStateException("Please invoke method \"init(Context context)\" in your Applications class");
        }
    }
}
