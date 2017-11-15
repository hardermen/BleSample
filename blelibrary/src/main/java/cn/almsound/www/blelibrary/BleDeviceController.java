package cn.almsound.www.blelibrary;

import android.content.Context;

/**
 *
 * @author alm
 * @date 2017/11/15
 */
@SuppressWarnings("UnusedReturnValue")
public class BleDeviceController {
    private BleMultiConnector bleMultiConnector;
    private String address;

    BleDeviceController(BleMultiConnector bleMultiConnector, String address) {
        this.bleMultiConnector = bleMultiConnector;
        this.address = address;
    }

    /**
     * 刷新蓝牙缓存
     *
     * @return true表示成功
     */
    public boolean refreshGattCache() {
        return bleMultiConnector != null && address != null&& bleMultiConnector.refreshGattCache(address);
    }

    /**
     * 写入数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @param values             数据
     * @return true表示成功
     */
    public boolean writeData( String serviceUUID, String characteristicUUID, byte[] values) {
        return bleMultiConnector != null && address != null && bleMultiConnector.writeData(address, serviceUUID, characteristicUUID, values);
    }


    /**
     * 读取数据
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean readData(String serviceUUID, String characteristicUUID) {
        return bleMultiConnector != null && address != null && bleMultiConnector.readData(address, serviceUUID, characteristicUUID);
    }

    /**
     * 打开通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean openNotification( String serviceUUID, String characteristicUUID) {
        return bleMultiConnector != null && address != null && bleMultiConnector.openNotification(address, serviceUUID, characteristicUUID);
    }

    /**
     * 关闭通知
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     * @return true表示成功
     */
    public boolean closeNotification( String serviceUUID, String characteristicUUID) {
        return bleMultiConnector != null && address != null && bleMultiConnector.closeNotification(address, serviceUUID, characteristicUUID);
    }
}
