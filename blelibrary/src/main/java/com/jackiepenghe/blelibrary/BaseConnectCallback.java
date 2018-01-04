package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGatt;


/**
 * @author alm
 * @date 2017/11/15
 */

@SuppressWarnings({"WeakerAccess", "unused"})
public abstract class BaseConnectCallback {

    private static final String TAG = "BaseConnectCallback";

    public void onDisConnected(BluetoothGatt gatt) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onDisConnected");
    }

    public void onConnecting(BluetoothGatt gatt) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onConnecting");
    }

    public void onConnected(BluetoothGatt gatt) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onConnected");
    }


    public void onDisconnecting(BluetoothGatt gatt) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onDisconnecting");
    }

    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onServicesDiscovered");
    }

    public void onCharacteristicRead(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onCharacteristicRead");
    }

    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onPhyUpdate");
    }

    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onPhyRead");
    }

    public void onCharacteristicWrite(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onCharacteristicWrite");
    }

    public void onReceivedNotification(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onReceivedNotification");
    }

    public void onDescriptorRead(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onDescriptorRead");
    }

    public void onDescriptorWrite(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onDescriptorWrite");
    }

    public void onReliableWriteCompleted(BluetoothGatt gatt) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onReliableWriteCompleted");
    }

    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onReadRemoteRssi");
    }

    public void onMtuChanged(BluetoothGatt gatt, int mtu) {
        Tool.warnOut(TAG,gatt.getDevice().getAddress() + " onMtuChanged");
    }

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    public abstract void onDiscoverServicesFailed(BluetoothGatt gatt);

    /**
     * 蓝牙GATT被关闭时回调
     * @param address 设备地址
     */
    public abstract void onGattClosed(String address);
}
