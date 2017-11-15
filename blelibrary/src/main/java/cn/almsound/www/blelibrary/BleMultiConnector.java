package cn.almsound.www.blelibrary;

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

    public boolean disconnect(String address) {
        return bluetoothMultiService.disconnect(address);
    }

    public boolean disconnectAll() {
        return bluetoothMultiService.disconnectAll();
    }

    public boolean close(String address) {
        return bluetoothMultiService.close(address);
    }

    public boolean closeAll() {
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
        return true;
    }

    public boolean connect(@NonNull String address, @NonNull BleConnectCallback bleConnectCallback) {
        return connect(address, bleConnectCallback, false);
    }

    public boolean connect(@NonNull String address, @NonNull BleConnectCallback bleConnectCallback, boolean autoConnect) {
        return bluetoothMultiService != null && bluetoothMultiService.connectDevice(address, bleConnectCallback, autoConnect);
    }

    public boolean reConnect(String address) {
        return bluetoothMultiService != null && bluetoothMultiService.reConnect(address);
    }

    public List<BluetoothGattService> getServices(String address) {
        if (bluetoothMultiService == null) {
            return null;
        }
        return bluetoothMultiService.getServices(address);
    }


    public BluetoothGattService getService(String address, UUID uuid) {
        if (bluetoothMultiService == null) {
            return null;
        }
        return bluetoothMultiService.getService(address, uuid);
    }

    public BluetoothMultiService getBluetoothMultiService() {
        return bluetoothMultiService;
    }

    public void setBluetoothMultiService(BluetoothMultiService bluetoothMultiService) {
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

    public Context getContext() {
        return contextWeakReference.get();
    }

    public BleDeviceController getBleDeviceController(String address){
        if (bluetoothMultiService == null){
            return null;
        }
       if (!bluetoothMultiService.isConnected(address)){
            return null;
       }
        return new BleDeviceController(this,address);
    }
}
