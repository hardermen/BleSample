package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;

import java.util.HashMap;

/**
 * 多连接时库中默认预设的连接回调
 * @author jacke
 */

public class BleBluetoothMultiGattCallback extends BluetoothGattCallback {

    /*-------------------------静态常量-------------------------*/

    private static final String TAG = BleBluetoothMultiGattCallback.class.getSimpleName();

     /*-------------------------成员变量-------------------------*/

    HashMap<String, BaseConnectCallback> callbackHashMap = new HashMap<>();

     /*-------------------------构造函数-------------------------*/


     /*-------------------------重写父类函数-------------------------*/

    /**
     * Callback triggered as result of {@link BluetoothGatt#setPreferredPhy}, or as a result of
     * remote device changing the PHY.
     *
     * @param gatt   GATT client
     * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}.
     * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}.
     * @param status Status of the PHY update operation.
     *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onPhyUpdate(final BluetoothGatt gatt, final int txPhy, final int rxPhy, final int status) {
        final String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {

            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onPhyUpdate", status);
                    } else {
                        baseConnectCallback.onPhyUpdate(gatt, txPhy, rxPhy);
                    }
                }
            });
        }

    }

    /**
     * Callback triggered as result of {@link BluetoothGatt#readPhy}
     *
     * @param gatt   GATT client
     * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}.
     * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
     *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}.
     * @param status Status of the PHY read operation.
     *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onPhyRead(final BluetoothGatt gatt, final int txPhy, final int rxPhy, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onPhyRead", status);
                    } else {
                        baseConnectCallback.onPhyRead(gatt, txPhy, rxPhy);
                    }
                }
            });
        }
    }

    /**
     * BaseConnectCallback indicating when GATT client has connected/disconnected to/from a remote
     * GATT server.
     *
     * @param gatt     GATT client
     * @param status   Status of the connect or disconnect operation.
     *                 {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     * @param newState Returns the new connection state. Can be one of
     *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
     *                 {@link BluetoothProfile#STATE_CONNECTED}
     */
    @Override
    public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {

        String gattAddress;
        final BaseConnectCallback baseConnectCallback;


        switch (newState) {
            case BluetoothGatt.STATE_DISCONNECTED:
                gattAddress = gatt.getDevice().getAddress();
                if (callbackHashMap.containsKey(gattAddress)) {
                    baseConnectCallback = callbackHashMap.get(gattAddress);
                    BleManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            baseConnectCallback.setConnected(false);
                            baseConnectCallback.onDisConnected(gatt);
                        }
                    });
                }
                break;
            case BluetoothGatt.STATE_CONNECTING:
                gattAddress = gatt.getDevice().getAddress();
                if (callbackHashMap.containsKey(gattAddress)) {
                    baseConnectCallback = callbackHashMap.get(gattAddress);
                    BleManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            baseConnectCallback.onConnecting(gatt);
                        }
                    });
                }
                break;
            case BluetoothGatt.STATE_CONNECTED:
                gattAddress = gatt.getDevice().getAddress();
                if (callbackHashMap.containsKey(gattAddress)) {
                    baseConnectCallback = callbackHashMap.get(gattAddress);
                    if (!gatt.discoverServices()) {
                        BleManager.getHandler().post(new Runnable() {
                            @Override
                            public void run() {
                                baseConnectCallback.onDiscoverServicesFailed(gatt);
                            }
                        });
                        return;
                    }
                    BleManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            baseConnectCallback.setConnected(true);
                            baseConnectCallback.onConnected(gatt);
                        }
                    });
                }
                break;
            case BluetoothGatt.STATE_DISCONNECTING:
                gattAddress = gatt.getDevice().getAddress();
                if (callbackHashMap.containsKey(gattAddress)) {
                    baseConnectCallback = callbackHashMap.get(gattAddress);
                    BleManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            baseConnectCallback.onDisconnecting(gatt);
                        }
                    });
                }
                break;
            default:
                Tool.warnOut(TAG, gatt.getDevice().getAddress() + "other state");
                break;
        }
    }

    /**
     * Callback invoked when the list of remote services, characteristics and descriptors
     * for the remote device have been updated, ie new services have been discovered.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#discoverServices}
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the remote device
     */
    @Override
    public void onServicesDiscovered(final BluetoothGatt gatt, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onServicesDiscovered", status);
                    } else {
                        baseConnectCallback.onServicesDiscovered(gatt);
                    }
                }
            });
        }
    }

    /**
     * Callback reporting the result of a characteristic read operation.
     *
     * @param gatt           GATT client invoked {@link BluetoothGatt#readCharacteristic}
     * @param characteristic Characteristic that was read from the associated
     *                       remote device.
     * @param status         {@link BluetoothGatt#GATT_SUCCESS} if the read operation
     */
    @Override
    public void onCharacteristicRead(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final byte[] values = characteristic.getValue();
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onCharacteristicRead", status);
                    } else {
                        baseConnectCallback.onCharacteristicRead(gatt, values);
                    }
                }
            });
        }
    }

    /**
     * Callback indicating the result of a characteristic write operation.
     *
     * If this callback is invoked while a reliable write transaction is
     * in progress, the value of the characteristic represents the value
     * reported by the remote device. An application should compare this
     * value to the desired value to be written. If the values don't match,
     * the application must abort the reliable write transaction.
     *
     * @param gatt           GATT client invoked {@link BluetoothGatt#writeCharacteristic}
     * @param characteristic Characteristic that was written to the associated
     *                       remote device.
     * @param status         The result of the write operation
     *                       {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, final int status) {
        String gattAddress = gatt.getDevice().getAddress() + gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final byte[] values = characteristic.getValue();
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onCharacteristicWrite", status);
                    } else {
                        baseConnectCallback.onCharacteristicWrite(gatt, values);
                    }
                }
            });
        }
    }

    /**
     * Callback triggered as a result of a remote characteristic notification.
     *
     * @param gatt           GATT client the characteristic is associated with
     * @param characteristic Characteristic that has been updated as a result
     */
    @Override
    public void onCharacteristicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final byte[] values = characteristic.getValue();
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    baseConnectCallback.onReceivedNotification(gatt, values);
                }
            });
        }
    }

    /**
     * Callback reporting the result of a descriptor read operation.
     *
     * @param gatt       GATT client invoked {@link BluetoothGatt#readDescriptor}
     * @param descriptor Descriptor that was read from the associated
     *                   remote device.
     * @param status     {@link BluetoothGatt#GATT_SUCCESS} if the read operation
     */
    @Override
    public void onDescriptorRead(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final byte[] values = descriptor.getValue();
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onDescriptorRead", status);
                    } else {
                        baseConnectCallback.onDescriptorRead(gatt, values);
                    }
                }
            });
        }
    }

    /**
     * Callback indicating the result of a descriptor write operation.
     *
     * @param gatt       GATT client invoked {@link BluetoothGatt#writeDescriptor}
     * @param descriptor Descriptor that was writte to the associated
     *                   remote device.
     * @param status     The result of the write operation
     *                   {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
     */
    @Override
    public void onDescriptorWrite(final BluetoothGatt gatt, BluetoothGattDescriptor descriptor, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final byte[] values = descriptor.getValue();
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onDescriptorWrite", status);
                    } else {
                        baseConnectCallback.onDescriptorWrite(gatt, values);
                    }
                }
            });
        }
    }

    /**
     * Callback invoked when a reliable write transaction has been completed.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#executeReliableWrite}
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the reliable write
     */
    @Override
    public void onReliableWriteCompleted(final BluetoothGatt gatt, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onReliableWriteCompleted", status);
                    } else {
                        baseConnectCallback.onReliableWriteCompleted(gatt);
                    }
                }
            });
        }
    }

    /**
     * Callback reporting the RSSI for a remote device connection.
     * <p>
     * This callback is triggered in response to the
     * {@link BluetoothGatt#readRemoteRssi} function.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#readRemoteRssi}
     * @param rssi   The RSSI value for the remote device
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the RSSI was read successfully
     */
    @Override
    public void onReadRemoteRssi(final BluetoothGatt gatt, final int rssi, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onReliableWriteCompleted", status);
                    } else {
                        baseConnectCallback.onReadRemoteRssi(gatt, rssi);
                    }
                }
            });
        }
    }

    /**
     * Callback indicating the MTU for a given device connection has changed.
     * <p>
     * This callback is triggered in response to the
     * {@link BluetoothGatt#requestMtu} function, or in response to a connection
     * event.
     *
     * @param gatt   GATT client invoked {@link BluetoothGatt#requestMtu}
     * @param mtu    The new MTU size
     * @param status {@link BluetoothGatt#GATT_SUCCESS} if the MTU has been changed successfully
     */
    @Override
    public void onMtuChanged(final BluetoothGatt gatt, final int mtu, final int status) {
        String gattAddress = gatt.getDevice().getAddress();
        if (callbackHashMap.containsKey(gattAddress)) {
            final BaseConnectCallback baseConnectCallback = callbackHashMap.get(gattAddress);
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (BluetoothGatt.GATT_SUCCESS != status) {
                        baseConnectCallback.onBluetoothGattOptionsNotSuccess(gatt, "onReliableWriteCompleted", status);
                    } else {
                        baseConnectCallback.onMtuChanged(gatt, mtu);
                    }
                }
            });
        }
    }

    public void close() {
        callbackHashMap.clear();
        callbackHashMap = null;
    }
}
