package com.jackiepenghe.blelibrary.systems;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.HashMap;

/**
 * @author jackie
 */
public class BleHashMap<K,V> extends HashMap<K,V> implements Parcelable {


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @SuppressWarnings("WeakerAccess")
    public BleHashMap() {
        super();
    }

    @SuppressWarnings("WeakerAccess")
    protected BleHashMap(Parcel in) {
    }

    public static final Creator<BleHashMap> CREATOR = new Creator<BleHashMap>() {
        @Override
        public BleHashMap createFromParcel(Parcel source) {
            return new BleHashMap(source);
        }

        @Override
        public BleHashMap[] newArray(int size) {
            return new BleHashMap[size];
        }
    };
}
