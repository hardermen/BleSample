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
import android.os.Handler;
import android.support.annotation.RequiresApi;

import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

/**
 * BLE连接器
 *
 * @author alm
 */

public class BleConnector {

    /*-------------------------静态常量-------------------------*/

    /**
     * TAG
     */
    private static final String TAG = BleConnector.class.getSimpleName();

    /**
     * 上下文弱引用
     */
    private WeakReference<Context> contextWeakReference;

    /**
     * 关闭完成时执行的回调
     */
    private BleInterface.OnCloseCompleteListener onCloseCompleteListener;

    /**
     * 服务连接工具
     */
    private BleServiceConnection bleServiceConnection;

    /**
     * Handler
     */
    private Handler handler = new Handler();

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

    /*-------------------------构造函数-------------------------*/

    /**
     * 构造函数
     *
     * @param context 上下文
     */
    BleConnector(Context context) {
        contextWeakReference = new WeakReference<>(context);
        connectBleBroadcastReceiver = new ConnectBleBroadcastReceiver();
        boundBleBroadcastReceiver = new BoundBleBroadcastReceiver();
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

    /**
     * 检查关闭状况（用于调用回调）
     */
    private void checkCloseStatus() {
        mClosed = true;
        if (onCloseCompleteListener != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    onCloseCompleteListener.onCloseComplete();
                }
            });
        }
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

    /*-------------------------公开函数-------------------------*/

    /**
     * 检查设备地址并设置地址
     *
     * @param address 设备地址
     * @return true表示成功设置地址
     */
    public boolean checkAndSetAddress(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return false;
        }
        setAddress(address);
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
    public boolean startConnect(boolean autoConnect) {
        if (bleServiceConnection == null) {
            return false;
        }

        mClosed = false;

        bleServiceConnection.setAutoConnect(autoConnect);

        //注册广播接收者
        contextWeakReference.get().registerReceiver(connectBleBroadcastReceiver, makeConnectBLEIntentFilter());
        //绑定BLE连接服务
        Intent intent = new Intent(contextWeakReference.get(), BluetoothLeService.class);
        return contextWeakReference.get().bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE);
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

        BluetoothManager bluetoothManager = (BluetoothManager) contextWeakReference.get().getSystemService(Context.BLUETOOTH_SERVICE);

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
        contextWeakReference.get().registerReceiver(boundBleBroadcastReceiver, makeBoundBLEIntentFilter());

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
     * 通过设备地址直接解绑某个设备
     *
     * @param context 上下文
     * @param address 设备地址
     * @return true表示成功解绑
     */
    public static boolean unBound(Context context, String address) {

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
     * 解除之前前发起绑定的设备之间的配对
     *
     * @return true代表成功
     */
    public boolean unBound() {
        Context context = contextWeakReference.get();
        if (context == null) {
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
        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(bondAddress);
        if (remoteDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
            return false;
        }
        Method removeBondMethod;
        boolean result = false;
        try {
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

    public BluetoothGatt getBluetoothGatt() {
        return bleServiceConnection.getBluetoothGatt();
    }

    /**
     * 关闭BLE连接工具
     *
     * @return true表示关闭成功
     */
    public boolean close() {
        if (bleServiceConnection == null) {
            return false;
        }

        if (mClosed) {
            return false;
        }

        try {
            bondAddress = null;
            contextWeakReference.get().unregisterReceiver(boundBleBroadcastReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }

        disconnect();
        bleServiceConnection.closeGatt();
        bleServiceConnection.stopService();
        contextWeakReference.get().unbindService(bleServiceConnection);
        contextWeakReference.get().unregisterReceiver(connectBleBroadcastReceiver);

        checkCloseStatus();
        return true;
    }

    /**
     * 设置连接成功的监听事件
     *
     * @param onConnectedListener 连接成功的监听事件
     */
    public void setOnConnectedListener(BleInterface.OnConnectedListener onConnectedListener) {
        connectBleBroadcastReceiver.setOnConnectedListener(onConnectedListener);
    }

    /**
     * 设置连接断开的监听事件
     *
     * @param onDisconnectedListener 连接断开的监听事件
     */
    public void setOnDisconnectedListener(BleInterface.OnDisconnectedListener onDisconnectedListener) {
        connectBleBroadcastReceiver.setOnDisconnectedListener(onDisconnectedListener);
    }

    /**
     * 设置服务发现完成的监听事件
     *
     * @param onServicesDiscoveredListener 服务发现完成的监听事件
     */
    public void setOnServicesDiscoveredListener(BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener) {
        connectBleBroadcastReceiver.setOnServicesDiscoveredListener(onServicesDiscoveredListener);
    }

    /**
     * 设置正在连接的监听事件
     *
     * @param onConnectingListener 正在连接的监听事件
     */
    public void setOnConnectingListener(BleInterface.OnConnectingListener onConnectingListener) {
        connectBleBroadcastReceiver.setOnConnectingListener(onConnectingListener);
    }

    /**
     * 设置正在断开连接的监听事件
     *
     * @param onDisconnectingListener 正在断开连接的监听事件
     */
    public void setOnDisconnectingListener(BleInterface.OnDisconnectingListener onDisconnectingListener) {
        connectBleBroadcastReceiver.setOnDisconnectingListener(onDisconnectingListener);
    }

    /**
     * 设置读到特征数据的回调
     *
     * @param onCharacteristicReadListener 读到特征数据的回调
     */
    public void setOnCharacteristicReadListener(BleInterface.OnCharacteristicReadListener onCharacteristicReadListener) {
        connectBleBroadcastReceiver.setOnCharacteristicReadListener(onCharacteristicReadListener);
    }

    /**
     * 设置收到远端设备通知数据的回调
     *
     * @param onReceiveNotificationListener 收到远端设备通知数据的回调
     */
    public void setOnReceiveNotificationListener(BleInterface.OnReceiveNotificationListener onReceiveNotificationListener) {
        connectBleBroadcastReceiver.setOnReceiveNotificationListener(onReceiveNotificationListener);
    }

    /**
     * 设置写入特征数据的回调
     *
     * @param onCharacteristicWriteListener 写入特征数据的回调
     */
    public void setOnCharacteristicWriteListener(BleInterface.OnCharacteristicWriteListener onCharacteristicWriteListener) {
        connectBleBroadcastReceiver.setOnCharacteristicWriteListener(onCharacteristicWriteListener);
    }

    /**
     * 设置读取描述符数据的回调
     *
     * @param onDescriptorReadListener 读取描述符数据的回调
     */
    public void setOnDescriptorReadListener(BleInterface.OnDescriptorReadListener onDescriptorReadListener) {
        connectBleBroadcastReceiver.setOnDescriptorReadListener(onDescriptorReadListener);
    }

    /**
     * 设置写入描述符数据的回调
     *
     * @param onDescriptorWriteListener 写入描述符数据的
     */
    public void setOnDescriptorWriteListener(BleInterface.OnDescriptorWriteListener onDescriptorWriteListener) {
        connectBleBroadcastReceiver.setOnDescriptorWriteListener(onDescriptorWriteListener);
    }

    /**
     * 设置绑定状态改变时的回调
     *
     * @param onDeviceBondStateChangedListener 绑定状态改变时的回调
     */
    public void setOnBondStateChangedListener(BleInterface.OnDeviceBondStateChangedListener onDeviceBondStateChangedListener) {
        boundBleBroadcastReceiver.setOnDeviceBondStateChangedListener(onDeviceBondStateChangedListener);
    }

    /**
     * 设置可靠数据写入完成的回调
     *
     * @param onReliableWriteCompletedListener 可靠数据写入完成的回调z
     */
    public void setOnReliableWriteCompletedListener(BleInterface.OnReliableWriteCompletedListener onReliableWriteCompletedListener) {
        connectBleBroadcastReceiver.setOnReliableWriteCompletedListener(onReliableWriteCompletedListener);
    }

    /**
     * 设置读到远端设备rssi的回调
     *
     * @param onReadRemoteRssiListener 读到远端设备rssi的回调
     */
    public void setOnReadRemoteRssiListener(BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener) {
        connectBleBroadcastReceiver.setOnReadRemoteRssiListener(onReadRemoteRssiListener);
    }

    /**
     * 设置最大传输单位被改变的回调
     *
     * @param onMtuChangedListener 最大传输单位被改变的回调
     */
    public void setOnMtuChangedListener(BleInterface.OnMtuChangedListener onMtuChangedListener) {
        connectBleBroadcastReceiver.setOnMtuChangedListener(onMtuChangedListener);
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
        return bleServiceConnection != null && bleServiceConnection.writeData(serviceUUID, characteristicUUID, value);
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
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
     * 获取上下文
     *
     * @return 上下文
     */
    public Context getContext() {
        return contextWeakReference.get();
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
}
