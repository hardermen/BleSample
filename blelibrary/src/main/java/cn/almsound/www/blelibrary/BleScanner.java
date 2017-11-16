package cn.almsound.www.blelibrary;

import android.annotation.TargetApi;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanCallback;
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

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * BLE扫描器
 *
 * @author alm
 *         Created by alm on 17-6-5.
 */

@SuppressWarnings({"AliDeprecation", "UnusedReturnValue"})
public class BleScanner {

    private static final String TAG = "BleScanner";
    /**
     * 扫描的结果
     */
    private ArrayList<BleDevice> mScanResults;

    /**
     * 检测蓝牙状态的广播接收者
     */
    private BluetoothStateReceiver bluetoothStateReceiver;

    /**
     * BLE扫描器是否被打开的标志
     */
    private boolean mOpened;

    /**
     * 上下文弱引用
     */
    private WeakReference<Context> contextWeakReference;

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
    private BleInterface.OnScanFindOneDeviceListener mOnScanFindOneDeviceListener;

    /**
     * 发现一个新设备进行的回调
     */
    private BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener;

    /**
     * 扫描的定时器
     */
    private ScanTimer scanTimer;

    /**
     * 构造器
     *
     * @param context 上下文
     */
    BleScanner(Context context) {
        contextWeakReference = new WeakReference<>(context);
        scanTimer = new ScanTimer(BleScanner.this);
        mHandler = new Handler();
        // Use this check to determine whether BLE is supported on the device.
        if (!(contextWeakReference.get().getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE))) {
            Tool.toastL(context, R.string.ble_not_supported);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            //初始化扫描回调(API21及以上的设备)
            initBLeScanCallBack();
        } else {
            //初始化BLE扫描回调(API21以下且不包含API21)
            initBLeScanCallback();
        }

        bluetoothStateReceiver = new BluetoothStateReceiver(BleScanner.this);

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        if (bluetoothManager == null) {
            return;
        }
        mBluetoothAdapter = bluetoothManager.getAdapter();

        if (mBluetoothAdapter == null || !mBluetoothAdapter.isEnabled()) {
            //如果是由activity创建的，可以直接请求打开蓝牙
            if (contextWeakReference.get() instanceof Activity) {
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                contextWeakReference.get().startActivity(enableBtIntent);
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

    /**
     * 初始化BLE扫描回调(API21以下且不包含API21)
     */
    private void initBLeScanCallback() {
        mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
            @Override
            public void onLeScan(final BluetoothDevice device, final int rssi, final byte[] scanRecord) {
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        Tool.warnOut(TAG, "-------------------------API < 21 onScanResult-----------------------------");
                        Tool.warnOut(TAG, "name = " + device.getName());
                        Tool.warnOut(TAG, "address = " + device.getAddress());
                        Tool.warnOut(TAG, "rssi = " + rssi);
                        Tool.warnOut(TAG, "scanRecord = " + Tool.bytesToHexStr(scanRecord));
                        Tool.warnOut(TAG, "-------------------------API < 21 onScanResult-----------------------------");
                        if (mOnScanFindOneDeviceListener != null) {
                            mOnScanFindOneDeviceListener.scanFindOneDevice(device, rssi, scanRecord);
                        }
                        BleDevice bleDevice = new BleDevice(device, rssi, scanRecord, null);
                        if (!mScanResults.contains(bleDevice)) {
                            mScanResults.add(bleDevice);
                            onScanFindOneNewDeviceListener.scanFindOneNewDevice(bleDevice);
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
    private void initBLeScanCallBack() {
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
                Tool.warnOut(TAG, "------------------------API >= 21 onScanResult------------------------------");
                Tool.warnOut(TAG, "callbackType = " + callbackType);
                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                ScanRecord scanRecord = result.getScanRecord();
                byte[] scanRecordBytes = null;
                String deviceName = null;
                if (scanRecord != null) {
                    scanRecordBytes = scanRecord.getBytes();
                    deviceName = scanRecord.getDeviceName();
                    Tool.warnOut(TAG, "device.getDeviceName() = " + deviceName);
                }
                Tool.warnOut(TAG, "device.getName() = " + device.getName());
                Tool.warnOut(TAG, "device.getAddress() = " + device.getAddress());
                Tool.warnOut(TAG, "rssi = " + rssi);
                Tool.warnOut(TAG, "scanRecord = " + scanRecord);
                Tool.warnOut(TAG, "scanRecordByte = " + Tool.bytesToHexStr(scanRecordBytes));
                Tool.warnOut(TAG, "------------------------API >= 21 onScanResult------------------------------");
                BleDevice bleDevice = new BleDevice(device, rssi, scanRecordBytes, deviceName);
                bleDevice.setScanRecord(scanRecord);
                if (mOnScanFindOneDeviceListener != null) {
                    mOnScanFindOneDeviceListener.scanFindOneDevice(device, rssi, scanRecordBytes);
                }
                if (onScanFindOneNewDeviceListener != null) {
                    if (!mScanResults.contains(bleDevice)) {
                        mScanResults.add(bleDevice);
                        onScanFindOneNewDeviceListener.scanFindOneNewDevice(bleDevice);
                    }
                }
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
    @SuppressWarnings("SameParameterValue")
    public boolean open(@NonNull ArrayList<BleDevice> scanResults, @NonNull BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener, long scanPeriod, boolean scanContinueFlag, @NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        if (scanPeriod <= 0 || contextWeakReference.get() == null) {
            return false;
        }

        //广播接收者过滤器
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        if (contextWeakReference == null) {
            return false;
        }
        mScanResults = scanResults;
        contextWeakReference.get().registerReceiver(bluetoothStateReceiver, filter);
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
     * @return true表示成功开始扫描
     */
    public boolean startScan() {
        if (mBluetoothAdapter == null) {
            Tool.toastL(contextWeakReference.get(), R.string.no_bluetooth_mode);
            return false;
        }

        if (!mOpened) {
            Tool.toastL(contextWeakReference.get(), R.string.scanner_not_opened);
            return false;
        }

        if (!mBluetoothAdapter.isEnabled()) {
            Tool.toastL(contextWeakReference.get(), R.string.bluetooth_not_enable);
            return false;
        }

        if (scanning) {
            Tool.toastL(contextWeakReference.get(), R.string.scanning);
            return false;
        }
        scanTimer.startTimer(scanPeriod);
        mScanResults.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
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
    public boolean stopScan() {
        if (!mOpened) {
            Tool.toastL(contextWeakReference.get(), R.string.scanner_not_opened);
            return false;
        }

        if (!scanning) {
            Tool.toastL(contextWeakReference.get(), R.string.not_scanning);
            return false;
        }

        if (mBluetoothAdapter == null) {
            Tool.toastL(contextWeakReference.get(), R.string.no_bluetooth_mode);
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

    public boolean close() {
        if (!mOpened) {
            Tool.toastL(contextWeakReference.get(), R.string.scanner_not_opened);
            return false;
        }

        if (scanning) {
            stopScan();
        }

        if (bluetoothStateReceiver != null) {
            contextWeakReference.get().unregisterReceiver(bluetoothStateReceiver);
        }

        scanPeriod = 0;
        mOpened = false;
        scanning = false;
        scanContinue = false;
        mScanResults = null;
        bluetoothStateReceiver = null;
        contextWeakReference = null;
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
     * 获取是否需要继续扫描的标志
     *
     * @return 是否需要继续扫描的标志
     */
    boolean isScanContinue() {
        return scanContinue;
    }

    /**
     * 清空扫描结果
     */
    public void clearScanResults() {
        mScanResults.clear();
    }

    public void setOnScanFindOneDeviceListener(BleInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener) {
        mOnScanFindOneDeviceListener = onScanFindOneDeviceListener;
    }

    void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
    }

    public boolean isScanning() {
        return scanning;
    }

    @SuppressWarnings("SameParameterValue")
    void setScanning(boolean scanning) {
        this.scanning = scanning;
    }

    public void setScanPeriod(long scanPeriod) {
        this.scanPeriod = scanPeriod;
    }

    public void setScanContinue(boolean scanContinue) {
        this.scanContinue = scanContinue;
    }

    public void setOnScanFindOneNewDeviceListener(BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener) {
        this.onScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
    }

    public void setOnScanCompleteListener(@NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        scanTimer.setOnScanCompleteListener(onScanCompleteListener);
    }
}
