package cn.almsound.www.blelibrary;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;

import java.lang.ref.WeakReference;
import java.util.List;

/**
 * @author alm
 *         Created by alm on 17-6-5.
 *         BLE连接器
 */

@SuppressWarnings("unused")
public class BleConnector {

    /**
     * 地址长度
     */
    private static final int ADDRESS_LENGTH = 17;

    /**
     * 记录是否执行过绑定操作的标志
     */
    private boolean doBonded;

    /**
     * 上下文弱引用
     */
    private WeakReference<Context> contextWeakReference;

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
    private CloseTask closeTask;
    private boolean autoReconnect;

    public BleConnector(Context context) {
        contextWeakReference = new WeakReference<>(context);
        connectBleBroadcastReceiver = new ConnectBleBroadcastReceiver();
        boundBleBroadcastReceiver = new BoundBleBroadcastReceiver();
        closeTask = new CloseTask(BleConnector.this);
    }

    /**
     * 设置连接地址
     *
     * @param address 连接地址
     */
    private void setAddress(String address) {
        //初始化服务连接工具
        bleServiceConnection = new BleServiceConnection(address);
    }

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
     * 发起连接
     *
     * @return true表示开始连接
     */
    public boolean startConnect() {
        return startConnect(false);
    }

    /**
     * 发起连接
     *
     * @return true表示开始连接
     */
    @SuppressWarnings({"SameParameterValue", "WeakerAccess"})
    public boolean startConnect(boolean autoReconnect) {
        if (bleServiceConnection == null) {
            return false;
        }

        bleServiceConnection.setAutoReconnect(autoReconnect);

        mClosed = false;

        //注册广播接收者
        contextWeakReference.get().registerReceiver(connectBleBroadcastReceiver, makeConnectBLEIntentFilter());
        //绑定BLE连接服务
        Intent intent = new Intent(contextWeakReference.get(), BluetoothLeService.class);
        return contextWeakReference.get().bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE);
    }

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

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        switch (remoteDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                return BleConstants.DEVICE_BOND_BONDED;
            case BluetoothDevice.BOND_BONDING:
                return BleConstants.DEVICE_BOND_BONDING;
            default:
                break;
        }

        doBonded = true;
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
    @SuppressWarnings("UnusedReturnValue")
    private boolean disconnect() {
        bleServiceConnection.setAutoReconnect(false);
        return bleServiceConnection != null && bleServiceConnection.disconnect();
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

        disconnect();
        bleServiceConnection.closeGatt();
        bleServiceConnection.stopService();
        contextWeakReference.get().unbindService(bleServiceConnection);
        contextWeakReference.get().unregisterReceiver(connectBleBroadcastReceiver);
        if (doBonded) {
            contextWeakReference.get().unregisterReceiver(boundBleBroadcastReceiver);
        }
        checkCloseStatus();
        return true;
    }

    private void checkCloseStatus() {
        closeTask.execute();
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
     * 设置特征被改变的回调
     *
     * @param onReceiveNotificationListener 特征被改变的回调
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
     * @param onDescriptorWriteListener 写入描述符数据的回调
     */
    public void setOnDescriptorWriteListener(BleInterface.OnDescriptorWriteListener onDescriptorWriteListener) {
        connectBleBroadcastReceiver.setOnDescriptorWriteListener(onDescriptorWriteListener);
    }

    public void setOnBondStateChangedListener(BleInterface.OnDeviceBondStateChangedListener onDeviceBondStateChangedListener) {
        boundBleBroadcastReceiver.setOnDeviceBondStateChangedListener(onDeviceBondStateChangedListener);
    }

    /**
     * 设置可靠数据写入完成的回调
     *
     * @param onReliableWriteCompletedListener 可靠数据写入完成的回调
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

    public void setOnCloseCompleteListener(BleInterface.OnCloseCompleteListener onCloseCompleteListener) {
        closeTask.setOnCloseCompleteListener(onCloseCompleteListener);
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
     * 打开通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean openNotification(String serviceUUID, String characteristicUUID) {
        return bleServiceConnection != null && bleServiceConnection.openNotification(serviceUUID, characteristicUUID);
    }

    /**
     * 关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean closeNotification(String serviceUUID, String characteristicUUID) {
        return bleServiceConnection != null && bleServiceConnection.closeNotification(serviceUUID, characteristicUUID);
    }

    public void setOnBluetoothOpenListener(BleInterface.OnBluetoothOpenListener onBluetoothOpenListener) {
        connectBleBroadcastReceiver.setOnBluetoothOpenListener(onBluetoothOpenListener);
    }

    public void setOnBluetoothCloseListener(BleInterface.OnBluetoothCloseListener onBluetoothCloseListener) {
        connectBleBroadcastReceiver.setOnBluetoothCloseListener(onBluetoothCloseListener);
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

    @SuppressWarnings("SameParameterValue")
    void setClosed(boolean closed) {
        mClosed = closed;
    }

    public List<BluetoothGattService> getServices() {
        return bleServiceConnection.getServices();
    }
}
