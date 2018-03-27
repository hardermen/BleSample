package com.jackiepenghe.blelibrary;

import android.os.Binder;


/**
 * BLE多连接服务的Binder
 *
 * @author alm
 */

class BluetoothMultiServiceBinder extends Binder {

    /*------------------------静态常量----------------------------*/

    private static final String TAG = BluetoothMultiServiceBinder.class.getSimpleName();

    /*------------------------成员变量----------------------------*/

    private BluetoothMultiService bluetoothMultiService;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造函数
     *
     * @param bluetoothMultiService BLE多连接服务
     */
    BluetoothMultiServiceBinder(BluetoothMultiService bluetoothMultiService) {
        this.bluetoothMultiService = bluetoothMultiService;
    }

    /*------------------------公开函数----------------------------*/

    /**
     * 获取多连接服务
     *
     * @return BluetoothMultiService
     */
    BluetoothMultiService getBluetoothMultiService() {
        Tool.warnOut(TAG, "蓝牙多连接服务绑定成功");
        return bluetoothMultiService;
    }
}
