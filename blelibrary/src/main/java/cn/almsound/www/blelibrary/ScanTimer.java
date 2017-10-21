package cn.almsound.www.blelibrary;

import android.os.Handler;
import android.support.annotation.NonNull;

import java.lang.ref.WeakReference;
import java.util.TimerTask;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

/**
 * Created by alm on 17-6-5.
 * 扫描的定时器
 */

class ScanTimer {
    /**
     * private Timer timer;
     */
    private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1, new ThreadFactory() {
        @Override
        public Thread newThread(@NonNull Runnable r) {
            return new Thread(r);
        }
    });
    private ScanTask scanTask;
    private WeakReference<BleScanner> bleScannerWeakReference;
    private BleInterface.OnScanCompleteListener mOnScanCompleteListener;
    private Handler handler;

    /**
     * 构造器
     *
     * @param bleScanner BLE扫描器
     */
    ScanTimer(BleScanner bleScanner) {
        bleScannerWeakReference = new WeakReference<>(bleScanner);
//        timer = new Timer();
        handler = new Handler();
    }

    /**
     * 开启定时器
     *
     * @param delayTime 延迟的时间
     */
    void startTimer(long delayTime) {
        scanTask = new ScanTask(ScanTimer.this);
        /*timer.schedule(scanTask, delayTime);*/
        scheduledThreadPoolExecutor.schedule(new Runnable() {
            @Override
            public void run() {

                if (bleScannerWeakReference.get() == null) {
                    return;
                }

                bleScannerWeakReference.get().stopScan();

                if (bleScannerWeakReference.get().isScanContinue()) {
                    bleScannerWeakReference.get().clearScanResults();
                    bleScannerWeakReference.get().startScan();
                } else {
                    if (mOnScanCompleteListener != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                mOnScanCompleteListener.scanComplete();
                            }
                        });
                    }
                }
            }
        }, delayTime, TimeUnit.MILLISECONDS);
    }

    /**
     * 停止定时器
     */
    void stopTimer() {
        scanTask.cancel();
        BleScanner bleScanner = bleScannerWeakReference.get();
        bleScanner.setScanning(false);
    }

    void setOnScanCompleteListener(@NonNull BleInterface.OnScanCompleteListener onScanCompleteListener) {
        mOnScanCompleteListener = onScanCompleteListener;
    }

    private static class ScanTask extends TimerTask {

        private WeakReference<ScanTimer> scanTimerWeakReference;

        ScanTask(ScanTimer scanTimer) {
            this.scanTimerWeakReference = new WeakReference<>(scanTimer);
        }

        /**
         * The action to be performed by this timer task.
         */
        @Override
        public void run() {
            final ScanTimer scanTimer = scanTimerWeakReference.get();
            if (scanTimer == null) {
                return;
            }
            BleScanner bleScanner = scanTimer.bleScannerWeakReference.get();
            if (bleScanner == null) {
                return;
            }
            bleScanner.stopScan();

            if (bleScanner.isScanContinue()) {
                bleScanner.clearScanResults();
                bleScanner.startScan();
            } else {
                if (scanTimer.mOnScanCompleteListener != null) {
                    scanTimer.handler.post(new Runnable() {
                        @Override
                        public void run() {
                            scanTimer.mOnScanCompleteListener.scanComplete();
                        }
                    });
                }
            }
        }
    }
}