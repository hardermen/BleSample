package com.jackiepenghe.blelibrary.systems;

import android.os.Parcel;
import android.os.Parcelable;

import com.jackiepenghe.blelibrary.DebugUtil;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * @author jackie
 */
class BleArrayList<E> extends ArrayList<E> {

    private static final long serialVersionUID = -2475001965787890849L;

//    @Override
//    public int describeContents() {
//        return 0;
//    }
//
//    @Override
//    public void writeToParcel(Parcel dest, int flags) {
//    }

    @SuppressWarnings("WeakerAccess")
    public BleArrayList() {
        super();
    }

//    @SuppressWarnings("WeakerAccess")
//    protected BleArrayList(Parcel in) {
//    }
//
//    public static final Creator<BleArrayList> CREATOR = new Creator<BleArrayList>() {
//        @Override
//        public BleArrayList createFromParcel(Parcel source) {
//            return new BleArrayList(source);
//        }
//
//        @Override
//        public BleArrayList[] newArray(int size) {
//            return new BleArrayList[size];
//        }
//    };
}
