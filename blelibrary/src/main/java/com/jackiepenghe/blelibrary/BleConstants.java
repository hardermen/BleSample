package com.jackiepenghe.blelibrary;

/**
 * BLE常量
 *
 * @author alm
 */

public class BleConstants {

    /*库内常量定义区域*/

    /**
     * 用于传输远端设备的特征的KEY
     */
    static final String UUID = "UUID";
    /**
     * 打开通知时会使用到的UUID
     */
    static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    /*库内ACTION定义区域*/

    static final String ACTION_GATT_CONNECTED = "com.jackiepenghe.blelibrary.ACTION_GATT_CONNECTED";
    static final String ACTION_GATT_DISCONNECTED = "com.jackiepenghe.blelibrary.ACTION_GATT_DISCONNECTED";
    static final String ACTION_GATT_SERVICES_DISCOVERED = "com.jackiepenghe.blelibrary.ACTION_GATT_SERVICES_DISCOVERED";
    static final String ACTION_GATT_CONNECTING = "com.jackiepenghe.blelibrary.ACTION_GATT_CONNECTING";
    static final String ACTION_GATT_DISCONNECTING = "com.jackiepenghe.blelibrary.ACTION_GATT_DISCONNECTING";
    static final String ACTION_CHARACTERISTIC_READ = "com.jackiepenghe.blelibrary.ACTION_CHARACTERISTIC_READ";
    static final String ACTION_CHARACTERISTIC_CHANGED = "com.jackiepenghe.blelibrary.ACTION_CHARACTERISTIC_CHANGED";
    static final String ACTION_CHARACTERISTIC_WRITE = "com.jackiepenghe.blelibrary.ACTION_CHARACTERISTIC_WRITE";
    static final String ACTION_DESCRIPTOR_READ = "com.jackiepenghe.blelibrary.ACTION_DESCRIPTOR_READ";
    static final String ACTION_DESCRIPTOR_WRITE = "com.jackiepenghe.blelibrary.ACTION_DESCRIPTOR_WRITE";
    static final String ACTION_RELIABLE_WRITE_COMPLETED = "com.jackiepenghe.blelibrary.ACTION_RELIABLE_WRITE_COMPLETED";
    static final String ACTION_READ_REMOTE_RSSI = "com.jackiepenghe.blelibrary.ACTION_READ_REMOTE_RSSI";
    static final String ACTION_MTU_CHANGED = "com.jackiepenghe.blelibrary.ACTION_MTU_CHANGED";
    static final String ACTION_GATT_NOT_SUCCESS = "com.jackiepenghe.blelibrary.ACTION_GATT_NOT_SUCCESS";
    public static final String ACTION_GATT_DISCOVER_SERVICES_FAILED = "com.jackiepenghe.blelibrary.ACTION_GATT_DISCOVER_SERVICES_FAILED";
    public static final String ACTION_GATT_STATUS_ERROR = "com.jackiepenghe.blelibrary.ACTION_GATT_STATUS_ERROR";

    /*开放常量定义区*/

    public static final int DEVICE_BOND_START_SUCCESS = 0;
    public static final int DEVICE_BOND_START_FAILED = 1;
    public static final int BLUETOOTH_MANAGER_NULL = 2;
    public static final int BLUETOOTH_ADAPTER_NULL = 3;
    public static final int DEVICE_BOND_BONDED = 4;
    public static final int DEVICE_BOND_BONDING = 5;
    public static final int BLUETOOTH_ADDRESS_INCORRECT = 6;
}
