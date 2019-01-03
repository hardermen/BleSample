package com.jackiepenghe.blelibrary;

/**
 * @author jackie
 */
public class AdvertiseData {

    private int manufacturerId;
    private byte[] data;

    public AdvertiseData(int manufacturerId, byte[] data) {
        this.manufacturerId = manufacturerId;
        this.data = data;
    }

    @SuppressWarnings("WeakerAccess")
    public int getManufacturerId() {
        return manufacturerId;
    }

    public byte[] getData() {
        return data;
    }
}
