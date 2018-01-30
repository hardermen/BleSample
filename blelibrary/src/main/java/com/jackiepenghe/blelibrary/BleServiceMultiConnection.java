package com.jackiepenghe.blelibrary;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;


/**
 * BLE多连接服务的连接工具
 *
 * @author alm
 */

public class BleServiceMultiConnection implements ServiceConnection {

    /*------------------------静态常量----------------------------*/

    /**
     * TAG
     */
    private static final String TAG = "BleServiceMultiConnecti";

    /*------------------------成员变量----------------------------*/

    /**
     * BLE多连接工具
     */
    private BleMultiConnector bleMultiConnector;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造函数
     *
     * @param bleMultiConnector BLE多连接工具
     */
    BleServiceMultiConnection(BleMultiConnector bleMultiConnector) {
        this.bleMultiConnector = bleMultiConnector;
    }

    /*------------------------实现接口函数----------------------------*/

    /**
     * Called when a connection to the Service has been established, with
     * the {@link IBinder} of the communication channel to the
     * Service.
     *
     * @param name    The concrete component name of the service that has
     *                been connected.
     * @param iBinder The IBinder of the Service's communication channel,
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {

        if (iBinder == null) {
            return;
        }
        if (bleMultiConnector == null) {
            return;
        }
        if (iBinder instanceof BluetoothMultiServiceBinder) {
            bleMultiConnector.setBluetoothMultiService(((BluetoothMultiServiceBinder) iBinder).getBluetoothMultiService());
            if (bleMultiConnector.getBluetoothMultiService().initialize()) {
                Tool.warnOut(TAG, "蓝牙多连接初始化完成");
                bleMultiConnector.getBluetoothMultiService().setInitializeFinished();
            } else {
                Tool.warnOut(TAG, "蓝牙多连接初始化失败");
            }
        }
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does <em>not</em> remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     *             connection has been lost.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        if (bleMultiConnector == null) {
            return;
        }
        bleMultiConnector.setBluetoothMultiService(null);
    }


}
