package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;


/**
 * 监听BLE连接相关的广播接收者
 *
 * @author alm
 */

public class ConnectBleBroadcastReceiver extends BroadcastReceiver {

    /*------------------------成员变量----------------------------*/

    /**
     * 连接成功的回调
     */
    private BleInterface.OnConnectedListener onConnectedListener;
    /**
     * 断开连接的回调
     */
    private BleInterface.OnDisconnectedListener onDisconnectedListener;
    /**
     * 服务发现完成的回调
     */
    private BleInterface.OnServicesDiscoveredListener onServicesDiscoveedListener;
    /**
     * 状态码错误的回调
     */
    private BleInterface.OnStatusErrorListener onStatusErrorListener;
    /**
     * BluetoothGatt客户端配置失败的回调
     */
    private BleInterface.OnBluetoothGattOptionsNotSuccessListener onBluetoothGattOptionsNotSuccessListener;
    /**
     * 正在连接的回调
     */
    private BleInterface.OnConnectingListener onConnectingListener;
    /**
     * 正在断开连接的回调
     */
    private BleInterface.OnDisconnectingListener onDisconnectingListener;
    /**
     * 读取到远端设备的数据的回调
     */
    private BleInterface.OnCharacteristicReadListener onCharacteristicReadListener;
    /**
     * 收到远端设备的通知的回调
     */
    private BleInterface.OnReceiveNotificationListener onReceiveNotificationListener;
    /**
     * 向远端设备写入数据的回调
     */
    private BleInterface.OnCharacteristicWriteListener onCharacteristicWriteListener;
    /**
     * 读取到远端设备的描述符的回调
     */
    private BleInterface.OnDescriptorReadListener onDescriptorReadListener;
    /**
     * 向远端设备写入描述符的回调
     */
    private BleInterface.OnDescriptorWriteListener onDescriptorWriteListener;
    /**
     * 可靠数据写入完成的回调
     */
    private BleInterface.OnReliableWriteCompletedListener onReliableWriteCompletedListener;
    /**
     * 读到远端设备rssi值的回调
     */
    private BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener;
    /**
     * 最大传输单位被改变的回调
     */
    private BleInterface.OnMtuChangedListener onMtuChangedListener;

    /**
     * 蓝牙开关状态被改变时的回调
     */
    private BleInterface.OnBluetoothSwitchChangedListener onBluetoothSwitchChangedListener;
    /**
     * Handler
     */
    private static final Handler HANDLER = new Handler();

    /*------------------------实现父类函数----------------------------*/

    /**
     * This method is called when the BroadcastReceiver is receiving an Intent
     * broadcast.  During this time you can use the other methods on
     * BroadcastReceiver to view/modify the current result values.  This method
     * is always called within the main thread of its process, unless you
     * explicitly asked for it to be scheduled on a different thread using
     * {@link Context#registerReceiver(BroadcastReceiver, * IntentFilter , String, Handler)}. When it runs on the main
     * thread you should
     * never perform long-running operations in it (there is a timeout of
     * 10 seconds that the system allows before considering the receiver to
     * be blocked and a candidate to be killed). You cannot launch a popup dialog
     * in your implementation of onReceive().
     * <p>
     * <p><b>If this BroadcastReceiver was launched through a &lt;receiver&gt; tag,
     * then the object is no longer alive after returning from this
     * function.</b>  This means you should not perform any operations that
     * return a result to you asynchronously -- in particular, for interacting
     * with services, you should use
     * {@link Context#startService(Intent)} instead of
     * {@link Context#bindService(Intent, ServiceConnection, int)}.  If you wish
     * to interact with a service that is already running, you can use
     * {@link #peekService}.
     * <p>
     * <p>The Intent filters used in {@link Context#registerReceiver}
     * and in application manifests are <em>not</em> guaranteed to be exclusive. They
     * are hints to the operating system about how to find suitable recipients. It is
     * possible for senders to force delivery to specific recipients, bypassing filter
     * resolution.  For this reason, {@link #onReceive(Context, Intent) onReceive()}
     * implementations should respond only to known actions, ignoring any unexpected
     * Intents that they may receive.
     *
     * @param context The Context in which the receiver is running.
     * @param intent  The Intent being received.
     */
    @SuppressWarnings({"JavadocReference", "JavaDoc"})
    @Override
    public void onReceive(Context context, Intent intent) {
        final byte[] values = intent.getByteArrayExtra(LibraryConstants.VALUE);
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        final String uuid = intent.getStringExtra(BleConstants.UUID);
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                processBluetoothStateChanged(context, intent);
                break;
            case BleConstants.ACTION_GATT_CONNECTED:
                processGattConnect();
                break;
            case BleConstants.ACTION_GATT_DISCONNECTED:
                processGattDisconnect();
                break;
            case BleConstants.ACTION_GATT_SERVICES_DISCOVERED:
                processGattServiceDiscovered();
                break;
            case BleConstants.ACTION_GATT_NOT_SUCCESS:
                processGattNotSuccess(intent);
                break;
            case BleConstants.ACTION_GATT_CONNECTING:
                processGattConnecting();
                break;
            case BleConstants.ACTION_GATT_DISCONNECTING:
                processGattDisconnecting();
                break;
            case BleConstants.ACTION_CHARACTERISTIC_READ:
                processCharacteristicRead(values, uuid);
                break;
            case BleConstants.ACTION_CHARACTERISTIC_CHANGED:
                processCharacteristicChanged(values, uuid);
                break;
            case BleConstants.ACTION_CHARACTERISTIC_WRITE:
                processCharacteristicWrite(values, uuid);
                break;
            case BleConstants.ACTION_DESCRIPTOR_READ:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_DESCRIPTOR_READ,value = " + Tool.bytesToHexStr(values));
                processGattDescriptorRead(values, uuid);
                break;
            case BleConstants.ACTION_DESCRIPTOR_WRITE:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_DESCRIPTOR_WRITE,value = " + Tool.bytesToHexStr(values));
                processDescriptorWrite(values, uuid);
                break;
            case BleConstants.ACTION_RELIABLE_WRITE_COMPLETED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_RELIABLE_WRITE_COMPLETED");
                processReliableWriteCompleted();
                break;
            case BleConstants.ACTION_READ_REMOTE_RSSI:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_READ_REMOTE_RSSI,rssi = " + values[0]);
                processReadRemoteRssi(values);
                break;
            case BleConstants.ACTION_MTU_CHANGED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_MTU_CHANGED,mtu = " + values[0]);
                processMtuChanged(values);
                break;
            case BleConstants.ACTION_GATT_DISCOVER_SERVICES_FAILED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_DISCOVER_SERVICES_FAILED");
                processDiscoverServiceFailed();
                break;
            case BleConstants.ACTION_GATT_STATUS_ERROR:
                int status = intent.getIntExtra(LibraryConstants.STATUS_ERROR, -1);
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_STATUS_ERROR,status = " + status);
                processGattStatusError(status);
                break;
            default:
                Tool.warnOut("ConnectBleBroadcastReceiver", "get other action" + action);
                break;
        }
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 设置设备已连接连接的回调
     *
     * @param onConnectedListener 设备已连接连接的回调
     */
    void setOnConnectedListener(BleInterface.OnConnectedListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
    }

    /**
     * 设置设备断开连接的回调
     *
     * @param onDisconnectedListener 设备断开连接的回调
     */
    void setOnDisconnectedListener(BleInterface.OnDisconnectedListener onDisconnectedListener) {
        this.onDisconnectedListener = onDisconnectedListener;
    }

    /**
     * 设置状态码错误的回调
     * @param onStatusErrorListener 状态码错误的回调
     */
    void setOnStatusErrorListener(BleInterface.OnStatusErrorListener onStatusErrorListener) {
        this.onStatusErrorListener = onStatusErrorListener;
    }

    /**
     * 设置服务扫描完成的回调
     *
     * @param onServicesDiscoveredListener 服务扫描完成的回调
     */
    void setOnServicesDiscoveredListener(BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener) {
        this.onServicesDiscoveedListener = onServicesDiscoveredListener;
    }

    /**
     * 设置正在连接的回调
     *
     * @param onConnectingListener 正在连接的回调
     */
    void setOnConnectingListener(BleInterface.OnConnectingListener onConnectingListener) {
        this.onConnectingListener = onConnectingListener;
    }

    void setOnDisconnectingListener(BleInterface.OnDisconnectingListener onDisconnectingListener) {
        this.onDisconnectingListener = onDisconnectingListener;
    }

    /**
     * 设置读取到远端设备数据的回调
     *
     * @param onCharacteristicReadListener 读取到远端设备数据的回调
     */
    void setOnCharacteristicReadListener(BleInterface.OnCharacteristicReadListener onCharacteristicReadListener) {
        this.onCharacteristicReadListener = onCharacteristicReadListener;
    }

    /**
     * 设置收到远端设备通知数据的回调
     *
     * @param onReceiveNotificationListener 收到远端设备通知数据的回调
     */
    void setOnReceiveNotificationListener(BleInterface.OnReceiveNotificationListener onReceiveNotificationListener) {
        this.onReceiveNotificationListener = onReceiveNotificationListener;
    }

    /**
     * 设置数据写入的回调
     *
     * @param onCharacteristicWriteListener 数据写入到远端数据的回调
     */
    void setOnCharacteristicWriteListener(BleInterface.OnCharacteristicWriteListener onCharacteristicWriteListener) {
        this.onCharacteristicWriteListener = onCharacteristicWriteListener;
    }

    /**
     * 设置读取到远端设备描述符的回调
     *
     * @param onDescriptorReadListener 读取到远端设备描述符的回调
     */
    void setOnDescriptorReadListener(BleInterface.OnDescriptorReadListener onDescriptorReadListener) {
        this.onDescriptorReadListener = onDescriptorReadListener;
    }

    /**
     * 设置描述符数据写入的回调
     *
     * @param onDescriptorWriteListener 描述符数据写入的回调
     */
    void setOnDescriptorWriteListener(BleInterface.OnDescriptorWriteListener onDescriptorWriteListener) {
        this.onDescriptorWriteListener = onDescriptorWriteListener;
    }

    /**
     * 设置可靠数据写入的回调
     *
     * @param onReliableWriteCompletedListener 可靠数据写入的回调
     */
    void setOnReliableWriteCompletedListener(BleInterface.OnReliableWriteCompletedListener onReliableWriteCompletedListener) {
        this.onReliableWriteCompletedListener = onReliableWriteCompletedListener;
    }

    /**
     * 设置获取到远端设备RSSI的回调
     *
     * @param onReadRemoteRssiListener 获取到远端设备RSSI的回调
     */
    void setOnReadRemoteRssiListener(BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener) {
        this.onReadRemoteRssiListener = onReadRemoteRssiListener;
    }

    /**
     * 设置mtu被改变时的回调
     *
     * @param onMtuChangedListener mtu被改变时的回调
     */
    void setOnMtuChangedListener(BleInterface.OnMtuChangedListener onMtuChangedListener) {
        this.onMtuChangedListener = onMtuChangedListener;
    }

    /**
     * 设置蓝牙开关状态改变时的回调
     *
     * @param onBluetoothSwitchChangedListener 蓝牙开关状态改变时的回调
     */
    void setOnBluetoothSwitchChangedListener(BleInterface.OnBluetoothSwitchChangedListener onBluetoothSwitchChangedListener) {
        this.onBluetoothSwitchChangedListener = onBluetoothSwitchChangedListener;
    }

    /**
     * 设置蓝牙配置未成功的回调
     *
     * @param onBluetoothGattOptionsNotSuccessListener 蓝牙配置未成功的回调
     */
    void setOnBluetoothGattOptionsNotSuccessListener(BleInterface.OnBluetoothGattOptionsNotSuccessListener onBluetoothGattOptionsNotSuccessListener) {
        this.onBluetoothGattOptionsNotSuccessListener = onBluetoothGattOptionsNotSuccessListener;
    }

    /*------------------------库内函数----------------------------*/

    /**
     *蓝牙状态更改时的处理
     * @param context 上下文
     * @param intent intent对象
     */
    private void processBluetoothStateChanged(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -2147483648);
        //如果蓝牙被关闭
        if (state == BluetoothAdapter.STATE_OFF) {
            Tool.toastL(context, R.string.bluetooth_off);
            if (onBluetoothSwitchChangedListener != null) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        onBluetoothSwitchChangedListener.onBluetoothSwitchChanged(false);
                    }
                });
            }
        }
        //如果蓝牙被打开
        else if (state == BluetoothAdapter.STATE_ON) {
            Tool.toastL(context, R.string.bluetooth_on);
            if (onBluetoothSwitchChangedListener != null) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        onBluetoothSwitchChangedListener.onBluetoothSwitchChanged(true);
                    }
                });
            }
        }
    }

    /**
     * 与远程设备建立连接时进行的处理
     */
    private void processGattConnect() {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_CONNECTED");
        if (onConnectedListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onConnectedListener.onConnected();
                }
            });
        }
    }

    /**
     * 与远程设备断开连接时进行的处理
     */
    private void processGattDisconnect() {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_SERVICES_DISCOVERED");
        if (onDisconnectedListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onDisconnectedListener.onDisconnected();
                }
            });
        }
    }

    /**
     * 发现远程设备服务时进行的处理
     */
    private void processGattServiceDiscovered() {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_SERVICES_DISCOVERED");
        HANDLER.post(new Runnable() {
            @Override
            public void run() {
                if (onServicesDiscoveedListener != null) {
                    onServicesDiscoveedListener.onServicesDiscovered();
                }
            }
        });
    }

    /**
     * GATT处理数据失败时进行的处理
     * @param intent intent
     */
    private void processGattNotSuccess(Intent intent) {
        final String methodName = intent.getStringExtra(LibraryConstants.METHOD);
        final int errorStatus = intent.getIntExtra(LibraryConstants.STATUS, LibraryConstants.DEFAULT_STATUS);
        if (onBluetoothGattOptionsNotSuccessListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onBluetoothGattOptionsNotSuccessListener.onBluetoothGattOptionsNotSuccess(methodName, errorStatus);
                }
            });
        }
    }

    /**
     * GATT正在进行连接时进行的处理
     */
    private void processGattConnecting() {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_CONNECTING");
        if (onConnectingListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onConnectingListener.onConnecting();
                }
            });
        }
    }

    /**
     * GATT正在断开连接时进行的处理
     */
    private void processGattDisconnecting() {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_DISCONNECTING");
        if (onDisconnectingListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onDisconnectingListener.onDisconnecting();
                }
            });
        }
    }

    /**
     * GATT读取到远端设备数据时进行的处理
     * @param values 数据内容
     * @param uuid 数据来源uuid
     */
    private void processCharacteristicRead(final byte[] values, final String uuid) {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_CHARACTERISTIC_READ,value = " + Tool.bytesToHexStr(values));
        if (onCharacteristicReadListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onCharacteristicReadListener.onCharacteristicRead(uuid, values);
                }
            });
        }
    }

    /**
     * GATT发现远端设备特征数据发生改变时(接收到通知时)进行的处理
     * @param values 数据内容
     * @param uuid 数据来源uuid
     */
    private void processCharacteristicChanged(final byte[] values, final String uuid) {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_CHARACTERISTIC_CHANGED,value = " + Tool.bytesToHexStr(values));
        if (onReceiveNotificationListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onReceiveNotificationListener.onReceiveNotification(uuid, values);
                }
            });
        }
    }

    /**
     * GATT向远端设备特征写入数据时的处理
     * @param values 数据内容
     * @param uuid 数据目标uuid
     */
    private void processCharacteristicWrite(final byte[] values, final String uuid) {
        Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_CHARACTERISTIC_WRITE,value = " + Tool.bytesToHexStr(values));
        if (onCharacteristicWriteListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onCharacteristicWriteListener.onCharacteristicWrite(uuid, values);
                }
            });
        }
    }

    /**
     * GATT读取到远端设备描述数据时的处理
     * @param values 数据内容
     * @param uuid 数据来源uuid
     */
    private void processGattDescriptorRead(final byte[] values, final String uuid) {
        if (onDescriptorReadListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onDescriptorReadListener.onDescriptorRead(uuid, values);
                }
            });
        }
    }

    /**
     * GATT向远端设备描述写入数据时的处理
     * @param values 数据内容
     * @param uuid 数据目标uuid
     */
    private void processDescriptorWrite(final byte[] values, final String uuid) {
        if (onDescriptorWriteListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onDescriptorWriteListener.onDescriptorWrite(uuid, values);
                }
            });
        }
    }

    /**
     * GATT向远端设备写入可靠数据完成时的处理
     */
    private void processReliableWriteCompleted() {
        if (onReliableWriteCompletedListener != null) {
            onReliableWriteCompletedListener.onReliableWriteCompleted();
        }
    }

    /**
     * GATT读取到远端设备RSSI时的处理
     * @param values 数据内容
     */
    private void processReadRemoteRssi(final byte[] values) {
        if (onReadRemoteRssiListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onReadRemoteRssiListener.onReadRemoteRssi(values[0]);
                }
            });
        }
    }

    /**
     * 与远端设备的MTU(单包最大读写数据长度)改变时的处理
     * @param values 数据内容
     */
    private void processMtuChanged(final byte[] values) {
        if (onMtuChangedListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onMtuChangedListener.onMtuChanged(values[0]);
                }
            });
        }
    }

    /**
     * 发现远端设备的服务失败时的处理
     */
    private void processDiscoverServiceFailed() {
        if (onServicesDiscoveedListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onServicesDiscoveedListener.onDiscoverServiceFailed();
                }
            });
        }
    }

    /**
     * 状态码错误的处理
     * @param status 状态码
     */
    private void processGattStatusError(final int status) {
        if (onStatusErrorListener != null) {
            HANDLER.post(new Runnable() {
                @Override
                public void run() {
                    onStatusErrorListener.onStatusError(status);
                }
            });
        }
    }
}
