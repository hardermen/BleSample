package com.jackiepenghe.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanRecord;
import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.RequiresApi;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 自定义BLE设备been类
 *
 * @author alm
 */

public class BleDevice implements Serializable, Parcelable {

    /**
     * 设备名
     */
    private String mDeviceName;

    /**
     * 蓝牙设备对象
     */
    private BluetoothDevice mBluetoothDevice;

    /**
     * rssi值
     */
    private int mRssi;

    /**
     * 广播包内容(字节数组)
     */
    private byte[] scanRecordBytes;

    /**
     * ScanRecord.在5.0以上才用得上
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    private ScanRecord scanRecord;

    /**
     * 解析广播包后保存的数据
     */
    private ArrayList<AdRecord> adRecords;


    /**
     * 构造器
     *
     * @param bluetoothDevice 蓝牙设备
     * @param rssi            rssi值
     * @param deviceName      设备名
     */
    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, String deviceName) {
        this(bluetoothDevice, rssi, null, deviceName);
    }

    /**
     * 构造器
     *
     * @param bluetoothDevice 蓝牙设备
     * @param rssi            rssi值
     * @param scanRecordBytes 广播包内容（字节数组）
     * @param deviceName      如果获取到的设备名为空，默认的设备名
     */
    @SuppressWarnings("WeakerAccess")
    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecordBytes, String deviceName) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        this.scanRecordBytes = scanRecordBytes;
        adRecords = AdRecord.parseScanRecord(scanRecordBytes);
        if (bluetoothDevice.getName() == null || "".equals(bluetoothDevice.getName())) {
            setDeviceName(deviceName);
        }
    }

    /**
     * 获取蓝牙设备对象
     *
     * @return BluetoothDevice
     */
    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    /**
     * 获取设备信号强度
     *
     * @return RSSI值
     */
    public int getRssi() {
        return mRssi;
    }

    /**
     * 获取广播包内容
     *
     * @return 广播包内容
     */
    public byte[] getScanRecordBytes() {
        return scanRecordBytes;
    }

    /**
     * 获取广播包（API21以上才有内容）
     *
     * @return ScanRecord对象
     */
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public ScanRecord getScanRecord() {
        return scanRecord;
    }

    /**
     * 设置广播包内容
     *
     * @param scanRecord ScanRecord对象
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    void setScanRecord(ScanRecord scanRecord) {
        this.scanRecord = scanRecord;
    }

    /**
     * 获取设备名称
     *
     * @return 设备名称
     */
    public String getDeviceName() {
        String deviceName = mBluetoothDevice.getName();
        if (deviceName != null) {
            return deviceName;
        } else {
            return mDeviceName;
        }
    }

    /**
     * 设置设备名
     *
     * @param deviceName 设备名
     */
    private void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    /**
     * 获取设备地址
     *
     * @return 设备地址
     */
    public String getDeviceAddress() {
        return mBluetoothDevice.getAddress();
    }

    /**
     * 获取广播包的解析结果集合
     *
     * @return 广播包的解析结果集合
     */
    public ArrayList<AdRecord> getAdRecords() {
        return adRecords;
    }

    /**
     * 获取指定类型的广播包字段
     *
     * @param type 指定类型
     * @return 广播包字段
     */
    public byte[] getManufacturerSpecificData(int type) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (scanRecord != null) {
                byte[] manufacturerSpecificData = scanRecord.getManufacturerSpecificData(type);
                if (manufacturerSpecificData != null) {
                    return manufacturerSpecificData;
                }
            }
        }

        if (adRecords != null) {
            for (int i = 0; i < adRecords.size(); i++) {
                AdRecord adRecord = adRecords.get(i);
                if (adRecord.getType() == type) {
                    return adRecord.getData();
                }
            }
        }

        return new byte[0];
    }

    /*------------------------重写父类函数----------------------------*/

    /**
     * Indicates whether some other object is "equal to" this one.
     * <p>
     * The {@code equals} method implements an equivalence relation
     * on non-null object references:
     * <ul>
     * <li>It is <i>reflexive</i>: for any non-null reference value
     * {@code x}, {@code x.equals(x)} should return
     * {@code true}.
     * <li>It is <i>symmetric</i>: for any non-null reference values
     * {@code x} and {@code y}, {@code x.equals(y)}
     * should return {@code true} if and only if
     * {@code y.equals(x)} returns {@code true}.
     * <li>It is <i>transitive</i>: for any non-null reference values
     * {@code x}, {@code y}, and {@code z}, if
     * {@code x.equals(y)} returns {@code true} and
     * {@code y.equals(z)} returns {@code true}, then
     * {@code x.equals(z)} should return {@code true}.
     * <li>It is <i>consistent</i>: for any non-null reference values
     * {@code x} and {@code y}, multiple invocations of
     * {@code x.equals(y)} consistently return {@code true}
     * or consistently return {@code false}, provided no
     * information used in {@code equals} comparisons on the
     * objects is modified.
     * <li>For any non-null reference value {@code x},
     * {@code x.equals(null)} should return {@code false}.
     * </ul>
     * <p>
     * The {@code equals} method for class {@code Object} implements
     * the most discriminating possible equivalence relation on objects;
     * that is, for any non-null reference values {@code x} and
     * {@code y}, this method returns {@code true} if and only
     * if {@code x} and {@code y} refer to the same object
     * ({@code x == y} has the value {@code true}).
     * <p>
     * Note that it is generally necessary to override the {@code hashCode}
     * method whenever this method is overridden, so as to maintain the
     * general contract for the {@code hashCode} method, which states
     * that equal objects must have equal hash codes.
     *
     * @param obj the reference object with which to compare.
     * @return {@code true} if this object is the same as the obj
     * argument; {@code false} otherwise.
     * @see #hashCode()
     * @see HashMap
     * <p>
     * 重写equals方法
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BleDevice)) {
            return false;
        }

        BleDevice bleDevice = (BleDevice) obj;
        return bleDevice.getBluetoothDevice().equals(getBluetoothDevice());
    }

    /*------------------------Parcelable接口----------------------------*/

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mBluetoothDevice, flags);
        dest.writeInt(this.mRssi);
        dest.writeByteArray(this.scanRecordBytes);
    }

    @SuppressWarnings("WeakerAccess")
    protected BleDevice(Parcel in) {
        this.mBluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.mRssi = in.readInt();
        this.scanRecordBytes = in.createByteArray();
    }

    public static final Parcelable.Creator<BleDevice> CREATOR = new Parcelable.Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel source) {
            return new BleDevice(source);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };
}
