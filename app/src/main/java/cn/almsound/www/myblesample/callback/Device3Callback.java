package cn.almsound.www.myblesample.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.jackiepenghe.baselibrary.tools.DebugUtil;
import com.jackiepenghe.baselibrary.tools.ToastUtil;
import com.jackiepenghe.baselibrary.tools.Tool;
import com.jackiepenghe.blelibrary.BaseBleConnectCallback;
import com.jackiepenghe.blelibrary.BleDeviceController;
import com.jackiepenghe.blelibrary.BleMultiConnector;

import cn.almsound.www.myblesample.utils.Constants;
import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

public class Device3Callback extends BaseBleConnectCallback {

    private static final String TAG = Device3Callback.class.getSimpleName();
    private BleMultiConnector bleMultiConnector;

    private CustomTextCircleView customTextCircleView;

    public Device3Callback(CustomTextCircleView customTextCircleView, BleMultiConnector bleMultiConnector) {
        this.customTextCircleView = customTextCircleView;
        this.bleMultiConnector = bleMultiConnector;
    }

    /**
     * 当蓝牙客户端配置失败时调用此函式
     *
     * @param gatt        蓝牙客户端
     * @param methodName  方法名
     * @param errorStatus 错误状态码
     */
    @Override
    public void onBluetoothGattOptionsNotSuccess(BluetoothGatt gatt, String methodName, int errorStatus) {
        DebugUtil.warnOut(TAG, "onBluetoothGattOptionsNotSuccess");
        ToastUtil.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onBluetoothGattOptionsNotSuccess");
    }

    /**
     * GATT state unknown
     *
     * @param gatt  GATT
     * @param state state code
     */
    @Override
    public void onUnknownState(BluetoothGatt gatt, int state) {

    }

    /**
     * connect time out
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onConnectTimeOut(BluetoothGatt gatt) {
        DebugUtil.warnOut(TAG, "onBluetoothGattOptionsNotSuccess");
        ToastUtil.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onConnectTimeOut");
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        DebugUtil.warnOut(TAG, gatt.getDevice().getAddress() + " onServicesDiscovered");
        customTextCircleView.setColor(Color.GREEN);
        ToastUtil.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onServicesDiscovered");
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(gatt.getDevice().getAddress());
        if (bleDeviceController != null) {
            if (bleDeviceController.isConnected()) {
                bleDeviceController.writeData(Constants.DEVICE_SERVICE_UUID, Constants.DEVICE_CHARACTERISTIC_UUID, new byte[]{0x00, 0x00});
            }
        }
    }

    /**
     * callback triggered if auto discovered GATT service failed
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onServicesAutoDiscoverFailed(BluetoothGatt gatt) {
        DebugUtil.warnOut(TAG, "onDiscoverServicesFailed");
        customTextCircleView.setColor(Color.RED);
        ToastUtil.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onDiscoverServicesFailed");
    }

    /**
     * callback triggered if GATT has been closed
     *
     * @param address remote device
     */
    @Override
    public void onGattClosed(@Nullable BluetoothDevice address) {
        DebugUtil.warnOut(TAG, "onGattClosed");
        ToastUtil.toastL(customTextCircleView.getContext(), address + ":onGattClosed");
    }

    @Override
    public void onConnected(BluetoothGatt gatt) {
        customTextCircleView.setColor(Color.BLUE);
        ToastUtil.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onConnected");
    }

    @Override
    public void onDisConnected(BluetoothGatt gatt) {
        customTextCircleView.setColor(Color.RED);
        ToastUtil.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onDisConnected");
    }
}
