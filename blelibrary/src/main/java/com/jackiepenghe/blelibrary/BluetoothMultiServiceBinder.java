package com.jackiepenghe.blelibrary;

import android.os.Binder;


/**
 *
 * @author alm
 * @date 2017/11/15
 * BLE多连接服务的Binder
 */

class BluetoothMultiServiceBinder extends Binder {

    /*------------------------静态常量----------------------------*/

    private static final String TAG = "BluetoothMultiServiceBi";

    /*------------------------成员变量----------------------------*/

    private BluetoothMultiService bluetoothMultiService;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造函数
     * @param bluetoothMultiService BLE多连接服务
     */
    BluetoothMultiServiceBinder(BluetoothMultiService bluetoothMultiService) {
        this.bluetoothMultiService = bluetoothMultiService;
    }

    /*------------------------公开函数----------------------------*/

    /**
     * 获取多连接服务
     * @return BluetoothMultiService
     */
    BluetoothMultiService getBluetoothMultiService() {
        Tool.warnOut(TAG, "蓝牙多连接服务绑定成功");
        return bluetoothMultiService;
    }
}
