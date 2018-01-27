package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

import static com.jackiepenghe.blelibrary.BleManager.resetBleMultiConnector;

/**
 * @author alm
 * @date 2017/11/15
 * 多连接时的Ble连接工具
 */

public class BleMultiConnector {

    /*------------------------成员变量----------------------------*/

    /**
     * 上下文弱引用
     */
    private WeakReference<Context> contextWeakReference;
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
     * @param context 上下文
     */
    BleMultiConnector(Context context) {
        contextWeakReference = new WeakReference<>(context);
        bleServiceMultiConnection = new BleServiceMultiConnection(this);
        Intent intent = new Intent(context.getApplicationContext(), BluetoothMultiService.class);
        context.getApplicationContext().bindService(intent, bleServiceMultiConnection, Context.BIND_AUTO_CREATE);
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
     * 设置蓝牙多连接服务（将蓝牙多连接服务传递进来以进行操作）
     *
     * @param bluetoothMultiService 蓝牙多连接服务
     */
    void setBluetoothMultiService(BluetoothMultiService bluetoothMultiService) {
        this.bluetoothMultiService = bluetoothMultiService;
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
     * 获取上下文
     *
     * @return 上下文
     */
    Context getContext() {
        return contextWeakReference.get();
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

    /*------------------------公开函数----------------------------*/

    /**
     * 断开所有连接
     */
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
        Context context = contextWeakReference.get();
        if (context == null) {
            return false;
        }
        try {
            context.getApplicationContext().unbindService(bleServiceMultiConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        contextWeakReference = null;
        bleServiceMultiConnection = null;
        bluetoothMultiService = null;
        resetBleMultiConnector();
        return true;
    }

    /**
     * 发起一个连接
     *
     * @param address 设备地址
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull String address) {
        return connect(address, false);
    }

    /**
     * 发起一个连接
     *
     * @param address     设备地址
     * @param autoConnect 自动重连标识
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull String address, boolean autoConnect) {
        return connect(address, new DefaultConnectCallBack(), autoConnect);
    }

    /**
     * 发起一个连接
     *
     * @param address             设备地址
     * @param baseConnectCallback 连接回调
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull String address, @NonNull BaseConnectCallback baseConnectCallback) {
        return connect(address, baseConnectCallback, false);
    }

    /**
     * 发起一个连接
     *
     * @param address             设备地址
     * @param baseConnectCallback 连接回调
     * @param autoConnect         自动重连标识
     * @return true表示请求发起成功
     */
    public boolean connect(@NonNull String address, @NonNull BaseConnectCallback baseConnectCallback, boolean autoConnect) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.connectDevice(address, baseConnectCallback, autoConnect);
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
