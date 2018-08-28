package com.jackiepenghe.blelibrary;

/**
 * 默认的大数据传输回调
 *
 * @author jackie
 */
public class DefaultBigDataSendStateChangedListener implements BleInterface.OnBigDataSendStateChangedListener {

    /*-------------------------静态常量-------------------------*/

    private static final String TAG = DefaultBigDataSendStateChangedListener.class.getSimpleName();

    /*-------------------------重写父类方法-------------------------*/

    /**
     * 传输开始
     */
    @Override
    public void sendStarted() {
        Tool.warnOut(TAG, "sendStarted");
    }

    /**
     * 传输完成
     */
    @Override
    public void sendFinished() {
        Tool.warnOut(TAG, "sendFinished");
    }

    /**
     * 数据发送成功
     *
     * @param currentPackageCount 当前发送成功的包数
     * @param pageCount           总包数
     * @param data                本包发送的数据
     */
    @Override
    public void packageSendProgressChanged(int currentPackageCount, int pageCount, byte[] data) {
        Tool.warnOut(TAG, "packageSendProgressChanged : currentPackageCount = " + currentPackageCount + ",pageCount = " + pageCount + ",data = " + Tool.bytesToHexStr(data));
    }

    /**
     * 数据发送失败
     *
     * @param currentPackageCount 当前发送失败的包数
     * @param pageCount           总包数
     * @param data                本包发送的数据
     */
    @Override
    public void packageSendFailed(int currentPackageCount, int pageCount, byte[] data) {
        Tool.warnOut(TAG, "packageSendFailed : currentPackageCount = " + currentPackageCount + ",pageCount = " + pageCount + ",data = " + Tool.bytesToHexStr(data));
    }

    /**
     * 本包数据发送失败，正在重新发送
     *
     * @param currentPackageCount 当前发送失败的包数
     * @param pageCount           总包数
     * @param tryCount            尝试次数
     * @param data                本包发送的数据
     */
    @Override
    public void packageSendFailedAndRetry(int currentPackageCount, int pageCount, int tryCount, byte[] data) {
        Tool.warnOut(TAG, "packageSendFailedAndRetry : currentPackageCount = " + currentPackageCount + ",pageCount = " + pageCount + ",tryCount = " + tryCount + ",data = " + Tool.bytesToHexStr(data));
    }
}
