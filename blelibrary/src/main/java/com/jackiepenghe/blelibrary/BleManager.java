package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.RequiresApi;

/**
 * BlE管理类
 * @author alm
 */

public class BleManager {

    /*------------------------静态变量----------------------------*/

    /**
     * Ble连接实例
     */
    private static BleConnector bleConnector;
    /**
     * Ble扫描实例
     */
    private static BleScanner bleScanner;
    /**
     * Ble多连接实例
     */
    private static BleMultiConnector bleMultiConnector;
    /**
     * Ble广播实例
     */
    private static BleBroadCastor bleBroadCastor;
    /**
     * 重置Ble广播实例的标志（避免无限循环调用）
     */
    private static boolean resetBleBroadCastorFlag;

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
    static void resetBleBroadCastor() {
        if (resetBleBroadCastorFlag) {
            return;
        }
        resetBleBroadCastorFlag = true;

        if (bleBroadCastor != null) {
            bleBroadCastor.close();
        }
        bleBroadCastor = null;
        resetBleBroadCastorFlag = false;
    }

    /*------------------------公开静态函数----------------------------*/

    /**
     * 判断手机是否支持BLE
     *
     * @param context 上下文
     * @return true表示支持BLE
     */
    public static boolean isSupportBle(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /**
     * 创建一个新的BLE连接实例
     *
     * @param context 上下文
     * @return BleConnector
     */
    public static BleConnector newBleConnector(Context context) {
        if (!isSupportBle(context)) {
            return null;
        }
        return new BleConnector(context);
    }

    /**
     * 获取BLE连接实例的单例
     *
     * @param context 上下文
     * @return BleConnector单例
     */
    public static BleConnector getBleConnectorInstance(Context context) {
        if (!isSupportBle(context)) {
            return null;
        }

        if (bleConnector == null) {
            synchronized (BleManager.class) {
                if (bleConnector == null) {
                    bleConnector = new BleConnector(context);
                }
            }
        }
        return bleConnector;
    }

    /**
     * 创建一个新的BLE扫描实例
     *
     * @param context 上下文
     * @return BleScanner
     */
    public static BleScanner newBleScanner(Context context) {
        if (!isSupportBle(context)) {
            return null;
        }
        return new BleScanner(context);
    }

    /**
     * 获取BLE扫描实例的单例
     *
     * @param context 上下文
     * @return BleScanner单例
     */
    public static BleScanner getBleScannerInstance(Context context) {
        if (!isSupportBle(context)) {
            return null;
        }
        if (bleScanner == null) {
            synchronized (BleManager.class) {
                if (bleScanner == null) {
                    bleScanner = new BleScanner(context);
                }
            }
        }
        return bleScanner;
    }

    /**
     * 获取蓝牙广播单例实例
     *
     * @param context 上下文
     * @return BleBroadCastor
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static BleBroadCastor getBleBroadCastor(Context context) {
        if (!isSupportBle(context)) {
            return null;
        }
        if (bleBroadCastor == null) {
            synchronized (BleManager.class) {
                if (bleBroadCastor == null) {
                    bleBroadCastor = new BleBroadCastor(context);
                }
            }
        }
        return bleBroadCastor;
    }

    /**
     * 创建一个新的广播实例
     *
     * @param context 上下文
     * @return 广播实例
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public static BleBroadCastor newBleBroadCastor(Context context) {
        if (!isSupportBle(context)) {
            return null;
        }
        return new BleBroadCastor(context);
    }

    /**
     * 获取BLE多连接单例
     *
     * @param context 上下文
     * @return BleMultiConnector
     */
    public static BleMultiConnector getBleMultiConnector(Context context) {
        if (!isSupportBle(context)) {
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
     * 判断当前手机蓝牙是否打开
     *
     * @param context 上下文
     * @return true表示蓝牙已打开
     */
    public static boolean isBluetoothOpened(Context context) {
        if (!isSupportBle(context)) {
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
     * @param context 上下文
     * @return true表示请求发起成功（只是发起打开蓝牙的请求成功，并不是开启蓝牙成功）
     */
    public static boolean openBluetooth(Context context) {
        if (!isSupportBle(context)) {
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
    public static boolean closeBluetooth(Context context) {
        if (!isSupportBle(context)) {
            return false;
        }
        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return false;
        }
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        return adapter != null && adapter.disable();
    }
}
