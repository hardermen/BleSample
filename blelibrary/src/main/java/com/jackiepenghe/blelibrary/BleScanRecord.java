package com.jackiepenghe.blelibrary;

import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Copy from Android source code
 *
 * @author jackie
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class BleScanRecord implements Serializable, Parcelable {

    private static final String TAG = "BleScanRecord";

    private static final long serialVersionUID = 366536345375961707L;

    /**
     * The following data type values are assigned by Bluetooth SIG.
     * For more details refer to Bluetooth 4.1 specification, Volume 3, Part C, Section 18.
     */
    private static final int DATA_TYPE_FLAGS = 0x01;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL = 0x02;
    private static final int DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE = 0x03;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL = 0x04;
    private static final int DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE = 0x05;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL = 0x06;
    private static final int DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE = 0x07;
    private static final int DATA_TYPE_LOCAL_NAME_SHORT = 0x08;
    private static final int DATA_TYPE_LOCAL_NAME_COMPLETE = 0x09;
    private static final int DATA_TYPE_TX_POWER_LEVEL = 0x0A;
    private static final int DATA_TYPE_SERVICE_DATA_16_BIT = 0x16;
    private static final int DATA_TYPE_SERVICE_DATA_32_BIT = 0x20;
    private static final int DATA_TYPE_SERVICE_DATA_128_BIT = 0x21;
    private static final int DATA_TYPE_MANUFACTURER_SPECIFIC_DATA = 0xFF;

    /**
     * Flags of the advertising data.
     */
    private final int mAdvertiseFlags;

    @Nullable
    private final List<BleParcelUuid> mServiceUuids;

    private final BleSparseArray<byte[]> mManufacturerSpecificData;

    private final Map<BleParcelUuid, byte[]> mServiceData;

    /**
     * Transmission power level(in dB).
     */
    private final int mTxPowerLevel;

    /**
     * Local name of the Bluetooth LE device.
     */
    private final String mDeviceName;

    /**
     * Raw bytes of scan record.
     */
    private final byte[] mBytes;

    /**
     * Returns the advertising flags indicating the discoverable mode and capability of the device.
     * Returns -1 if the flag field is not set.
     */
    public int getAdvertiseFlags() {
        return mAdvertiseFlags;
    }

    /**
     * Returns a list of service UUIDs within the advertisement that are used to identify the
     * bluetooth GATT services.
     */
    public List<BleParcelUuid> getServiceUuids() {
        return mServiceUuids;
    }

    /**
     * Returns a sparse array of manufacturer identifier and its corresponding manufacturer specific
     * data.
     */
    public BleSparseArray<byte[]> getManufacturerSpecificData() {
        return mManufacturerSpecificData;
    }

    /**
     * Returns the manufacturer specific data associated with the manufacturer id. Returns
     * {@code null} if the {@code manufacturerId} is not found.
     */
    @Nullable
    public  byte[] getManufacturerSpecificData(int manufacturerId) {
        return mManufacturerSpecificData.get(manufacturerId);
    }

    /**
     * Returns a map of service UUID and its corresponding service data.
     */
    public Map<BleParcelUuid, byte[]> getServiceData() {
        return mServiceData;
    }

    /**
     * Returns the service data byte array associated with the {@code serviceUuid}. Returns
     * {@code null} if the {@code serviceDataUuid} is not found.
     */
    @Nullable
    public byte[] getServiceData(BleParcelUuid serviceDataUuid) {
        if (serviceDataUuid == null) {
            return null;
        }
        return mServiceData.get(serviceDataUuid);
    }

    /**
     * Returns the transmission power level of the packet in dBm. Returns {@link Integer#MIN_VALUE}
     * if the field is not set. This value can be used to calculate the path loss of a received
     * packet using the following equation:
     * <p>
     * <code>pathloss = txPowerLevel - rssi</code>
     */
    int getTxPowerLevel() {
        return mTxPowerLevel;
    }

    /**
     * Returns the local name of the BLE device. The is a UTF-8 encoded string.
     */
    @Nullable
    public String getDeviceName() {
        return mDeviceName;
    }

    /**
     * Returns raw bytes of scan record.
     */
    public byte[] getBytes() {
        return mBytes;
    }

    private BleScanRecord(@Nullable List<BleParcelUuid> serviceUuids,
                          BleSparseArray<byte[]> manufacturerData,
                          Map<BleParcelUuid, byte[]> serviceData,
                          int advertiseFlags, int txPowerLevel,
                          String localName, byte[] bytes) {
        mServiceUuids = serviceUuids;
        mManufacturerSpecificData = manufacturerData;
        mServiceData = serviceData;
        mDeviceName = localName;
        mAdvertiseFlags = advertiseFlags;
        mTxPowerLevel = txPowerLevel;
        mBytes = bytes;
    }

    /**
     * Parse scan record bytes to {@link android.bluetooth.le.ScanRecord}.
     * <p>
     * The format is defined in Bluetooth 4.1 specification, Volume 3, Part C, Section 11 and 18.
     * <p>
     * All numerical multi-byte entities and values shall use little-endian <strong>byte</strong>
     * order.
     *
     * @param scanRecord The scan record of Bluetooth LE advertisement and/or scan response.
     */
    public static BleScanRecord parseFromBytes(byte[] scanRecord) {
        if (scanRecord == null) {
            return null;
        }

        int currentPos = 0;
        int advertiseFlag = -1;
        List<BleParcelUuid> serviceUuids = new ArrayList<>();
        String localName = null;
        int txPowerLevel = Integer.MIN_VALUE;

        BleSparseArray<byte[]> manufacturerData = new BleSparseArray<>();
        Map<BleParcelUuid, byte[]> serviceData = new HashMap<>(5);

        try {
            while (currentPos < scanRecord.length) {
                // length is unsigned int.
                int length = scanRecord[currentPos++] & 0xFF;
                if (length == 0) {
                    break;
                }
                // Note the length includes the length of the field type itself.
                int dataLength = length - 1;
                // fieldType is unsigned int.
                int fieldType = scanRecord[currentPos++] & 0xFF;
                ParseData parseData = new ParseData(scanRecord, currentPos, advertiseFlag, serviceUuids, localName, txPowerLevel, manufacturerData, serviceData, dataLength, fieldType).invoke();
                advertiseFlag = parseData.getAdvertiseFlag();
                localName = parseData.getLocalName();
                txPowerLevel = parseData.getTxPowerLevel();
                currentPos += dataLength;
            }

            if (serviceUuids.isEmpty()) {
                serviceUuids = null;
            }
            return new BleScanRecord(serviceUuids, manufacturerData, serviceData,
                    advertiseFlag, txPowerLevel, localName, scanRecord);
        } catch (Exception e) {
            Log.e(TAG, "unable to parse scan record: " + Arrays.toString(scanRecord));
            // As the record is invalid, ignore all the parsed results for this packet
            // and return an empty record with raw scanRecord bytes in results
            return new BleScanRecord(null, null, null, -1, Integer.MIN_VALUE, null, scanRecord);
        }
    }

    @NonNull
    @Override
    public String toString() {
        return "BleScanRecord [mAdvertiseFlags=" + mAdvertiseFlags + ", mServiceUuids=" + mServiceUuids
                + ", mManufacturerSpecificData=" + BluetoothLeUtils.toString(mManufacturerSpecificData)
                + ", mServiceData=" + BluetoothLeUtils.toString(mServiceData)
                + ", mTxPowerLevel=" + mTxPowerLevel + ", mDeviceName=" + mDeviceName + "]";
    }

    /**
     * Parse service UUIDs.
     *
     * @param scanRecord   scanRecord
     * @param currentPos   currentPos
     * @param dataLength   dataLength
     * @param uuidLength   uuidLength
     * @param serviceUuids serviceUuids
     * @return currentPos
     */
    @SuppressWarnings("UnusedReturnValue")
    private static int parseServiceUuid(byte[] scanRecord, int currentPos, int dataLength,
                                        int uuidLength, List<BleParcelUuid> serviceUuids) {
        while (dataLength > 0) {
            byte[] uuidBytes = extractBytes(scanRecord, currentPos,
                    uuidLength);
            BleParcelUuid bleParcelUuid = BluetoothUuid.parseUuidFrom(uuidBytes);
            serviceUuids.add(bleParcelUuid);
            dataLength -= uuidLength;
            currentPos += uuidLength;
        }
        return currentPos;
    }

    /**
     * Helper method to extract bytes from byte array.
     *
     * @param scanRecord scanRecord
     * @param start start
     * @param length length
     * @return extract bytes
     */
    private static byte[] extractBytes(byte[] scanRecord, int start, int length) {
        byte[] bytes = new byte[length];
        System.arraycopy(scanRecord, start, bytes, 0, length);
        return bytes;
    }

    private static class ParseData {
        private byte[] scanRecord;
        private int currentPos;
        private int advertiseFlag;
        private List<BleParcelUuid> serviceUuids;
        private String localName;
        private int txPowerLevel;
        private BleSparseArray<byte[]> manufacturerData;
        private Map<BleParcelUuid, byte[]> serviceData;
        private int dataLength;
        private int fieldType;

        ParseData(byte[] scanRecord, int currentPos, int advertiseFlag, List<BleParcelUuid> serviceUuids, String localName, int txPowerLevel, BleSparseArray<byte[]> manufacturerData, Map<BleParcelUuid, byte[]> serviceData, int dataLength, int fieldType) {
            this.scanRecord = scanRecord;
            this.currentPos = currentPos;
            this.advertiseFlag = advertiseFlag;
            this.serviceUuids = serviceUuids;
            this.localName = localName;
            this.txPowerLevel = txPowerLevel;
            this.manufacturerData = manufacturerData;
            this.serviceData = serviceData;
            this.dataLength = dataLength;
            this.fieldType = fieldType;
        }

        int getAdvertiseFlag() {
            return advertiseFlag;
        }

        String getLocalName() {
            return localName;
        }

        int getTxPowerLevel() {
            return txPowerLevel;
        }

        ParseData invoke() {
            switch (fieldType) {
                case DATA_TYPE_FLAGS:
                    advertiseFlag = scanRecord[currentPos] & 0xFF;
                    break;
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_PARTIAL:
                case DATA_TYPE_SERVICE_UUIDS_16_BIT_COMPLETE:
                    parseServiceUuid(scanRecord, currentPos,
                            dataLength, BluetoothUuid.UUID_BYTES_16_BIT, serviceUuids);
                    break;
                case DATA_TYPE_SERVICE_UUIDS_32_BIT_PARTIAL:
                case DATA_TYPE_SERVICE_UUIDS_32_BIT_COMPLETE:
                    parseServiceUuid(scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_32_BIT, serviceUuids);
                    break;
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_PARTIAL:
                case DATA_TYPE_SERVICE_UUIDS_128_BIT_COMPLETE:
                    parseServiceUuid(scanRecord, currentPos, dataLength,
                            BluetoothUuid.UUID_BYTES_128_BIT, serviceUuids);
                    break;
                case DATA_TYPE_LOCAL_NAME_SHORT:
                case DATA_TYPE_LOCAL_NAME_COMPLETE:
                    localName = new String(
                            extractBytes(scanRecord, currentPos, dataLength));
                    break;
                case DATA_TYPE_TX_POWER_LEVEL:
                    txPowerLevel = scanRecord[currentPos];
                    break;
                case DATA_TYPE_SERVICE_DATA_16_BIT:
                case DATA_TYPE_SERVICE_DATA_32_BIT:
                case DATA_TYPE_SERVICE_DATA_128_BIT:
                    int serviceUuidLength = BluetoothUuid.UUID_BYTES_16_BIT;
                    if (fieldType == DATA_TYPE_SERVICE_DATA_32_BIT) {
                        serviceUuidLength = BluetoothUuid.UUID_BYTES_32_BIT;
                    } else if (fieldType == DATA_TYPE_SERVICE_DATA_128_BIT) {
                        serviceUuidLength = BluetoothUuid.UUID_BYTES_128_BIT;
                    }

                    byte[] serviceDataUuidBytes = extractBytes(scanRecord, currentPos,
                            serviceUuidLength);
                    BleParcelUuid bleParcelUuid = BluetoothUuid.parseUuidFrom(serviceDataUuidBytes);
                    byte[] serviceDataArray = extractBytes(scanRecord,
                            currentPos + serviceUuidLength, dataLength - serviceUuidLength);
                    serviceData.put(bleParcelUuid, serviceDataArray);
                    break;
                case DATA_TYPE_MANUFACTURER_SPECIFIC_DATA:
                    // The first two bytes of the manufacturer specific data are
                    // manufacturer ids in little endian.
                    int manufacturerId = ((scanRecord[currentPos + 1] & 0xFF) << 8) +
                            (scanRecord[currentPos] & 0xFF);
                    byte[] manufacturerDataBytes = extractBytes(scanRecord, currentPos + 2,
                            dataLength - 2);
                    manufacturerData.put(manufacturerId, manufacturerDataBytes);
                    break;
                default:
                    // Just ignore, we don't handle such data type.
                    break;
            }
            return this;
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.mAdvertiseFlags);
        dest.writeTypedList(this.mServiceUuids);
        dest.writeParcelable(this.mManufacturerSpecificData, flags);
        dest.writeInt(this.mServiceData.size());
        for (Map.Entry<BleParcelUuid, byte[]> entry : this.mServiceData.entrySet()) {
            dest.writeParcelable(entry.getKey(), flags);
            dest.writeByteArray(entry.getValue());
        }
        dest.writeInt(this.mTxPowerLevel);
        dest.writeString(this.mDeviceName);
        dest.writeByteArray(this.mBytes);
    }

    protected BleScanRecord(Parcel in) {
        this.mAdvertiseFlags = in.readInt();
        this.mServiceUuids = in.createTypedArrayList(BleParcelUuid.CREATOR);
        this.mManufacturerSpecificData = in.readParcelable(BleSparseArray.class.getClassLoader());
        int mServiceDataSize = in.readInt();
        this.mServiceData = new HashMap<>(mServiceDataSize);
        for (int i = 0; i < mServiceDataSize; i++) {
            BleParcelUuid key = in.readParcelable(ParcelUuid.class.getClassLoader());
            byte[] value = in.createByteArray();
            if (key != null && value != null) {
                this.mServiceData.put(key, value);
            }
        }
        this.mTxPowerLevel = in.readInt();
        this.mDeviceName = in.readString();
        this.mBytes = in.createByteArray();
    }

    public static final Parcelable.Creator<BleScanRecord> CREATOR = new Parcelable.Creator<BleScanRecord>() {
        @Override
        public BleScanRecord createFromParcel(Parcel source) {
            return new BleScanRecord(source);
        }

        @Override
        public BleScanRecord[] newArray(int size) {
            return new BleScanRecord[size];
        }
    };
}