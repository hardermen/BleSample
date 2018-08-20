package com.jackiepenghe.blelibrary;

import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * 扫描的定时器
 * Created by alm on 17-6-5.
 */

class ScanTimer {

    /*------------------------成员变量----------------------------*/

    /**
     * 线程池工厂类
     */
    private ThreadFactory threadFactory = new ThreadFactory() {
        /**
         * Constructs a new {@code Thread}.  Implementations may also initialize
         * priority, name, daemon status, {@code ThreadGroup}, etc.
         *
         * @param r a runnable to be executed by new thread instance
         * @return constructed thread, or {@code null} if the request to
         * create a thread is rejected
         */
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r);
        }
    };
    /**
     * 定时或延迟执行任务
     */
    private ScheduledExecutorService scheduledExecutorService;
    /**
     * 要执行的任务
     */
    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            final BleScanner bleScanner = bleScannerWeakReference.get();
            if (bleScanner == null) {
                return;
            }
            bleScanner.stopScan();

            if (bleScanner.isScanContinue()) {
                bleScanner.startScan();
            } else {
                BleManager.getHandler().post(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            bleScanner.flushPendingScanResults();
                        }
                        if (mOnScanCompleteListener != null) {
                            mOnScanCompleteListener.onScanComplete();
                        }
                    }
                });
            }
        }
    };
    /**
     * BleScanner弱引用
     */
    private WeakReference<BleScanner> bleScannerWeakReference;
    /**
     * 扫描完成时执行的回调
     */
    private BleInterface.OnScanCompleteListener mOnScanCompleteListener;

    /*------------------------构造函数----------------------------*/

    /**
     * 构造器
     *
     * @param bleScanner BLE扫描器
     */
    ScanTimer(BleScanner bleScanner) {
        bleScannerWeakReference = new WeakReference<>(bleScanner);
    }

    /*------------------------库内函数----------------------------*/

    /**
     * 开启定时器
     *
     * @param delayTime 延迟的时间
     */
    void startTimer(long delayTime) {
        scheduledExecutorService = new ScheduledThreadPoolExecutor(1, threadFactory);
        scheduledExecutorService.schedule(runnable, delayTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止定时器
     */
    void stopTimer() {
        scheduledExecutorService.shutdownNow();
        scheduledExecutorService = null;
        BleScanner bleScanner = bleScannerWeakReference.get();
        bleScanner.setScanningFalse();
    }

    /**
     * 设置扫描完成的回调
     *
     * @param onScanCompleteListener 扫描完成的回调
     */
    void setOnScanCompleteListener(@NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        mOnScanCompleteListener = onScanCompleteListener;
    }
}