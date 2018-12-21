package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;

import java.util.ArrayList;
import java.util.List;

/**
 * BLE扫描器
 *
 * @author alm
 */

@SuppressWarnings("ALL")
public class BleScanner {

    /*------------------------静态常量----------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BleScanner.class.getSimpleName();

    private static final String SPACE = " ";

    /**
     * 剩余可用长度
     */
    private static final int REMAINING_LENGTH = 2;

    private static final int MIN_LENGTH_TWO = 2;
    private static final byte MIN_LENGTH_SIXTEEN = 16;

    /*------------------------成员变量----------------------------*/

    /**
     * 扫描的结果
     */
    private ArrayList<BleDevice> mScanResults;

    /**
     * 检测蓝牙状态的广播接收者
     */
    private BleScannerBluetoothStateReceiver bleScannerBluetoothStateReceiver;

    /**
     * BLE扫描器是否被打开的标志
     */
    private boolean mOpened;

    /**
     * 安卓5.0以上的API才拥有的接口
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private BleInterface.On21ScanCallback on21ScanCallback = new BleInterface.On21ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Tool.warnOut(TAG, "onBatchScanResults");
            if (results == null) {
                return;
            }
            for (int i = 0; i < results.size(); i++) {
                ScanResult scanResult = results.get(i);
                Tool.warnOut(TAG, "scanResult[" + i + "] = " + scanResult.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Tool.warnOut(TAG, "onScanFailed:errorCode = " + errorCode);
        }
    };

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
    private long scanPeriod;

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
    private BleInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener;

    /**
     * 发现一个新设备进行的回调
     */
    private BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener;

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
    /**
     * 蓝牙扫描器
     */
    private BluetoothLeScanner bluetoothLeScanner;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器
     */
    BleScanner() {
        scanTimer = new ScanTimer(BleScanner.this);
        if (!(BleManager.getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Tool.toastL(BleManager.getContext(), R.string.ble_not_supported);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //初始化扫描回调(API21及以上的设备)
            initBleScanCallBack21();
        } else {
            //初始化BLE扫描回调(API21以下且不包含API21)
            initBleScanCallBack18();
        }

        bleScannerBluetoothStateReceiver = new BleScannerBluetoothStateReceiver(BleScanner.this);

        BluetoothManager bluetoothManager = (BluetoothManager) BleManager.getContext().getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return;
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            if (mBluetoothAdapter != null) {
                boolean enable = mBluetoothAdapter.enable();
                if (!enable) {
                    Tool.toastL(BleManager.getContext(), R.string.bluetooth_not_enable);
                }
            }
        }
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
    @SuppressWarnings({"unused", "UnusedReturnValue"})
    public boolean init() {
        return init(new ArrayList<BleDevice>(), new DefaultOnScanFindOneNewDeviceListener(), 20000, false, new DefaultOnScanCompleteListener());
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
    public boolean init(@NonNull ArrayList<BleDevice> scanResults, @NonNull BleInterface.OnScanFindOneNewDeviceListener
            onScanFindOneNewDeviceListener, @SuppressWarnings("SameParameterValue") long scanPeriod,
                        @SuppressWarnings("SameParameterValue") boolean scanContinueFlag,
                        @NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        if (scanPeriod <= 0 || BleManager.getContext() == null) {
            return false;
        }

        //广播接收者过滤器
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (BleManager.getContext() == null) {
            return false;
        }
        mScanResults = scanResults;
        BleManager.getContext().registerReceiver(bleScannerBluetoothStateReceiver, filter);
        mScanResults.clear();
        mOpened = true;
        this.onScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
        this.scanPeriod = scanPeriod;
        scanContinue = scanContinueFlag;
        scanTimer.setOnScanCompleteListener(onScanCompleteListener);
        return true;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void flushPendingScanResults() {
        if (bluetoothLeScanner != null) {
            bluetoothLeScanner.flushPendingScanResults(mScanCallback);
        }
    }

    /**
     * 开始扫描
     *
     * @return true表示成功开启扫描
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean startScan() {
        return startScan(false);
    }

    /**
     * 开始扫描
     *
     * @param clearScanResult 是否要清空之前的扫描记录
     * @return true表示成功开启扫描
     */
    @SuppressWarnings("UnusedReturnValue")
    public boolean startScan(boolean clearScanResult) {
        if (BleManager.getContext() == null) {
            return false;
        }
        if (mBluetoothAdapter == null) {
            Tool.toastL(BleManager.getContext(), R.string.no_bluetooth_mode);
            return false;
        }

        if (!mOpened) {
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Tool.toastL(BleManager.getContext(), R.string.bluetooth_not_enable);
            return false;
        }

        if (scanning) {
            return false;
        }

        if (clearScanResult) {
            clearScanResults();
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
            bluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();
            try {
                bluetoothLeScanner.startScan(scanFilters, scanSettings, mScanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                //noinspection AliDeprecation
                mBluetoothAdapter.startLeScan(this.mLeScanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (BleManager.getContext() == null) {
            return false;
        }
        if (!mOpened) {
            return false;
        }

        if (!scanning) {
            return false;
        }

        if (mBluetoothAdapter == null) {
            Tool.toastL(BleManager.getContext(), R.string.no_bluetooth_mode);
            return false;
        }

        scanTimer.stopTimer();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (bluetoothLeScanner == null) {
                return false;
            }
            try {
                bluetoothLeScanner.stopScan(mScanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            try {
                //noinspection AliDeprecation
                this.mBluetoothAdapter.stopLeScan(this.mLeScanCallback);
            } catch (Exception e) {
                e.printStackTrace();
            }
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
        if (BleManager.getContext() == null) {
            return false;
        }
        if (!mOpened) {
            return false;
        }

        if (scanning) {
            stopScan();
        }

        if (bleScannerBluetoothStateReceiver != null) {
            bleScannerBluetoothStateReceiver.setOnBluetoothSwitchChangedListener(null);
            bleScannerBluetoothStateReceiver.releaseData();
            BleManager.getContext().unregisterReceiver(bleScannerBluetoothStateReceiver);
        }

        scanPeriod = 0;
        mOpened = false;
        scanning = false;
        scanContinue = false;
        mScanResults = null;
        bleScannerBluetoothStateReceiver = null;
        mBluetoothAdapter = null;
        mLeScanCallback = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            on21ScanCallback = null;
        }
        mScanCallback = null;
        onScanFindOneDeviceListener = null;
        onScanFindOneNewDeviceListener = null;
        scanTimer = null;
        return true;
    }


    /**
     * 获取扫描过滤器列表
     *
     * @return 扫描过滤器列表
     */
    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ArrayList<ScanFilter> getScanFilters() {
        return scanFilters;
    }

    /**
     * 设置扫描过滤器列表
     *
     * @param scanFilters 扫描过滤器列表
     */
    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public void setScanFilters(ArrayList<ScanFilter> scanFilters) {
        this.scanFilters = scanFilters;
    }

    /**
     * 获取扫描参数
     *
     * @return 扫描参数
     */
    @SuppressWarnings("unused")
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanSettings getScanSettings() {
        return scanSettings;
    }

    /**
     * 设置扫描参数
     *
     * @param scanSettings 扫描参数
     */
    @SuppressWarnings("unused")
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
    public void setOnScanFindOneDeviceListener(BleInterface.OnScanFindOneDeviceListener
                                                       onScanFindOneDeviceListener) {
        this.onScanFindOneDeviceListener = onScanFindOneDeviceListener;
    }

    /**
     * 设置安卓5.0以上的API才拥有的接口
     *
     * @param on21ScanCallback 安卓5.0以上的API才拥有的接口
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public void setOn21ScanCallback(BleInterface.On21ScanCallback on21ScanCallback) {
        this.on21ScanCallback = on21ScanCallback;
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
    @SuppressWarnings("unused")
    public void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    /**
     * 设置当扫描结束后是否立即进行下一次扫描
     *
     * @param scanContinue 当扫描结束后是否立即进行下一次扫描的标志
     */
    @SuppressWarnings("unused")
    public void setScanContinue(boolean scanContinue) {
        this.scanContinue = scanContinue;
    }

    /**
     * 设置发现一个新设备时的回调
     *
     * @param onScanFindOneNewDeviceListener 发现一个新设备时的回调
     */
    @SuppressWarnings("unused")
    public void setOnScanFindOneNewDeviceListener(BleInterface.OnScanFindOneNewDeviceListener
                                                          onScanFindOneNewDeviceListener) {
        this.onScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
    }

    /**
     * 设置扫描完成的回调
     *
     * @param onScanCompleteListener 扫描完成的回调
     */
    @SuppressWarnings("unused")
    public void setOnScanCompleteListener(@Nullable BleInterface.OnScanCompleteListener
                                                  onScanCompleteListener) {
        scanTimer.setOnScanCompleteListener(onScanCompleteListener);
    }

    /**
     * 设置蓝牙状态更改时进行的回调
     */
    public void setOnBluetoothSwitchChangedListener(BleInterface.OnBluetoothSwitchChangedListener onBluetoothStateChangedListener) {
        bleScannerBluetoothStateReceiver.setOnBluetoothSwitchChangedListener(onBluetoothStateChangedListener);
    }

    public ArrayList<BleDevice> getScanResults() {
        return mScanResults;
    }

    /*------------------------私有函数----------------------------*/

    /**
     * 调用相关回调
     *
     * @param bleDevice    BleDevice
     * @param mScanResults
     */
    private void callOnScanFindOneNewDeviceListener(final int inedx, final BleDevice bleDevice, final ArrayList<BleDevice> mScanResults) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onScanFindOneNewDeviceListener != null) {
                    onScanFindOneNewDeviceListener.onScanFindOneNewDevice(inedx, bleDevice, mScanResults);
                }
            }
        });
    }

    /**
     * 调用相关回调
     *
     * @param bleDevice BleDevice
     */
    private void callOnScanFindOneDeviceListener(final BleDevice bleDevice) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onScanFindOneDeviceListener != null) {
                    onScanFindOneDeviceListener.onScanFindOneDevice(bleDevice);
                }
            }
        });
    }

    /*------------------------私有函数----------------------------*/

    /**
     * 初始化BLE扫描回调(API21以下且不包含API21)
     */
    private void initBleScanCallBack18() {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                if (BleManager.getContext() == null) {
                    return;
                }
                if (scanRecord == null) {
                    return;
                }
                String name = device.getName();

                BleScanRecord bleScanRecordParseFromBytes = BleScanRecord.parseFromBytes(scanRecord);
                if (null == name || "".equals(name)) {
                    name = bleScanRecordParseFromBytes.getDeviceName();
                }

                final BleDevice bleDevice = new BleDevice(device, rssi, scanRecord, name);
                bleDevice.setBleScanRecord(bleScanRecordParseFromBytes);
                callOnScanFindOneDeviceListener(bleDevice);
                if (mScanResults == null) {
                    mScanResults = new ArrayList<>();
                }
                if (!mScanResults.contains(bleDevice)) {
                    mScanResults.add(bleDevice);
                    callOnScanFindOneNewDeviceListener(mScanResults.size() - 1, bleDevice, mScanResults);
                }
            }
        };
    }

    /**
     * 初始化扫描回调(API21及以上的设备)
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void initBleScanCallBack21() {
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
                onApi21ScanResultProcessor(result);
            }

            /**
             * BaseConnectCallback when batch results are delivered.
             *
             * @param results List of scan results that are previously scanned.
             */
            @Override
            public void onBatchScanResults(final List<ScanResult> results) {
                BleManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (on21ScanCallback != null) {
                            on21ScanCallback.onBatchScanResults(results);
                        }
                    }
                });
            }

            /**
             * BaseConnectCallback when scan could not be started.
             *
             * @param errorCode Error code (one of SCAN_FAILED_*) for scan failure.
             */
            @Override
            public void onScanFailed(final int errorCode) {
                BleManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (on21ScanCallback != null) {
                            on21ScanCallback.onScanFailed(errorCode);
                        }
                    }
                });
            }
        };
    }

    /**
     * API21以上扫描处理的方法
     *
     * @param result 扫描结果
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onApi21ScanResultProcessor(ScanResult result) {
        if (BleManager.getContext() == null) {
            return;
        }
        ScanRecord scanRecord = result.getScanRecord();

        if (scanRecord == null) {
            return;
        }

        BluetoothDevice device = result.getDevice();
        int rssi = result.getRssi();
        byte[] scanRecordBytes = scanRecord.getBytes();
        String deviceName;
        deviceName = result.getDevice().getName();
        if (null == deviceName || "".equals(deviceName)) {
            deviceName = scanRecord.getDeviceName();
        }
        BleScanRecord bleScanRecordParseFromBytes = BleScanRecord.parseFromBytes(scanRecordBytes);
        if (null == deviceName || "".equals(deviceName)) {
            deviceName = bleScanRecordParseFromBytes.getDeviceName();
        }
        final BleDevice bleDevice = new BleDevice(device, rssi, scanRecordBytes, deviceName);
        bleDevice.setBleScanRecord(bleScanRecordParseFromBytes);

        callOnScanFindOneDeviceListener(bleDevice);

        if (mScanResults == null) {
            return;
        }
        if (!mScanResults.contains(bleDevice)) {
            mScanResults.add(bleDevice);
            callOnScanFindOneNewDeviceListener(mScanResults.size() - 1, bleDevice, mScanResults);
        } else {
            int index = mScanResults.indexOf(bleDevice);
            BleDevice bleDevice1 = mScanResults.get(index);
            if (bleDevice1.getDeviceName() == null && bleDevice.getDeviceName() != null) {
                mScanResults.set(index, bleDevice);
                callOnScanFindOneNewDeviceListener(index, null, mScanResults);
            }
        }
    }
}
