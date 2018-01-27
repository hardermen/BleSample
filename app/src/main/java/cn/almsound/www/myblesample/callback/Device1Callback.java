package cn.almsound.www.myblesample.callback;

import android.bluetooth.BluetoothGatt;
import android.graphics.Color;

import com.jackiepenghe.blelibrary.BaseConnectCallback;
import com.jackiepenghe.blelibrary.Tool;

import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

public class Device1Callback extends BaseConnectCallback {
    private static final String TAG = "Device1Callback";

    private CustomTextCircleView customTextCircleView;
    public Device1Callback(CustomTextCircleView customTextCircleView) {
        this.customTextCircleView = customTextCircleView;
    }

    /**
     * 蓝牙连接后无法正常进行服务发现时回调
     *
     * @param gatt BluetoothGatt
     */
    @Override
    public void onDiscoverServicesFailed(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"onDiscoverServicesFailed");
        customTextCircleView.setColor(Color.RED);
    }

    /**
     * 蓝牙GATT被关闭时回调
     * @param address 设备地址
     */
    @Override
    public void onGattClosed(String address) {
        Tool.warnOut(TAG,"onGattClosed");
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
        Tool.warnOut(TAG,"onGattClosed");
    }

    @Override
    public void onServicesDiscovered(BluetoothGatt gatt) {
        Tool.warnOut(TAG,"设备1 onServicesDiscovered");
        customTextCircleView.setColor(Color.GREEN);
    }

    @Override
    public void onConnected(BluetoothGatt gatt) {
        customTextCircleView.setColor(Color.BLUE);
    }

    @Override
    public void onDisConnected(BluetoothGatt gatt) {
        customTextCircleView.setColor(Color.RED);
    }
}
