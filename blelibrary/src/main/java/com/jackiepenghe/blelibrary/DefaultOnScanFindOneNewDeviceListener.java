package com.jackiepenghe.blelibrary;

class DefaultOnScanFindOneNewDeviceListener implements BleInterface.OnScanFindOneNewDeviceListener {

    private static final String TAG = DefaultOnScanFindOneNewDeviceListener.class.getSimpleName();

    /**
     * 发现一个新的蓝牙设备时回调此函数
     *
     * @param bleDevice 自定义Ble设备Been类
     */
    @Override
    public void onScanFindOneNewDevice(int index, BleDevice bleDevice) {
        Tool.warnOut(TAG, "bleDevice name = " + bleDevice.getDeviceName() + ", address = " + bleDevice.getDeviceAddress());
    }
}
