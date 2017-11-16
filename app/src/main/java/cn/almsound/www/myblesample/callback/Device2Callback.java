package cn.almsound.www.myblesample.callback;

import android.bluetooth.BluetoothGatt;

import cn.almsound.www.baselibrary.Tool;
import cn.almsound.www.blelibrary.BaseConnectCallback;
import cn.almsound.www.blelibrary.BleDeviceController;
import cn.almsound.www.blelibrary.BleMultiConnector;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

public class Device2Callback extends BaseConnectCallback {

    private static final String TAG = "Device2Callback";

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"onDiscoverServicesFailed");

    }

    /**
     * 蓝牙GATT被关闭时回调
     * @param address 设备地址
     */
    @Override
    public void onGattClosed(String address) {
        Tool.warnOut(TAG,"onGattClosed");
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"设备2 onServicesDiscovered");
    }
}
