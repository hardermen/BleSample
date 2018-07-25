package com.jackiepenghe.blelibrary;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * BLE连接的服务
 *
 * @author alm
 */

public class BluetoothLeService extends Service {

    /*------------------------静态常量----------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BluetoothLeService.class.getSimpleName();

    /*------------------------成员变量----------------------------*/

    /**
     * Binder对象
     */
    private BluetoothLeServiceBinder bluetoothLeServiceBinder;

    /**
     * 蓝牙连接的回调
     */
    private BleBluetoothGattCallback bleBluetoothGattCallback;

    /**
     * 蓝牙管理器
     */
    private BluetoothManager bluetoothManager;

    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;

    /**
     * 蓝牙GATT服务
     */
    private BluetoothGatt bluetoothGatt;

    /*------------------------重写父类函数----------------------------*/

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        bleBluetoothGattCallback = new BleBluetoothGattCallback(BluetoothLeService.this);
        bluetoothLeServiceBinder = new BluetoothLeServiceBinder(BluetoothLeService.this);
    }

    /**
     * Called by the system to notify a Service that it is no longer used and is being removed.  The
     * service should clean up any resources it holds (threads, registered
     * receivers, etc) at this point.  Upon return, there will be no more calls
     * in to this Service object and it is effectively dead.  Do not call this method directly.
     */
    @Override
    public void onDestroy() {
        super.onDestroy();
        bluetoothLeServiceBinder = null;
        bleBluetoothGattCallback = null;
        bluetoothManager = null;
        bluetoothAdapter = null;
        bluetoothGatt = null;
    }

    /*------------------------实现父类函数----------------------------*/

    /**
     * Return the communication channel to the service.  May return null if
     * clients can not bind to the service.  The returned
     * {@link IBinder} is usually for a complex interface
     * that has been <a href="{@docRoot}guide/components/aidl.html">described using
     * aidl</a>.
     * <p>
     * <p><em>Note that unlike other application components, calls on to the
     * IBinder interface returned here may not happen on the main thread
     * of the process</em>.  More information about the main thread can be found in
     * <a href="{@docRoot}guide/topics/ public booleandamentals/processes-and-threads.html">Processes and
     * Threads</a>.</p>
     *
     * @param intent The Intent that was used to bind to this service,
     *               as given to {@link Context#bindService
     *               Context.bindService}.  Note that any extras that were included with
     *               the Intent at that point will <em>not</em> be seen here.
     * @return Return an IBinder through which clients can call on to the
     * service.
     */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Tool.warnOut(TAG, "蓝牙连接服务绑定成功");
        return bluetoothLeServiceBinder;
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 初始化蓝牙相关的对象
     *
     * @return true表示成功
     */
    boolean initialize() {
        //使用单例模式获取蓝牙管理器
        bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            Tool.warnOut("BluetoothLeService", "get bluetoothManager failed!");
            return false;
        }

        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            Tool.warnOut("BluetoothLeService", "get bluetoothAdapter failed!");
            return false;
        }
        return true;
    }

    /**
     * 发起连接远端设备的请求
     *
     * @param address       远端设备地址
     * @param autoReconnect 自动重连标志
     * @return true表示成功
     */
    boolean connect(String address, boolean autoReconnect) {
        if (bluetoothAdapter == null || address == null) {
            return false;
        }

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            return false;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            bluetoothGatt = remoteDevice.connectGatt(this, autoReconnect, bleBluetoothGattCallback);
        }else {
            bluetoothGatt = remoteDevice.connectGatt(this, autoReconnect, bleBluetoothGattCallback,BluetoothDevice.TRANSPORT_LE);
        }

        return autoReconnect || bluetoothGatt != null && bluetoothGatt.connect();

    }

    /**
     * 发起连接远端设备的请求
     *
     * @param bluetoothDevice 远端设备地址
     * @param autoReconnect   自动重连标志
     * @return true表示成功
     */
    boolean connect(BluetoothDevice bluetoothDevice, boolean autoReconnect) {
        if (bluetoothAdapter == null || bluetoothDevice == null) {
            return false;
        }

        bluetoothGatt = bluetoothDevice.connectGatt(this, autoReconnect, bleBluetoothGattCallback);
        return bluetoothGatt != null && (autoReconnect || bluetoothGatt.connect());

    }

    /**
     * 发起断开连接的请求
     *
     * @return true表示成功
     */
    boolean disconnect() {
        if (bluetoothGatt == null) {
            return false;
        }
        bluetoothGatt.disconnect();
        return true;
    }

    /**
     * 关闭GATT服务
     *
     * @return true表示成功
     */
    boolean close() {
        if (bluetoothGatt == null) {
            return false;
        }

        bluetoothGatt.close();
        return true;
    }

    /**
     * 写入数据到蓝牙远端设备
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param values             数据
     * @return true表示成功
     */
    boolean writeData(String serviceUUID, String characteristicUUID, byte[] values) {
        if (serviceUUID == null || characteristicUUID == null || values == null || bluetoothGatt == null) {
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }


        if (!characteristic.setValue(values)) {
            return false;
        }

        Tool.warnOut(TAG, "values = " + Tool.bytesToHexStr(values));

        return bluetoothGatt.writeCharacteristic(characteristic);
    }

    /**
     * 从蓝牙远端设备获取数据
     *
     * @param serviceUUID        服务UUID
     *                           *
     * @param characteristicUUID 特征UUID
     *                           *
     * @return true表示成功
     */
    boolean readData(String serviceUUID, String characteristicUUID) {
        if (serviceUUID == null || characteristicUUID == null || bluetoothGatt == null) {
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));

        //noinspection SimplifiableIfStatement
        if (characteristic == null) {
            return false;
        }

        return bluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 打开或者关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param enable             是否打开通知
     * @return true表示执行成功
     */
    boolean enableNotification(String serviceUUID, String characteristicUUID, boolean enable) {
        if (serviceUUID == null || characteristicUUID == null) {
            return false;
        }
        BluetoothGattService bluetoothGattService = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (bluetoothGattService == null) {
            return false;
        }
        BluetoothGattCharacteristic bluetoothGattCharacteristic = bluetoothGattService.getCharacteristic(UUID.fromString(characteristicUUID));
        if (!bluetoothGatt.setCharacteristicNotification(bluetoothGattCharacteristic, enable)) {
            return false;
        }
        BluetoothGattDescriptor bluetoothGattDescriptor = bluetoothGattCharacteristic.getDescriptor(UUID.fromString(BleConstants.CLIENT_CHARACTERISTIC_CONFIG));
        if (bluetoothGattDescriptor == null) {
            return false;
        } else {
            bluetoothGattDescriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        }
        return bluetoothGatt.writeDescriptor(bluetoothGattDescriptor);
    }

    /**
     * 获取设备信号强度
     *
     * @return true表示成功
     */
    boolean getRssi() {
        return bluetoothGatt.readRemoteRssi();
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    @SuppressWarnings("TryWithIdenticalCatches")
    boolean refreshGattCache() {
        if (bluetoothGatt == null) {
            return false;
        }

        try {
            //noinspection JavaReflectionMemberAccess
            Method refresh = bluetoothGatt.getClass().getMethod("refresh");
            if (refresh != null) {
                return (boolean) refresh.invoke(bluetoothGatt);
            }
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return false;
    }

    /**
     * 获取服务列表
     *
     * @return 服务列表
     */
    List<BluetoothGattService> getServices() {
        return bleBluetoothGattCallback.getServices();
    }

    /**
     * 根据指定的UUID获取服务
     *
     * @param uuid 服务的UUID
     * @return BluetoothGattService
     */
    BluetoothGattService getService(UUID uuid) {
        if (bleBluetoothGattCallback == null) {
            return null;
        }
        return bleBluetoothGattCallback.getService(uuid);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    boolean requestMtu(int mtu) {
        return bluetoothGatt != null && bluetoothGatt.requestMtu(mtu);
    }

    /**
     * 获取当前连接的GATT
     *
     * @return BluetoothGatt
     */
    BluetoothGatt getBluetoothGatt() {
        return bluetoothGatt;
    }
}
