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
 * Created by alm on 17-6-5.
 * BLE连接器
 */

public class BleConnector {

    /**
     * 地址长度
     */
    private int ADDRESS_LENGTH = 17;

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
    private BLEServiceConnection bleServiceConnection;

    /**
     * BLE连接的广播接收者
     */
    private ConnectBLEBroadcastReceiver connectBLEBroadcastReceiver;

    /**
     * BLE绑定的广播接收者
     */
    private BoundBLEBroadcastReceiver boundBLEBroadcastReceiver;

    /**
     * 记录BLE连接工具是否关闭的标志
     */
    private boolean mClosed;
    private CloseTask closeTask;

    public BleConnector(Context context){
        contextWeakReference = new WeakReference<>(context);
        connectBLEBroadcastReceiver = new ConnectBLEBroadcastReceiver();
        boundBLEBroadcastReceiver = new BoundBLEBroadcastReceiver();
        closeTask = new CloseTask(BleConnector.this);
    }

    /**
     * 设置连接地址
     *
     * @param address 连接地址
     */
    private void setAddress(String address) {
        //初始化服务连接工具
        bleServiceConnection = new BLEServiceConnection(address);
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
        if (bleServiceConnection == null) {
            return false;
        }

        mClosed = false;

        //注册广播接收者
        contextWeakReference.get().registerReceiver(connectBLEBroadcastReceiver, makeConnectBLEIntentFilter());

        //绑定BLE连接服务
        Intent intent = new Intent(contextWeakReference.get(), BluetoothLeService.class);
        return contextWeakReference.get().bindService(intent, bleServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public int startBound(String address) {
        if (!BluetoothAdapter.checkBluetoothAddress(address)) {
            return BLEConstants.BLUETOOTH_ADDRESS_INCORRECT;
        }

        BluetoothManager bluetoothManager = (BluetoothManager) contextWeakReference.get().getSystemService(Context.BLUETOOTH_SERVICE);

        if (bluetoothManager == null) {
            return BLEConstants.BLUETOOTH_MANAGER_NULL;
        }

        BluetoothAdapter bluetoothAdapter = bluetoothManager.getAdapter();

        if (bluetoothAdapter == null) {
            return BLEConstants.BLUETOOTH_ADAPTER_NULL;
        }

        BluetoothDevice remoteDevice = bluetoothAdapter.getRemoteDevice(address);
        switch (remoteDevice.getBondState()) {
            case BluetoothDevice.BOND_BONDED:
                return BLEConstants.DEVICE_BOND_BONDED;
            case BluetoothDevice.BOND_BONDING:
                return BLEConstants.DEVICE_BOND_BONDING;
        }

        doBonded = true;
        //注册绑定BLE的广播接收者
        contextWeakReference.get().registerReceiver(boundBLEBroadcastReceiver, makeBoundBLEIntentFilter());

        //发起绑定
        if (remoteDevice.createBond()) {
            return BLEConstants.DEVICE_BOND_START_SUCCESS;
        } else {
            return BLEConstants.DEVICE_BOND_START_FAILED;
        }
    }

    /**
     * 断开连接
     *
     * @return true表示成功断开
     */
    private boolean disconnect() {
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
        contextWeakReference.get().unregisterReceiver(connectBLEBroadcastReceiver);
        if (doBonded) {
            contextWeakReference.get().unregisterReceiver(boundBLEBroadcastReceiver);
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
        connectBLEBroadcastReceiver.setOnConnectedListener(onConnectedListener);
    }

    /**
     * 设置连接断开的监听事件
     *
     * @param onDisconnectedListener 连接断开的监听事件
     */
    public void setOnDisconnectedListener(BleInterface.OnDisconnectedListener onDisconnectedListener) {
        connectBLEBroadcastReceiver.setOnDisconnectedListener(onDisconnectedListener);
    }

    /**
     * 设置服务发现完成的监听事件
     *
     * @param onServicesDiscoveredListener 服务发现完成的监听事件
     */
    public void setOnServicesDiscoveredListener(BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener) {
        connectBLEBroadcastReceiver.setOnServicesDiscoveredListener(onServicesDiscoveredListener);
    }

    /**
     * 设置正在连接的监听事件
     *
     * @param onConnectingListener 正在连接的监听事件
     */
    public void setOnConnectingListener(BleInterface.OnConnectingListener onConnectingListener) {
        connectBLEBroadcastReceiver.setOnConnectingListener(onConnectingListener);
    }

    /**
     * 设置正在断开连接的监听事件
     *
     * @param onDisconnectingListener 正在断开连接的监听事件
     */
    public void setOnDisconnectingListener(BleInterface.OnDisconnectingListener onDisconnectingListener) {
        connectBLEBroadcastReceiver.setOnDisconnectingListener(onDisconnectingListener);
    }

    /**
     * 设置读到特征数据的回调
     *
     * @param onCharacteristicReadListener 读到特征数据的回调
     */
    public void setOnCharacteristicReadListener(BleInterface.OnCharacteristicReadListener onCharacteristicReadListener) {
        connectBLEBroadcastReceiver.setOnCharacteristicReadListener(onCharacteristicReadListener);
    }

    /**
     * 设置特征被改变的回调
     *
     * @param onReceiveNotificationListener 特征被改变的回调
     */
    public void setOnReceiveNotificationListener(BleInterface.OnReceiveNotificationListener onReceiveNotificationListener) {
        connectBLEBroadcastReceiver.setOnReceiveNotificationListener(onReceiveNotificationListener);
    }

    /**
     * 设置写入特征数据的回调
     *
     * @param onCharacteristicWriteListener 写入特征数据的回调
     */
    public void setOnCharacteristicWriteListener(BleInterface.OnCharacteristicWriteListener onCharacteristicWriteListener) {
        connectBLEBroadcastReceiver.setOnCharacteristicWriteListener(onCharacteristicWriteListener);
    }

    /**
     * 设置读取描述符数据的回调
     *
     * @param onDescriptorReadListener 读取描述符数据的回调
     */
    public void setOnDescriptorReadListener(BleInterface.OnDescriptorReadListener onDescriptorReadListener) {
        connectBLEBroadcastReceiver.setOnDescriptorReadListener(onDescriptorReadListener);
    }

    /**
     * 设置写入描述符数据的回调
     *
     * @param onDescriptorWriteListener 写入描述符数据的回调
     */
    public void setOnDescriptorWriteListener(BleInterface.OnDescriptorWriteListener onDescriptorWriteListener) {
        connectBLEBroadcastReceiver.setOnDescriptorWriteListener(onDescriptorWriteListener);
    }

    public void setOnBondStateChangedListener(BleInterface.OnDeviceBondStateChangedListener onDeviceBondStateChangedListener) {
        boundBLEBroadcastReceiver.setOnDeviceBondStateChangedListener(onDeviceBondStateChangedListener);
    }

    /**
     * 设置可靠数据写入完成的回调
     *
     * @param onReliableWriteCompletedListener 可靠数据写入完成的回调
     */
    public void setOnReliableWriteCompletedListener(BleInterface.OnReliableWriteCompletedListener onReliableWriteCompletedListener) {
        connectBLEBroadcastReceiver.setOnReliableWriteCompletedListener(onReliableWriteCompletedListener);
    }

    /**
     * 设置读到远端设备rssi的回调
     *
     * @param onReadRemoteRssiListener 读到远端设备rssi的回调
     */
    public void setOnReadRemoteRssiListener(BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener) {
        connectBLEBroadcastReceiver.setOnReadRemoteRssiListener(onReadRemoteRssiListener);
    }

    /**
     * 设置最大传输单位被改变的回调
     *
     * @param onMtuChangedListener 最大传输单位被改变的回调
     */
    public void setOnMtuChangedListener(BleInterface.OnMtuChangedListener onMtuChangedListener) {
        connectBLEBroadcastReceiver.setOnMtuChangedListener(onMtuChangedListener);
    }

    public void  setOnCloseCompleteListener(BleInterface.OnCloseCompleteListener onCloseCompleteListener){
        closeTask.setOnCloseCompleteListener(onCloseCompleteListener);
    }

    /**
     * 广播接收者Action过滤器

     * @return 接收者Action过滤器
     */
    private IntentFilter makeConnectBLEIntentFilter()    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLEConstants.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLEConstants.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BLEConstants.ACTION_GATT_DISCONNECTING);
        intentFilter.addAction(BLEConstants.ACTION_CHARACTERISTIC_READ);
        intentFilter.addAction(BLEConstants.ACTION_CHARACTERISTIC_CHANGED);
        intentFilter.addAction(BLEConstants.ACTION_CHARACTERISTIC_WRITE);
        intentFilter.addAction(BLEConstants.ACTION_DESCRIPTOR_READ);
        intentFilter.addAction(BLEConstants.ACTION_DESCRIPTOR_WRITE);
        intentFilter.addAction(BLEConstants.ACTION_RELIABLE_WRITE_COMPLETED);
        intentFilter.addAction(BLEConstants.ACTION_READ_REMOTE_RSSI);
        intentFilter.addAction(BLEConstants.ACTION_MTU_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        return intentFilter;
    }

    /**
     * 广播接收者Action过滤器

     * @return 接收者Action过滤器
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private IntentFilter makeBoundBLEIntentFilter()  {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_PAIRING_REQUEST);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.setPriority(Integer.MAX_VALUE);
        return intentFilter;
    }

    void setClosed(boolean Closed) {
        mClosed = Closed;
    }

    public List<BluetoothGattService> getServices(){
        return bleServiceConnection.getServices();
    }
}
