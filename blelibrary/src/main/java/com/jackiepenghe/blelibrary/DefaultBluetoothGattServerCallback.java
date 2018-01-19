package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.os.Handler;

/**
 * @author jacke
 * @date 2018/1/19 0019
 */

class DefaultBluetoothGattServerCallback extends BluetoothGattServerCallback {

    private static final String TAG = "DefaultBluetoothGattSer";

    private BleInterface.OnBluetoothGattServerCallbackListener onBluetoothGattServerCallbackListener;

    private Handler handler = new Handler();

    /**
     * Callback indicating when a remote device has been connected or disconnected.
     *
     * @param device   Remote device that has been connected or disconnected.
     * @param status   Status of the connect or disconnect operation.
     * @param newState Returns the new connection state. Can be one of
     *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
     *                 {@link BluetoothProfile#STATE_CONNECTED}
     */
    @Override
    public void onConnectionStateChange(final BluetoothDevice device, final int status, final int newState) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onConnectionStateChange(device, status, newState);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onConnectionStateChange:device name = " +
                    device.getName() + ",device address = " + device.getAddress() + ",status = " +
                    status + ",newState = " + newState);
        }
    }

    /**
     * Indicates whether a local service has been added successfully.
     *
     * @param status  Returns {@link BluetoothGatt#GATT_SUCCESS} if the service
     *                was added successfully.
     * @param service The service that has been added
     */
    @Override
    public void onServiceAdded(final int status, final BluetoothGattService service) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onServiceAdded(status, service);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onServiceAdded:status = " + status + ",serviceUUID = " +
                    service.getUuid().toString());
        }
    }

    /**
     * A remote client has requested to read a local characteristic.
     * <p>
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device         The remote device that has requested the read operation
     * @param requestId      The Id of the request
     * @param offset         Offset into the value of the characteristic
     * @param characteristic Characteristic to be read
     */
    @Override
    public void onCharacteristicReadRequest(final BluetoothDevice device, final int requestId, final int offset, final BluetoothGattCharacteristic characteristic) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onCharacteristicReadRequest(device, requestId, offset, characteristic);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicReadRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() +
                    ",requestId = " + requestId + ",offset = " + offset + ",characteristicUUID = " +
                    characteristic.getUuid().toString());
        }
    }

    /**
     * A remote client has requested to write to a local characteristic.
     * <p>
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device         The remote device that has requested the write operation
     * @param requestId      The Id of the request
     * @param characteristic Characteristic to be written to.
     * @param preparedWrite  true, if this write operation should be queued for
     *                       later execution.
     * @param responseNeeded true, if the remote device requires a response
     * @param offset         The offset given for the value
     * @param value          The value the client wants to assign to the characteristic
     */
    @Override
    public void onCharacteristicWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattCharacteristic characteristic, final boolean preparedWrite, final boolean responseNeeded, final int offset, final byte[] value) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite, responseNeeded, offset, value);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() +
                    ",requestId = " + requestId + ",characteristicUUID = " +
                    characteristic.getUuid().toString() + ",preparedWrite = " + preparedWrite +
                    ",responseNeeded = " + responseNeeded + ",offset = " + offset + ",value = " +
                    Tool.bytesToHexStr(value));
        }
    }

    /**
     * A remote client has requested to read a local descriptor.
     * <p>
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device     The remote device that has requested the read operation
     * @param requestId  The Id of the request
     * @param offset     Offset into the value of the characteristic
     * @param descriptor Descriptor to be read
     */
    @Override
    public void onDescriptorReadRequest(final BluetoothDevice device, final int requestId, final int offset, final BluetoothGattDescriptor descriptor) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onDescriptorReadRequest(device, requestId, offset, descriptor);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() +
                    ",requestId = " + requestId + ",offset = " + offset + ",descriptor = " + descriptor.getUuid().toString());
        }
    }

    /**
     * A remote client has requested to write to a local descriptor.
     * <p>
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device         The remote device that has requested the write operation
     * @param requestId      The Id of the request
     * @param descriptor     Descriptor to be written to.
     * @param preparedWrite  true, if this write operation should be queued for
     *                       later execution.
     * @param responseNeeded true, if the remote device requires a response
     * @param offset         The offset given for the value
     * @param value          The value the client wants to assign to the descriptor
     */
    @Override
    public void onDescriptorWriteRequest(final BluetoothDevice device, final int requestId, final BluetoothGattDescriptor descriptor, final boolean preparedWrite, final boolean responseNeeded, final int offset, final byte[] value) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onDescriptorWriteRequest(device, requestId, descriptor, preparedWrite, responseNeeded, offset, value);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() +
                    ",requestId = " + requestId + ",descriptorUUID = " +
                    descriptor.getUuid().toString() + ",preparedWrite = " + preparedWrite +
                    ",responseNeeded = " + responseNeeded + ",offset = " + offset + ",value = " +
                    Tool.bytesToHexStr(value));
        }
    }

    /**
     * Execute all pending write operations for this device.
     * <p>
     * <p>An application must call {@link BluetoothGattServer#sendResponse}
     * to complete the request.
     *
     * @param device    The remote device that has requested the write operations
     * @param requestId The Id of the request
     * @param execute   Whether the pending writes should be executed (true) or
     */
    @Override
    public void onExecuteWrite(final BluetoothDevice device, final int requestId, final boolean execute) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onExecuteWrite(device, requestId, execute);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() +
                    ",requestId = " + requestId + ",execute = " + execute);
        }
    }

    /**
     * Callback invoked when a notification or indication has been sent to
     * a remote device.
     * <p>
     * <p>When multiple notifications are to be sent, an application must
     * wait for this callback to be received before sending additional
     * notifications.
     *
     * @param device The remote device the notification has been sent to
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the operation was successful
     */
    @Override
    public void onNotificationSent(final BluetoothDevice device, final int status) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onNotificationSent(device, status);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() + ",status = " + status);
        }
    }

    /**
     * Callback indicating the MTU for a given device connection has changed.
     * <p>
     * <p>This callback will be invoked if a remote client has requested to change
     * the MTU for a given connection.
     *
     * @param device The remote device that requested the MTU change
     * @param mtu    The new MTU size
     */
    @Override
    public void onMtuChanged(final BluetoothDevice device, final int mtu) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onMtuChanged(device, mtu);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() + ",mtu = " + mtu);
        }
    }

    /**
     * Callback triggered as result of {@link BluetoothGattServer#setPreferredPhy}, or as a result
     * of remote device changing the PHY.
     *
     * @param device The remote device
     * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
     * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
     * @param status Status of the PHY update operation.
     *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onPhyUpdate(final BluetoothDevice device, final int txPhy, final int rxPhy, final int status) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onPhyUpdate(device, txPhy, rxPhy, status);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() + ",txPhy = " +
                    txPhy + ",rxPhy = " + rxPhy + ",status = " + status);
        }
    }

    /**
     * Callback triggered as result of {@link BluetoothGattServer#readPhy}
     *
     * @param device The remote device that requested the PHY read
     * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
     * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
     * @param status Status of the PHY read operation.
     *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onPhyRead(final BluetoothDevice device, final int txPhy, final int rxPhy, final int status) {
        if (onBluetoothGattServerCallbackListener != null) {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattServerCallbackListener.onPhyRead(device, txPhy, rxPhy, status);
                }
            };
            handler.post(runnable);
        } else {
            Tool.warnOut(TAG, "onCharacteristicWriteRequest:device name = " +
                    device.getName() + ",device address = " + device.getAddress() + ",txPhy = " +
                    txPhy + ",rxPhy = " + rxPhy + ",status = " + status);
        }
    }

    /**
     * 设置连接回调
     * @param onBluetoothGattServerCallbackListener 连接回调
     */
    void setOnBluetoothGattServerCallbackListener(BleInterface.OnBluetoothGattServerCallbackListener onBluetoothGattServerCallbackListener) {
        this.onBluetoothGattServerCallbackListener = onBluetoothGattServerCallbackListener;
    }
}
