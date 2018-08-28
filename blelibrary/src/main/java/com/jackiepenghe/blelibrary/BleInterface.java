package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.util.List;

/**
 * 接口定义区
 *
 * @author alm
 */

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
         * @param bleDevice 自定义Ble设备Been类
         */
        void onScanFindOneDevice(BleDevice bleDevice);
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
        void onScanFindOneNewDevice(BleDevice bleDevice);
    }

    /**
     * 安卓5.0以上的API才拥有的接口
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    public interface On21ScanCallback {

        /**
         * 将扫描结果批量提交
         *
         * @param results 扫描结果
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        void onBatchScanResults(List<ScanResult> results);

        /**
         * 扫描失败
         *
         * @param errorCode 错误码
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
        void onScanFailed(int errorCode);
    }

    /**
     * 扫描完成的回调监听
     */
    public interface OnScanCompleteListener {
        /**
         * 扫描完成时回调此函数
         */
        void onScanComplete();
    }

    /**
     * BLE蓝牙设备绑定状态改变的回调
     */
    public interface OnDeviceBondStateChangedListener {
        /**
         * 设备正在绑定
         */
        void onDeviceBinding();

        /**
         * 设备已经绑定过了
         */
        void onDeviceBonded();

        /**
         * 取消绑定或者绑定失败
         */
        void onDeviceBindNone();
    }

    /**
     * BluetoothGatt客户端配置失败的回调接口
     */
    public interface OnBluetoothGattOptionsNotSuccessListener {
        /**
         * BluetoothGatt客户端配置失败
         *
         * @param methodName  执行失败的函数名
         * @param errorStatus 执行失败的错误状态码
         */
        void onBluetoothGattOptionsNotSuccess(String methodName, int errorStatus);
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

        /**
         * 远端设备服务列表扫描失败
         */
        void onDiscoverServiceFailed();
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
         * @param uuid   uuid的String字符串
         * @param values 读取到的数据
         */
        void onCharacteristicRead(String uuid, byte[] values);
    }

    /**
     * 收到远端设备的通知的回调接口
     */
    public interface OnReceiveNotificationListener {
        /**
         * 收到远端设备的通知
         *
         * @param uuid   uuid的字符串
         * @param values 远端设备的通知数据
         */
        void onReceiveNotification(String uuid, byte[] values);
    }

    /**
     * 向远端设备写入数据的回调
     */
    public interface OnCharacteristicWriteListener {
        /**
         * 向远端设备写入数据
         *
         * @param uuid   uuid的字符串
         * @param values 向远端设备写入的数据
         */
        void onCharacteristicWrite(String uuid, byte[] values);
    }

    /**
     * 读取到远端设备的描述符的回调
     */
    public interface OnDescriptorReadListener {
        /**
         * 读取到远端设备的描述符
         *
         * @param uuid   uuid的字符串
         * @param values 远端设备的描述符
         */
        void onDescriptorRead(String uuid, byte[] values);
    }

    /**
     * 向远端设备写入描述符的回调
     */
    public interface OnDescriptorWriteListener {
        /**
         * 向远端设备写入描述符
         *
         * @param uuid   uuid的字符串
         * @param values 写入的描述符
         */
        void onDescriptorWrite(String uuid, byte[] values);
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
     * 蓝牙开关状态改变时的回调
     */
    public interface OnBluetoothSwitchChangedListener {
        /**
         * 蓝牙开关状态改变
         *
         * @param switchStatus true表示开关打开，false表示开关关闭
         */
        void onBluetoothSwitchChanged(boolean switchStatus);
    }

    /**
     * 蓝牙广播时，作为服务端的相关回调
     */
    public interface OnBluetoothGattServerCallbackListener {

        /**
         * Callback indicating when a remote device has been connected or disconnected.
         *
         * @param device   Remote device that has been connected or disconnected.
         * @param status   Status of the connect or disconnect operation.
         * @param newState Returns the new connection state. Can be one of
         *                 {@link BluetoothProfile#STATE_DISCONNECTED} or
         *                 {@link BluetoothProfile#STATE_CONNECTED}
         */
        void onConnectionStateChange(BluetoothDevice device, int status, int newState);

        /**
         * Indicates whether a local service has been added successfully.
         *
         * @param status  Returns {@link BluetoothGatt#GATT_SUCCESS} if the service
         *                was added successfully.
         * @param service The service that has been added
         */
        void onServiceAdded(int status, BluetoothGattService service);

        /**
         * A remote client has requested to read a local characteristic.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device         The remote device that has requested the read operation
         * @param requestId      The Id of the request
         * @param offset         Offset into the value of the characteristic
         * @param characteristic Characteristic to be read
         */
        void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattCharacteristic characteristic);

        /**
         * A remote client has requested to write to a local characteristic.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device         The remote device that has requested the write operation
         * @param requestId      The Id of the request
         * @param characteristic Characteristic to be written to.
         * @param preparedWrite  true, if this write operation should be queued for
         *                       later execution.
         * @param responseNeeded true, if the remote device requires a response
         * @param offset         The offset given for the value
         * @param value          The value the client wants to assign to the characteristic
         */
        void onCharacteristicWriteRequest(BluetoothDevice device, int requestId, BluetoothGattCharacteristic characteristic, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value);

        /**
         * A remote client has requested to read a local descriptor.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device     The remote device that has requested the read operation
         * @param requestId  The Id of the request
         * @param offset     Offset into the value of the characteristic
         * @param descriptor Descriptor to be read
         */
        void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset, BluetoothGattDescriptor descriptor);

        /**
         * A remote client has requested to write to a local descriptor.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device         The remote device that has requested the write operation
         * @param requestId      The Id of the request
         * @param descriptor     Descriptor to be written to.
         * @param preparedWrite  true, if this write operation should be queued for
         *                       later execution.
         * @param responseNeeded true, if the remote device requires a response
         * @param offset         The offset given for the value
         * @param value          The value the client wants to assign to the descriptor
         */
        void onDescriptorWriteRequest(BluetoothDevice device, int requestId, BluetoothGattDescriptor descriptor, boolean preparedWrite, boolean responseNeeded, int offset, byte[] value);

        /**
         * Execute all pending write operations for this device.
         * <p>
         * <p>An application must call {@link BluetoothGattServer#sendResponse}
         * to complete the request.
         *
         * @param device    The remote device that has requested the write operations
         * @param requestId The Id of the request
         * @param execute   Whether the pending writes should be executed (true) or
         */
        void onExecuteWrite(BluetoothDevice device, int requestId, boolean execute);

        /**
         * Callback invoked when a notification or indication has been sent to
         * a remote device.
         * <p>
         * <p>When multiple notifications are to be sent, an application must
         * wait for this callback to be received before sending additional
         * notifications.
         *
         * @param device The remote device the notification has been sent to
         * @param status {@link BluetoothGatt#GATT_SUCCESS} if the operation was successful
         */
        void onNotificationSent(BluetoothDevice device, int status);

        /**
         * Callback indicating the MTU for a given device connection has changed.
         * <p>
         * <p>This callback will be invoked if a remote client has requested to change
         * the MTU for a given connection.
         *
         * @param device The remote device that requested the MTU change
         * @param mtu    The new MTU size
         */
        void onMtuChanged(BluetoothDevice device, int mtu);

        /**
         * Callback triggered as result of {@link BluetoothGattServer#setPreferredPhy}, or as a result
         * of remote device changing the PHY.
         *
         * @param device The remote device
         * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
         *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
         * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
         *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
         * @param status Status of the PHY update operation.
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        void onPhyUpdate(BluetoothDevice device, int txPhy, int rxPhy, int status);

        /**
         * Callback triggered as result of {@link BluetoothGattServer#readPhy}
         *
         * @param device The remote device that requested the PHY read
         * @param txPhy  the transmitter PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
         *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
         * @param rxPhy  the receiver PHY in use. One of {@link BluetoothDevice#PHY_LE_1M},
         *               {@link BluetoothDevice#PHY_LE_2M}, and {@link BluetoothDevice#PHY_LE_CODED}
         * @param status Status of the PHY read operation.
         *               {@link BluetoothGatt#GATT_SUCCESS} if the operation succeeds.
         */
        void onPhyRead(BluetoothDevice device, int txPhy, int rxPhy, int status);
    }

    /**
     * 状态码错误监听
     */
    public interface OnStatusErrorListener {
        /**
         * 状态码错误
         *
         * @param status 状态码
         */
        void onStatusError(int status);
    }

    /**
     * 在大数据传输时进行的相关回调
     */
    public interface OnBigDataSendStateChangedListener {

        /**
         * 传输开始
         */
        void sendStarted();

        /**
         * 传输完成
         */
        void sendFinished();

        /**
         * 数据发送成功
         *
         * @param currentPackageCount 当前发送成功的包数
         * @param pageCount           总包数
         * @param data                本包发送的数据
         */
        void packageSendProgressChanged(int currentPackageCount, int pageCount, byte[] data);

        /**
         * 数据发送失败
         *
         * @param currentPackageCount 当前发送失败的包数
         * @param pageCount           总包数
         * @param data                本包发送的数据
         */
        void packageSendFailed(int currentPackageCount, int pageCount, byte[] data);

        /**
         * 本包数据发送失败，正在重新发送
         *
         * @param currentPackageCount 当前发送失败的包数
         * @param pageCount           总包数
         * @param tryCount            尝试次数
         * @param data                本包发送的数据
         */
        void packageSendFailedAndRetry(int currentPackageCount, int pageCount, int tryCount, byte[] data);
    }

    /**
     * 写入大数据并包含通知 到远端设备时相关的回调
     */
    public interface OnBigDataWriteWithNotificationSendStateChangedListener {

        /**
         * 收到远端设备的通知时进行的回调
         *
         * @param currentPackageData  当前包的数据
         * @param currentPackageCount 当前包数
         * @param packageCount        总包数
         * @param values              远端设备的通知内容
         * @return true表示可以继续下一包发送，false表示传输出错并终止本次传输
         */
        boolean onReceiveNotification(byte[] currentPackageData, int currentPackageCount, int packageCount, byte[] values);

        /**
         * 数据发送完成
         */
        void onDataSendFinished();

        /**
         * 数据发送失败
         *
         * @param currentPackageCount 当前发送失败的包数
         * @param pageCount           总包数
         * @param data                当前发送失败的数据内容
         */
        void onDataSendFailed(int currentPackageCount, int pageCount, byte[] data);

        /**
         * 数据发送失败并尝试重发
         *
         * @param currentPackageCount 当前包数
         * @param pageCount           总包数
         * @param data                当前包数据内容
         * @param tryCount            重试次数
         */
        void onDataSendFailedAndRetry(int currentPackageCount, int pageCount, byte[] data, int tryCount);

        /**
         * 数据发送进度有更改
         *
         * @param currentPackageCount 当前包数
         * @param pageCount           总包数
         * @param data                当前包数据内容
         */
        void onDataSendProgressChanged(int currentPackageCount, int pageCount, byte[] data);

        /**
         * 因为通知返回的数据出错而导致的传输失败
         */
        void onSendFailedWithWrongNotifyData();

        /**
         * 数据发送失败（通知返回数据有错误）
         *
         * @param tryCount            重试次数
         * @param currentPackageIndex 当前发送的包数
         * @param packageCount        总包数
         * @param data                当前包数据
         */
        void onSendFailedWithWrongNotifyDataAndRetry(int tryCount, int currentPackageIndex, int packageCount, byte[] data);
    }
}
