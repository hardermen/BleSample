package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

/**
 * @author alm
 * @date 2017/11/15
 */
@SuppressWarnings({"UnusedReturnValue", "unused"})
public class BleDeviceController {
    private WeakReference<BleMultiConnector> bleMultiConnectorWeakReference;
    private String address;

    BleDeviceController(BleMultiConnector bleMultiConnector, String address) {
        this.bleMultiConnectorWeakReference = new WeakReference<>(bleMultiConnector);
        this.address = address;
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    public boolean refreshGattCache() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.refreshGattCache(address);
    }

    /**
     * 写入数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param values             数据
     * @return true表示成功
     */
    public boolean writeData(String serviceUUID, String characteristicUUID, byte[] values) {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.writeData(address, serviceUUID, characteristicUUID, values);
    }


    /**
     * 读取数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean readData(String serviceUUID, String characteristicUUID) {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.readData(address, serviceUUID, characteristicUUID);
    }

    /**
     * 打开通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean openNotification(String serviceUUID, String characteristicUUID) {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.openNotification(address, serviceUUID, characteristicUUID);
    }

    /**
     * 关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean closeNotification(String serviceUUID, String characteristicUUID) {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.closeNotification(address, serviceUUID, characteristicUUID);
    }

    public boolean reConnect() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.reConnect(address);
    }

    public boolean close() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && bleMultiConnector.close(address);
    }


    public List<BluetoothGattService> getServices() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        if (bleMultiConnector == null) {
            return null;
        }
        if (address == null) {
            return null;
        }
        return bleMultiConnector.getServices(address);
    }

    public boolean disconnect() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && bleMultiConnector.disconnect(address);
    }

    public Context getContext() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        if (bleMultiConnector == null) {
            return null;
        }
        return bleMultiConnector.getContext();
    }

    public BluetoothGattService getService(UUID uuid) {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        if (bleMultiConnector == null) {
            return null;
        }
        if (address == null) {
            return null;
        }
        return bleMultiConnector.getService(address, uuid);
    }
}
