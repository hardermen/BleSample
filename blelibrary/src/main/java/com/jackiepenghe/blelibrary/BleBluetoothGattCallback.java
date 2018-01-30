package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import java.util.List;
import java.util.UUID;

/**
 * BLE Gatt服务的回调
 *
 * @author alm
 */

class BleBluetoothGattCallback extends BluetoothGattCallback {

    /*-------------------------静态常量-------------------------*/

    /**
     * TAG
     */
    private static final String TAG = "BleBluetoothGattCallbac";

    /*-------------------------成员变量-------------------------*/

    /**
     * BLE连接服务
     */
    private BluetoothLeService bluetoothLeService;
    /**
     * BluetoothGatt客户端
     */
    private BluetoothGatt gatt;

    /*-------------------------构造函数-------------------------*/

    /**
     * 构造器
     *
     * @param bluetoothLeService BLE连接服务
     */
    BleBluetoothGattCallback(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    /*-------------------------重写父类函数-------------------------*/

    /**
     * 连接状态被改变的回调
     * 根据新的连接状态发送对应的广播
     *
     * @param gatt     蓝牙Gatt服务
     * @param status   上一次的状态
     * @param newState 新的状态
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        //创建intent对象
        Intent intent = new Intent();
        //判断当前的连接状态
        switch (newState) {
            //连接断开
            case BluetoothGatt.STATE_DISCONNECTED:
                Tool.warnOut(TAG, "STATE_DISCONNECTED");
                intent.setAction(BleConstants.ACTION_GATT_DISCONNECTED);
                break;
            //正在连接
            case BluetoothGatt.STATE_CONNECTING:
                Tool.warnOut(TAG, "STATE_CONNECTING");
                intent.setAction(BleConstants.ACTION_GATT_CONNECTING);
                break;
            //已连接
            case BluetoothGatt.STATE_CONNECTED:
                Tool.warnOut(TAG, "STATE_CONNECTED");
                intent.setAction(BleConstants.ACTION_GATT_CONNECTED);
                if (!gatt.discoverServices()) {
                    Tool.warnOut(TAG, "无法进行服务发现");
                }
                break;
            //正在断开连接
            case BluetoothGatt.STATE_DISCONNECTING:
                Tool.warnOut(TAG, "STATE_DISCONNECTING");
                intent.setAction(BleConstants.ACTION_GATT_DISCONNECTING);
                break;
            default:
                //其他情况
                Tool.warnOut(TAG, "other state");
                break;
        }
        //发送广播
        bluetoothLeService.sendBroadcast(intent);
    }

    /**
     * 服务扫描完成
     *
     * @param gatt   BluetoothGatt客户端
     * @param status BluetoothGatt客户端配置状态
     */
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        Tool.warnOut(TAG, "onServicesDiscovered");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onServicesDiscovered", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            broadcastUpdate(BleConstants.ACTION_GATT_SERVICES_DISCOVERED);
            this.gatt = gatt;
        }
    }

    /**
     * 获取到远端设备的数据
     *
     * @param gatt           BluetoothGatt客户端
     * @param characteristic 远端设备的特征
     * @param status         BluetoothGatt客户端配置状态
     */
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Tool.warnOut(TAG, "onCharacteristicRead");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onCharacteristicRead", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            byte[] value = characteristic.getValue();
            broadcastUpdate(BleConstants.ACTION_CHARACTERISTIC_READ, value);
        }
    }

    /**
     * 向远端设备写入数据
     *
     * @param gatt           BluetoothGatt客户端
     * @param characteristic 远端设备的特征
     * @param status         BluetoothGatt客户端配置状态
     */
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        Tool.warnOut(TAG, "onCharacteristicWrite");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onCharacteristicWrite", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            byte[] value = characteristic.getValue();
            broadcastUpdate(BleConstants.ACTION_CHARACTERISTIC_WRITE, value);
        }
    }

    /**
     * 远端设备的特征改变了（有通知数据来了）
     *
     * @param gatt           BluetoothGatt客户端
     * @param characteristic 远端设备的特征
     */
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        Tool.warnOut(TAG, "onReceivedNotification");
        byte[] value = characteristic.getValue();
        broadcastUpdate(BleConstants.ACTION_CHARACTERISTIC_CHANGED, value);
    }

    /**
     * 获取到远端设备的描述
     *
     * @param gatt       BluetoothGatt客户端
     * @param descriptor 远端设备的描述
     * @param status     BluetoothGatt客户端配置状态
     */
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Tool.warnOut(TAG, "onDescriptorRead");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onDescriptorRead", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            byte[] value = descriptor.getValue();
            broadcastUpdate(BleConstants.ACTION_DESCRIPTOR_READ, value);
        }
    }

    /**
     * 向远端设备写入描述
     *
     * @param gatt       BluetoothGatt客户端
     * @param descriptor 远端设备的描述
     * @param status     BluetoothGatt客户端配置状态
     */
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        Tool.warnOut(TAG, "onDescriptorWrite");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onDescriptorWrite", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            byte[] value = descriptor.getValue();
            broadcastUpdate(BleConstants.ACTION_DESCRIPTOR_WRITE, value);
        }

    }

    /**
     * 向远端设备写入可靠数据完成
     *
     * @param gatt   BluetoothGatt客户端
     * @param status BluetoothGatt客户端配置状态
     */
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        Tool.warnOut(TAG, "onReliableWriteCompleted");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onReliableWriteCompleted", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            broadcastUpdate(BleConstants.ACTION_RELIABLE_WRITE_COMPLETED);
        }
    }

    /**
     * 获取到远端设备的RSSI
     *
     * @param gatt   BluetoothGatt客户端
     * @param rssi   信号强度
     * @param status BluetoothGatt客户端配置状态
     */
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        Tool.warnOut(TAG, "onReadRemoteRssi");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onReadRemoteRssi", status);
        }
        //BluetoothGatt客户端配置成功
        else {
            byte[] value = new byte[]{(byte) rssi};
            broadcastUpdate(BleConstants.ACTION_READ_REMOTE_RSSI, value);
        }
    }

    /**
     * mtu被更改
     *
     * @param gatt   BluetoothGatt客户端
     * @param mtu    mtu值
     * @param status BluetoothGatt客户端配置状态
     */
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        Tool.warnOut(TAG, "onMtuChanged");
        //BluetoothGatt客户端配置失败了
        if (BluetoothGatt.GATT_SUCCESS != status) {
            broadcastUpdate(BleConstants.ACTION_GATT_NOT_SUCCESS, "onMtuChanged", status);
        } else {
            byte[] value = new byte[]{(byte) mtu};
            broadcastUpdate(BleConstants.ACTION_MTU_CHANGED, value);
        }
    }

    /*-------------------------私有函数-------------------------*/

    /**
     * 发送广播
     *
     * @param action 广播中需要包含的action
     */
    private void broadcastUpdate(String action) {
        Intent intent = new Intent();
        intent.setAction(action);
        bluetoothLeService.sendBroadcast(intent);
    }

    /**
     * 发送广播
     *
     * @param action 广播中需要包含的action
     * @param value  广播中需要包含的数据
     */
    private void broadcastUpdate(String action, byte[] value) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(LibraryConstants.VALUE, value);
        bluetoothLeService.sendBroadcast(intent);
    }

    /**
     * 发送广播
     *
     * @param action 广播中需要包含的action
     * @param method 广播中需要包含的数据
     */
    private void broadcastUpdate(String action, String method, int status) {
        Intent intent = new Intent();
        intent.setAction(action);
        intent.putExtra(LibraryConstants.METHOD, method);
        intent.putExtra(LibraryConstants.STATUS, status);
        bluetoothLeService.sendBroadcast(intent);
    }

    /*-------------------------库内可使用函数-------------------------*/

    /**
     * 获取服务列表
     *
     * @return 服务列表
     */
    List<BluetoothGattService> getServices() {
        if (gatt == null) {
            return null;
        }
        return gatt.getServices();
    }

    /**
     * 根据UUID获取指定的服务
     *
     * @param uuid UUID
     * @return BluetoothGattService
     */
    BluetoothGattService getService(UUID uuid) {
        if (gatt == null) {
            return null;
        }
        if (uuid == null) {
            return null;
        }
        return gatt.getService(uuid);
    }
}
