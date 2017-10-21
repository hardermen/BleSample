package cn.almsound.www.blelibrary;

import android.os.AsyncTask;

import java.lang.ref.WeakReference;

/**
 * Created by alm on 17-6-6.
 * BLE连接工具被关闭时，检测关闭状态并回调关闭完成的接口
 */

class CloseTask extends AsyncTask<Object, Object, Object> {
    private WeakReference<BleConnector> bleConnectorWeakReference;
    private BleInterface.OnCloseCompleteListener mOnCloseCompleteListener;

    CloseTask(BleConnector bleConnector) {
        bleConnectorWeakReference = new WeakReference<>(bleConnector);
    }

    /**
     * Override this method to perform a computation on a background thread. The
     * specified parameters are the parameters passed to {@link #execute}
     * by the caller of this task.
     * <p>
     * This method can call {@link #publishProgress} to publish updates
     * on the UI thread.
     *
     * @param params The parameters of the task.
     * @return A result, defined by the subclass of this task.
     * @see #onPreExecute()
     * @see #onPostExecute
     * @see #publishProgress
     */
    @Override
    protected Object doInBackground(Object... params) {
        return new Object();
    }

    /**
     * <p>Runs on the UI thread after {@link #doInBackground}. The
     * specified result is the value returned by {@link #doInBackground}.</p>
     * <p>
     * <p>This method won't be invoked if the task was cancelled.</p>
     *
     * @param o The result of the operation computed by {@link #doInBackground}.
     * @see #onPreExecute
     * @see #doInBackground
     * @see #onCancelled(Object)
     */
    @Override
    protected void onPostExecute(Object o) {
        super.onPostExecute(o);
        BleConnector bleConnector = bleConnectorWeakReference.get();
        if (bleConnector == null) {
            return;
        }
        bleConnector.setClosed(true);
        Tool.waitTime(100);
        if (mOnCloseCompleteListener!= null) {
            mOnCloseCompleteListener.onCloseComplete();
        }
    }

    void setOnCloseCompleteListener(BleInterface.OnCloseCompleteListener onCloseCompleteListener) {
        mOnCloseCompleteListener = onCloseCompleteListener;
    }
}
