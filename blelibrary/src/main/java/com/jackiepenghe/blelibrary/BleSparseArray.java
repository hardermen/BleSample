package com.jackiepenghe.blelibrary;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.SparseArray;

import java.io.Serializable;

@SuppressWarnings("WeakerAccess")
public class BleSparseArray<T> extends SparseArray<T> implements Serializable, Parcelable {

    private static final long serialVersionUID = -7680637790048655801L;


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    public BleSparseArray() {
    }

    protected BleSparseArray(Parcel in) {
    }

    public static final Parcelable.Creator<BleSparseArray> CREATOR = new Parcelable.Creator<BleSparseArray>() {
        @Override
        public BleSparseArray createFromParcel(Parcel source) {
            return new BleSparseArray(source);
        }

        @Override
        public BleSparseArray[] newArray(int size) {
            return new BleSparseArray[size];
        }
    };
}
