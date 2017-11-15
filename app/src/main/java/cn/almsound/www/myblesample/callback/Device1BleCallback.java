package cn.almsound.www.myblesample.callback;

import android.bluetooth.BluetoothGatt;

import cn.almsound.www.baselibrary.Tool;
import cn.almsound.www.blelibrary.BleConnectCallback;
import cn.almsound.www.blelibrary.BleDeviceController;
import cn.almsound.www.blelibrary.BleMultiConnector;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

public class Device1BleCallback extends BleConnectCallback {
    private static final String TAG = "Device1BleCallback";

    private static final byte[] OPEN_SOCKET_BYTE_ARRAY = new byte[]{0x00, 0x00};
    private static final byte[] CLOSE_SOCKET_BYTE_ARRAY = new byte[]{0x00, 0x01};
    /**
     * 手机直控插座时，需要用到的服务UUID
     */
    private static final String SOCKET_SERVICE_UUID = "0000FFF0-0000-1000-8000-00805f9b34fb";
    /**
     * 手机直控插座时，进行开启或关闭操作的特征UUID
     */
    private static final String CHARACTERISTIC_PHONE_CONTROL = "0000fff3-0000-1000-8000-00805f9b34fb";

    private BleMultiConnector bleMultiConnector;
    private BleDeviceController bleDeviceController;

    public Device1BleCallback(BleMultiConnector bleMultiConnector) {
        this.bleMultiConnector = bleMultiConnector;
    }

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"onDiscoverServicesFailed");
    }

    /**
     * 蓝牙GATT被关闭时回调
     */
    @Override
    public void onGattClosed() {
        Tool.warnOut(TAG,"onGattClosed");
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"设备1 onServicesDiscovered");
        String address = gatt.getDevice().getAddress();
        bleDeviceController = bleMultiConnector.getBleDeviceController(address);
    }

    public void open() {
        if (bleDeviceController == null){
            return;
        }
        bleDeviceController.writeData(SOCKET_SERVICE_UUID,CHARACTERISTIC_PHONE_CONTROL,OPEN_SOCKET_BYTE_ARRAY);
    }

    public void close() {
        if (bleDeviceController == null){
            return;
        }
        bleDeviceController.writeData(SOCKET_SERVICE_UUID,CHARACTERISTIC_PHONE_CONTROL,CLOSE_SOCKET_BYTE_ARRAY);
    }
}
