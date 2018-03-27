package com.jackiepenghe.blelibrary;

import android.os.Binder;

/**
 * BluetoothLeService绑定成功后返回的IBinder对象,通过这个对象的方法来获取当前的BluetoothLeService服务
 * Created by alm on 17-6-5.
 */

class BluetoothLeServiceBinder extends Binder {

    /*------------------------成员变量----------------------------*/

    /**
     * BLE连接服务
     */
    private BluetoothLeService bluetoothLeService;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器
     *
     * @param bluetoothLeService BLE连接服务
     */
    BluetoothLeServiceBinder(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 获取BLE连接服务
     *
     * @return BLE连接服务
     */
    BluetoothLeService getBluetoothLeService() {
        return bluetoothLeService;
    }
}
