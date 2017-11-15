package cn.almsound.www.blelibrary;

import android.os.Binder;

/**
 *
 * @author alm
 * @date 2017/11/15
 */

class BluetoothMultiServiceBinder extends Binder {
    private static final String TAG = "BluetoothMultiServiceBi";

    private BluetoothMultiService bluetoothMultiService;

    BluetoothMultiServiceBinder(BluetoothMultiService bluetoothMultiService) {
        this.bluetoothMultiService = bluetoothMultiService;
    }

    BluetoothMultiService getBluetoothMultiService() {
        Tool.warnOut(TAG, "蓝牙多连接服务绑定成功");
        return bluetoothMultiService;
    }
}
