package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.ParcelUuid;
import android.support.annotation.NonNull;

import java.util.UUID;
import java.util.concurrent.ThreadFactory;

/**
 * BLE广播实例
 *
 * @author jacke
 */
@SuppressWarnings("unused")
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class BleAdvertiser {

    private static final String TAG = BleAdvertiser.class.getSimpleName();

    /*---------------------成员变量---------------------*/

    /**
     * 上下文
     */
    private Context context;
    /**
     * 蓝牙适配器
     */
    private BluetoothAdapter mBluetoothAdapter;
    /**
     * 蓝牙广播实例
     */
    private BluetoothLeAdvertiser mBluetoothAdvertiser;
    /**
     * 是否初始化并且初始化成功
     */
    private boolean initSuccess;

    /**
     * 线程池工厂
     */
    private ThreadFactory threadFactory = new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r);
        }
    };

    /**
     * Handler
     */
    private static final Handler HANDLER = new Handler();

    /**
     * 默认的广播设置
     */
    private AdvertiseSettings defaultAdvertiseSettings = new AdvertiseSettings.Builder()
            .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
            .setTimeout(0)
            .setConnectable(false)
            .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
            .build();

    /**
     * 厂商自定义的数据内容
     */
    private byte[] manufacturerSpecificData = new byte[]{20};
    /**
     * 服务数据
     */
    private byte[] serviceData = new byte[]{10};
    /**
     * 服务UUID
     */
    private ParcelUuid serviceUUID = new ParcelUuid(UUID.fromString("0000FFFA-0000-1000-8000-00805f9b34fb"));

    /**
     * 默认的广播数据
     */
    private AdvertiseData defaultAdvertiseData = new AdvertiseData.Builder()
            .addServiceUuid(serviceUUID)
            .addServiceData(serviceUUID, serviceData)
            .addManufacturerData(20, manufacturerSpecificData)
            .setIncludeDeviceName(true)
            .setIncludeTxPowerLevel(true)
            .build();


    /**
     * 默认的扫描回应数据
     */
    private AdvertiseData defaultScanResponse = new AdvertiseData.Builder()
            .addManufacturerData(20, manufacturerSpecificData)
            .addServiceData(serviceUUID, serviceData)
            .addServiceUuid(serviceUUID)
            .setIncludeTxPowerLevel(true)
            .setIncludeDeviceName(true)
            .build();

    /**
     * 蓝牙管理器
     */
    private BluetoothManager bluetoothManager;

    /**
     * 蓝牙服务连接的回调
     */
    private DefaultBluetoothGattServerCallback defaultBluetoothGattServerCallback;

    /**
     * BluetoothGattServer
     */
    private BluetoothGattServer bluetoothGattServer;

    /**
     * 广播的回调
     */
    private BaseAdvertiseCallback baseAdvertiseCallback;

    /**
     * 广播的回调
     */
    private AdvertiseCallback advertiseCallback = new AdvertiseCallback() {

        /**
         * TAG
         */
        private final String TAG = DefaultAdvertiseCallback.class.getSimpleName();

        /**
         * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
         * that the advertising has been started successfully.
         *
         * @param settingsInEffect The actual settings used for advertising, which may be different from
         *                         what has been requested.
         */
        @Override
        public void onStartSuccess(final AdvertiseSettings settingsInEffect) {
            Tool.warnOut(TAG, "onStartSuccess");
            if (settingsInEffect != null) {
                Tool.warnOut(TAG, "onStartSuccess TxPowerLv=" + settingsInEffect.getTxPowerLevel() + " mode=" + settingsInEffect.getMode()
                        + " timeout=" + settingsInEffect.getTimeout());
            } else {
                Tool.warnOut(TAG, "onStartSuccess, settingInEffect is null");
            }
            Tool.warnOut(TAG, "onStartSuccess settingsInEffect" + settingsInEffect);
            if (baseAdvertiseCallback != null) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        baseAdvertiseCallback.onBroadCastStartSuccess(settingsInEffect);
                    }
                });
            }
        }

        /**
         * Callback when advertising could not be started.
         *
         * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
         *                  failures.
         */
        @Override
        public void onStartFailure(final int errorCode) {
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
            if (baseAdvertiseCallback != null) {
                HANDLER.post(new Runnable() {
                    @Override
                    public void run() {
                        baseAdvertiseCallback.onBroadCastStartFailure(errorCode);
                    }
                });
            }
        }
    };

    /*---------------------构造函数---------------------*/

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    BleAdvertiser(Context context) {
        this.context = context;

        // Use this check to determine whether BLE is supported on the device.
        if (!(this.context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Tool.toastL(context, R.string.ble_not_supported);
            return;
        }

        bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return;
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null) {
            return;
        }

        if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
            Tool.toastL(context, R.string.multiple_advertisement_not_supported);
            return;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            //如果是由activity创建的，可以通过请求码打开蓝牙
            if (this.context instanceof Activity) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                this.context.startActivity(enableBtIntent);
            }
            //如果不是由activity创建的，直接通过蓝牙适配器打开蓝牙
            else {
                boolean enable = mBluetoothAdapter.enable();
                if (!enable) {
                    Tool.toastL(context, R.string.bluetooth_not_enable);
                }
            }
        }
    }

    /*---------------------自定义外部访问函数---------------------*/

    /**
     * 初始化
     *
     * @return true表示初始化成功
     */
    public boolean init() {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, new DefaultAdvertiseCallback());
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseData 广播内容
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseData defaultAdvertiseData) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, new DefaultAdvertiseCallback());
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseCallback 广播回调
     * @return true表示初始化成功
     */
    public boolean init(BaseAdvertiseCallback defaultAdvertiseCallback) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, defaultAdvertiseCallback);
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseSettings 广播设置
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseSettings defaultAdvertiseSettings) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, new DefaultAdvertiseCallback());
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseSettings 广播设置
     * @param defaultAdvertiseData     广播内容
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseSettings defaultAdvertiseSettings, AdvertiseData defaultAdvertiseData) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, new DefaultAdvertiseCallback());
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseSettings 广播设置
     * @param defaultAdvertiseCallback 广播回调
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseSettings defaultAdvertiseSettings, BaseAdvertiseCallback defaultAdvertiseCallback) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, defaultAdvertiseCallback);
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseData 广播内容
     * @param defaultScanResponse  广播相应内容
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseData defaultAdvertiseData, AdvertiseData defaultScanResponse) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, new DefaultAdvertiseCallback());
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseData     广播内容
     * @param defaultAdvertiseCallback 广播回调
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseData defaultAdvertiseData, BaseAdvertiseCallback defaultAdvertiseCallback) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, defaultAdvertiseCallback);
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseSettings 广播设置
     * @param defaultAdvertiseData     广播内容
     * @param defaultScanResponse      广播相应内容
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseSettings defaultAdvertiseSettings, AdvertiseData defaultAdvertiseData, AdvertiseData defaultScanResponse) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, new DefaultAdvertiseCallback());
    }

    /**
     * 初始化
     *
     * @param defaultAdvertiseSettings 广播设置
     * @param defaultAdvertiseData     广播内容
     * @param defaultAdvertiseCallback 广播回调
     * @return true表示初始化成功
     */
    public boolean init(AdvertiseSettings defaultAdvertiseSettings, AdvertiseData defaultAdvertiseData, BaseAdvertiseCallback defaultAdvertiseCallback) {
        return init(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, defaultAdvertiseCallback);
    }

    /**
     * 初始化
     *
     * @param advertiseSettings 广播设置
     * @param advertiseData     广播数据
     * @param scanResponse      扫描回应数据
     * @return true表示初始化成功
     */
    @SuppressWarnings("WeakerAccess")
    public boolean init(@NonNull AdvertiseSettings advertiseSettings, @NonNull AdvertiseData advertiseData, @NonNull AdvertiseData scanResponse, BaseAdvertiseCallback defaultAdvertiseCallback) {
        if (mBluetoothAdapter == null) {
            initSuccess = false;
            return false;
        }

        // 获取蓝牙ble广播
        mBluetoothAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
        if (mBluetoothAdvertiser == null) {
            initSuccess = false;
            return false;
        }

        Context context = this.context;
        if (context == null) {
            initSuccess = false;
            return false;
        }

        defaultAdvertiseSettings = advertiseSettings;
        defaultAdvertiseData = advertiseData;
        defaultScanResponse = scanResponse;
        boolean connectable = defaultAdvertiseSettings.isConnectable();
        if (connectable) {
            if (defaultBluetoothGattServerCallback == null) {
                defaultBluetoothGattServerCallback = new DefaultBluetoothGattServerCallback();
            }
            bluetoothGattServer = bluetoothManager.openGattServer(context, defaultBluetoothGattServerCallback);
        }
        this.baseAdvertiseCallback = defaultAdvertiseCallback;
        initSuccess = true;
        return true;
    }

    /**
     * 开始广播
     */
    public boolean startAdvertising() {
        if (mBluetoothAdapter == null) {
            return false;
        }

        if (mBluetoothAdvertiser == null) {
            return false;
        }
        if (!initSuccess) {
            return false;
        }

        mBluetoothAdvertiser.startAdvertising(defaultAdvertiseSettings, defaultAdvertiseData, defaultScanResponse, advertiseCallback);
        final int timeout = defaultAdvertiseSettings.getTimeout();
        if (timeout > 0) {
            startThreadToCheckAdvertiserStatus(timeout);
        }
        return true;
    }

    /**
     * 停止广播
     *
     * @return true表示成功
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean stopAdvertising() {
        if (mBluetoothAdapter == null) {
            return false;
        }

        if (mBluetoothAdvertiser == null) {
            return false;
        }
        try {
            mBluetoothAdvertiser.stopAdvertising(advertiseCallback);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        if (baseAdvertiseCallback != null) {
            baseAdvertiseCallback.onBroadCastStopped();
        }
        return true;
    }

    /**
     * 关闭广播实例
     */
    public void close() {
        if (mBluetoothAdvertiser != null) {
            stopAdvertising();
        }
        initSuccess = false;
        context = null;
        mBluetoothAdvertiser = null;
        mBluetoothAdapter = null;
        defaultAdvertiseSettings = null;
        defaultAdvertiseData = null;
        defaultScanResponse = null;
        advertiseCallback = null;
        BleManager.resetBleAdvertiser();
    }

    /**
     * 获取BluetoothGattServer实例
     *
     * @return BluetoothGattServer
     */
    public BluetoothGattServer getBluetoothGattServer() {
        return bluetoothGattServer;
    }

    /**
     * 设置作为服务端的相关回调
     *
     * @param onBluetoothGattServerCallbackListener 作为服务端的相关回调
     */
    public void setOnBluetoothGattServerCallbackListener(BleInterface.OnBluetoothGattServerCallbackListener onBluetoothGattServerCallbackListener) {
        if (defaultBluetoothGattServerCallback != null) {
            defaultBluetoothGattServerCallback.setOnBluetoothGattServerCallbackListener(onBluetoothGattServerCallbackListener);
        }
    }

    /*---------------------自定义私有函数---------------------*/

    /**
     * 发起一个线程，检测广播状态，当广播停止是进行回调
     *
     * @param timeout 超时时间
     */
    private void startThreadToCheckAdvertiserStatus(final int timeout) {
        final long startTime = System.currentTimeMillis();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (System.currentTimeMillis() - startTime > timeout) {
                        break;
                    }
                }
                stopAdvertising();
            }
        };
        Thread thread = threadFactory.newThread(runnable);
        thread.start();
    }
}
