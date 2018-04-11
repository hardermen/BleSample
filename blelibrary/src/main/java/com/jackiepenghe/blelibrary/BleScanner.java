package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanRecord;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.util.ArrayList;
import java.util.List;

/**
 * BLE扫描器
 *
 * @author alm
 */

public class BleScanner {

    /*------------------------静态常量----------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BleScanner.class.getSimpleName();

    /*------------------------成员变量----------------------------*/

    /**
     * 扫描的结果
     */
    private ArrayList<BleDevice> mScanResults = new ArrayList<>();

    /**
     * 检测蓝牙状态的广播接收者
     */
    private BluetoothStateReceiver bluetoothStateReceiver;

    /**
     * BLE扫描器是否被打开的标志
     */
    private boolean mOpened;

    /**
     * 上下文
     */
    private Context context;

    /**
     * mHandler
     */
    private Handler mHandler;

    /**
     * 蓝牙设配器
     */
    private BluetoothAdapter mBluetoothAdapter;

    /**
     * 是否正在扫描的标志
     */
    private boolean scanning;

    /**
     * 扫描一次的时间
     */
    private long scanPeriod = 20000;

    /**
     * 系统的扫描回调(API 20 及以下)
     */
    private BluetoothAdapter.LeScanCallback mLeScanCallback;

    /**
     * 系统的扫描回调（API 21 及以上）
     */
    private ScanCallback mScanCallback;

    /**
     * 是否在扫描完成后立即进行下一次扫描的标志
     */
    private boolean scanContinue;

    /**
     * 发现一个设备进行的回调
     */
    private BleInterface.OnScanFindOneDeviceListener mOnScanFindOneDeviceListener;

    /**
     * 发现一个新设备进行的回调
     */
    private BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener = new BleInterface.OnScanFindOneNewDeviceListener() {
        @Override
        public void onScanFindOneNewDevice(BleDevice bleDevice) {
            mScanResults.add(bleDevice);
        }
    };

    /**
     * 扫描的定时器
     */
    private ScanTimer scanTimer;

    /**
     * 扫描过滤器
     */
    private ArrayList<ScanFilter> scanFilters;

    /**
     * 扫描设置
     */
    private ScanSettings scanSettings;

    private BleInterface.OnScanCompleteListener onScanCompleteListener = new BleInterface.OnScanCompleteListener() {
        @Override
        public void onScanComplete() {
            Tool.warnOut(TAG, "onScanComplete");
        }
    };

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器
     *
     * @param context 上下文
     */
    BleScanner(Context context) {
        this.context = context;
        scanTimer = new ScanTimer(BleScanner.this);
        mHandler = new Handler();
        if (!(context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Tool.toastL(context, R.string.ble_not_supported);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //初始化扫描回调(API21及以上的设备)
            initBLeScanCallBack21();
        } else {
            //初始化BLE扫描回调(API21以下且不包含API21)
            initBLeScanCallBack18();
        }

        bluetoothStateReceiver = new BluetoothStateReceiver(BleScanner.this);

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return;
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //如果是由activity创建的，可以直接请求打开蓝牙
            if (context instanceof Activity) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                context.startActivity(enableBtIntent);
            } else {
                if (mBluetoothAdapter != null) {
                    boolean enable = mBluetoothAdapter.enable();
                    if (!enable) {
                        Tool.toastL(context, R.string.bluetooth_not_enable);
                    }
                }
            }
        }
    }

    /*------------------------私有函数----------------------------*/

    /**
     * 初始化BLE扫描回调(API21以下且不包含API21)
     */
    private void initBLeScanCallBack18() {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                final Context context = BleScanner.this.context;
                if (context == null) {
                    return;
                }
                if (scanRecord == null) {
                    return;
                }
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String name = device.getName();
                        if (null == name || "".equals(name)) {
                            name = context.getString(R.string.un_named);
                        }
                        BleDevice bleDevice = new BleDevice(device, rssi, scanRecord, name);
                        if (mOnScanFindOneDeviceListener != null) {
                            mOnScanFindOneDeviceListener.onScanFindOneDevice(bleDevice);
                        }
                        if (mScanResults == null){
                            return;
                        }
                        if (!mScanResults.contains(bleDevice)) {
                            mScanResults.add(bleDevice);
                            onScanFindOneNewDeviceListener.onScanFindOneNewDevice(bleDevice);
                        }
                    }
                });
            }
        };
    }

    /**
     * 初始化扫描回调(API21及以上的设备)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initBLeScanCallBack21() {
        mScanCallback = new ScanCallback() {
            /**
             * BaseConnectCallback when a BLE advertisement has been found.
             *
             * @param callbackType Determines how this callback was triggered. Could be one of
             *                     {@link ScanSettings#CALLBACK_TYPE_ALL_MATCHES},
             *                     {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
             *                     {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
             * @param result       A Bluetooth LE scan result.
             */
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Context context = BleScanner.this.context;
                if (context == null) {
                    return;
                }
                ScanRecord scanRecord = result.getScanRecord();

                if (scanRecord == null) {
                    return;
                }

                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                byte[] scanRecordBytes;
                String deviceName;
                scanRecordBytes = scanRecord.getBytes();
                deviceName = scanRecord.getDeviceName();
                if (deviceName == null || "".equals(deviceName)) {
                    deviceName = context.getString(R.string.un_named);
                }
                if (scanRecordBytes != null) {
                    Tool.warnOut(TAG, "scanRecordByte = " + Tool.bytesToHexStr(scanRecordBytes));
                }
                final BleDevice bleDevice = new BleDevice(device, rssi, scanRecordBytes, deviceName);
                bleDevice.setScanRecord(scanRecord);

                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        if (mOnScanFindOneDeviceListener != null) {
                            mOnScanFindOneDeviceListener.onScanFindOneDevice(bleDevice);
                        }
                        if (onScanFindOneNewDeviceListener != null) {
                            if (mScanResults == null){
                                return;
                            }
                            if (!mScanResults.contains(bleDevice)) {
                                mScanResults.add(bleDevice);
                                onScanFindOneNewDeviceListener.onScanFindOneNewDevice(bleDevice);
                            }
                        }
                    }
                });
            }

            /**
             * BaseConnectCallback when batch results are delivered.
             *
             * @param results List of scan results that are previously scanned.
             */
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            /**
             * BaseConnectCallback when scan could not be started.
             *
             * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
             */
            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
            }
        };
    }

    /*------------------------库内函数----------------------------*/


    /**
     * 获取是否需要继续扫描的标志
     *
     * @return 是否需要继续扫描的标志
     */
    boolean isScanContinue() {
        return scanContinue;
    }

    /**
     * 设置蓝牙适配器
     *
     * @param bluetoothAdapter 蓝牙适配器
     */
    void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
    }

    /**
     * 设置扫描标志为false
     */
    void setScanningFalse() {
        this.scanning = false;
    }

    /*------------------------公开函数----------------------------*/

    /**
     * 打开扫描器
     */
    public boolean open() {
        return open(mScanResults,onScanFindOneNewDeviceListener,scanPeriod,true,onScanCompleteListener);
    }

    /**
     * 打开扫描器
     *
     * @param scanResults                    扫描设备结果存放列表
     * @param onScanFindOneNewDeviceListener 发现一个新设备的回调
     * @param scanPeriod                     扫描持续时间
     * @param scanContinueFlag               是否在扫描完成后立即进行下一次扫描的标志
     * @param onScanCompleteListener         扫描完成的回调
     * @return true表示打开成功
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean open(@NonNull ArrayList<BleDevice> scanResults, @NonNull BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener, @SuppressWarnings("SameParameterValue") long scanPeriod, @SuppressWarnings("SameParameterValue") boolean scanContinueFlag, @NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        Context context = this.context;
        if (scanPeriod <= 0 || context == null) {
            return false;
        }

        //广播接收者过滤器
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (this.context == null) {
            return false;
        }
        mScanResults = scanResults;
        context.registerReceiver(bluetoothStateReceiver, filter);
        mScanResults.clear();
        mOpened = true;
        this.onScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
        this.scanPeriod = scanPeriod;
        scanContinue = scanContinueFlag;
        scanTimer.setOnScanCompleteListener(onScanCompleteListener);
        return true;
    }

    /**
     * 开始扫描
     *
     * @return true表示成功开启扫描
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean startScan() {
        Context context = this.context;
        if (context == null) {
            return false;
        }
        if (mBluetoothAdapter == null) {
            Tool.toastL(context, R.string.no_bluetooth_mode);
            return false;
        }

        if (!mOpened) {
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Tool.toastL(context, R.string.bluetooth_not_enable);
            return false;
        }

        if (scanning) {
            return false;
        }
        scanTimer.startTimer(scanPeriod);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (scanFilters == null) {
                scanFilters = new ArrayList<>();
                ScanFilter scanFilter = new ScanFilter.Builder()
                        .build();

                scanFilters.add(scanFilter);
            }
            if (scanSettings == null) {
                scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
            }
            mBluetoothAdapter.getBluetoothLeScanner().startScan(scanFilters, scanSettings, mScanCallback);

        } else {
            //noinspection deprecation
            mBluetoothAdapter.startLeScan(this.mLeScanCallback);
        }
        scanning = true;
        return true;
    }

    /**
     * 停止扫描
     *
     * @return true表示成功停止扫描
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean stopScan() {
        Context context = this.context;
        if (context == null) {
            return false;
        }
        if (!mOpened) {
            return false;
        }

        if (!scanning) {
            return false;
        }

        if (mBluetoothAdapter == null) {
            Tool.toastL(context, R.string.no_bluetooth_mode);
            return false;
        }

        scanTimer.stopTimer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter.getBluetoothLeScanner().stopScan(mScanCallback);
        } else {
            //noinspection deprecation
            this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
        }
        scanning = false;
        return true;
    }

    /**
     * 关闭当前GATT连接
     *
     * @return true表示成功
     */
    public boolean close() {
        Context context = this.context;
        if (context == null) {
            return false;
        }
        if (!mOpened) {
            return false;
        }

        if (scanning) {
            stopScan();
        }

        if (bluetoothStateReceiver != null) {
            bluetoothStateReceiver.releaseData();
            context.unregisterReceiver(bluetoothStateReceiver);
        }

        scanPeriod = 0;
        mOpened = false;
        scanning = false;
        scanContinue = false;
        mScanResults = null;
        bluetoothStateReceiver = null;
        this.context = null;
        mHandler = null;
        mBluetoothAdapter = null;
        mLeScanCallback = null;
        mScanCallback = null;
        mOnScanFindOneDeviceListener = null;
        onScanFindOneNewDeviceListener = null;
        scanTimer = null;
        return true;
    }

    /**
     * 获取扫描过滤器列表
     *
     * @return 扫描过滤器列表
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArrayList<ScanFilter> getScanFilters() {
        return scanFilters;
    }

    /**
     * 设置扫描过滤器列表
     *
     * @param scanFilters 扫描过滤器列表
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setScanFilters(ArrayList<ScanFilter> scanFilters) {
        this.scanFilters = scanFilters;
    }

    /**
     * 获取扫描参数
     *
     * @return 扫描参数
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanSettings getScanSettings() {
        return scanSettings;
    }

    /**
     * 设置扫描参数
     *
     * @param scanSettings 扫描参数
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setScanSettings(ScanSettings scanSettings) {
        this.scanSettings = scanSettings;
    }

    /**
     * 清空扫描结果
     */
    public void clearScanResults() {
        mScanResults.clear();
    }

    /**
     * 设置当发现一个设备时的回调
     *
     * @param onScanFindOneDeviceListener 当发现一个设备时的回调
     */
    public void setOnScanFindOneDeviceListener(BleInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener) {
        mOnScanFindOneDeviceListener = onScanFindOneDeviceListener;
    }

    /**
     * 获取当前扫描状态
     *
     * @return true表示正在扫描
     */
    public boolean isScanning() {
        return scanning;
    }

    /**
     * 设置扫描周期
     *
     * @param scanPeriod 扫描周期
     */
    public void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    /**
     * 设置当扫描结束后是否立即进行下一次扫描
     *
     * @param scanContinue 当扫描结束后是否立即进行下一次扫描的标志
     */
    public void setScanContinue(boolean scanContinue) {
        this.scanContinue = scanContinue;
    }

    /**
     * 设置发现一个新设备时的回调
     *
     * @param onScanFindOneNewDeviceListener 发现一个新设备时的回调
     */
    public void setOnScanFindOneNewDeviceListener(BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener) {
        this.onScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
    }

    /**
     * 设置扫描完成的回调
     *
     * @param onScanCompleteListener 扫描完成的回调
     */
    public void setOnScanCompleteListener(@NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        scanTimer.setOnScanCompleteListener(onScanCompleteListener);
    }

    /**
     * 获取上下文
     * @return 上下文
     */
    public Context getContext(){
        return context;
    }
}
