package com.jackiepenghe.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.RequiresApi;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * BLE连接器
 *
 * @author alm
 */

@SuppressWarnings({"unused", "WeakerAccess"})
public class BleConnector {

    /*-------------------------静态常量-------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BleConnector.class.getSimpleName();
    /**
     * 默认的连接超时
     */
    private static final long DEFAULT_TIME_OUT = 10000;
    /**
     * 数据连接传输时，每一包数据的最大长度
     */
    private static final int PACKAGE_MAX_LENGTH = 20;
    /**
     * 分包传输时，每一包传输的有效数据的最大长度
     */
    private static final int LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH = 17;
    /**
     * 大数据的最大字节长度
     */
    private static final int LARGE_DATA_MAX_LENGTH = 0xFFFF;
    /**
     * 默认的重试次数
     */
    private static final int DEFAULT_MAX_TRY_COUNT = Short.MAX_VALUE;
    /**
     * 默认的延迟时间
     */
    private static final int DEFAULT_DELAY_TIME = 20;

    /**
     * 上下文弱引用
     */
    private Context context;

    /**
     * 关闭完成时执行的回调
     */
    private BleInterface.OnCloseCompleteListener onCloseCompleteListener;

    /**
     * 服务连接工具
     */
    private BleServiceConnection bleServiceConnection;

    /**
     * BLE连接的广播接收者
     */
    private ConnectBleBroadcastReceiver connectBleBroadcastReceiver;

    /**
     * BLE绑定的广播接收者
     */
    private BoundBleBroadcastReceiver boundBleBroadcastReceiver;

    /**
     * 记录BLE连接工具是否关闭的标志
     */
    private boolean mClosed;
    /**
     * 如果发起绑定，会记录下绑定设备的地址
     */
    private String bondAddress;
    /**
     * 写入大量数据（需要收到通知）的总包数
     */
    private int writeBigDataWithNotificationPackageCount;
    /**
     * 写入大量数据（需要收到通知）的当前包数
     */
    private int writeBigDataWithNotificationCurrentPackageCount;
    /**
     * 写入大量数据（需要收到通知）的同一包数据的当前重发次数
     */
    private int writeBigDataWithNotificationTryCount;
    /**
     * 是否继续写入大量数据（需要收到通知）的标志
     */
    private boolean writeBigDataWithNotificationContinueFlag;
    /**
     * 写入大量数据（需要收到通知）时，是否收到通知消息的标志
     */
    private boolean receivedNotification;
    /**
     * 接收到错误的通知数据的次数
     */
    private int wrongNotificationResultCount;
    /**
     * 是否继续写入大量数据的标志
     */
    private boolean writeBigDataContinueFlag;
    /**
     * 是否执行过绑定指令
     */
    private boolean doBonded;
    /**
     * 连接超时的时间
     */
    private long timeOut = DEFAULT_TIME_OUT;
    /**
     * 连接超时的回调
     */
    private BleInterface.OnConnectTimeOutListener onConnectTimeOutListener;

    /*-------------------------构造函数-------------------------*/

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    BleConnector(Context context) {
        this.context = context;
        connectBleBroadcastReceiver = new ConnectBleBroadcastReceiver();
        boundBleBroadcastReceiver = new BoundBleBroadcastReceiver();
    }

    /*-------------------------公开函数-------------------------*/

    /**
     * 检查设备地址并设置地址
     *
     * @param bluetoothDevice 设备
     * @return true表示成功设置地址
     */
    public boolean checkAndSetDevice(BluetoothDevice bluetoothDevice) {
        if (bluetoothDevice == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAddress(bluetoothDevice.getAddress());
        } else {
            setDevice(bluetoothDevice);
        }
        return true;
    }

    /**
     * 通过设备地址直接解绑某个设备
     *
     * @param context 上下文
     * @param address 设备地址
     * @return true表示成功解绑
     */
    @SuppressWarnings("WeakerAccess")
    public static boolean unBound(Context context, String address) {

        if (context == null) {
            return false;
        }

        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            return false;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            return false;
        }

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);

        if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            return false;
        }

        Method removeBondMethod;
        boolean result = false;
        try {
            //noinspection JavaReflectionMemberAccess
            removeBondMethod = BluetoothDevice.class.getMethod("removeBond");
            result = (boolean) removeBondMethod.invoke(remoteDevice);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        if (result) {
            Tool.warnOut(TAG, "解除配对成功");
        } else {
            Tool.warnOut(TAG, "解除配对失败");
        }

        return result;
    }

    /**
     * 检查设备地址并设置地址
     *
     * @param address 设备地址
     * @return true表示成功设置地址
     */
    public boolean checkAndSetAddress(String address) {
        if (address == null || !BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setAddress(address);
        } else {
            setDevice(BluetoothAdapter.getDefaultAdapter().getRemoteDevice(address));
        }
        return true;
    }

    /**
     * 请求更改mtu
     *
     * @param mtu mtu值
     * @return true代表请求发起成功，执行结果在回调 onMtuChanged 中对比mtu值，来判断mtu是否真的被更改
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public boolean requestMtu(int mtu) {
        return bleServiceConnection != null && bleServiceConnection.requestMtu(mtu);
    }

    /**
     * 发起连接
     *
     * @return true表示成功发起连接
     */
    public boolean startConnect() {
        return startConnect(false);
    }

    /**
     * 发起连接
     *
     * @param autoConnect 自动连接（当连接被断开后，自动尝试重连，这是系统中蓝牙的API中自带的参数）
     * @return true表示成功发起连接
     */
    @SuppressWarnings("WeakerAccess")
    public boolean startConnect(boolean autoConnect) {
        if (bleServiceConnection == null) {
            return false;
        }

        if (context == null) {
            return false;
        }
        mClosed = false;

        bleServiceConnection.setAutoConnect(autoConnect);

        //注册广播接收者
        context.registerReceiver(connectBleBroadcastReceiver, makeConnectBLEIntentFilter());
        //绑定BLE连接服务
        Intent intent = new Intent(context, BluetoothLeService.class);
        boolean bindService = context.bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE);
        if (bindService) {
            startThreadToCheckTimeOut();
        }
        return bindService;
    }

    /**
     * 发起设备绑定
     *
     * @param address 设备地址
     * @return BleConstants中定义的常量
     * {@link BleConstants#BLUETOOTH_ADDRESS_INCORRECT} 设备地址错误
     * {@link BleConstants#BLUETOOTH_MANAGER_NULL} 没有蓝牙管理器
     * {@link BleConstants#BLUETOOTH_ADAPTER_NULL} 没有蓝牙适配器
     * {@link BleConstants#DEVICE_BOND_BONDED} 该设备已被绑定
     * {@link BleConstants#DEVICE_BOND_BONDING} 该设备正在进行绑定（或正在向该设备发起绑定）
     * {@link BleConstants#DEVICE_BOND_START_SUCCESS} 成功发起绑定请求
     * {@link BleConstants#DEVICE_BOND_START_FAILED} 发起绑定请求失败
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int startBound(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return BleConstants.BLUETOOTH_ADDRESS_INCORRECT;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            return BleConstants.BLUETOOTH_MANAGER_NULL;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            return BleConstants.BLUETOOTH_ADAPTER_NULL;
        }

        bondAddress = address;
        this.mClosed = false;

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        switch (remoteDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                return BleConstants.DEVICE_BOND_BONDED;
            case BluetoothDevice.BOND_BONDING:
                return BleConstants.DEVICE_BOND_BONDING;
            default:
                break;
        }

        //注册绑定BLE的广播接收者
        context.registerReceiver(boundBleBroadcastReceiver, makeBoundBLEIntentFilter());
        doBonded = true;
        //发起绑定
        if (remoteDevice.createBond()) {
            return BleConstants.DEVICE_BOND_START_SUCCESS;
        } else {
            return BleConstants.DEVICE_BOND_START_FAILED;
        }
    }

    /**
     * 断开连接
     *
     * @return true表示成功断开
     */
    public boolean disconnect() {
        return bleServiceConnection != null && bleServiceConnection.disconnect();
    }

    /**
     * 解除之前前发起绑定的设备之间的配对
     *
     * @return true代表成功
     */
    public boolean unBound() {
        return unBound(context, bondAddress);
    }

    public BluetoothGatt getBluetoothGatt() {
        return bleServiceConnection.getBluetoothGatt();
    }

    /**
     * 关闭BLE连接工具
     *
     * @return true表示关闭成功
     */
    public boolean close() {
        return close(false);
    }

    /**
     * 是否已经成功连接到了远端设备
     *
     * @return true表示已连接
     */
    public boolean isConnected() {
        return bleServiceConnection.isConnected();
    }

    /**
     * 是否已经成功发现了远端设备的服务
     *
     * @return true表示已经成功发现了远端设备的服务
     */
    public boolean isServiceDiscovered() {
        return bleServiceConnection.isServiceDiscovered();
    }

    /**
     * 设置连接超时的时间
     *
     * @param timeOut 连接超时的时间
     */
    public void setTimeOut(long timeOut) {
        this.timeOut = timeOut;
    }

    /**
     * 关闭BLE连接工具
     *
     * @param withGattRefresh 是否要在关闭连接之前 刷新GATT缓存
     * @return true表示关闭成功
     */
    public boolean close(boolean withGattRefresh) {
        if (bleServiceConnection == null) {
            return false;
        }

        if (mClosed) {
            return false;
        }
        mClosed = true;
        writeBigDataWithNotificationContinueFlag = false;
        writeBigDataContinueFlag = false;
        if (doBonded) {
            bondAddress = null;
            boundBleBroadcastReceiver.setOnDeviceBondStateChangedListener(null);
            context.unregisterReceiver(boundBleBroadcastReceiver);
        }

        context.unregisterReceiver(connectBleBroadcastReceiver);
        if (withGattRefresh) {
            refreshGattCache();
        }
        disconnect();
        bleServiceConnection.closeGatt();
        bleServiceConnection.stopService();
        try {
            context.unbindService(bleServiceConnection);
        } catch (Exception e) {
            e.printStackTrace();
        }
        onConnectTimeOutListener = null;
        connectBleBroadcastReceiver.setOnReceiveNotificationListener(null);
        connectBleBroadcastReceiver.setOnServicesDiscoveredListener(null);
        connectBleBroadcastReceiver.setOnStatusErrorListener(null);
        connectBleBroadcastReceiver.setOnCharacteristicReadListener(null);
        connectBleBroadcastReceiver.setOnConnectedListener(null);
        connectBleBroadcastReceiver.setOnDisconnectedListener(null);
        connectBleBroadcastReceiver.setOnDisconnectingListener(null);
        setAddress(null);
        setDevice(null);
        connectBleBroadcastReceiver.setOnBluetoothGattOptionsNotSuccessListener(null);
        connectBleBroadcastReceiver.setOnBluetoothSwitchChangedListener(null);
        boundBleBroadcastReceiver.setOnDeviceBondStateChangedListener(null);
        connectBleBroadcastReceiver.setOnConnectingListener(null);
        connectBleBroadcastReceiver.setOnDescriptorReadListener(null);
        connectBleBroadcastReceiver.setOnDescriptorWriteListener(null);
        connectBleBroadcastReceiver.setOnMtuChangedListener(null);
        connectBleBroadcastReceiver.setOnReadRemoteRssiListener(null);
        connectBleBroadcastReceiver.setOnReliableWriteCompletedListener(null);
        checkCloseStatus();
        boundBleBroadcastReceiver = null;
        connectBleBroadcastReceiver = null;
        bleServiceConnection = null;
        context = null;
        receivedNotification = false;
        wrongNotificationResultCount = 0;
        writeBigDataWithNotificationPackageCount = 0;
        writeBigDataWithNotificationCurrentPackageCount = 0;
        writeBigDataWithNotificationTryCount = 0;
        return true;
    }

    /**
     * 设置连接成功的监听事件
     *
     * @param onConnectedListener 连接成功的监听事件
     */
    public void setOnConnectedListener(BleInterface.OnConnectedListener onConnectedListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnConnectedListener(onConnectedListener);
    }

    /**
     * 设置连接断开的监听事件
     *
     * @param onDisconnectedListener 连接断开的监听事件
     */
    public void setOnDisconnectedListener(BleInterface.OnDisconnectedListener onDisconnectedListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnDisconnectedListener(onDisconnectedListener);
    }

    public void setOnStatusErrorListener(BleInterface.OnStatusErrorListener onStatusErrorListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnStatusErrorListener(onStatusErrorListener);
    }

    /**
     * 设置服务发现完成的监听事件
     *
     * @param onServicesDiscoveredListener 服务发现完成的监听事件
     */
    public void setOnServicesDiscoveredListener(BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnServicesDiscoveredListener(onServicesDiscoveredListener);
    }

    /**
     * 设置正在连接的监听事件
     *
     * @param onConnectingListener 正在连接的监听事件
     */
    public void setOnConnectingListener(BleInterface.OnConnectingListener onConnectingListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnConnectingListener(onConnectingListener);
    }

    /**
     * 设置正在断开连接的监听事件
     *
     * @param onDisconnectingListener 正在断开连接的监听事件
     */
    public void setOnDisconnectingListener(BleInterface.OnDisconnectingListener onDisconnectingListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnDisconnectingListener(onDisconnectingListener);
    }

    /**
     * 设置读到特征数据的回调
     *
     * @param onCharacteristicReadListener 读到特征数据的回调
     */
    public void setOnCharacteristicReadListener(BleInterface.OnCharacteristicReadListener onCharacteristicReadListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnCharacteristicReadListener(onCharacteristicReadListener);
    }

    /**
     * 设置收到远端设备通知数据的回调
     *
     * @param onReceiveNotificationListener 收到远端设备通知数据的回调
     */
    public void setOnReceiveNotificationListener(BleInterface.OnReceiveNotificationListener onReceiveNotificationListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnReceiveNotificationListener(onReceiveNotificationListener);
    }

    /**
     * 设置写入特征数据的回调
     *
     * @param onCharacteristicWriteListener 写入特征数据的回调
     */
    public void setOnCharacteristicWriteListener(BleInterface.OnCharacteristicWriteListener onCharacteristicWriteListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnCharacteristicWriteListener(onCharacteristicWriteListener);
    }

    /**
     * 设置连接超时的回调
     *
     * @param onConnectTimeOutListener 连接超时的回调
     */
    public void setOnConnectTimeOutListener(BleInterface.OnConnectTimeOutListener onConnectTimeOutListener) {
        this.onConnectTimeOutListener = onConnectTimeOutListener;
    }

    /**
     * 设置读取描述符数据的回调
     *
     * @param onDescriptorReadListener 读取描述符数据的回调
     */
    public void setOnDescriptorReadListener(BleInterface.OnDescriptorReadListener onDescriptorReadListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnDescriptorReadListener(onDescriptorReadListener);
    }

    /**
     * 设置写入描述符数据的回调
     *
     * @param onDescriptorWriteListener 写入描述符数据的
     */
    public void setOnDescriptorWriteListener(BleInterface.OnDescriptorWriteListener onDescriptorWriteListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnDescriptorWriteListener(onDescriptorWriteListener);
    }

    /**
     * 设置绑定状态改变时的回调
     *
     * @param onDeviceBondStateChangedListener 绑定状态改变时的回调
     */
    public void setOnDeviceBondStateChangedListener(BleInterface.OnDeviceBondStateChangedListener onDeviceBondStateChangedListener) {
        if (boundBleBroadcastReceiver == null) {
            return;
        }
        boundBleBroadcastReceiver.setOnDeviceBondStateChangedListener(onDeviceBondStateChangedListener);
    }

    /**
     * 设置可靠数据写入完成的回调
     *
     * @param onReliableWriteCompletedListener 可靠数据写入完成的回调z
     */
    public void setOnReliableWriteCompletedListener(BleInterface.OnReliableWriteCompletedListener onReliableWriteCompletedListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnReliableWriteCompletedListener(onReliableWriteCompletedListener);
    }

    /**
     * 设置读到远端设备rssi的回调
     *
     * @param onReadRemoteRssiListener 读到远端设备rssi的回调
     */
    public void setOnReadRemoteRssiListener(BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnReadRemoteRssiListener(onReadRemoteRssiListener);
    }

    /**
     * 设置最大传输单位被改变的回调
     *
     * @param onMtuChangedListener 最大传输单位被改变的回调
     */
    public void setOnMtuChangedListener(BleInterface.OnMtuChangedListener onMtuChangedListener) {
        if (connectBleBroadcastReceiver == null) {
            return;
        }
        connectBleBroadcastReceiver.setOnMtuChangedListener(onMtuChangedListener);
    }

    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid        服务UUID
     * @param characteristicUuid 特征UUID
     * @param bigData            数据
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData) {
        writeBigData(serviceUuid, characteristicUuid, bigData, true);
    }

    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid        服务UUID
     * @param characteristicUuid 特征UUID
     * @param bigData            数据
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData, boolean autoFormat) {
        writeBigData(serviceUuid, characteristicUuid, bigData, new DefaultBigDataSendStateChangedListener(), autoFormat);
    }

    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid                       服务UUID
     * @param characteristicUuid                特征UUID
     * @param bigData                           数据
     * @param onBigDataSendStateChangedListener 数据发送的相关回调
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData, BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        writeBigData(serviceUuid, characteristicUuid, bigData, DEFAULT_DELAY_TIME, onBigDataSendStateChangedListener);
    }

    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid                       服务UUID
     * @param characteristicUuid                特征UUID
     * @param bigData                           数据
     * @param onBigDataSendStateChangedListener 数据发送的相关回调
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData, BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener, boolean autoFormat) {
        writeBigData(serviceUuid, characteristicUuid, bigData, DEFAULT_DELAY_TIME, onBigDataSendStateChangedListener, autoFormat);
    }


    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid                       服务UUID
     * @param characteristicUuid                特征UUID
     * @param bigData                           大量数据
     * @param packageDelayTime                  每一包数据之间的时间间隔
     * @param onBigDataSendStateChangedListener 数据发送的相关回调
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData, int packageDelayTime, BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        writeBigData(serviceUuid, characteristicUuid, bigData, packageDelayTime, DEFAULT_MAX_TRY_COUNT, onBigDataSendStateChangedListener, true);
    }


    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid                       服务UUID
     * @param characteristicUuid                特征UUID
     * @param bigData                           大量数据
     * @param packageDelayTime                  每一包数据之间的时间间隔
     * @param onBigDataSendStateChangedListener 数据发送的相关回调
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData, int packageDelayTime, BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener, boolean autoFormat) {
        writeBigData(serviceUuid, characteristicUuid, bigData, packageDelayTime, DEFAULT_MAX_TRY_COUNT, onBigDataSendStateChangedListener, autoFormat);
    }

    /**
     * 发送大量数据到远端设备，并进行自动数据包格式化
     *
     * @param serviceUuid                       服务UUID
     * @param characteristicUuid                特征UUID
     * @param bigData                           大量数据
     * @param packageDelayTime                  每一包数据之间的时间间隔
     * @param maxTryCount                       每一包数据最大重发次数
     * @param onBigDataSendStateChangedListener 数据发送的相关回调
     */
    public void writeBigData(String serviceUuid, String characteristicUuid, byte[] bigData, int packageDelayTime, int maxTryCount, BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener, boolean autoFormat) {
        int dataLength = bigData.length;

        if (dataLength <= PACKAGE_MAX_LENGTH || dataLength > LARGE_DATA_MAX_LENGTH) {
            throw new WrongBigDataArrayException();
        }

        startThreadToWriteBigData(serviceUuid, characteristicUuid, bigData, dataLength, packageDelayTime, maxTryCount, onBigDataSendStateChangedListener, autoFormat);
    }

    /**
     * 设置close的回调
     *
     * @param onCloseCompleteListener close的回调
     */
    public void setOnCloseCompleteListener(BleInterface.OnCloseCompleteListener onCloseCompleteListener) {
        this.onCloseCompleteListener = onCloseCompleteListener;
    }

    /**
     * 写入数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param value              数据
     * @return true表示成功
     */
    public boolean writeData(String serviceUUID, String characteristicUUID, byte[] value) {
        Tool.warnOut(TAG, "bleServiceConnection == " + bleServiceConnection);
        return bleServiceConnection != null && bleServiceConnection.writeData(serviceUUID, characteristicUUID, value);
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    @SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
    public boolean refreshGattCache() {
        return bleServiceConnection != null && bleServiceConnection.refreshGattCache();
    }

    /**
     * 读取数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean readData(String serviceUUID, String characteristicUUID) {
        return bleServiceConnection != null && bleServiceConnection.readData(serviceUUID, characteristicUUID);
    }

    /**
     * 打开或关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param enable             true表示开启，false表示关闭
     * @return true表示成功
     */
    public boolean enableNotification(String serviceUUID, String characteristicUUID, @SuppressWarnings("SameParameterValue") boolean enable) {
        return bleServiceConnection != null && bleServiceConnection.enableNotification(serviceUUID, characteristicUUID, enable);
    }

    /**
     * 设置蓝牙开关状态被改变时的回调
     *
     * @param onBluetoothSwitchChangedListener 蓝牙开启时的回调
     */
    public void setOnBluetoothSwitchChangedListener(BleInterface.OnBluetoothSwitchChangedListener onBluetoothSwitchChangedListener) {
        connectBleBroadcastReceiver.setOnBluetoothSwitchChangedListener(onBluetoothSwitchChangedListener);
    }

    /**
     * 设置蓝牙GATT客户端配置出错时的回调
     *
     * @param onBluetoothGattOptionsNotSuccessListener 蓝牙GATT客户端配置出错时的回调
     */
    public void setOnBluetoothGattOptionsNotSuccessListener(BleInterface.OnBluetoothGattOptionsNotSuccessListener onBluetoothGattOptionsNotSuccessListener) {
        connectBleBroadcastReceiver.setOnBluetoothGattOptionsNotSuccessListener(onBluetoothGattOptionsNotSuccessListener);
    }

    /**
     * 获取远端设备的所有服务
     *
     * @return 远端设备的所有服务
     */
    public List<BluetoothGattService> getServices() {
        return bleServiceConnection.getServices();
    }

    /**
     * 根据UUID获取指定的服务
     *
     * @param uuid UUID
     * @return 服务
     */
    @SuppressWarnings("WeakerAccess")
    public BluetoothGattService getService(UUID uuid) {
        return bleServiceConnection.getService(uuid);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID        写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID 写入数据的特征UUID，通知的特征UUID
     * @param bigData            大数据内容
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID, byte[] bigData) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, bigData, new DefaultBigDataWriteWithNotificationSendStateChangedListener());
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID        写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID 写入数据的特征UUID，通知的特征UUID
     * @param bigData            大数据内容
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID, byte[] bigData, boolean autoFormat) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, bigData, new DefaultBigDataWriteWithNotificationSendStateChangedListener(), autoFormat);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID                                            写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID                                     写入数据的特征UUID，通知的特征UUID
     * @param bigData                                                大数据内容
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID, byte[] bigData, BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, bigData, DEFAULT_DELAY_TIME, onBigDataWriteWithNotificationSendStateChangedListener);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID                                            写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID                                     写入数据的特征UUID，通知的特征UUID
     * @param bigData                                                大数据内容
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID, byte[] bigData, BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener, boolean autoFormat) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, bigData, DEFAULT_DELAY_TIME, onBigDataWriteWithNotificationSendStateChangedListener, autoFormat);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID                                            写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID                                     写入数据的特征UUID，通知的特征UUID
     * @param bigData                                                大数据内容
     * @param packageDelayTime                                       每一包之间的发送间隔
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID,
                                                byte[] bigData, int packageDelayTime, BleInterface.
                                                        OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, bigData, packageDelayTime, DEFAULT_MAX_TRY_COUNT, onBigDataWriteWithNotificationSendStateChangedListener);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID                                            写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID                                     写入数据的特征UUID，通知的特征UUID
     * @param bigData                                                大数据内容
     * @param packageDelayTime                                       每一包之间的发送间隔
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID,
                                                byte[] bigData, int packageDelayTime, BleInterface.
                                                        OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener,
                                                boolean autoFormat) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, bigData, packageDelayTime, DEFAULT_MAX_TRY_COUNT, onBigDataWriteWithNotificationSendStateChangedListener, autoFormat);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID                                            写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID                                     写入数据的特征UUID，通知的特征UUID
     * @param bigData                                                大数据内容
     * @param packageDelayTime                                       每一包之间的发送间隔
     * @param maxTryCount                                            最大重试次数
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID,
                                                byte[] bigData, int packageDelayTime, int maxTryCount,
                                                BleInterface.
                                                        OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, serviceUUID, characteristicUUID, bigData, packageDelayTime, maxTryCount, onBigDataWriteWithNotificationSendStateChangedListener, true);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param serviceUUID                                            写入数据的服务UUID，通知的服务UUID
     * @param characteristicUUID                                     写入数据的特征UUID，通知的特征UUID
     * @param bigData                                                大数据内容
     * @param packageDelayTime                                       每一包之间的发送间隔
     * @param maxTryCount                                            最大重试次数
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String serviceUUID, String characteristicUUID,
                                                byte[] bigData, int packageDelayTime, int maxTryCount, BleInterface.
                                                        OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener, boolean autoFormat) {
        return writeBigDataWithNotification(serviceUUID, characteristicUUID, serviceUUID, characteristicUUID, bigData, packageDelayTime, maxTryCount, onBigDataWriteWithNotificationSendStateChangedListener, autoFormat);
    }

    /**
     * 传输大量数据并需要通知回调以继续发送
     *
     * @param writeDataServiceUUID                                   写入数据的服务UUID
     * @param writeDataCharacteristicUUID                            写入数据的特征UUID
     * @param notificationServiceUUID                                通知的服务UUID
     * @param notificationCharacteristicUUID                         通知的特征UUID
     * @param bigData                                                大数据内容
     * @param packageDelayTime                                       每一包之间的发送间隔
     * @param maxTryCount                                            最大重试次数
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    public boolean writeBigDataWithNotification(String writeDataServiceUUID, String
            writeDataCharacteristicUUID, String notificationServiceUUID, String
                                                        notificationCharacteristicUUID, byte[] bigData,
                                                int packageDelayTime, int maxTryCount,
                                                BleInterface.
                                                        OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener,
                                                boolean autoFormat) {
        int length = bigData.length;
        if (length <= PACKAGE_MAX_LENGTH) {
            throw new WrongBigDataArrayException();
        }
        if (!canWrite(writeDataServiceUUID, writeDataCharacteristicUUID)) {
            return false;
        }
        if (!canNotify(notificationServiceUUID, notificationCharacteristicUUID)) {
            return false;
        }
        if (!enableNotification(notificationServiceUUID, notificationCharacteristicUUID, true)) {
            return false;
        }
        startThreadToWriteBigDataWithNotification(bigData, length, maxTryCount, writeDataServiceUUID, writeDataCharacteristicUUID, notificationServiceUUID, notificationCharacteristicUUID, packageDelayTime, onBigDataWriteWithNotificationSendStateChangedListener, autoFormat);
        return true;
    }

    /**
     * 开启一个线程发送大数据
     *
     * @param bigData                                                大数据内容
     * @param dataLength                                             数据大小
     * @param maxTryCount                                            最大重试次数
     * @param writeDataServiceUUID                                   写入数据的服务UUID
     * @param writeDataCharacteristicUUID                            写入数据的特征UUID
     * @param notificationServiceUUID                                通知的服务UUID
     * @param notificationCharacteristicUUID                         通知的特征UUID
     * @param packageDelayTime                                       每一包之间的发送间隔
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关回调
     */
    private void startThreadToWriteBigDataWithNotification(final byte[] bigData,
                                                           final int dataLength, final int maxTryCount, final String writeDataServiceUUID,
                                                           final String writeDataCharacteristicUUID, String notificationServiceUUID,
                                                           final String notificationCharacteristicUUID, final int packageDelayTime,
                                                           final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener, final boolean autoFormat) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                boolean first = true;
                writeBigDataWithNotificationPackageCount = getPageCount(dataLength, autoFormat);
                writeBigDataWithNotificationCurrentPackageCount = 0;
                //记录数据重发的次数
                writeBigDataWithNotificationTryCount = 0;
                writeBigDataWithNotificationContinueFlag = true;
                setBigDataNotifyListener(notificationCharacteristicUUID, onBigDataWriteWithNotificationSendStateChangedListener, bigData, autoFormat, maxTryCount);
                receivedNotification = true;
                while (writeBigDataWithNotificationContinueFlag) {
                    if (!receivedNotification) {
                        continue;
                    }
                    final byte[] data = getBigPackageData(writeBigDataWithNotificationCurrentPackageCount, bigData, writeBigDataWithNotificationPackageCount, autoFormat);
                    if (data == null) {
                        performBigDataWriteWithNotificationSendFinishedListener(onBigDataWriteWithNotificationSendStateChangedListener);
                        break;
                    }
                    if (writeBigDataWithNotificationTryCount >= maxTryCount) {
                        performBigDataWriteWithNotificationSendFailedListener(writeBigDataWithNotificationPackageCount, data, writeBigDataWithNotificationCurrentPackageCount + 1, onBigDataWriteWithNotificationSendStateChangedListener);
                        break;
                    }
                    if (writeData(writeDataServiceUUID, writeDataCharacteristicUUID, data)) {
                        performBigDataWriteWithNotificationSendProgressChangedListener(writeBigDataWithNotificationPackageCount, data, writeBigDataWithNotificationCurrentPackageCount + 1, onBigDataWriteWithNotificationSendStateChangedListener);
                        writeBigDataWithNotificationTryCount = 0;
                        writeBigDataWithNotificationCurrentPackageCount++;
                        receivedNotification = false;
                        Tool.warnOut(TAG, "writeData success");
                    } else {
                        performBigDataWriteWithNotificationSendFailedAndRetryListener(writeBigDataWithNotificationPackageCount, data, writeBigDataWithNotificationTryCount, writeBigDataWithNotificationCurrentPackageCount + 1, onBigDataWriteWithNotificationSendStateChangedListener);
                        writeBigDataWithNotificationTryCount++;
                        Tool.warnOut(TAG, "writeData failed");
                    }
                    Tool.warnOut(TAG, "packageDelayTime = " + packageDelayTime);
                    sleepTime(packageDelayTime);
                }
                setOnReceiveNotificationListener(null);
            }
        };
        BleManager.getThreadFactory().newThread(runnable).start();
    }

    /**
     * 让线程睡眠一段时间
     *
     * @param packageDelayTime 线程睡眠的时间
     */
    private void sleepTime(int packageDelayTime) {
        if (packageDelayTime >= 20 && packageDelayTime <= 2000) {
            Tool.sleep(packageDelayTime);
        } else {
            if (packageDelayTime < 20) {
                Tool.sleep(20);
            } else {
                Tool.sleep(2000);
            }
        }
    }

    private void setBigDataNotifyListener(final String notificationCharacteristicUUID, final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener, final byte[] bigData, final boolean autoFormat, final int maxTryCount) {
        BleInterface.OnReceiveNotificationListener writeBigDataWithNotificationOnReceiveNotificationListener = new BleInterface.OnReceiveNotificationListener() {
            @Override
            public void onReceiveNotification(String uuid, final byte[] values) {
                if (uuid.equalsIgnoreCase(notificationCharacteristicUUID)) {
                    BleManager.getHandler().post(new Runnable() {
                        @Override
                        public void run() {
                            if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                                boolean result = onBigDataWriteWithNotificationSendStateChangedListener.onReceiveNotification(values, writeBigDataWithNotificationCurrentPackageCount + 1, writeBigDataWithNotificationPackageCount, getBigPackageData(writeBigDataWithNotificationCurrentPackageCount, bigData, writeBigDataWithNotificationPackageCount, autoFormat));
                                Tool.warnOut(TAG, "onBigDataWriteWithNotificationSendStateChangedListener onReceiveNotification result = " + result);
                                if (!result) {
                                    if (wrongNotificationResultCount >= maxTryCount) {
                                        performBigDataWriteWithNotificationSendFailedWithWrongNotifyDataListener(onBigDataWriteWithNotificationSendStateChangedListener);
                                        writeBigDataWithNotificationContinueFlag = false;
                                    } else {
                                        writeBigDataWithNotificationCurrentPackageCount--;
                                        wrongNotificationResultCount++;
                                        performBigDataWriteWithNotificationSendFailedWithWrongNotifyDataAndRetryListener(onBigDataWriteWithNotificationSendStateChangedListener, wrongNotificationResultCount, writeBigDataWithNotificationCurrentPackageCount + 1, writeBigDataWithNotificationPackageCount, getBigPackageData(writeBigDataWithNotificationCurrentPackageCount, bigData, writeBigDataWithNotificationPackageCount, autoFormat));
                                    }
                                } else {
                                    wrongNotificationResultCount = 0;
                                }
                            }
                            receivedNotification = true;
                        }
                    });
                }
            }
        };
        setOnReceiveNotificationListener(writeBigDataWithNotificationOnReceiveNotificationListener);
    }

    /**
     * 获取上下文
     *
     * @return 上下文
     */
    public Context getContext() {
        return context;
    }

    /**
     * 检查特征是否支持通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持通知
     */
    public boolean canNotify(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_NOTIFY) != 0;
    }

    /**
     * 检查特征是否支持读取
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持读取
     */
    public boolean canRead(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_READ) != 0;
    }

    /**
     * 检查特征是否支持写入（带符号）
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持写入（带符号）
     */
    public boolean canSignedWrite(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_SIGNED_WRITE) != 0;
    }

    /**
     * 检查特征是否支持写入
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持写入
     */
    public boolean canWrite(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_WRITE) != 0;
    }

    /**
     * 检查特征是否支持写入（无回复方式）
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示支持写入（无回复方式）
     */
    public boolean canWriteNoResponse(String serviceUUID, String characteristicUUID) {
        BluetoothGattService service = getService(UUID.fromString(serviceUUID));
        if (service == null) {
            return false;
        }
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(UUID.fromString(characteristicUUID));
        if (characteristic == null) {
            return false;
        }

        int properties = characteristic.getProperties();
        return (properties & BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE) != 0;
    }

    /*-------------------------私有函数-------------------------*/

    /**
     * 设置连接地址
     *
     * @param address 连接地址
     */
    private void setAddress(String address) {
        //将地址传入服务连接工具并初始化
        bleServiceConnection = new BleServiceConnection(address);
    }

    private void setDevice(BluetoothDevice bluetoothDevice) {
        bleServiceConnection = new BleServiceConnection(bluetoothDevice);
    }

    /**
     * 检查关闭状况（用于调用回调）
     */
    private void checkCloseStatus() {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onCloseCompleteListener != null) {
                    onCloseCompleteListener.onCloseComplete();
                    onCloseCompleteListener = null;
                }
            }
        });
    }

    /**
     * 广播接收者Action过滤器
     *
     * @return 接收者Action过滤器
     */
    private IntentFilter makeConnectBLEIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleConstants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleConstants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BleConstants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleConstants.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BleConstants.ACTION_GATT_DISCONNECTING);
        intentFilter.addAction(BleConstants.ACTION_CHARACTERISTIC_READ);
        intentFilter.addAction(BleConstants.ACTION_CHARACTERISTIC_CHANGED);
        intentFilter.addAction(BleConstants.ACTION_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BleConstants.ACTION_DESCRIPTOR_READ);
        intentFilter.addAction(BleConstants.ACTION_DESCRIPTOR_WRITE);
        intentFilter.addAction(BleConstants.ACTION_RELIABLE_WRITE_COMPLETED);
        intentFilter.addAction(BleConstants.ACTION_READ_REMOTE_RSSI);
        intentFilter.addAction(BleConstants.ACTION_MTU_CHANGED);
        intentFilter.addAction(BleConstants.ACTION_GATT_NOT_SUCCESS);
        intentFilter.addAction(BleConstants.ACTION_GATT_DISCOVER_SERVICES_FAILED);
        intentFilter.addAction(BleConstants.ACTION_GATT_STATUS_ERROR);
        intentFilter.setPriority(Integer.MAX_VALUE);
        return intentFilter;
    }

    /**
     * 广播接收者Action过滤器
     *
     * @return 接收者Action过滤器
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private IntentFilter makeBoundBLEIntentFilter() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        return intentFilter;
    }

    /**
     * 获取分包传输时，要传输的总包数
     *
     * @param dataLength 有效数据的总长度
     * @param autoFormat 是否有自动格式化
     */
    private int getPageCount(int dataLength, boolean autoFormat) {

        if (autoFormat) {
            if (dataLength % LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH == 0) {
                return dataLength / LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH;
            } else {
                return (dataLength / LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH) + 1;
            }
        } else {
            if (dataLength % PACKAGE_MAX_LENGTH == 0) {
                return dataLength / PACKAGE_MAX_LENGTH;
            } else {
//                PACKAGE_MAX_LENGTH
                return (dataLength / PACKAGE_MAX_LENGTH) + 1;
            }
        }
    }

    /**
     * 根据当前需要第几包，获取对应的数据包内容
     *
     * @param packageIndex 当前需要第几包
     * @param bigData      整个大型数据
     * @param pageCount    总包数
     * @param autoFormat   是否自动格式化
     * @return byte数组
     */
    private byte[] getBigPackageData(int packageIndex, byte[] bigData, int pageCount, boolean autoFormat) {
        if (packageIndex >= pageCount) {
            return null;
        }
        int largeDataLength = bigData.length;
        if (autoFormat) {
            if (packageIndex == pageCount - 1) {
                int remainder = largeDataLength % LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH;
                if (remainder == 0) {
                    byte[] data = new byte[20];
                    data[0] = (byte) pageCount;
                    data[1] = (byte) (packageIndex + 1);
                    data[2] = LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH;
                    System.arraycopy(bigData, packageIndex * LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH, data, PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH, data.length - (PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH));
                    return data;
                } else {
                    byte[] data = new byte[remainder + PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH];
                    byte[] bytes = Tool.intToBytes2(largeDataLength);
                    data[0] = (byte) pageCount;
                    data[1] = (byte) (packageIndex + 1);
                    data[2] = (byte) remainder;
                    System.arraycopy(bigData, packageIndex * LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH, data, PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH, data.length - (PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH));
                    return data;
                }
            } else {
                byte[] data = new byte[20];
                byte[] bytes = Tool.intToBytes2(largeDataLength);
                data[0] = (byte) pageCount;
                data[1] = (byte) (packageIndex + 1);
                data[2] = (byte) LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH;
                System.arraycopy(bigData, packageIndex * LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH, data, PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH, data.length - (PACKAGE_MAX_LENGTH - LARGE_DATA_AUTO_FORMAT_TRANSFORM_PACKAGE_MAX_LENGTH));
                return data;
            }
        } else {
            if (packageIndex == pageCount - 1) {
                int remainder = largeDataLength % PACKAGE_MAX_LENGTH;
                if (remainder == 0) {
                    byte[] data = new byte[20];
                    System.arraycopy(bigData, packageIndex * PACKAGE_MAX_LENGTH, data, 0, data.length);
                    return data;
                } else {
                    byte[] data = new byte[remainder];
                    System.arraycopy(bigData, packageIndex * PACKAGE_MAX_LENGTH, data, 0, data.length);
                    return data;
                }
            } else {
                byte[] data = new byte[20];
                System.arraycopy(bigData, packageIndex * PACKAGE_MAX_LENGTH, data, 0, data.length);
                return data;
            }
        }
    }

    /**
     * 发起一个线程开始进行数据传输
     *
     * @param serviceUuid                       服务UUID
     * @param characteristicUuid                特征UUID
     * @param bigData                           大数据内容
     * @param dataLength                        数据长度
     * @param packageDelayTime                  每一包数据之间的间隔时间
     * @param maxTryCount                       最大的重发次数
     * @param onBigDataSendStateChangedListener 大数据传输时的相关回调
     * @param autoFormat                        是否自动格式化数据包
     */
    private void startThreadToWriteBigData(final String serviceUuid,
                                           final String characteristicUuid, final byte[] bigData, final int dataLength,
                                           final int packageDelayTime, final int maxTryCount,
                                           final BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener, final boolean autoFormat) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                final int pageCount = getPageCount(dataLength, autoFormat);
                int currentPackageCount = 0;
                //记录数据重发的次数
                int tryCount = 0;
                performBigDataSendStartedListener(onBigDataSendStateChangedListener);
                writeBigDataContinueFlag = true;
                while (writeBigDataContinueFlag) {
                    final byte[] data = getBigPackageData(currentPackageCount, bigData, pageCount, autoFormat);
                    if (data == null) {
                        performBigDataSendFinishedListener(onBigDataSendStateChangedListener);
                        break;
                    }
                    if (tryCount >= maxTryCount) {
                        performBigDataSendFailedListener(pageCount, data, currentPackageCount, onBigDataSendStateChangedListener);
                        break;
                    }
                    if (writeData(serviceUuid, characteristicUuid, data)) {
                        performBigDataSendProgressChangedListener(pageCount, data, currentPackageCount, onBigDataSendStateChangedListener);
                        tryCount = 0;
                        currentPackageCount++;
                    } else {
                        performBigDataSendFailedAndRetryListener(pageCount, data, tryCount, currentPackageCount, onBigDataSendStateChangedListener);
                        tryCount++;
                    }
                    sleepTime(packageDelayTime);
                }

            }
        };
        BleManager.getThreadFactory().newThread(runnable).start();
    }

    /**
     * 触发大量数据传输进度更新时进行的回调
     *
     * @param pageCount                         总包数
     * @param data                              当前包数据
     * @param currentPackageCount               当前包数
     * @param onBigDataSendStateChangedListener 相关的回调
     */
    private void performBigDataSendProgressChangedListener(final int pageCount, final byte[] data, final int currentPackageCount,
                                                           final BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataSendStateChangedListener != null) {
                    onBigDataSendStateChangedListener.packageSendProgressChanged(currentPackageCount + 1, pageCount, data);
                }
            }
        });
    }

    /**
     * 触发大量数据传输时失败，尝试重传时的回调
     *
     * @param pageCount                         总包数
     * @param data                              当前包数据
     * @param tryCount                          尝试次数
     * @param currentPackageCount               当前包数
     * @param onBigDataSendStateChangedListener 相关的回调
     */
    private void performBigDataSendFailedAndRetryListener(final int pageCount, final byte[] data,
                                                          final int tryCount, final int currentPackageCount,
                                                          final BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataSendStateChangedListener != null) {
                    onBigDataSendStateChangedListener.packageSendFailedAndRetry(currentPackageCount + 1, pageCount, tryCount, data);
                }
            }
        });
    }

    /**
     * 触发大量数据传输时失败时的回调
     *
     * @param pageCount                         总包数
     * @param data                              当前包数据
     * @param currentPackageCount               当前包数
     * @param onBigDataSendStateChangedListener 相关的回调
     */
    private void performBigDataSendFailedListener(final int pageCount, final byte[] data, final int currentPackageCount,
                                                  final BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataSendStateChangedListener != null) {
                    onBigDataSendStateChangedListener.packageSendFailed(currentPackageCount + 1, pageCount, data);
                }
            }
        });
    }

    /**
     * 触发大量数据传输时完成时的回调
     *
     * @param onBigDataSendStateChangedListener 相关的回调
     */
    private void performBigDataSendFinishedListener(final BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataSendStateChangedListener != null) {
                    onBigDataSendStateChangedListener.sendFinished();
                }
            }
        });
    }

    /**
     * 触发大量数据开始传输时进行的回调
     *
     * @param onBigDataSendStateChangedListener 相关的回调
     */
    private void performBigDataSendStartedListener(final BleInterface.OnBigDataSendStateChangedListener onBigDataSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataSendStateChangedListener != null) {
                    onBigDataSendStateChangedListener.sendStarted();
                }
            }
        });
    }

    /**
     * 触发写入大量数据并包含通知时，数据发送完成的回调
     *
     * @param onBigDataWriteWithNotificationSendStateChangedListener 写入大数据并包含通知 到远端设备时相关的回调
     */
    private void performBigDataWriteWithNotificationSendFinishedListener(
            final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                    onBigDataWriteWithNotificationSendStateChangedListener.onDataSendFinished();
                }
            }
        });
    }

    /**
     * 触发写入大量数据并包含通知时，数据传输失败的回调
     *
     * @param pageCount                                              数据总包数
     * @param data                                                   传输失败的当前包内容
     * @param currentPackageCount                                    当前传输的包数
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关的回调
     */
    private void performBigDataWriteWithNotificationSendFailedListener(final int pageCount,
                                                                       final byte[] data, final int currentPackageCount,
                                                                       final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                    onBigDataWriteWithNotificationSendStateChangedListener.onDataSendFailed(currentPackageCount, pageCount, data);
                }
            }
        });
    }

    /**
     * 触发写入大量数据并包含通知时，数据传输失败并尝试重发的回调
     *
     * @param pageCount                                              数据总包数
     * @param data                                                   传输失败的当前包内容
     * @param tryCount                                               本包重试次数
     * @param currentPackageCount                                    当前传输的包数
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关的回调
     */
    private void performBigDataWriteWithNotificationSendFailedAndRetryListener(
            final int pageCount, final byte[] data, final int tryCount, final int currentPackageCount,
            final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                    onBigDataWriteWithNotificationSendStateChangedListener.onDataSendFailedAndRetry(currentPackageCount, pageCount, data, tryCount);
                }
            }
        });
    }

    /**
     * 触发写入大量数据并包含通知时，数据传输进度更改时进行的回调的回调
     *
     * @param pageCount                                              数据总包数
     * @param data                                                   传输失败的当前包内容
     * @param currentPackageCount                                    当前传输的包数
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关的回调
     */
    private void performBigDataWriteWithNotificationSendProgressChangedListener(
            final int pageCount, final byte[] data, final int currentPackageCount,
            final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                    onBigDataWriteWithNotificationSendStateChangedListener.onDataSendProgressChanged(currentPackageCount, pageCount, data);
                }
            }
        });
    }

    /**
     * 触发写入大量数据并包含通知时,通知数据异常导致传输结束的回调
     *
     * @param onBigDataWriteWithNotificationSendStateChangedListener 相关的回调
     */
    private void performBigDataWriteWithNotificationSendFailedWithWrongNotifyDataListener(final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                    onBigDataWriteWithNotificationSendStateChangedListener.onSendFailedWithWrongNotifyData();
                }
            }
        });
    }

    private void performBigDataWriteWithNotificationSendFailedWithWrongNotifyDataAndRetryListener(final BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener onBigDataWriteWithNotificationSendStateChangedListener, final int tryCount, final int currentPackageIndex, final int packageCount, final byte[] data) {
        BleManager.getHandler().post(new Runnable() {
            @Override
            public void run() {
                if (onBigDataWriteWithNotificationSendStateChangedListener != null) {
                    onBigDataWriteWithNotificationSendStateChangedListener.onSendFailedWithWrongNotifyDataAndRetry(tryCount, currentPackageIndex, packageCount, data);
                }
            }
        });
    }

    private void startThreadToCheckTimeOut() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                while (!mClosed) {
                    if (System.currentTimeMillis() - currentTimeMillis > timeOut) {
                        break;
                    }
                    if (bleServiceConnection.isConnected() && bleServiceConnection.isServiceDiscovered()) {
                        break;
                    }
                }
                if (bleServiceConnection.isConnected() && bleServiceConnection.isServiceDiscovered()) {
                    return;
                }
                checkTimeOut();
            }
        };
        BleManager.getThreadFactory().newThread(runnable).start();
    }

    /**
     * 检查超时的回调
     */
    private void checkTimeOut() {
        if (mClosed) {
            return;
        }
        if (!bleServiceConnection.isConnected() || !bleServiceConnection.isServiceDiscovered()) {
            BleManager.getHandler().post(new Runnable() {
                @Override
                public void run() {
                    if (onConnectTimeOutListener != null) {
                        onConnectTimeOutListener.onConnectTimeOut();
                    }
                }
            });
        }
    }
}
