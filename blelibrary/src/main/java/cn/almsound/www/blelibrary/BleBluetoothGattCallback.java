package cn.almsound.www.blelibrary;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;

import java.util.List;
import java.util.UUID;

/**
 * Created by alm on 17-6-6.
 * BLE Gatt服务的回调
 */

class BleBluetoothGattCallback extends BluetoothGattCallback {

    private static final String TAG = "BleBluetoothGattCallbac";
    /**
     * BLE连接服务
     */
    private BluetoothLeService bluetoothLeService;
    private BluetoothGatt gatt;
    private boolean autoReconnect;

    /**
     * 构造器
     *
     * @param bluetoothLeService BLE连接服务
     */
    BleBluetoothGattCallback(BluetoothLeService bluetoothLeService) {
        this.bluetoothLeService = bluetoothLeService;
    }

    /**
     * 连接状态被改变的回调
     *
     * @param gatt     蓝牙Gatt服务
     * @param status   上一次的状态
     * @param newState 新的状态
     */
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        super.onConnectionStateChange(gatt, status, newState);
        Intent intent = new Intent();
        switch (newState) {
            case BluetoothGatt.STATE_DISCONNECTED:
                Tool.warnOut(TAG, "STATE_DISCONNECTED");
                intent.setAction(BleConstants.ACTION_GATT_DISCONNECTED);
                if (autoReconnect) {
                    Tool.warnOut(TAG, "autoReconnect");
                    gatt.connect();
                }
                break;
            case BluetoothGatt.STATE_CONNECTING:
                Tool.warnOut(TAG, "STATE_CONNECTING");
                intent.setAction(BleConstants.ACTION_GATT_CONNECTING);
                break;
            case BluetoothGatt.STATE_CONNECTED:
                Tool.warnOut(TAG, "STATE_CONNECTED");
                intent.setAction(BleConstants.ACTION_GATT_CONNECTED);
                if (!gatt.discoverServices()) {
                    Tool.warnOut(TAG, "无法进行服务发现");
                }
                break;
            case BluetoothGatt.STATE_DISCONNECTING:
                Tool.warnOut(TAG, "STATE_DISCONNECTING");
                intent.setAction(BleConstants.ACTION_GATT_DISCONNECTING);
                break;
            default:
                Tool.warnOut(TAG, "other state");
                break;
        }
        bluetoothLeService.sendBroadcast(intent);
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        super.onServicesDiscovered(gatt, status);
        Tool.warnOut(TAG, "onServicesDiscovered");
        broadcastUpdate(BleConstants.ACTION_GATT_SERVICES_DISCOVERED);
        this.gatt = gatt;
    }

    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicRead(gatt, characteristic, status);
        Tool.warnOut(TAG, "onCharacteristicRead");
        byte[] value = characteristic.getValue();
        broadcastUpdate(BleConstants.ACTION_CHARACTERISTIC_READ, value);
    }

    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
        super.onCharacteristicWrite(gatt, characteristic, status);
        Tool.warnOut(TAG, "onCharacteristicWrite");
        byte[] value = characteristic.getValue();
        broadcastUpdate(BleConstants.ACTION_CHARACTERISTIC_WRITE, value);
    }

    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        super.onCharacteristicChanged(gatt, characteristic);
        Tool.warnOut(TAG, "onReceivedNotification");
        byte[] value = characteristic.getValue();
        broadcastUpdate(BleConstants.ACTION_CHARACTERISTIC_CHANGED, value);
    }

    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorRead(gatt, descriptor, status);
        Tool.warnOut(TAG, "onDescriptorRead");
        byte[] value = descriptor.getValue();
        broadcastUpdate(BleConstants.ACTION_DESCRIPTOR_READ, value);
    }

    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        super.onDescriptorWrite(gatt, descriptor, status);
        Tool.warnOut(TAG, "onDescriptorWrite");
        byte[] value = descriptor.getValue();
        broadcastUpdate(BleConstants.ACTION_DESCRIPTOR_WRITE, value);

    }

    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        super.onReliableWriteCompleted(gatt, status);
        Tool.warnOut(TAG, "onReliableWriteCompleted");
        broadcastUpdate(BleConstants.ACTION_RELIABLE_WRITE_COMPLETED);
    }

    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        super.onReadRemoteRssi(gatt, rssi, status);
        Tool.warnOut(TAG, "onReadRemoteRssi");
        byte[] value = new byte[]{(byte) rssi};
        broadcastUpdate(BleConstants.ACTION_READ_REMOTE_RSSI, value);
    }

    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        super.onMtuChanged(gatt, mtu, status);
        Tool.warnOut(TAG, "onMtuChanged");
        byte[] value = new byte[]{(byte) mtu};
        broadcastUpdate(BleConstants.ACTION_MTU_CHANGED, value);
    }

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

    void setAutoReconnect(boolean autoReconnect) {
        this.autoReconnect = autoReconnect;
    }

    BluetoothGattService getService(UUID uuid) {
        if (gatt == null){
            return null;
        }
        if (uuid == null){
            return  null;
        }
        return gatt.getService(uuid);
    }
}
