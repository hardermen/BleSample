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

@SuppressWarnings("AliDeprecation")
public class BleScanner {
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
    private boolean mScanning;

    /**
     * 扫描一次的时间
     */
    private long mScanPeriod;

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
    private boolean mScanContinue;

    /**
     * 发现一个设备进行的回调
     */
    private BleInterface.OnScanFindOneDeviceListener mOnScanFindOneDeviceListener;

    /**
     * 发现一个新设备进行的回调
     */
    private BleInterface.OnScanFindOneNewDeviceListener mOnScanFindOneNewDeviceListener;

    /**
     * 扫描的定时器
     */
    private ScanTimer scanTimer;

    /**
     * 构造器
     *
     * @param context 上下文
     */
    public BleScanner(Context context) {
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
                        Tool.warnOut("BleScan::API < 21::onScanResult", "------------------------------------------------------");
                        Tool.warnOut("BleScan::API < 21::onScanResult", "name = " + device.getName());
                        Tool.warnOut("BleScan::API < 21::onScanResult", "address = " + device.getAddress());
                        Tool.warnOut("BleScan::API < 21::onScanResult", "rssi = " + rssi);
                        Tool.warnOut("BleScan::API < 21::onScanResult", "scanRecord = " + Tool.bytesToHexStr(scanRecord));
                        if (mOnScanFindOneDeviceListener != null) {
                            mOnScanFindOneDeviceListener.scanFindOneDevice(device, rssi, scanRecord);
                        }
                        BleDevice bleDevice = new BleDevice(device, rssi, scanRecord, null);
                        if (!mScanResults.contains(bleDevice)) {
                            mScanResults.add(bleDevice);
                            mOnScanFindOneNewDeviceListener.scanFindOneNewDevice(bleDevice);
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
             * Callback when a BLE advertisement has been found.
             *
             * @param callbackType Determines how this callback was triggered. Could be one of
             *                     {@link ScanSettings#CALLBACK_TYPE_ALL_MATCHES},
             *                     {@link ScanSettings#CALLBACK_TYPE_FIRST_MATCH} or
             *                     {@link ScanSettings#CALLBACK_TYPE_MATCH_LOST}
             * @param result       A Bluetooth LE scan result.
             */
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                Tool.warnOut("BleScan::API >= 21::onScanResult", "------------------------------------------------------");
                Tool.warnOut("BleScan::API >= 21::onScanResult", "callbackType = " + callbackType);
                BluetoothDevice device = result.getDevice();
                int rssi = result.getRssi();
                ScanRecord scanrecord = result.getScanRecord();
                byte[] scanRecord = null;
                String deviceName = null;
                if (scanrecord != null) {
                    scanRecord = scanrecord.getBytes();
                    deviceName = scanrecord.getDeviceName();
                    Tool.warnOut("BleScan::API >= 21::onScanResult", "scanRecord.getDeviceName() = " + deviceName);
                }
                Tool.warnOut("BleScan::API >= 21::onScanResult", "device.getName() = " + device.getName());
                Tool.warnOut("BleScan::API >= 21::onScanResult", "device.getAddress() = " + device.getAddress());
                Tool.warnOut("BleScan::API >= 21::onScanResult", "rssi = " + rssi);
                Tool.warnOut("BleScan::API >= 21::onScanResult", "scanRecord = " + scanrecord);
                BleDevice bleDevice = new BleDevice(device, rssi, scanRecord, deviceName);
                if (mOnScanFindOneDeviceListener != null) {
                    mOnScanFindOneDeviceListener.scanFindOneDevice(device, rssi, scanRecord);
                }
                if (mOnScanFindOneNewDeviceListener != null) {
                    if (!mScanResults.contains(bleDevice)) {
                        mScanResults.add(bleDevice);
                        mOnScanFindOneNewDeviceListener.scanFindOneNewDevice(bleDevice);
                    }
                }
            }

            /**
             * Callback when batch results are delivered.
             *
             * @param results List of scan results that are previously scanned.
             */
            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
            }

            /**
             * Callback when scan could not be started.
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
     * @param scanResults                  扫描设备结果存放列表
     * @param onScanFindOneNewDeviceListener 发现一个新设备的回调
     * @param scanPeriod                   扫描持续时间
     * @param scanContinueFlag             是否在扫描完成后立即进行下一次扫描的标志
     * @param onScanCompleteListener       扫描完成的回调
     * @return true表示打开成功
     */
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
        mOnScanFindOneNewDeviceListener = onScanFindOneNewDeviceListener;
        mScanPeriod = scanPeriod;
        mScanContinue = scanContinueFlag;
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

        if (mScanning) {
            Tool.toastL(contextWeakReference.get(), R.string.scanning);
            return false;
        }
        scanTimer.startTimer(mScanPeriod);
        mScanResults.clear();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothAdapter.getBluetoothLeScanner().startScan(mScanCallback);
        } else {
            //noinspection deprecation
            mBluetoothAdapter.startLeScan(this.mLeScanCallback);
        }
        mScanning = true;
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

        if (!mScanning) {
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
        mScanning = false;
        return true;
    }

    public boolean close() {
        if (!mOpened) {
            Tool.toastL(contextWeakReference.get(), R.string.scanner_not_opened);
            return false;
        }

        if (mScanning) {
            stopScan();
        }

        if (bluetoothStateReceiver != null) {
            contextWeakReference.get().unregisterReceiver(bluetoothStateReceiver);
        }

        mScanPeriod = 0;
        mOpened = false;
        mScanning = false;
        mScanContinue = false;
        mScanResults = null;
        bluetoothStateReceiver = null;
        contextWeakReference = null;
        mHandler = null;
        mBluetoothAdapter = null;
        mLeScanCallback = null;
        mScanCallback = null;
        mOnScanFindOneDeviceListener = null;
        mOnScanFindOneNewDeviceListener = null;
        scanTimer = null;
        return true;
    }

    /**
     * 获取是否需要继续扫描的标志
     *
     * @return 是否需要继续扫描的标志
     */
    boolean isScanContinue() {
        return mScanContinue;
    }

    /**
     * 清空扫描结果
     */
    public void clearScanResults() {
        mScanResults.clear();
    }

    public void setOnScanFindADeviceListener(BleInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener) {
        mOnScanFindOneDeviceListener = onScanFindOneDeviceListener;
    }

    void setBluetoothAdapter(BluetoothAdapter bluetoothAdapter) {
        mBluetoothAdapter = bluetoothAdapter;
    }

    public boolean isScanning() {
        return mScanning;
    }

    void setScanning(boolean scanning) {
        mScanning = scanning;
    }
}
