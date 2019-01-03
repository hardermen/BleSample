package com.jackiepenghe.blelibrary.systems;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Collection;
import java.util.HashSet;

/**
 * @author jackie
 */
public class BleHashSet<E> extends HashSet<E> implements Parcelable {

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
    }

    @SuppressWarnings("unused")
    public BleHashSet() {
        super();
    }

    /**
     * Constructs a new set containing the elements in the specified
     * collection.  The <tt>HashMap</tt> is created with default load factor
     * (0.75) and an initial capacity sufficient to contain the elements in
     * the specified collection.
     *
     * @param c the collection whose elements are to be placed into this set
     * @throws NullPointerException if the specified collection is null
     */
    @SuppressWarnings("WeakerAccess")
    public BleHashSet(Collection<? extends E> c) {
        super(c);
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and the specified load factor.
     *
     * @param initialCapacity the initial capacity of the hash map
     * @param loadFactor      the load factor of the hash map
     * @throws IllegalArgumentException if the initial capacity is less
     *                                  than zero, or if the load factor is nonpositive
     */
    @SuppressWarnings("unused")
    public BleHashSet(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    /**
     * Constructs a new, empty set; the backing <tt>HashMap</tt> instance has
     * the specified initial capacity and default load factor (0.75).
     *
     * @param initialCapacity the initial capacity of the hash table
     * @throws IllegalArgumentException if the initial capacity is less
     *                                  than zero
     */
    @SuppressWarnings("unused")
    public BleHashSet(int initialCapacity) {
        super(initialCapacity);
    }

    @SuppressWarnings("WeakerAccess")
    protected BleHashSet(Parcel in) {
    }

    public static final Creator<BleHashSet> CREATOR = new Creator<BleHashSet>() {
        @Override
        public BleHashSet createFromParcel(Parcel source) {
            return new BleHashSet(source);
        }

        @Override
        public BleHashSet[] newArray(int size) {
            return new BleHashSet[size];
        }
    };
}
