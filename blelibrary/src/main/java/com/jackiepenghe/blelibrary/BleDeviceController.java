package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.UUID;

/**
 * @author alm
 * @date 2017/11/15
 * 在多连接中，对某一个设备进行操作的工具类
 */
public class BleDeviceController {

    /*------------------------成员变量----------------------------*/

    /**
     * BleMultiConnector弱引用
     */
    private WeakReference<BleMultiConnector> bleMultiConnectorWeakReference;
    /**
     * 设备地址
     */
    private String address;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器
     *
     * @param bleMultiConnector BleMultiConnector
     * @param address           设备地址
     */
    BleDeviceController(BleMultiConnector bleMultiConnector, String address) {
        this.bleMultiConnectorWeakReference = new WeakReference<>(bleMultiConnector);
        this.address = address;
    }

    /*------------------------公开函数----------------------------*/

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
    public boolean enableNotification(String serviceUUID, String characteristicUUID, boolean enable) {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.enableNotification(address, serviceUUID, characteristicUUID, enable);
    }

    /**
     * 重连之前连接过的设备
     *
     * @return true表示发起连接成功
     */
    public boolean reConnect() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && address != null && bleMultiConnector.reConnect(address);
    }

    /**
     * 关闭当前连接
     *
     * @return true表示关闭成功
     */
    public boolean close() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && bleMultiConnector.close(address);
    }

    /**
     * 获取服务列表
     *
     * @return 服务列表
     */
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

    /**
     * 断开连接
     *
     * @return true表示断开成功
     */
    public boolean disconnect() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        return bleMultiConnector != null && bleMultiConnector.disconnect(address);
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public Context getContext() {
        BleMultiConnector bleMultiConnector = bleMultiConnectorWeakReference.get();
        if (bleMultiConnector == null) {
            return null;
        }
        return bleMultiConnector.getContext();
    }

    /**
     * 根据指定UUID获取服务
     *
     * @param uuid UUID
     * @return 服务
     */
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

    /**
     * 检查特征是否支持通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持通知
     */
    public boolean canNotify(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    /**
     * 检查特征是否支持读取
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持读取
     */
    public boolean canRead(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    /**
     * 检查特征是否支持写入（带符号）
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持写入（带符号）
     */
    public boolean canSignedWrite(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0;
    }

    /**
     * 检查特征是否支持写入
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持写入
     */
    public boolean canWrite(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    /**
     * 检查特征是否支持写入（无回复方式）
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持写入（无回复方式）
     */
    public boolean canWriteNoResponse(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }
}
