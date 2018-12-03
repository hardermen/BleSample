package com.jackiepenghe.blelibrary;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import java.io.Serializable;
import java.util.UUID;

/**
 * @author jackie
 */
@SuppressWarnings("WeakerAccess")
public class BleParcelUuid implements Serializable, Parcelable {


    private static final long serialVersionUID = 8540083992621545338L;

    private final UUID mUuid;

    /**
     * Constructor creates a ParcelUuid instance from the
     * given {@link UUID}.
     *
     * @param uuid UUID
     */
    public BleParcelUuid(UUID uuid) {
        mUuid = uuid;
    }

    /**
     * Creates a new ParcelUuid from a string representation of {@link UUID}.
     *
     * @param uuid
     *            the UUID string to parse.
     * @return a ParcelUuid instance.
     * @throws NullPointerException
     *             if {@code uuid} is {@code null}.
     * @throws IllegalArgumentException
     *             if {@code uuid} is not formatted correctly.
     */
    public static BleParcelUuid fromString(String uuid) {
        return new BleParcelUuid(UUID.fromString(uuid));
    }

    /**
     * Get the {@link UUID} represented by the ParcelUuid.
     *
     * @return UUID contained in the ParcelUuid.
     */
    public UUID getUuid() {
        return mUuid;
    }

    /**
     * Returns a string representation of the ParcelUuid
     * For example: 0000110B-0000-1000-8000-00805F9B34FB will be the return value.
     *
     * @return a String instance.
     */
    @NonNull
    @Override
    public String toString() {
        return mUuid.toString();
    }


    @Override
    public int hashCode() {
        return mUuid.hashCode();
    }

    /**
     * Compares this ParcelUuid to another object for equality. If {@code object}
     * is not {@code null}, is a ParcelUuid instance, and all bits are equal, then
     * {@code true} is returned.
     *
     * @param object
     *            the {@code Object} to compare to.
     * @return {@code true} if this ParcelUuid is equal to {@code object}
     *         or {@code false} if not.
     */
    @Override
    public boolean equals(Object object) {
        if (object == null) {
            return false;
        }

        if (this == object) {
            return true;
        }

        if (!(object instanceof BleParcelUuid)) {
            return false;
        }

        BleParcelUuid that = (BleParcelUuid) object;

        return (this.mUuid.equals(that.mUuid));
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeSerializable(this.mUuid);
    }

    protected BleParcelUuid(Parcel in) {
        this.mUuid = (UUID) in.readSerializable();
    }

    public static final Parcelable.Creator<BleParcelUuid> CREATOR = new Parcelable.Creator<BleParcelUuid>() {
        @Override
        public BleParcelUuid createFromParcel(Parcel source) {
            return new BleParcelUuid(source);
        }

        @Override
        public BleParcelUuid[] newArray(int size) {
            return new BleParcelUuid[size];
        }
    };
}
