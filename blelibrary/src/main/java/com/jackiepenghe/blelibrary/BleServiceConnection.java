package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattService;
import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.RequiresApi;

import java.util.List;
import java.util.UUID;

/**
 * BLE连接服务的连接回调
 * Created by alm on 17-6-5.
 */

class BleServiceConnection implements ServiceConnection {

    /*------------------------静态常量----------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BleServiceConnection.class.getSimpleName();

    /*------------------------成员变量----------------------------*/

    /**
     * 设备
     */
    private BluetoothDevice mBluetoothDevice;
    /**
     * 设备地址
     */
    private String mAddress;
    /**
     * BLE连接服务
     */
    private BluetoothLeService bluetoothLeService;
    /**
     * 是否自动连接的标识
     */
    private boolean autoConnect;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器
     *
     * @param address 设备地址
     */
    BleServiceConnection(String address) {
        mAddress = address;
    }

    BleServiceConnection(BluetoothDevice bluetoothDevice) {
        mBluetoothDevice = bluetoothDevice;
    }

    /*------------------------实现接口函数----------------------------*/

    /**
     * Called when a connection to the Service has been established, with
     * the {@link IBinder} of the communication channel to the
     * Service.
     *
     * @param name    The concrete component name of the service that has
     *                been connected.
     * @param iBinder The IBinder of the Service's communication channel,
     */
    @Override
    public void onServiceConnected(ComponentName name, IBinder iBinder) {
        BluetoothLeServiceBinder bluetoothLeServiceBinder = (BluetoothLeServiceBinder) iBinder;
        bluetoothLeService = bluetoothLeServiceBinder.getBluetoothLeService();
        if (bluetoothLeService == null) {
            Tool.warnOut(TAG, "bluetoothLeService is null.");
            return;
        }
        if (!bluetoothLeService.initialize()) {
            Tool.warnOut(TAG, "bluetoothLeService initialize failed!");
            return;
        }
        if (mAddress == null && mBluetoothDevice == null) {
            Tool.warnOut(TAG, "address and mBluetoothDevice is null!");
            return;
        }
        boolean connect;
        if (mAddress != null) {
            connect = bluetoothLeService.connect(mAddress, autoConnect);
        } else {
            connect = bluetoothLeService.connect(mBluetoothDevice, autoConnect);
        }
        Tool.warnOut(TAG, "connect " + connect);
    }

    /**
     * Called when a connection to the Service has been lost.  This typically
     * happens when the process hosting the service has crashed or been killed.
     * This does <em>not</em> remove the ServiceConnection itself -- this
     * binding to the service will remain active, and you will receive a call
     * to {@link #onServiceConnected} when the Service is next running.
     *
     * @param name The concrete component name of the service whose
     *             connection has been lost.
     */
    @Override
    public void onServiceDisconnected(ComponentName name) {
        bluetoothLeService = null;
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 与远端设备断开连接
     *
     * @return true表示成功
     */
    boolean disconnect() {
        return bluetoothLeService != null && bluetoothLeService.disconnect();
    }

    /**
     * 关闭蓝牙GATT服务
     *
     * @return true表示成功
     */
    @SuppressWarnings("UnusedReturnValue")
    boolean closeGatt() {
        return bluetoothLeService != null && bluetoothLeService.close();
    }

    /**
     * 写入数据到远端设备
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param value              数据内容
     * @return true表示成功
     */
    boolean writeData(String serviceUUID, String characteristicUUID, byte[] value) {
        return bluetoothLeService != null && !(serviceUUID == null || characteristicUUID == null || value == null) && bluetoothLeService.writeData(serviceUUID, characteristicUUID, value);
    }

    /**
     * 获取远端设备的数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    boolean readData(String serviceUUID, String characteristicUUID) {
        return bluetoothLeService != null && !(serviceUUID == null || characteristicUUID == null) && bluetoothLeService.readData(serviceUUID, characteristicUUID);
    }

    /**
     * 打开或关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param enable             true表示打开通知，false表示关闭通知
     * @return true表示执行成功
     */
    boolean enableNotification(String serviceUUID, String characteristicUUID, boolean enable) {
        return bluetoothLeService != null && bluetoothLeService.enableNotification(serviceUUID, characteristicUUID, enable);
    }

    /**
     * 停止BLE服务
     */
    void stopService() {
        if (bluetoothLeService == null) {
            return;
        }
        bluetoothLeService.stopSelf();
    }

    /**
     * 获取设备信号强度
     *
     * @return true表示成功
     */
    boolean getRssi() {
        return bluetoothLeService != null && bluetoothLeService.getRssi();
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    boolean refreshGattCache() {
        return bluetoothLeService != null && bluetoothLeService.refreshGattCache();
    }

    /**
     * 获取服务列表
     *
     * @return 服务列表
     */
    List<BluetoothGattService> getServices() {
        if (bluetoothLeService == null) {
            return null;
        }
        return bluetoothLeService.getServices();
    }

    /**
     * 设置自动连接标识
     *
     * @param autoConnect 自动连接标识
     */
    void setAutoConnect(boolean autoConnect) {
        this.autoConnect = autoConnect;
    }

    /**
     * 根据UUID获取设备的服务
     *
     * @param uuid BLUETOOTH_GATT_CHARACTERISTIC
     * @return BluetoothGattService
     */
    BluetoothGattService getService(UUID uuid) {
        if (bluetoothLeService == null) {
            return null;
        }
        return bluetoothLeService.getService(uuid);
    }

    /**
     * 请求改变最大传输字节限制
     *
     * @param mtu 最大传输字节数
     * @return true表示成功
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    boolean requestMtu(int mtu) {
        return bluetoothLeService != null && bluetoothLeService.requestMtu(mtu);
    }

    /**
     * 获取GATT对象
     *
     * @return BluetoothGatt
     */
    BluetoothGatt getBluetoothGatt() {
        if (bluetoothLeService == null) {
            return null;
        }
        return bluetoothLeService.getBluetoothGatt();
    }
}
