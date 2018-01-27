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
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * @author alm
 * @date 2017/11/15
 * BLE多连接服务
 */

public class BluetoothMultiService extends Service {

    /*------------------------静态常量----------------------------*/

    private static final String TAG = "BluetoothMultiService";

    /*------------------------成员变量----------------------------*/

    /**
     * BLE多连接的Binder对象
     */
    private BluetoothMultiServiceBinder bluetoothMultiServiceBinder;
    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter bluetoothAdapter;
    /**
     * 多连接服务是否初始化的标志
     */
    private boolean initializeFinished;

    /**
     * 先设置一个默认的连接回调，通过这个回调再触发对应的设备的回调
     */
    private BleBluetoothMultiGattCallback bleBluetoothMultiGattCallback;
    /**
     * 存储GATT的集合
     */
    private HashMap<String, BluetoothGatt> gattCallbackHashMap = new HashMap<>();

    /*------------------------重写父类函数----------------------------*/

    /**
     * Called by the system when the service is first created.  Do not call this method directly.
     */
    @Override
    public void onCreate() {
        super.onCreate();
        bleBluetoothMultiGattCallback = new BleBluetoothMultiGattCallback();
        this.bluetoothMultiServiceBinder = new BluetoothMultiServiceBinder(this);
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
        bleBluetoothMultiGattCallback.close();
        bluetoothMultiServiceBinder = null;
        bluetoothAdapter = null;
        initializeFinished = false;
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
     * <a href="{@docRoot}guide/topics/fundamentals/processes-and-threads.html">Processes and
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
        return bluetoothMultiServiceBinder;
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 初始化
     *
     * @return true表示初始化成功
     */
    boolean initialize() {
        //使用单例模式获取蓝牙管理器
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

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
     * 发起一个连接
     *
     * @param address             设备地址
     * @param baseConnectCallback 连接回调
     * @param autoConnect         自动连接标志
     * @return true表示成功发起连接
     */
    boolean connectDevice(String address, BaseConnectCallback baseConnectCallback, boolean autoConnect) {
        if (bluetoothAdapter == null) {
            return false;
        }
        //检查蓝牙地址是否符合规范
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (bluetoothAdapter == null) {
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            return false;
        }

        BluetoothGatt bluetoothGatt = remoteDevice.connectGatt(this, autoConnect, bleBluetoothMultiGattCallback);
        if (autoConnect) {
            if (!bleBluetoothMultiGattCallback.callbackHashMap.containsKey(address)) {
                bleBluetoothMultiGattCallback.callbackHashMap.put(address, baseConnectCallback);
            }
            if (!gattCallbackHashMap.containsKey(address)) {
                gattCallbackHashMap.put(address, bluetoothGatt);
            }
            return true;
        }

        if (bluetoothGatt == null) {
            return false;
        }

        if (!bleBluetoothMultiGattCallback.callbackHashMap.containsKey(address)) {
            bleBluetoothMultiGattCallback.callbackHashMap.put(address, baseConnectCallback);
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            gattCallbackHashMap.put(address, bluetoothGatt);
        }
        return bluetoothGatt.connect();
    }

    /**
     * 根据指定的设备地址关闭GATT
     *
     * @param address 设备地址
     * @return true表示执行成功
     */
    boolean close(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt gatt = gattCallbackHashMap.get(address);
        gatt.close();
        gattCallbackHashMap.remove(address);
        bleBluetoothMultiGattCallback.callbackHashMap.remove(address);
        return true;
    }

    /**
     * 关闭所有的GATT
     */
    void closeAll() {
        for (Map.Entry<String, BluetoothGatt> entry : gattCallbackHashMap.entrySet()) {
            BluetoothGatt gatt = entry.getValue();
            gatt.disconnect();
            gatt.close();
        }
        for (Map.Entry<String, BaseConnectCallback> entry : bleBluetoothMultiGattCallback.callbackHashMap.entrySet()) {
            BaseConnectCallback baseConnectCallback = entry.getValue();
            baseConnectCallback.onGattClosed(entry.getKey());
        }
        bleBluetoothMultiGattCallback.callbackHashMap.clear();
        gattCallbackHashMap.clear();
    }

    /**
     * 根据设备地址断开指定的设备
     *
     * @param address 设备地址
     * @return true表示发起请求成功
     */
    boolean disconnect(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt gatt = gattCallbackHashMap.get(address);
        gatt.disconnect();
        return true;
    }

    /**
     * 断开所有设备
     */
    void disconnectAll() {
        for (Map.Entry<String, BluetoothGatt> entry : gattCallbackHashMap.entrySet()) {
            BluetoothGatt gatt = entry.getValue();
            gatt.disconnect();
        }
    }

    /**
     * 根据地址重新连接某个被断开的设备
     *
     * @param address 设备地址
     * @return true表示成功发起请求
     */
    boolean reConnect(String address) {
        return reConnect(address, false);
    }

    /**
     * 根据设备地址重新连接某个被断开的设备
     *
     * @param address     设备地址
     * @param autoConnect 是否自动连接
     * @return true表示成功发起请求
     */
    boolean reConnect(String address, @SuppressWarnings("SameParameterValue") boolean autoConnect) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!bleBluetoothMultiGattCallback.callbackHashMap.containsKey(address)) {
            return false;
        }
        if (gattCallbackHashMap.containsKey(address)) {
            BluetoothGatt gatt = gattCallbackHashMap.get(address);
            return gatt.connect();
        }
        if (bluetoothAdapter == null) {
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        if (remoteDevice == null) {
            return false;
        }
        BluetoothGatt bluetoothGatt = remoteDevice.connectGatt(this, autoConnect, bleBluetoothMultiGattCallback);
        if (autoConnect) {
            if (!gattCallbackHashMap.containsKey(address)) {
                gattCallbackHashMap.put(address, bluetoothGatt);
            }
            return true;
        }
        if (bluetoothGatt == null) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            gattCallbackHashMap.put(address, bluetoothGatt);
        }
        return bluetoothGatt.connect();
    }

    /**
     * 根据地址获取指定设备的服务
     *
     * @param address 设备地址
     * @return List<BluetoothGattService>
     */
    List<BluetoothGattService> getServices(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return null;
        }
        if (gattCallbackHashMap.containsKey(address)) {
            BluetoothGatt gatt = gattCallbackHashMap.get(address);
            return gatt.getServices();
        }
        return null;
    }

    /**
     * 根据地址和UUID获取指定设备的指定服务
     *
     * @param address 设备地址
     * @param uuid    UUID
     * @return BluetoothGattService
     */
    BluetoothGattService getService(String address, UUID uuid) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return null;
        }
        if (uuid == null) {
            return null;
        }
        if (gattCallbackHashMap.containsKey(address)) {
            BluetoothGatt gatt = gattCallbackHashMap.get(address);
            return gatt.getService(uuid);
        }
        return null;
    }

    /**
     * 根据设备地址清除GATT的缓存
     *
     * @param address 设备地址
     * @return true表示执行成功
     */
    boolean refreshGattCache(String address) {

        if (BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);

        if (bluetoothGatt == null) {
            return false;
        }

        try {
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
     * 清除所有GATT的缓存
     */
    void refreshAllGattCache() {
        for (Map.Entry<String, BluetoothGatt> entries : gattCallbackHashMap.entrySet()) {
            String key = entries.getKey();
            refreshGattCache(key);
        }
    }

    /**
     * 根据设备地址来给对应的设备写入数据
     *
     * @param address            设备地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param values             数据内容
     * @return true表示成功发起请求
     */
    boolean writeData(String address, String serviceUUID, String characteristicUUID, byte[] values) {

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);

        if (serviceUUID == null || characteristicUUID == null || values == null || bluetoothGatt == null) {
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        return characteristic != null && characteristic.setValue(values) && bluetoothGatt.writeCharacteristic(characteristic);


    }

    /**
     * 根据设备地址来读取对应的设备的数据
     *
     * @param address            设备地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功发起请求
     */
    boolean readData(String address, String serviceUUID, String characteristicUUID) {

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);

        if (serviceUUID == null || characteristicUUID == null || bluetoothGatt == null) {
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }

        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));

        return characteristic != null && bluetoothGatt.readCharacteristic(characteristic);
    }

    /**
     * 打开或关闭设备的通知
     *
     * @param address            设备地址
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param enable             true表示开启通知，false表示关闭通知
     * @return true表示成功发起请求
     */
    boolean enableNotification(String address, String serviceUUID, String characteristicUUID, boolean enable) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
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
     * 查看设备是否已经连接
     *
     * @param address 设备地址
     * @return true表示已经连接
     */
    boolean isConnected(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BaseConnectCallback baseConnectCallback = bleBluetoothMultiGattCallback.callbackHashMap.get(address);
        return baseConnectCallback.isConnected();
    }

    /**
     * 设置初始化状态为初始化完成
     */
    void setInitializeFinished() {
        this.initializeFinished = true;
    }

    /**
     * 获取初始化状态
     *
     * @return 初始化状态
     */
    boolean isInitializeFinished() {
        return initializeFinished;
    }
}
