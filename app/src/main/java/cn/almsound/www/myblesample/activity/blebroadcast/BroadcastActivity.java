package cn.almsound.www.myblesample.activity.blebroadcast;

import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.os.Build;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.DefaultAdvertiseCallback;
import com.jackiepenghe.blelibrary.BleAdvertiser;
import com.jackiepenghe.blelibrary.BleManager;

import cn.almsound.www.myblesample.R;

/**
 * @author jacke
 */
public class BroadcastActivity extends BaseAppCompatActivity {

    private static final String TAG = "BroadcastActivity";

    /*--------------------成员变量--------------------*/

    /**
     * BLE广播实例
     */
    private BleAdvertiser bleAdvertiser;
    /**
     * 显示广播开启状态的文本
     */
    private TextView broadcastStatusTv;

    private DefaultAdvertiseCallback advertiseCallback = new DefaultAdvertiseCallback() {


        /**
         * Callback triggered in response to {@link BluetoothLeAdvertiser#startAdvertising} indicating
         * that the advertising has been started successfully.
         *
         * @param settingsInEffect The actual settings used for advertising, which may be different from
         *                         what has been requested.
         */
        @Override
        public void onBroadCastStartSuccess(AdvertiseSettings settingsInEffect) {
            broadcastStatusTv.setText(R.string.open_broadcast_success);
        }

        /**
         * Callback when advertising could not be started.
         *
         * @param errorCode Error code (see ADVERTISE_FAILED_* constants) for advertising start
         *                  failures.
         */
        @Override
        public void onBroadCastStartFailure(int errorCode) {
            broadcastStatusTv.setText(R.string.open_broadcast_failed);
            Tool.warnOut(TAG, "errorCode = " + errorCode);
        }

        @Override
        public void onBroadCastStopped() {
            broadcastStatusTv.setText(R.string.broadcast_stopped);
        }

    };

    /**
     * 标题栏的返回按钮被按下的时候回调此函数
     */
    @Override
    protected void titleBackClicked() {
        onBackPressed();
    }

    /**
     * 在设置布局之前需要进行的操作
     */
    @Override
    protected void doBeforeSetLayout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleAdvertiser = BleManager.getBleAdvertiserInstance(this);
            if (bleAdvertiser != null) {
                //默认的初始化
//            bleAdvertiser.init()
                AdvertiseSettings advertiseSettings = new AdvertiseSettings.Builder()
                        .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_LOW_LATENCY)
                        .setConnectable(false)
                        .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_HIGH)
                        .setTimeout(6000)
                        .build();
                //带回调的初始化
                if (!bleAdvertiser.init(advertiseSettings, advertiseCallback)) {
                    Tool.warnOut(TAG, "初始化失败");
                } else {
                    Tool.warnOut(TAG, "初始化成功");
                }
            }
        } else {
            Tool.toastL(BroadcastActivity.this, "系统版本过低，不支持蓝牙广播");
        }
        Tool.warnOut(TAG, "bleAdvertiser = " + bleAdvertiser);
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_ble_broadcast;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {

    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        broadcastStatusTv = findViewById(R.id.broad_cast_status_tv);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {

    }

    /**
     * 初始化其他数据
     */
    @Override
    protected void initOtherData() {

    }

    /**
     * 初始化事件
     */
    @Override
    protected void initEvents() {

    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
        if (bleAdvertiser != null) {
            boolean b;
            b = bleAdvertiser.startAdvertising();
            if (b) {
                Tool.warnOut(TAG, "广播请求发起成功（是否真的成功，在init的advertiseCallback回调中查看）");
            } else {
                Tool.warnOut(TAG, "广播请求发起失败（这是真的失败了，连请求都没有发起成功）");
            }
            Tool.warnOut(TAG, "startAdvertising = " + b);

        }
    }

    /**
     * 设置菜单
     *
     * @param menu 菜单
     * @return 只是重写 public boolean onCreateOptionsMenu(Menu menu)
     */
    @Override
    protected boolean createOptionsMenu(Menu menu) {
        return false;
    }

    /**
     * 设置菜单监听
     *
     * @param item 菜单的item
     * @return true表示处理了监听事件
     */
    @Override
    protected boolean optionsItemSelected(MenuItem item) {
        return false;
    }

    /**
     * Take care of popping the fragment back stack or finishing the activity
     * as appropriate.
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (bleAdvertiser != null) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                bleAdvertiser.stopAdvertising();
                bleAdvertiser.close();
                bleAdvertiser = null;
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //释放所有BLE设备相关的内存
        BleManager.releaseAll();
    }
}
