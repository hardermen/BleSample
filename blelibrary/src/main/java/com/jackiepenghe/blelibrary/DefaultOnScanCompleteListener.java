package com.jackiepenghe.blelibrary;

class DefaultOnScanCompleteListener implements BleInterface.OnScanCompleteListener {

    private static final String TAG = DefaultOnScanCompleteListener.class.getSimpleName();

    /**
     * 扫描完成时回调此函数
     */
    @Override
    public void onScanComplete() {
        Tool.warnOut(TAG,"扫描完成");
    }
}
