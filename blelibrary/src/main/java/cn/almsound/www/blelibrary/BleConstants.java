package cn.almsound.www.blelibrary;

/**
 * @author alm
 *         Created by alm on 17-6-6.
 *         BLE常量
 */

public class BleConstants {
    /**
     * 打开通知时会使用到的UUID
     */
    static final String CLIENT_CHARACTERISTIC_CONFIG = "00002902-0000-1000-8000-00805f9b34fb";

    /**
     * ACTION定义区域
     */
    static final String ACTION_GATT_CONNECTED = "ACTION_GATT_CONNECTED";
    static final String ACTION_GATT_DISCONNECTED = "ACTION_GATT_DISCONNECTED";
    static final String ACTION_GATT_SERVICES_DISCOVERED = "ACTION_GATT_SERVICES_DISCOVERED";
    static final String ACTION_GATT_CONNECTING = "ACTION_GATT_CONNECTING";
    static final String ACTION_GATT_DISCONNECTING = "ACTION_GATT_DISCONNECTING";
    static final String ACTION_CHARACTERISTIC_READ = "ACTION_CHARACTERISTIC_READ";
    static final String ACTION_CHARACTERISTIC_CHANGED = "ACTION_CHARACTERISTIC_CHANGED";
    static final String ACTION_CHARACTERISTIC_WRITE = "ACTION_CHARACTERISTIC_WRITE";
    static final String ACTION_DESCRIPTOR_READ = "ACTION_DESCRIPTOR_READ";
    static final String ACTION_DESCRIPTOR_WRITE = "ACTION_DESCRIPTOR_WRITE";
    static final String ACTION_RELIABLE_WRITE_COMPLETED = "ACTION_RELIABLE_WRITE_COMPLETED";
    static final String ACTION_READ_REMOTE_RSSI = "ACTION_READ_REMOTE_RSSI";
    static final String ACTION_MTU_CHANGED = "ACTION_MTU_CHANGED";

    public static final int DEVICE_BOND_START_SUCCESS = 0;
    public static final int DEVICE_BOND_START_FAILED = 1;
    public static final int BLUETOOTH_MANAGER_NULL = 2;
    public static final int BLUETOOTH_ADAPTER_NULL = 3;
    public static final int DEVICE_BOND_BONDED = 4;
    public static final int DEVICE_BOND_BONDING = 5;
    public static final int BLUETOOTH_ADDRESS_INCORRECT = 6;
}
