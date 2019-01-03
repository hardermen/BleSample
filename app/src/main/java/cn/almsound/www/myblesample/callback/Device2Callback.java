package cn.almsound.www.myblesample.callback;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.graphics.Color;
import android.support.annotation.Nullable;

import com.jackiepenghe.baselibrary.tools.Tool;
import com.jackiepenghe.blelibrary.BaseBleConnectCallback;

import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

public class Device2Callback extends BaseBleConnectCallback {

    private static final String TAG = Device2Callback.class.getSimpleName();

    private CustomTextCircleView customTextCircleView;

    public Device2Callback(CustomTextCircleView customTextCircleView) {
        this.customTextCircleView = customTextCircleView;
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
        Tool.warnOut(TAG, "onBluetoothGattOptionsNotSuccess");
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onBluetoothGattOptionsNotSuccess");
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
        Tool.warnOut(TAG, "onBluetoothGattOptionsNotSuccess");
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onConnectTimeOut");
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onServicesDiscovered");
        customTextCircleView.setColor(Color.GREEN);
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onServicesDiscovered");
    }

    /**
     * callback triggered if auto discovered GATT service failed
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onServicesAutoDiscoverFailed(BluetoothGatt gatt) {
        Tool.warnOut(TAG, "onDiscoverServicesFailed");
        customTextCircleView.setColor(Color.RED);
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onDiscoverServicesFailed");
    }

    /**
     * callback triggered if GATT has been closed
     *
     * @param address remote device
     */
    @Override
    public void onGattClosed(@Nullable BluetoothDevice address) {
        Tool.warnOut(TAG, "onGattClosed");
        Tool.toastL(customTextCircleView.getContext(), address + ":onGattClosed");
    }

    @Override
    public void onConnected(BluetoothGatt gatt) {
        customTextCircleView.setColor(Color.BLUE);
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onConnected");
    }

    @Override
    public void onDisConnected(BluetoothGatt gatt) {
        customTextCircleView.setColor(Color.RED);
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onDisConnected");
    }
}
