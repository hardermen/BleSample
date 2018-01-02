package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;

import com.jackiepenghe.baselibrary.Tool;


/**
 * @author alm
 *         Created by alm on 17-6-5.
 *         BLE相关事件的广播接收者
 */

public class ConnectBleBroadcastReceiver extends BroadcastReceiver {

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
    private BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener;
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
     * 蓝牙被打开的回调
     */
    private BleInterface.OnBluetoothOpenListener onBluetoothOpenListener;

    /**
     * 蓝牙被关闭的回调
     */
    private BleInterface.OnBluetoothCloseListener onBluetoothCloseListener;
    private Handler handler = new Handler();


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
        if (action == null){
            return;
        }
        switch (action) {
            case BluetoothAdapter.ACTION_STATE_CHANGED:
                int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -2147483648);
                //如果蓝牙被关闭
                if (state == BluetoothAdapter.STATE_OFF) {
                    Tool.toastL(context, R.string.bluetooth_off);
                    if (onBluetoothCloseListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onBluetoothCloseListener.onBluetoothClose();
                            }
                        });
                    }
                }
                //如果蓝牙被打开
                else if (state == BluetoothAdapter.STATE_ON) {
                    Tool.toastL(context, R.string.bluetooth_on);
                    if (onBluetoothOpenListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                onBluetoothOpenListener.onBluetoothOpen();
                            }
                        });
                    }
                }
                break;
            case BleConstants.ACTION_GATT_CONNECTED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_CONNECTED");
                if (onConnectedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onConnectedListener.onConnected();
                        }
                    });
                }
                break;
            case BleConstants.ACTION_GATT_DISCONNECTED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_SERVICES_DISCOVERED");
                if (onDisconnectedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onDisconnectedListener.onDisconnected();
                        }
                    });
                }
                break;
            case BleConstants.ACTION_GATT_SERVICES_DISCOVERED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_SERVICES_DISCOVERED");
                if (onServicesDiscoveredListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onServicesDiscoveredListener.onServicesDiscovered();
                        }
                    });
                }
                break;
            case BleConstants.ACTION_GATT_CONNECTING:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_CONNECTING");
                if (onConnectingListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onConnectingListener.onConnecting();
                        }
                    });
                }
                break;
            case BleConstants.ACTION_GATT_DISCONNECTING:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_GATT_DISCONNECTING");
                if (onDisconnectingListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onDisconnectingListener.onDisconnecting();
                        }
                    });
                }
                break;
            case BleConstants.ACTION_CHARACTERISTIC_READ:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_CHARACTERISTIC_READ,value = " + Tool.bytesToHexStr(values));
                if (onCharacteristicReadListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onCharacteristicReadListener.onCharacteristicRead(values);
                        }
                    });
                }
                break;
            case BleConstants.ACTION_CHARACTERISTIC_CHANGED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_CHARACTERISTIC_CHANGED,value = " + Tool.bytesToHexStr(values));
                if (onReceiveNotificationListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onReceiveNotificationListener.onReceiveNotification(values);
                        }
                    });
                }
                break;
            case BleConstants.ACTION_CHARACTERISTIC_WRITE:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_CHARACTERISTIC_WRITE,value = " + Tool.bytesToHexStr(values));
                if (onCharacteristicWriteListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onCharacteristicWriteListener.onCharacteristicWrite(values);
                        }
                    });
                }
                break;
            case BleConstants.ACTION_DESCRIPTOR_READ:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_DESCRIPTOR_READ,value = " + Tool.bytesToHexStr(values));
                if (onDescriptorReadListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onDescriptorReadListener.onDescriptorRead(values);
                        }
                    });
                }
                break;
            case BleConstants.ACTION_DESCRIPTOR_WRITE:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_DESCRIPTOR_WRITE,value = " + Tool.bytesToHexStr(values));
                if (onDescriptorWriteListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onDescriptorWriteListener.onDescriptorWrite(values);
                        }
                    });
                }
                break;
            case BleConstants.ACTION_RELIABLE_WRITE_COMPLETED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_RELIABLE_WRITE_COMPLETED");
                if (onReliableWriteCompletedListener != null) {
                    onReliableWriteCompletedListener.onReliableWriteCompleted();
                }
                break;
            case BleConstants.ACTION_READ_REMOTE_RSSI:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_READ_REMOTE_RSSI,rssi = " + values[0]);
                if (onReadRemoteRssiListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onReadRemoteRssiListener.onReadRemoteRssi(values[0]);
                        }
                    });
                }
                break;
            case BleConstants.ACTION_MTU_CHANGED:
                Tool.warnOut("ConnectBleBroadcastReceiver", "ACTION_MTU_CHANGED,mtu = " + values[0]);
                if (onMtuChangedListener != null) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            onMtuChangedListener.onMtuChanged(values[0]);
                        }
                    });
                }
                break;
            default:
                Tool.warnOut("ConnectBleBroadcastReceiver", "get other action" + action);
                break;
        }
    }

    public void setOnConnectedListener(BleInterface.OnConnectedListener onConnectedListener) {
        this.onConnectedListener = onConnectedListener;
    }

    public void setOnDisconnectedListener(BleInterface.OnDisconnectedListener onDisconnectedListener) {
        this.onDisconnectedListener = onDisconnectedListener;
    }

    public void setOnServicesDiscoveredListener(BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener) {
        this.onServicesDiscoveredListener = onServicesDiscoveredListener;
    }

    public void setOnConnectingListener(BleInterface.OnConnectingListener onConnectingListener) {
        this.onConnectingListener = onConnectingListener;
    }

    public void setOnDisconnectingListener(BleInterface.OnDisconnectingListener onDisconnectingListener) {
        this.onDisconnectingListener = onDisconnectingListener;
    }

    public void setOnCharacteristicReadListener(BleInterface.OnCharacteristicReadListener onCharacteristicReadListener) {
        this.onCharacteristicReadListener = onCharacteristicReadListener;
    }

    public void setOnReceiveNotificationListener(BleInterface.OnReceiveNotificationListener onReceiveNotificationListener) {
        this.onReceiveNotificationListener = onReceiveNotificationListener;
    }

    public void setOnCharacteristicWriteListener(BleInterface.OnCharacteristicWriteListener onCharacteristicWriteListener) {
        this.onCharacteristicWriteListener = onCharacteristicWriteListener;
    }

    public void setOnDescriptorReadListener(BleInterface.OnDescriptorReadListener onDescriptorReadListener) {
        this.onDescriptorReadListener = onDescriptorReadListener;
    }

    public void setOnDescriptorWriteListener(BleInterface.OnDescriptorWriteListener onDescriptorWriteListener) {
        this.onDescriptorWriteListener = onDescriptorWriteListener;
    }

    public void setOnReliableWriteCompletedListener(BleInterface.OnReliableWriteCompletedListener onReliableWriteCompletedListener) {
        this.onReliableWriteCompletedListener = onReliableWriteCompletedListener;
    }

    public void setOnReadRemoteRssiListener(BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener) {
        this.onReadRemoteRssiListener = onReadRemoteRssiListener;
    }

    public void setOnMtuChangedListener(BleInterface.OnMtuChangedListener onMtuChangedListener) {
        this.onMtuChangedListener = onMtuChangedListener;
    }

    public void setOnBluetoothOpenListener(BleInterface.OnBluetoothOpenListener onBluetoothOpenListener) {
        this.onBluetoothOpenListener = onBluetoothOpenListener;
    }

    public void setOnBluetoothCloseListener(BleInterface.OnBluetoothCloseListener onBluetoothCloseListener) {
        this.onBluetoothCloseListener = onBluetoothCloseListener;
    }
}
