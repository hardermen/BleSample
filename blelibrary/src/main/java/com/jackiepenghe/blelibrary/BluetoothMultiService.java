package com.jackiepenghe.blelibrary;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;


/**
 * BLE multi-connection service
 *
 * @author alm
 */

public final class BluetoothMultiService extends Service {

    /*-----------------------------------static constant-----------------------------------*/

    private static final String TAG = BluetoothMultiService.class.getSimpleName();

    /*-----------------------------------field variables-----------------------------------*/


    /**
     * First set a default connection callback, and then trigger the callback of the corresponding device through this callback.
     */
    private BleBluetoothMultiGattCallback bleBluetoothMultiGattCallback = new BleBluetoothMultiGattCallback();
    /**
     * Binder instance
     */
    private BluetoothMultiServiceBinder bluetoothMultiServiceBinder = new BluetoothMultiServiceBinder(this);
    /**
     * a collection for storing GATT
     */
    private HashMap<String, BluetoothGatt> gattCallbackHashMap = new HashMap<>();
    /**
     * Bluetooth Adapter
     */
    @Nullable
    private BluetoothAdapter bluetoothAdapter;
    /**
     * Whether it has been initialized
     */
    private boolean initializeFinished;
    /**
     * connect time
     */
    private long connectTimeOut = 10000;

    /*-----------------------------------Override method-----------------------------------*/

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
        bluetoothMultiServiceBinder.releaseData();
        bluetoothMultiServiceBinder = null;
        bluetoothAdapter = null;
        initializeFinished = false;
    }

    /*-----------------------------------Implementation method-----------------------------------*/

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

    /*-----------------------------------package private setter-----------------------------------*/

    /**
     * set connect timeout
     *
     * @param connectTimeOut connect timeout
     */
    void setConnectTimeOut(@IntRange(from = 0) long connectTimeOut) {
        this.connectTimeOut = connectTimeOut;
    }

    /*-----------------------------------package private method-----------------------------------*/

    /**
     * initialization
     *
     * @return true means initialization successful
     */
    boolean initialize() {
        //Use the singleton mode to get the Bluetooth manager
        BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            DebugUtil.warnOut(TAG, "get bluetoothManager failed!");
            return false;
        }
        bluetoothAdapter = bluetoothManager.getAdapter();
        if (bluetoothAdapter == null) {
            DebugUtil.warnOut("BluetoothLeService", "get bluetoothAdapter failed!");
            return false;
        }
        return true;
    }

    /**
     * Initiate a connection
     *
     * @param address                device address
     * @param baseBleConnectCallback callback for connection
     * @param autoConnect            Whether to connect automatically
     * @return true means request successful
     */
    boolean connect(@NonNull String address, @NonNull BaseBleConnectCallback baseBleConnectCallback, boolean autoConnect) {
        if (bluetoothAdapter == null) {
            return false;
        }
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (bluetoothAdapter == null) {
            return false;
        }
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);

        return connect(remoteDevice, baseBleConnectCallback, autoConnect);
    }

    /**
     * start connect
     *
     * @param bluetoothDevice        remote device
     * @param baseBleConnectCallback callback for connection
     * @param autoConnect            Whether to connect automatically
     * @return true means request successful
     */
    boolean connect(@NonNull BluetoothDevice bluetoothDevice, @NonNull BaseBleConnectCallback baseBleConnectCallback, boolean autoConnect) {
        if (bluetoothAdapter == null) {
            return false;
        }
        String address = bluetoothDevice.getAddress();
        if (bleBluetoothMultiGattCallback.callbackHashMap.containsKey(address) && gattCallbackHashMap.containsKey(address)) {
            BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
            if (bluetoothGatt != null){
                return bluetoothGatt.connect();
            }else {
                gattCallbackHashMap.remove(address);
                bleBluetoothMultiGattCallback.callbackHashMap.remove(address);
            }
        }
        BluetoothGatt bluetoothGatt = bluetoothDevice.connectGatt(this, autoConnect, bleBluetoothMultiGattCallback);
        if (autoConnect) {
            if (!bleBluetoothMultiGattCallback.callbackHashMap.containsKey(address)) {
                bleBluetoothMultiGattCallback.callbackHashMap.put(address, baseBleConnectCallback);
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
            bleBluetoothMultiGattCallback.callbackHashMap.put(address, baseBleConnectCallback);
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            gattCallbackHashMap.put(address, bluetoothGatt);
        }
        checkTimeOut(baseBleConnectCallback, bluetoothGatt);
        return bluetoothGatt.connect();
    }

    /**
     * close GATT by specified address
     *
     * @param address address
     * @return true means successful
     */
    boolean close(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt gatt = gattCallbackHashMap.get(address);
        if (gatt == null) {
            return false;
        }
        gatt.close();
        gattCallbackHashMap.remove(address);
        bleBluetoothMultiGattCallback.callbackHashMap.remove(address);
        return true;
    }

    /**
     * close all GATT
     */
    void closeAll() {
        BluetoothDevice device = null;
        for (Map.Entry<String, BluetoothGatt> entry : gattCallbackHashMap.entrySet()) {
            BluetoothGatt gatt = entry.getValue();
            device = gatt.getDevice();
            gatt.disconnect();
            gatt.close();
        }
        for (Map.Entry<String, BaseBleConnectCallback> entry : bleBluetoothMultiGattCallback.callbackHashMap.entrySet()) {
            BaseBleConnectCallback baseBleConnectCallback = entry.getValue();
            baseBleConnectCallback.onGattClosed(device);
        }
        bleBluetoothMultiGattCallback.callbackHashMap.clear();
        gattCallbackHashMap.clear();
    }

    /**
     * disconnect remote device by specified address
     *
     * @param address device address
     * @return true means request successful
     */
    boolean disconnect(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt gatt = gattCallbackHashMap.get(address);
        if (gatt == null) {
            return false;
        }
        gatt.disconnect();
        return true;
    }

    /**
     * disconnect all device
     */
    void disconnectAll() {
        for (Map.Entry<String, BluetoothGatt> entry : gattCallbackHashMap.entrySet()) {
            BluetoothGatt gatt = entry.getValue();
            gatt.disconnect();
        }
    }

    /**
     * reconnect remote device by specified address
     *
     * @param address device address
     * @return true means request successful
     */
    boolean reConnect(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!bleBluetoothMultiGattCallback.callbackHashMap.containsKey(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt gatt = gattCallbackHashMap.get(address);
        if (gatt == null) {
            return false;
        }
        return gatt.connect();
    }

    /**
     * get GATT services by specified address
     *
     * @param address device address
     * @return List<BluetoothGattService>
     */
    @Nullable
    List<BluetoothGattService> getServices(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return null;
        }
        if (gattCallbackHashMap.containsKey(address)) {
            BluetoothGatt gatt = gattCallbackHashMap.get(address);
            if (gatt == null) {
                return null;
            }
            return gatt.getServices();
        }
        return null;
    }

    /**
     * get GATT service by specified address and UUID
     *
     * @param address device address
     * @param uuid    UUID
     * @return BluetoothGattService
     */
    @Nullable
    BluetoothGattService getService(@NonNull String address, @NonNull UUID uuid) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return null;
        }
        if (gattCallbackHashMap.containsKey(address)) {
            BluetoothGatt gatt = gattCallbackHashMap.get(address);
            if (gatt == null) {
                return null;
            }
            return gatt.getService(uuid);
        }
        return null;
    }

    /**
     * refresh GATT cache by specified address
     *
     * @param address device address
     * @return true means request successful,
     */
    boolean refreshGattCache(@NonNull String address) {

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
            //noinspection JavaReflectionMemberAccess
            Method refresh = bluetoothGatt.getClass().getMethod("refresh");
            return (boolean) refresh.invoke(bluetoothGatt);
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
     * refresh all GATT cache
     */
    void refreshAllGattCache() {
        for (Map.Entry<String, BluetoothGatt> entries : gattCallbackHashMap.entrySet()) {
            String key = entries.getKey();
            refreshGattCache(key);
        }
    }

    /**
     * write data to remote device by specified address
     *
     * @param address            device address
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @param data               data
     * @return true means request success
     */
    boolean writeData(@NonNull String address, @NonNull String serviceUUID, @NonNull String characteristicUUID, @NonNull byte[] data) {

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);

        if (bluetoothGatt == null) {
            return false;
        }
        BluetoothGattService service = bluetoothGatt.getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        return characteristic != null && characteristic.setValue(data) && bluetoothGatt.writeCharacteristic(characteristic);


    }

    /**
     * read data from remote device by specified address
     *
     * @param address            device address
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means request success
     */
    boolean readData(@NonNull String address, @NonNull String serviceUUID, @NonNull String characteristicUUID) {

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);

        if (bluetoothGatt == null) {
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
     * enable or disable notification by specified address
     *
     * @param address            device address
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @param enable             true means enable,false means disable
     * @return true means request success
     */
    boolean enableNotification(@NonNull String address, @NonNull String serviceUUID, @NonNull String characteristicUUID, boolean enable) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }

        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
        if (bluetoothGatt == null) {
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
     * get connect state by specified address
     *
     * @param address device address
     * @return true means remote device is connected
     */
    boolean isConnected(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BaseBleConnectCallback baseBleConnectCallback = bleBluetoothMultiGattCallback.callbackHashMap.get(address);
        if (baseBleConnectCallback == null) {
            return false;
        }
        return baseBleConnectCallback.isConnected();
    }

    /**
     * Initiates a reliable write transaction for a given remote device.
     *
     * <p>Once a reliable write transaction has been initiated, all calls
     * to {@link BluetoothGatt#writeCharacteristic} are sent to the remote device for
     * verification and queued up for atomic execution. The application will
     * receive an {@link BluetoothGattCallback#onCharacteristicWrite} callback
     * in response to every {@link BluetoothGatt#writeCharacteristic} call and is responsible
     * for verifying if the value has been transmitted accurately.
     *
     * <p>After all characteristics have been queued up and verified,
     * {@link #executeReliableWrite} will execute all writes. If a characteristic
     * was not written correctly, calling {@link #abortReliableWrite} will
     * cancel the current transaction without commiting any values on the
     * remote device.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param address device address
     * @return true, if the reliable write transaction has been initiated
     */
    boolean beginReliableWrite(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
        if (bluetoothGatt == null) {
            return false;
        }
        return bluetoothGatt.beginReliableWrite();
    }

    /**
     * Cancels a reliable write transaction for a given device.
     *
     * <p>Calling this function will discard all queued characteristic write
     * operations for a given remote device.
     *
     * @param address device address
     *                <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     */
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    boolean abortReliableWrite(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
        if (bluetoothGatt == null) {
            return false;
        }
        bluetoothGatt.abortReliableWrite();
        return true;
    }

    /**
     * Discovers services offered by a remote device as well as their
     * characteristics and descriptors.
     *
     * <p>This is an asynchronous operation. Once service discovery is completed,
     * the {@link BluetoothGattCallback#onServicesDiscovered} callback is
     * triggered. If the discovery was successful, the remote services can be
     * retrieved using the {@link #getServices} function.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param address device address
     * @return true, if the remote service discovery has been started
     */
    boolean discoverServices(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
        if (bluetoothGatt == null) {
            return false;
        }
        return bluetoothGatt.discoverServices();
    }

    /**
     * Executes a reliable write transaction for a given remote device.
     *
     * <p>This function will commit all queued up characteristic write
     * operations for a given remote device.
     *
     * <p>A {@link BluetoothGattCallback#onReliableWriteCompleted} callback is
     * invoked to indicate whether the transaction has been executed correctly.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     *
     * @param address device address
     * @return true, if the request to execute the transaction has been sent
     */
    boolean executeReliableWrite(@NonNull String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (!gattCallbackHashMap.containsKey(address)) {
            return false;
        }
        BluetoothGatt bluetoothGatt = gattCallbackHashMap.get(address);
        if (bluetoothGatt == null) {
            return false;
        }
        return bluetoothGatt.executeReliableWrite();
    }

    /**
     * set initializeFinished = true
     */
    void setInitializeFinished() {
        this.initializeFinished = true;
    }

    /**
     * get initialize state
     *
     * @return initialize state
     */
    boolean isInitializeFinished() {
        return initializeFinished;
    }

    /**
     * return BluetoothAdapter
     *
     * @return BluetoothAdapter
     */
    @Nullable
    BluetoothAdapter getBluetoothAdapter() {
        return bluetoothAdapter;
    }

    /*-----------------------------------private method-----------------------------------*/

    /**
     * check connect time out
     *
     * @param baseBleConnectCallback BaseBleConnectCallback
     */
    private void checkTimeOut(@NonNull final BaseBleConnectCallback baseBleConnectCallback, final BluetoothGatt bluetoothGatt) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long startTime = System.currentTimeMillis();
                while (!baseBleConnectCallback.isConnected() && !baseBleConnectCallback.isServiceDiscovered()) {
                    if (System.currentTimeMillis() - startTime >= connectTimeOut) {
                        BleManager.getHANDLER().post(new Runnable() {
                            @Override
                            public void run() {
                                baseBleConnectCallback.onConnectTimeOut(bluetoothGatt);
                            }
                        });
                        break;
                    }
                }
            }
        };
        BleManager.getThreadFactory().newThread(runnable).start();
    }
}
