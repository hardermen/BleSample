package com.jackiepenghe.blelibrary;

/**
 * 默认的大数据传输回调（需要通知接收数据以继续进行传输的方式）
 *
 * @author jackie
 */
public class DefaultBigDataWriteWithNotificationSendStateChangedListener implements BleInterface.OnBigDataWriteWithNotificationSendStateChangedListener {

    /*-------------------------静态常量-------------------------*/

    private static final String TAG = DefaultBigDataWriteWithNotificationSendStateChangedListener.class.getSimpleName();

    /*-------------------------实现父类方法-------------------------*/

    /**
     * 收到远端设备的通知时进行的回调
     *
     * @param currentPackageData  当前包数据
     * @param currentPackageCount 当前包数
     * @param packageCount        总包数
     * @param values              远端设备的通知内容
     * @return true表示可以继续下一包发送，false表示传输出错
     */
    @Override
    public boolean onReceiveNotification(byte[] currentPackageData, int currentPackageCount, int packageCount, byte[] values) {
        Tool.warnOut(TAG, "onReceiveNotification values = " + Tool.bytesToHexStr(values));
        return false;
    }

    /**
     * 数据发送完成
     */
    @Override
    public void onDataSendFinished() {
        Tool.warnOut(TAG, "onDataSendFinished");
    }

    /**
     * 数据发送失败
     *
     * @param currentPackageCount 当前发送失败的包数
     * @param pageCount           总包数
     * @param data                当前发送失败的数据内容
     */
    @Override
    public void onDataSendFailed(int currentPackageCount, int pageCount, byte[] data) {
        Tool.warnOut(TAG, "onDataSendFailed currentPackageCount = " + currentPackageCount + ",pageCount = " + pageCount + "\ndata = " + Tool.bytesToHexStr(data));
    }

    /**
     * 数据发送失败并尝试重发
     *
     * @param currentPackageCount 当前包数
     * @param pageCount           总包数
     * @param data                当前包数据内容
     * @param tryCount            重试次数
     */
    @Override
    public void onDataSendFailedAndRetry(int currentPackageCount, int pageCount, byte[] data, int tryCount) {
        Tool.warnOut(TAG, "onDataSendFailedAndRetry currentPackageCount = " + currentPackageCount + ",pageCount = " + pageCount + ",tryCount = " + tryCount + "\ndata = " + Tool.bytesToHexStr(data));
    }

    /**
     * 数据发送进度有更改
     *
     * @param currentPackageCount 当前包数
     * @param pageCount           总包数
     * @param data                当前包数据内容
     */
    @Override
    public void onDataSendProgressChanged(int currentPackageCount, int pageCount, byte[] data) {
        Tool.warnOut(TAG, "onDataSendFailedAndRetry currentPackageCount = " + currentPackageCount + ",pageCount = " + pageCount + "\ndata = " + Tool.bytesToHexStr(data));
    }

    /**
     * 因为通知返回的数据出错而导致的传输失败
     */
    @Override
    public void onSendFailedWithWrongNotifyData() {
        Tool.warnOut(TAG, "onSendFailedWithWrongNotifyData");
    }

    /**
     * 数据发送失败（通知返回数据有错误）
     *
     * @param tryCount            重试次数
     * @param currentPackageIndex 当前发送的包数
     * @param packageCount        总包数
     * @param data                当前包数据
     */
    @Override
    public void onSendFailedWithWrongNotifyDataAndRetry(int tryCount, int currentPackageIndex, int packageCount, byte[] data) {
        Tool.warnOut(TAG, "onSendFailedWithWrongNotifyDataAndRetry：tryCount = " + tryCount + ",currentPackageIndex = " + currentPackageIndex + ",packageCount = " + packageCount + "\ndata = " + Tool.bytesToHexStr(data));
    }
}
