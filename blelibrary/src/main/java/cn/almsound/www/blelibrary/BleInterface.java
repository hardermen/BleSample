package cn.almsound.www.blelibrary;

import android.bluetooth.BluetoothDevice;

/**
 * @author alm
 *         Created by alm on 17-6-5.
 *         接口定义区
 */

@SuppressWarnings("ALL")
public class BleInterface {

    /**
     * ble连接工具关闭完成的回调
     */
    public interface OnCloseCompleteListener {
        /**
         * ble连接工具关闭完成的时候回调此函数
         */
        void onCloseComplete();
    }

    /**
     * 发现一个设备的回调监听
     */
    public interface OnScanFindOneDeviceListener {
        /**
         * 扫描到一个蓝牙设备时回调此函数
         *
         * @param bluetoothDevice 蓝牙设备
         * @param rssi            RSSI(信号强度)
         * @param scanRecord      广播包内容
         */
        void scanFindOneDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord);
    }

    /**
     * 发现一个新设备的回调监听
     */
    public interface OnScanFindOneNewDeviceListener {
        /**
         * 发现一个新的蓝牙设备时回调此函数
         *
         * @param bleDevice 自定义Ble设备Been类
         */
        void scanFindOneNewDevice(BleDevice bleDevice);
    }

    /**
     * 扫描完成的回调监听
     */
    public interface OnScanCompleteListener {
        /**
         * 扫描完成时回调此函数
         */
        void scanComplete();
    }

    /**
     * BLE蓝牙设备绑定状态改变的回调
     */
    public interface OnDeviceBondStateChangedListener {
        /**
         * 设备正在绑定
         */
        void deviceBinding();

        /**
         * 设备已经绑定过了
         */
        void deviceBonded();

        /**
         * 取消绑定或者绑定失败
         */
        void deviceBindNone();
    }


    /**
     * 连接成功的回调接口
     */
    public interface OnConnectedListener {
        /**
         * 连接成功
         */
        void onConnected();
    }

    /**
     * 断开连接的回调接口
     */
    public interface OnDisconnectedListener {
        /**
         * 断开连接
         */
        void onDisconnected();
    }

    /**
     * 服务发现完成的回调接口
     */
    public interface OnServicesDiscoveredListener {
        /**
         * 远端设备服务列表扫描完成
         */
        void onServicesDiscovered();
    }

    /**
     * 正在连接的回调接口
     */
    public interface OnConnectingListener {
        /**
         * 正在连接
         */
        void onConnecting();
    }

    /**
     * 正在断开连接的回调接口
     */
    public interface OnDisconnectingListener {
        /**
         * 正在断开连接
         */
        void onDisconnecting();
    }

    /**
     * 读取到远端设备的数据的回调接口
     */
    public interface OnCharacteristicReadListener {
        /**
         * 读取到远端设备的数据
         *
         * @param values 读取到的数据
         */
        void onCharacteristicRead(byte[] values);
    }

    /**
     * 收到远端设备的通知的回调接口
     */
    public interface OnReceiveNotificationListener {
        /**
         * 收到远端设备的通知
         *
         * @param values 远端设备的通知数据
         */
        void onReceiveNotification(byte[] values);
    }

    /**
     * 向远端设备写入数据的回调
     */
    public interface OnCharacteristicWriteListener {
        /**
         * 向远端设备写入数据
         *
         * @param values 向远端设备写入的数据
         */
        void onCharacteristicWrite(byte[] values);
    }

    /**
     * 读取到远端设备的描述符的回调
     */
    public interface OnDescriptorReadListener {
        /**
         * 读取到远端设备的描述符
         *
         * @param values 远端设备的描述符
         */
        void onDescriptorRead(byte[] values);
    }

    /**
     * 向远端设备写入描述符的回调
     */
    public interface OnDescriptorWriteListener {
        /**
         * 向远端设备写入描述符
         *
         * @param values 写入的描述符
         */
        void onDescriptorWrite(byte[] values);
    }

    /**
     * 可靠数据写入完成的回调
     */
    public interface OnReliableWriteCompletedListener {
        /**
         * 可靠数据写入完成
         */
        void onReliableWriteCompleted();
    }

    /**
     * 读到远端设备rssi值的回调
     */
    public interface OnReadRemoteRssiListener {
        /**
         * 读到远端设备rssi值
         *
         * @param rssi 远端设备的rssi值
         */
        void onReadRemoteRssi(int rssi);
    }

    /**
     * 最大传输单位被改变的回调
     */
    public interface OnMtuChangedListener {
        /**
         * 最大传输单位被改变
         *
         * @param mtu 最大传输单位
         */
        void onMtuChanged(int mtu);
    }

    /**
     * 蓝牙被打开的回调
     */
    public interface OnBluetoothOpenListener{
        /**
         *  蓝牙被打开
         */
        void onBluetoothOpen();
    }

    /**
     * 蓝牙被关闭的回调
     */
    public interface OnBluetoothCloseListener{
        /**
         *  蓝牙被关闭
         */
        void onBluetoothClose();
    }
}
