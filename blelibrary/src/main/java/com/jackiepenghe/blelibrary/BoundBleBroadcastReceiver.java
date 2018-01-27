package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;


/**
 * @author alm
 *         Created by alm on 17-6-5.
 *         BLE绑定设备时，监听绑定状态的广播接收者
 */

public class BoundBleBroadcastReceiver extends BroadcastReceiver {

    /*------------------------静态常量----------------------------*/

    private static final String TAG = "BoundBLEBroadcastReceiv";
    /**
     * The user will be prompted to enter a passkey
     */
    public static final int PAIRING_VARIANT_PASSKEY = 1;

    /*------------------------成员变量----------------------------*/

    private BleInterface.OnDeviceBondStateChangedListener mOnDeviceBondStateChangedListener;

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
    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return;
        }
        switch (action) {
            case BluetoothDevice.ACTION_PAIRING_REQUEST:
                Tool.warnOut(TAG, "ACTION_PAIRING_REQUEST");
                int mType = intent.getIntExtra(BluetoothDevice.EXTRA_PAIRING_VARIANT, BluetoothDevice.ERROR);
                Tool.warnOut(TAG, "mType = " + mType);
                switch (mType) {
                    case BluetoothDevice.PAIRING_VARIANT_PASSKEY_CONFIRMATION:
                        Tool.warnOut(TAG, "让用户确认PIN是否正确");
                        break;
                    case PAIRING_VARIANT_PASSKEY:
                        Tool.warnOut(TAG, "提示用户输入PIN或者自动输入PIN");
                        break;
                    default:
                        break;
                }
                break;
            case BluetoothDevice.ACTION_BOND_STATE_CHANGED:
                Tool.warnOut("BoundBleBroadcastReceiver", "ACTION_BOND_STATE_CHANGED");
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
                Tool.warnOut("BoundBleBroadcastReceiver", "bondState = " + bondState);
                switch (bondState) {
                    case BluetoothDevice.BOND_BONDING:
                        Tool.warnOut("BoundBleBroadcastReceiver", "BOND_BONDING");
                        if (mOnDeviceBondStateChangedListener != null) {
                            mOnDeviceBondStateChangedListener.onDeviceBinding();
                        }
                        break;
                    case BluetoothDevice.BOND_BONDED:
                        Tool.warnOut("BoundBleBroadcastReceiver", "BOND_BONDED");
                        if (mOnDeviceBondStateChangedListener != null) {
                            mOnDeviceBondStateChangedListener.onDeviceBonded();
                        }
                        break;
                    case BluetoothDevice.BOND_NONE:
                        Tool.warnOut("BoundBleBroadcastReceiver", "BOND_NONE");
                        if (mOnDeviceBondStateChangedListener != null) {
                            mOnDeviceBondStateChangedListener.onDeviceBindNone();
                        }
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    /*------------------------库内函数------------------------*/

    /**
     * 设置绑定状态改变时的回调
     * @param onDeviceBondStateChangedListener 绑定状态改变时的回调
     */
    void setOnDeviceBondStateChangedListener(BleInterface.OnDeviceBondStateChangedListener onDeviceBondStateChangedListener) {
        mOnDeviceBondStateChangedListener = onDeviceBondStateChangedListener;
    }
}
