package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;


/**
 * 默认的多连接回调
 * Created by alm on 2017/11/16.
 */

class DefaultConnectCallBack extends BaseConnectCallback {

    /*------------------------静态常量----------------------------*/

    /**
     * TAG
     */
    private static final String TAG = "DefaultConnectCallBack";

    /*------------------------重写父类函数----------------------------*/

    /**
     * 在与远端设备断开连接时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    @Override
    public void onDisConnected(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onDisConnected");
    }

    /**
     * 正在连接远端设备时，回调此函数（BLE好像不会回调此函数，经典蓝牙不知道）
     *
     * @param gatt BluetoothGatt客户端
     */
    @Override
    public void onConnecting(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onConnecting");
    }

    /**
     * 在连接上远端设备时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    @Override
    public void onConnected(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onConnected");
    }

    /**
     * 正在与远端设备断开连接时，回调此函数（BLE好像不会回调此函数，经典蓝牙不知道）
     *
     * @param gatt BluetoothGatt客户端
     */
    @Override
    public void onDisconnecting(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onDisconnecting");
    }

    /**
     * 连接设备成功后，会进行设备UUID扫描，当UUID扫描完成时，会回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onServicesDiscovered");
    }

    /**
     * 读取到远端设备的数据时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 读到的数据
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onCharacteristicRead");
        Tool.warnOut(TAG, "values = " + Tool.bytesToHexStr(values));
    }

    /**
     * 当蓝牙的phy层的属性变更时，回调此函数
     *
     * @param gatt  BluetoothGatt客户端
     * @param txPhy Tx值
     * @param rxPhy Rx值
     */
    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onPhyUpdate");
        Tool.warnOut(TAG, "txPhy = " + txPhy + ", rxPhy = " + rxPhy);
    }

    /**
     * 获取到蓝牙的phy层的属性时，回调此函数
     *
     * @param gatt  BluetoothGatt客户端
     * @param txPhy Tx值
     * @param rxPhy Rx值
     */
    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onPhyRead");
        Tool.warnOut(TAG, "txPhy = " + txPhy + ", rxPhy = " + rxPhy);
    }

    /**
     * 向远端设备写入数据时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 写入的数据
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onCharacteristicWrite");
        Tool.warnOut(TAG, "values = " + Tool.bytesToHexStr(values));
    }

    /**
     * 收到远端设备的通知时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 收到的通知数据
     */
    @Override
    public void onReceivedNotification(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onReceivedNotification");
        Tool.warnOut(TAG, "values = " + Tool.bytesToHexStr(values));
    }

    /**
     * 读取到描述数据时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 读取到的描述数据
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onDescriptorRead");
        Tool.warnOut(TAG, "values = " + Tool.bytesToHexStr(values));
    }

    /**
     * 向远端设备写入描述时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 写入的描述
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, byte[] values) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onDescriptorWrite");
        Tool.warnOut(TAG, "values = " + Tool.bytesToHexStr(values));
    }

    /**
     * 进行可靠数据写入并完成时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onReliableWriteCompleted");
    }

    /**
     * 读取到远端设备的rssi时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     * @param rssi rssi信号强度
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onReadRemoteRssi");
        Tool.warnOut(TAG, "rssi = " + rssi);
    }

    /**
     * 当Mtu被改变时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     * @param mtu  mtu值
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onMtuChanged");
        Tool.warnOut(TAG, "rssi = " + mtu);
    }

    /*------------------------实现父类函数----------------------------*/

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        BluetoothDevice device = gatt.getDevice();
        Tool.warnOut(TAG, "device address:" + device.getAddress() + " onDiscoverServicesFailed");
    }

    /**
     * 蓝牙GATT被关闭时回调
     *
     * @param address 设备地址
     */
    @Override
    public void onGattClosed(String address) {
        Tool.warnOut(TAG, "device address:" + address + " onGattClosed");
    }

    /**
     * 当蓝牙客户端配置失败时调用此函式
     *
     * @param gatt        蓝牙客户端
     * @param methodName  方法名
     * @param errorStatus 错误状态码
     */
    @Override
    public void onBluetoothGattOptionsNotSuccess(BluetoothGatt gatt, String methodName, int errorStatus) {
        String address = gatt.getDevice().getAddress();
        Tool.warnOut(TAG, "onBluetoothGattOptionsNotSuccess. device address:" + address + ",methodName:" + methodName + ",errorStatus:" + errorStatus);
    }
}
