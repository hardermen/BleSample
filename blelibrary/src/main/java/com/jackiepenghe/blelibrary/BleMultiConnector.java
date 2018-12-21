package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;

import java.util.List;
import java.util.UUID;

import static com.jackiepenghe.blelibrary.BleManager.resetBleMultiConnector;

/**
 * 多连接时的Ble连接工具
 *
 * @author alm
 */

public class BleMultiConnector {

    /*------------------------成员变量----------------------------*/
    /**
     * 多连接服务连接工具
     */
    private BleServiceMultiConnection bleServiceMultiConnection;
    /**
     * 多连接服务
     */
    private BluetoothMultiService bluetoothMultiService;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器 protected 禁止外部直接构造
     *
     */
    BleMultiConnector() {
        bleServiceMultiConnection = new BleServiceMultiConnection(this);
        Intent intent = new Intent(BleManager.getContext().getApplicationContext(), BluetoothMultiService.class);
        BleManager.getContext().getApplicationContext().bindService(intent, bleServiceMultiConnection, Context.BIND_AUTO_CREATE);
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 根据设备地址断开连接
     *
     * @param address 设备地址断
     * @return true表示请求发起成功
     */
    boolean disconnect(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.disconnect(address);
    }

    /**
     * 根据设备地址关闭GATT
     *
     * @param address 设备地址
     * @return true表示成功
     */
    boolean close(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.close(address);
    }

    /**
     * 根据设备地址重新连接断开的设备（G与该地址建立过GATT连接并且该GATT未被关闭才能重连）
     *
     * @param address 设备地址
     * @return true表示请求发起成功
     */
    boolean reConnect(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.reConnect(address);
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    boolean refreshGattCache(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.refreshGattCache(address);
    }

    /**
     * 写入数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param values             数据
     * @return true表示成功
     */
    boolean writeData(String address, String serviceUUID, String characteristicUUID, byte[] values) {
        return bluetoothMultiService != null && bluetoothMultiService.writeData(address, serviceUUID, characteristicUUID, values);
    }

    /**
     * 读取数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    boolean readData(String address, String serviceUUID, String characteristicUUID) {
        return bluetoothMultiService != null && bluetoothMultiService.readData(address, serviceUUID, characteristicUUID);
    }

    /**
     * 打开或关闭通知
     *
     * @param address            设备地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param enable             true表示打开通知
     * @return true表示成功
     */
    boolean enableNotification(String address, String serviceUUID, String characteristicUUID, boolean enable) {
        return bluetoothMultiService != null && bluetoothMultiService.enableNotification(address, serviceUUID, characteristicUUID, enable);
    }

    /**
     * 根据设备地址获取该设备的所有服务
     *
     * @param address 设备地址
     * @return 设备的所有服务
     */
    List<BluetoothGattService> getServices(String address) {
        if (bluetoothMultiService == null) {
            return null;
        }

        if (!bluetoothMultiService.isInitializeFinished()) {
            return null;
        }
        return bluetoothMultiService.getServices(address);
    }

    BluetoothGattService getService(String address, UUID uuid) {
        if (bluetoothMultiService == null) {
            return null;
        }
        if (!bluetoothMultiService.isInitializeFinished()) {
            return null;
        }
        return bluetoothMultiService.getService(address, uuid);
    }

    /**
     * 获取多连接服务
     *
     * @return 多连接服务
     */
    BluetoothMultiService getBluetoothMultiService() {
        return bluetoothMultiService;
    }

    /**
     * 设置蓝牙多连接服务（将蓝牙多连接服务传递进来以进行操作）
     *
     * @param bluetoothMultiService 蓝牙多连接服务
     */
    void setBluetoothMultiService(BluetoothMultiService bluetoothMultiService) {
        this.bluetoothMultiService = bluetoothMultiService;
    }

    /*------------------------公开函数----------------------------*/

    /**
     * 断开所有连接
     */
    @SuppressWarnings("unused")
    public boolean disconnectAll() {
        if (bluetoothMultiService == null) {
            return false;
        }
        if (!bluetoothMultiService.isInitializeFinished()) {
            return false;
        }
        bluetoothMultiService.disconnectAll();
        return true;
    }

    /**
     * 关闭所有GATT连接
     *
     * @return true表示成功
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean closeAll() {
        if (bluetoothMultiService == null) {
            return false;
        }

        if (!bluetoothMultiService.isInitializeFinished()) {
            return false;
        }

        bluetoothMultiService.closeAll();
        if (BleManager.getContext() == null) {
            return false;
        }
        try {
            BleManager.getContext().getApplicationContext().unbindService(bleServiceMultiConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        bleServiceMultiConnection = null;
        bluetoothMultiService = null;
        resetBleMultiConnector();
        return true;
    }

    /**
     * 发起一个连接
     *
     * @param bluetoothDevice 设备
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull BluetoothDevice bluetoothDevice) {
        return connect(bluetoothDevice, false);
    }


    /**
     * 发起一个连接
     *
     * @param address 设备地址
     * @return true表示请求发起成功
     */
    @Deprecated
    public boolean connect(String address) {
        return address != null && BluetoothAdapter.checkBluetoothAddress(address) && connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address), false);
    }

    /**
     * 发起一个连接
     *
     * @param bluetoothDevice 设备
     * @param autoConnect     自动重连标识
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull BluetoothDevice bluetoothDevice, boolean autoConnect) {
        return connect(bluetoothDevice, new DefaultConnectCallBack(), autoConnect);
    }

    /**
     * 发起一个连接
     *
     * @param address     设备地址
     * @param autoConnect 自动重连标识
     * @return true表示请求发起成功
     */
    @Deprecated
    public boolean connect(String address, boolean autoConnect) {
        return address != null && BluetoothAdapter.checkBluetoothAddress(address) && connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address), new DefaultConnectCallBack(), autoConnect);
    }

    /**
     * 发起一个连接
     *
     * @param bluetoothDevice     设备
     * @param baseConnectCallback 连接回调
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull BluetoothDevice bluetoothDevice, @NonNull BaseConnectCallback baseConnectCallback) {
        return connect(bluetoothDevice, baseConnectCallback, false);
    }

    /**
     * 发起一个连接
     *
     * @param address             设备地址
     * @param baseConnectCallback 连接回调
     * @return true表示请求发起成功
     */
    @Deprecated
    public boolean connect(String address, @NonNull BaseConnectCallback baseConnectCallback) {
        return address != null && BluetoothAdapter.checkBluetoothAddress(address) && connect(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address), baseConnectCallback, false);
    }

    /**
     * 发起一个连接
     *
     * @param bluetoothDevice     设备
     * @param baseConnectCallback 连接回调
     * @param autoConnect         自动重连标识
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull BluetoothDevice bluetoothDevice, @NonNull BaseConnectCallback baseConnectCallback, boolean autoConnect) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.connectDevice(bluetoothDevice, baseConnectCallback, autoConnect);
        } else {
            return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.connectAddress(bluetoothDevice.getAddress(), baseConnectCallback, autoConnect);
        }
    }

    /**
     * 发起一个连接
     *
     * @param address             设备地址
     * @param baseConnectCallback 连接回调
     * @param autoConnect         自动重连标识
     * @return true表示请求发起成功
     */
    @Deprecated
    public boolean connect(String address, @NonNull BaseConnectCallback baseConnectCallback, boolean autoConnect) {
        if (address == null || !BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        BluetoothDevice bluetoothDevice = BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.connectDevice(bluetoothDevice, baseConnectCallback, autoConnect);
        } else {
            return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.connectAddress(bluetoothDevice.getAddress(), baseConnectCallback, autoConnect);
        }
    }

    /**
     * 刷新蓝牙缓存
     */
    public void refreshAllGattCache() {
        if (bluetoothMultiService == null) {
            return;
        }
        bluetoothMultiService.refreshAllGattCache();
    }

    /**
     * 根据设备地址获取一个Ble多连接中的设备的控制器
     *
     * @param address 设备地址
     * @return BleDeviceController
     */
    @SuppressWarnings("unused")
    public BleDeviceController getBleDeviceController(String address) {
        if (bluetoothMultiService == null) {
            return null;
        }
        if (!bluetoothMultiService.isConnected(address)) {
            return null;
        }
        return new BleDeviceController(this, address);
    }
}
