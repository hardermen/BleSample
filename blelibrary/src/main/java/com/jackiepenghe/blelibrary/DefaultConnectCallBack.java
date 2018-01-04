package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;


/**
 * Created by alm on 2017/11/16.
 * 默认的多连接回调
 */

class DefaultConnectCallBack extends BaseConnectCallback {

    private static final String TAG = "DefaultConnectCallBack";

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onDiscoverServicesFailed");
    }

    /**
     * 蓝牙GATT被关闭时回调
     * @param address 设备地址
     */
    @Override
    public void onGattClosed(String address) {
        Tool.warnOut(TAG,"device address:" + address + " onGattClosed");
    }

    @Override
    public void onDisConnected(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onDisConnected");
    }

    @Override
    public void onConnecting(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onConnecting");
    }

    @Override
    public void onConnected(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onConnected");
    }

    @Override
    public void onDisconnecting(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onDisconnecting");
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onServicesDiscovered");
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onCharacteristicRead");
        Tool.warnOut(TAG,"values = " + Tool.bytesToHexStr(values));
    }

    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onPhyUpdate");
        Tool.warnOut(TAG,"txPhy = " + txPhy + ", rxPhy = " + rxPhy);
    }

    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onPhyRead");
        Tool.warnOut(TAG,"txPhy = " + txPhy + ", rxPhy = " + rxPhy);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onCharacteristicWrite");
        Tool.warnOut(TAG,"values = " + Tool.bytesToHexStr(values));
    }

    @Override
    public void onReceivedNotification(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onReceivedNotification");
        Tool.warnOut(TAG,"values = " + Tool.bytesToHexStr(values));
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onDescriptorRead");
        Tool.warnOut(TAG,"values = " + Tool.bytesToHexStr(values));
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onDescriptorWrite");
        Tool.warnOut(TAG,"values = " + Tool.bytesToHexStr(values));
    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onReliableWriteCompleted");
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onReadRemoteRssi");
        Tool.warnOut(TAG,"rssi = " + rssi);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG,"device address:" + device.getAddress() + " onMtuChanged");
        Tool.warnOut(TAG,"rssi = " + mtu);
    }
}
