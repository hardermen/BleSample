package cn.almsound.www.myblesample.callback;

import android.bluetooth.BluetoothGatt;
import android.graphics.Color;

import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.BaseConnectCallback;

import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

public class Device5Callback extends BaseConnectCallback {

    private static final String TAG = "Device5Callback";
    private CustomTextCircleView customTextCircleView;
    public Device5Callback(CustomTextCircleView customTextCircleView) {
        this.customTextCircleView = customTextCircleView;
    }

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        Tool.warnOut(TAG, "onDiscoverServicesFailed");
        customTextCircleView.setColor(Color.RED);
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onDiscoverServicesFailed");
    }

    /**
     * 蓝牙GATT被关闭时回调
     *
     * @param address 设备地址
     */
    @Override
    public void onGattClosed(String address) {
        Tool.warnOut(TAG, "onGattClosed");
        Tool.toastL(customTextCircleView.getContext(), address + ":onGattClosed");
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

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG, gatt.getDevice().getAddress() + " onServicesDiscovered");
        customTextCircleView.setColor(Color.GREEN);
        Tool.toastL(customTextCircleView.getContext(), gatt.getDevice().getAddress() + ":onServicesDiscovered");
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
