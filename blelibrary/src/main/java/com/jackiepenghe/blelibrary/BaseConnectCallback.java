package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGatt;


/**
 * 多连接时的连接回调
 * @author alm
 */

public abstract class BaseConnectCallback {

    /*-------------------------静态常量-------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BaseConnectCallback.class.getSimpleName();

    /**
     * 是否已经连接成功的标志
     */
    private boolean isConnected;

    /*-------------------------库内函数-------------------------*/

    /**
     * 设置连接状态
     * @param connected 连接状态
     */
    void setConnected(boolean connected) {
        isConnected = connected;
    }

    /*-------------------------公开函数-------------------------*/

    /**
     * 在与远端设备断开连接时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    public void onDisConnected(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onDisConnected");
    }

    /**
     * 正在连接远端设备时，回调此函数（BLE好像不会回调此函数，经典蓝牙不知道）
     *
     * @param gatt BluetoothGatt客户端
     */
    public void onConnecting(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onConnecting");
    }

    /**
     * 在连接上远端设备时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    public void onConnected(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onConnected");
    }

    /**
     * 正在与远端设备断开连接时，回调此函数（BLE好像不会回调此函数，经典蓝牙不知道）
     *
     * @param gatt BluetoothGatt客户端
     */
    public void onDisconnecting(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onDisconnecting");
    }

    /**
     * 连接设备成功后，会进行设备UUID扫描，当UUID扫描完成时，会回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onServicesDiscovered");
    }

    /**
     * 读取到远端设备的数据时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 读到的数据
     */
    public void onCharacteristicRead(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onCharacteristicRead");
    }

    /**
     * 当蓝牙的phy层的属性变更时，回调此函数
     *
     * @param gatt  BluetoothGatt客户端
     * @param txPhy Tx值
     * @param rxPhy Rx值
     */
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onPhyUpdate");
    }

    /**
     * 获取到蓝牙的phy层的属性时，回调此函数
     *
     * @param gatt  BluetoothGatt客户端
     * @param txPhy Tx值
     * @param rxPhy Rx值
     */
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onPhyRead");
    }

    /**
     * 向远端设备写入数据时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 写入的数据
     */
    public void onCharacteristicWrite(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onCharacteristicWrite");
    }

    /**
     * 收到远端设备的通知时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 收到的通知数据
     */
    public void onReceivedNotification(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onReceivedNotification");
    }

    /**
     * 读取到描述数据时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 读取到的描述数据
     */
    public void onDescriptorRead(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onDescriptorRead");
    }

    /**
     * 向远端设备写入描述时，回调此函数
     *
     * @param gatt   BluetoothGatt客户端
     * @param values 写入的描述
     */
    public void onDescriptorWrite(BluetoothGatt gatt, byte[] values) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onDescriptorWrite");
    }

    /**
     * 进行可靠数据写入并完成时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     */
    public void onReliableWriteCompleted(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onReliableWriteCompleted");
    }

    /**
     * 读取到远端设备的rssi时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     * @param rssi rssi信号强度
     */
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onReadRemoteRssi");
    }

    /**
     * 当Mtu被改变时，回调此函数
     *
     * @param gatt BluetoothGatt客户端
     * @param mtu  mtu值
     */
    public void onMtuChanged(BluetoothGatt gatt, int mtu) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onMtuChanged");
    }

    /**
     * 获取设备连接状态
     * @return true表示已连接
     */
    public boolean isConnected() {
        return isConnected;
    }

    /*-------------------------抽象函数-------------------------*/

    /**
     * 蓝牙连接后无法正常进行服务发现调用此函数
     *
     * @param gatt BluetoothGatt
     */
    public abstract void onDiscoverServicesFailed(BluetoothGatt gatt);

    /**
     * 蓝牙GATT被关闭时调用此函数
     *
     * @param address 关闭GATT时，对应的设备地址
     */
    public abstract void onGattClosed(String address);

    /**
     * 当蓝牙客户端配置失败时调用此函式
     *
     * @param gatt        蓝牙客户端
     * @param methodName  方法名
     * @param errorStatus 错误状态码
     */
    public abstract void onBluetoothGattOptionsNotSuccess(BluetoothGatt gatt, String methodName, int errorStatus);
}
