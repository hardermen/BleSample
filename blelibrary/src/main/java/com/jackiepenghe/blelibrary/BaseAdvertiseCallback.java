package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.AdvertisingSet;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;

/**
 * BLE广播回调
 * @author jacke
 */

@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public abstract class BaseAdvertiseCallback extends AdvertiseCallback {

    /*-------------------------静态常量-------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BaseAdvertiseCallback.class.getSimpleName();

    /*-------------------------重写父类函数-------------------------*/

    /**
     * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
     * that the advertising has been started successfully.
     *
     * @param settingsInEffect The actual settings used for advertising, which may be different from
     *                         what has been requested.
     */
    @Override
    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
        Tool.warnOut(TAG, "onStartSuccess");
        if (settingsInEffect != null) {
            Tool.warnOut(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
                    + " timeout=" + settingsInEffect.getTimeout());
        } else {
            Tool.warnOut(TAG, "onStartSuccess, settingInEffect is null");
        }
        Tool.warnOut(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);

        onBroadCastStartSuccess(settingsInEffect);
    }

    /**
     * Callback when advertising could not be started.
     *
     * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
     *                  failures.
     */
    @Override
    public void onStartFailure(int errorCode) {
        Tool.warnOut(TAG, "onStartFailure");
        if (errorCode == ADVERTISE_FAILED_DATA_TOO_LARGE) {
            Tool.errorOut(TAG, "Failed to start advertising as the advertise data to be broadcasted is larger than 31 bytes.");
        } else if (errorCode == ADVERTISE_FAILED_TOO_MANY_ADVERTISERS) {
            Tool.errorOut(TAG, "Failed to start advertising because no advertising instance is available.");
        } else if (errorCode == ADVERTISE_FAILED_ALREADY_STARTED) {
            Tool.errorOut(TAG, "Failed to start advertising as the advertising is already started");
        } else if (errorCode == ADVERTISE_FAILED_INTERNAL_ERROR) {
            Tool.errorOut(TAG, "Operation failed due to an internal error");
        } else if (errorCode == ADVERTISE_FAILED_FEATURE_UNSUPPORTED) {
            Tool.errorOut(TAG, "This feature is not supported on this platform");
        }
        onBroadCastStartFailure(errorCode);
    }

    /*-------------------------抽象函数-------------------------*/

    /**
     * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
     * that the advertising has been started successfully.
     *
     * @param settingsInEffect The actual settings used for advertising, which may be different from
     *                         what has been requested.
     */
    protected abstract void onBroadCastStartSuccess(AdvertiseSettings settingsInEffect);

    /**
     * Callback when advertising could not be started.
     *
     * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
     *                  failures.
     */
    protected abstract void onBroadCastStartFailure(int errorCode);

    /**
     * 如果设置了超时时间，在超时结束后，会执行此回调
     */
    protected abstract void onBroadCastStopped();
}
