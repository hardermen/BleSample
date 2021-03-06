package com.jackiepenghe.blelibrary;

import android.os.ParcelUuid;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jackiepenghe.blelibrary.systems.BleParcelUuid;


/**
 * broadcast service uuid keyBean.
 *
 * @author jackie
 */
final class AdvertiseServiceUuid {

    /*-----------------------------------field variables-----------------------------------*/

    /**
     * representation of a 128-bit universally unique identifier
     */
    @NonNull
    private BleParcelUuid bleParcelUuid;

    /**
     * uuid data
     */
    @Nullable
    private byte[] data;

    /*-----------------------------------Constructor-----------------------------------*/

    /**
     * Constructor
     *
     * @param bleParcelUuid BleParcelUuid
     * @param data       uuid data
     */
    AdvertiseServiceUuid(@NonNull BleParcelUuid bleParcelUuid, @Nullable byte[] data) {
        this.bleParcelUuid = bleParcelUuid;
        this.data = data;
    }

    /*-----------------------------------getter-----------------------------------*/

    /**
     * get BleParcelUuid
     * @return BleParcelUuid
     */
    @NonNull
    BleParcelUuid getBleParcelUuid() {
        return bleParcelUuid;
    }

    /**
     * get data
     * @return data
     */
    @Nullable
    byte[] getData() {
        return data;
    }

    /*-----------------------------------getter-----------------------------------*/

    /**
     * get BleParcelUuid
     * @return BleParcelUuid
     */
    @NonNull
    ParcelUuid getParcelUuid(){
        return ParcelUuid.fromString(bleParcelUuid.getUuid().toString());
    }
}
