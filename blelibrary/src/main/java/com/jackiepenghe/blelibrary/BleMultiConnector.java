package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

/**
 * @author alm
 * @date 2017/11/15
 * 多连接时的Ble连接工具
 */

@SuppressWarnings({"unused", "UnusedReturnValue", "WeakerAccess", "SameParameterValue"})
public class BleMultiConnector {
    private WeakReference<Context> contextWeakReference;
    private BleServiceMultiConnection bleServiceMultiConnection;
    private BluetoothMultiService bluetoothMultiService;

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

    boolean disconnect(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.disconnect(address);
    }

    public boolean disconnectAll() {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.disconnectAll();
    }

    boolean close(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.close(address);
    }

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
        BleManager.resetBleMultiConnector();
        return true;
    }

    public boolean connect(@NonNull String address) {
        return connect(address,false);
    }

    public boolean connect(@NonNull String address, boolean autoConnect) {
        return connect(address, new DefaultConnectCallBack(), autoConnect);
    }

    public boolean connect(@NonNull String address, @NonNull BaseConnectCallback baseConnectCallback) {
        return connect(address, baseConnectCallback,false);
    }

    public boolean connect(@NonNull String address, @NonNull BaseConnectCallback baseConnectCallback, boolean autoConnect) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.connectDevice(address, baseConnectCallback, autoConnect);
    }

    boolean reConnect(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.isInitializeFinished() && bluetoothMultiService.reConnect(address);
    }

    public List<BluetoothGattService> getServices(String address) {
        if (bluetoothMultiService == null) {
            return null;
        }

        if (!bluetoothMultiService.isInitializeFinished()) {
            return null;
        }
        return bluetoothMultiService.getServices(address);
    }


    public BluetoothGattService getService(String address, UUID uuid) {
        if (bluetoothMultiService == null) {
            return null;
        }
        if (!bluetoothMultiService.isInitializeFinished()) {
            return null;
        }
        return bluetoothMultiService.getService(address, uuid);
    }

    public BluetoothMultiService getBluetoothMultiService() {
        return bluetoothMultiService;
    }

    void setBluetoothMultiService(BluetoothMultiService bluetoothMultiService) {
        this.bluetoothMultiService = bluetoothMultiService;
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
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    public void refreshAllGattCache() {
        if (bluetoothMultiService == null) {
            return;
        }
        bluetoothMultiService.refreshAllGattCache();
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
     * 打开通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    boolean openNotification(String address, String serviceUUID, String characteristicUUID) {
        return bluetoothMultiService != null && bluetoothMultiService.openNotification(address, serviceUUID, characteristicUUID);
    }

    /**
     * 关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    boolean closeNotification(String address, String serviceUUID, String characteristicUUID) {
        return bluetoothMultiService != null && bluetoothMultiService.closeNotification(address, serviceUUID, characteristicUUID);
    }

    Context getContext() {
        return contextWeakReference.get();
    }

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
