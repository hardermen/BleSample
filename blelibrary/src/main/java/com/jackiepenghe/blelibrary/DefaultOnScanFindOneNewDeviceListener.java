package com.jackiepenghe.blelibrary;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import java.util.ArrayList;

class DefaultOnScanFindOneNewDeviceListener implements BleInterface.OnScanFindOneNewDeviceListener {

    private static final String TAG = DefaultOnScanFindOneNewDeviceListener.class.getSimpleName();

    /**
     * 发现一个新的蓝牙设备时回调此函数
     *
     * @param index      当前的设备在设备列表中的位置
     * @param bleDevice  自定义Ble设备Been类,如果数据内容为空，则说明扫描结果设备列表有数据更新
     * @param bleDevices 当前扫描到的所有设备列表
     */
    @Override
    public void onScanFindOneNewDevice(int index, @Nullable BleDevice bleDevice, @NonNull ArrayList<BleDevice> bleDevices) {
        if (bleDevice != null) {
            Tool.warnOut(TAG, "bleDevice name = " + bleDevice.getDeviceName() + ", address = " + bleDevice.getDeviceAddress());
        } else {
            Tool.warnOut(TAG, "bleDevice name = " + bleDevices.get(index).getDeviceName() + ", address = " + bleDevices.get(index).getDeviceAddress());

        }
    }
}
