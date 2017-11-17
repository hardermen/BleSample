package com.jackiepenghe.blelibrary;

import android.os.Binder;

import java.lang.ref.WeakReference;

/**
 * Created by alm on 17-6-5.
 * BluetoothLeService绑定成功后返回的IBinder对象,通过这个对象的方法来获取当前的BluetoothLeService服务
 */

class BluetoothLeServiceBinder extends Binder {

    /**
     * BLE连接服务的弱引用
     */
    private WeakReference<BluetoothLeService> bluetoothLeServiceWeakReference;

    /**
     * 构造器
     *
     * @param bluetoothLeService BLE连接服务
     */
    BluetoothLeServiceBinder(BluetoothLeService bluetoothLeService) {
        bluetoothLeServiceWeakReference = new WeakReference<>(bluetoothLeService);
    }

    /**
     * 获取BLE连接服务
     *
     * @return BLE连接服务
     */
    BluetoothLeService getBluetoothLeService() {
        return bluetoothLeServiceWeakReference.get();
    }
}
