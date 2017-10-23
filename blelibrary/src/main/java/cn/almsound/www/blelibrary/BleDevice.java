package cn.almsound.www.blelibrary;

import android.bluetooth.BluetoothDevice;
import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author alm
 *         Created by alm on 17-6-5.
 *         自定义BLE设备been类
 */

@SuppressWarnings("ALL")
public class BleDevice implements Serializable, Parcelable {

    private String mDeviceName;

    /**
     * 蓝牙设备
     */
    private BluetoothDevice mBluetoothDevice;

    /**
     * rssi值
     */
    private int mRssi;

    /**
     * 广播包
     */
    private byte[] mScanRecord;

    /**
     * 构造器
     *
     * @param bluetoothDevice 蓝牙设备
     * @param rssi            rssi值
     */
    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, String deviceName) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        if (bluetoothDevice.getName() == null || bluetoothDevice.getName().isEmpty()) {
            setDeviceName(deviceName);
        }
    }

    /**
     * 构造器
     *
     * @param bluetoothDevice 蓝牙设备
     * @param rssi            rssi值
     * @param scanRecord      广播包
     */
    public BleDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord, String deviceName) {
        mBluetoothDevice = bluetoothDevice;
        mRssi = rssi;
        mScanRecord = scanRecord;
        if (bluetoothDevice.getName() == null || bluetoothDevice.getName().isEmpty()) {
            setDeviceName(deviceName);
        }
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBluetoothDevice;
    }

    public void setBluetoothDevice(BluetoothDevice mBluetoothDevice) {
        this.mBluetoothDevice = mBluetoothDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public void setRssi(int mRssi) {
        this.mRssi = mRssi;
    }

    public byte[] getScanRecord() {
        return mScanRecord;
    }

    public void setScanRecord(byte[] mScanRecord) {
        this.mScanRecord = mScanRecord;
    }

    public String getDeviceName() {
        if (mDeviceName == null) {
            return mBluetoothDevice.getName();
        } else {
            return mDeviceName;
        }
    }

    private void setDeviceName(String deviceName) {
        mDeviceName = deviceName;
    }

    public String getDeviceAddress() {
        return mBluetoothDevice.getAddress();
    }

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
     */
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof BleDevice)) {
            return false;
        }

        BleDevice bleDevice = (BleDevice) obj;
        return bleDevice.getDeviceAddress().equals(getDeviceAddress());
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(this.mBluetoothDevice, flags);
        dest.writeInt(this.mRssi);
        dest.writeByteArray(this.mScanRecord);
    }

    protected BleDevice(Parcel in) {
        this.mBluetoothDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        this.mRssi = in.readInt();
        this.mScanRecord = in.createByteArray();
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
