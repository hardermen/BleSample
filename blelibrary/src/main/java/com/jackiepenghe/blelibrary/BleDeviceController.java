package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.List;
import java.util.UUID;

/**
 * A tool class that operates on a device when multiple devices are connected
 *
 * @author jackie
 */
public final class BleDeviceController {

    /*-----------------------------------field variables-----------------------------------*/

    /**
     * BleMultiConnector
     */
    private BleMultiConnector bleMultiConnector;
    /**
     * device address
     */
    private String address;

    /*-----------------------------------Constructor-----------------------------------*/

    /**
     * Constructor
     *
     * @param bleMultiConnector BleMultiConnector
     * @param address           device address
     */
    BleDeviceController(@NonNull BleMultiConnector bleMultiConnector,@NonNull String address) {
        this.bleMultiConnector = bleMultiConnector;
        this.address = address;
    }

    /*-----------------------------------getter and setter-----------------------------------*/

    /**
     * get BluetoothLeService
     *
     * @return BluetoothLeService
     */
    @SuppressWarnings("unused")
    @Nullable
    public BluetoothMultiService getBluetoothMultiService() {
        if (bleMultiConnector == null){
            return null;
        }
        return bleMultiConnector.getBluetoothMultiService();
    }

    /**
     * get Bluetooth Adapter
     *
     * @return Bluetooth Adapter
     */
    @Nullable
    public BluetoothAdapter getBluetoothAdapter() {
        if (bleMultiConnector == null) {
            return null;
        }
        return bleMultiConnector.getBluetoothAdapter();
    }

    /*-----------------------------------public methods-----------------------------------*/

    /**
     * Refresh gatt cache
     *
     * @return true means request successful
     */
    public boolean refreshGattCache() {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        return address != null && bleMultiConnector.refreshGattCache(address);
    }

    /**
     * write data
     *
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @param values             data
     * @return true means request successful
     */
    public boolean writeData(@NonNull String serviceUUID,@NonNull String characteristicUUID,@NonNull byte[] values) {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        return bleMultiConnector != null && address != null && bleMultiConnector.writeData(address, serviceUUID, characteristicUUID, values);
    }


    /**
     * read data
     *
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means request successful
     */
    public boolean readData(@NonNull String serviceUUID,@NonNull String characteristicUUID) {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        return bleMultiConnector != null && address != null && bleMultiConnector.readData(address, serviceUUID, characteristicUUID);
    }

    /**
     * enable notification
     *
     * @param serviceUUID        service UUID
     * @param characteristicUUID characteristic UUID
     * @param enable             true means enable,false means disable
     * @return true means request successful
     */
    public boolean enableNotification(@NonNull String serviceUUID, @NonNull String characteristicUUID, boolean enable) {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        return bleMultiConnector != null && address != null && bleMultiConnector.enableNotification(address, serviceUUID, characteristicUUID, enable);
    }

    /**
     * Reconnect device
     *
     * @return true means request successful
     */
    public boolean reConnect() {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        return bleMultiConnector != null && address != null && bleMultiConnector.reConnect(address);
    }

    /**
     * closeGatt current gatt connection
     *
     * @return true means request successful
     */
    public boolean close() {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        this.bleMultiConnector = null;
        boolean result =  bleMultiConnector != null && bleMultiConnector.close(address);
        if (result){
            address = null;
        }
        return result;
    }

    /**
     * get remote device service list
     *
     * @return service list
     */
    @Nullable
    public List<BluetoothGattService> getServices() {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        if (bleMultiConnector == null) {
            return null;
        }
        if (address == null) {
            return null;
        }
        return bleMultiConnector.getServices(address);
    }

    /**
     * disconnect remote device
     *
     * @return true means request successful
     */
    public boolean disconnect() {
        BleMultiConnector bleMultiConnector = this.bleMultiConnector;
        return bleMultiConnector != null && bleMultiConnector.disconnect(address);
    }

    /**
     * get gatt service by uuid
     *
     * @param uuid UUID
     * @return gatt service
     */
    @SuppressWarnings("WeakerAccess")
    @Nullable
    public BluetoothGattService getService(@NonNull UUID uuid) {
        if (bleMultiConnector == null) {
            return null;
        }
        if (address == null) {
            return null;
        }
        return bleMultiConnector.getService(address, uuid);
    }

    /**
     * Check for support notifications
     *
     * @param serviceUUID        Service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means support
     */
    public boolean canNotify(@NonNull String serviceUUID,@NonNull String characteristicUUID) {
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
     * Check for support read
     *
     * @param serviceUUID        Service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means support
     */
    public boolean canRead(@NonNull  String serviceUUID,@NonNull  String characteristicUUID) {
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
     * Check for support write(Signed)
     *
     * @param serviceUUID        Service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means support
     */
    public boolean canSignedWrite(@NonNull String serviceUUID,@NonNull  String characteristicUUID) {
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
     * Check for support write
     *
     * @param serviceUUID        Service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means support
     */
    public boolean canWrite(@NonNull  String serviceUUID,@NonNull  String characteristicUUID) {
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
     * Check for support write(no response)
     *
     * @param serviceUUID        Service UUID
     * @param characteristicUUID characteristic UUID
     * @return true means support
     */
    public boolean canWriteNoResponse(@NonNull  String serviceUUID,@NonNull  String characteristicUUID) {
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
     * @return true, if the reliable write transaction has been initiated
     */
    public boolean beginReliableWrite() {
        if (bleMultiConnector == null) {
            return false;
        }
        if (address == null) {
            return false;
        }
        return bleMultiConnector.beginReliableWrite(address);
    }

    /**
     * Cancels a reliable write transaction for a given device.
     *
     * <p>Calling this function will discard all queued characteristic write
     * operations for a given remote device.
     *
     * <p>Requires {@link android.Manifest.permission#BLUETOOTH} permission.
     */
    @SuppressWarnings("WeakerAccess")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public boolean abortReliableWrite() {
        if (bleMultiConnector == null) {
            return false;
        }
        if (address == null) {
            return false;
        }
        return bleMultiConnector.abortReliableWrite(address);
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
     * @return true, if the remote service discovery has been started
     */
    public boolean discoverServices() {
        if (bleMultiConnector == null) {
            return false;
        }
        if (address == null) {
            return false;
        }
        return bleMultiConnector.discoverServices(address);
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
     * @return true, if the request to execute the transaction has been sent
     */
    @SuppressWarnings("WeakerAccess")
    public boolean executeReliableWrite() {
        if (bleMultiConnector == null) {
            return false;
        }
        if (address == null) {
            return false;
        }
        return bleMultiConnector.executeReliableWrite(address);
    }
}
