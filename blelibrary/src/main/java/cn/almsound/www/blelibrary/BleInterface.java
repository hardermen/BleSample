package cn.almsound.www.blelibrary;

import android.bluetooth.BluetoothDevice;

/**
 * Created by alm on 17-6-5.
 * 借口定义区
 */

@SuppressWarnings("ALL")
public class BleInterface {

    /**
     * ble连接工具关闭完成的回调
     */
    public interface OnCloseCompleteListener {
        void onCloseComplete();
    }

    /**
     * 发现一个设备的回调监听
     */
    public interface OnScanFindADeviceListener {
        void scanFindADevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord);
    }

    /**
     * 发现一个新设备的回调监听
     */
    public  interface OnScanFindANewDeviceListener {
        void scanFindANewDevice(BleDevice bleDevice);
    }

    /**
     * 扫描完成的回调监听
     */
    public  interface OnScanCompleteListener {
        void scanComplete();
    }

    /**
     * BLE蓝牙设备绑定状态改变的回调
     */
    public interface OnDeviceBondStateChangedListener {
        void deviceBinding();

        void deviceBonded();

        void deviceBindNone();
    }


    /**
     * 连接成功的回调接口
     */
    public  interface OnConnectedListener {
        void onConnected();
    }

    /**
     * 断开连接的回调接口
     */
    public   interface OnDisconnectedListener {
        void onDisconnected();
    }

    /**
     * 服务发现完成的回调接口
     */
    public  interface OnServicesDiscoveredListener {
        void onServicesDiscovered();
    }

    /**
     * 正在连接的回调接口
     */
    public  interface OnConnectingListener {
        void onConnecting();
    }

    /**
     * 正在断开连接的回调接口
     */
    public interface OnDisconnectingListener {
        void onDisconnecting();
    }

    /**
     * 读取到远端设备的数据的回调接口
     */
    public interface OnCharacteristicReadListener {
        void onCharacteristicRead(byte[] value);
    }

    /**
     * 收到远端设备的通知的回调接口
     */
    public interface OnReceiveNotificationListener {
        void OnReceiveNotification(byte[] value);
    }

    /**
     * 向远端设备写入数据的回调
     */
    public interface OnCharacteristicWriteListener {
        void onCharacteristicWrite(byte[] value);
    }

    /**
     * 读取到远端设备的描述符的回调
     */
    public  interface OnDescriptorReadListener {
        void onDescriptorRead(byte[] value);
    }

    /**
     * 向远端设备写入描述符的回调
     */
    public   interface OnDescriptorWriteListener {
        void onDescriptorWrite(byte[] value);
    }

    /**
     * 可靠数据写入完成的回调
     */
    public interface OnReliableWriteCompletedListener {
        void onReliableWriteCompleted();
    }

    /**
     * 读到远端设备rssi值的回调
     */
    public  interface OnReadRemoteRssiListener {
        void onReadRemoteRssi(int rssi);
    }

    /**
     * 最大传输单位被改变的回调
     */
    public interface OnMtuChangedListener {
        void onMtuChanged(int mtu);
    }
}
