package cn.almsound.www.myblesample.activity.bleconnect;

import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;

import com.jackiepenghe.baselibrary.activity.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.view.utils.DefaultItemDecoration;
import com.jackiepenghe.blelibrary.BleDevice;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.BleScanner;
import com.jackiepenghe.blelibrary.interfaces.OnBleScanStateChangedListener;

import java.util.ArrayList;
import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.BroadcastIntervalAdapter;
import cn.almsound.www.myblesample.utils.Constants;

public class BroadcastIntervalTestActivity extends BaseAppCompatActivity {

    private static final String TAG = BroadcastIntervalTestActivity.class.getSimpleName();
    private OnBleScanStateChangedListener onBleScanStateChangedListener = new OnBleScanStateChangedListener() {
        @Override
        public void onScanFindOneDevice(BleDevice bleDevice) {
            if (bleDevice.getDeviceAddress().equals(deviceAddress)) {
                long currentTimeMillis = System.currentTimeMillis();
                if (lastTime == 0) {
                    lastTime = currentTimeMillis;
                    return;
                }
                long time = currentTimeMillis - lastTime;
                lastTime = currentTimeMillis;
                longArrayList.add(time);
                broadcastIntervalAdapter.notifyItemChanged(longArrayList.size());
            }
        }

        @Override
        public void onScanFindOneNewDevice(int index, @Nullable BleDevice bleDevice, @NonNull ArrayList<BleDevice> bleDevices) {

        }

        @Override
        public void onScanComplete() {

        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {

        }

        @Override
        public void onScanFailed(int errorCode) {

        }
    };

    private ArrayList<Long> longArrayList = new ArrayList<>();

    private BroadcastIntervalAdapter broadcastIntervalAdapter = new BroadcastIntervalAdapter(longArrayList);

    private DefaultItemDecoration defaultItemDecoration = DefaultItemDecoration.getDefaultItemDecoration(Color.GRAY, DefaultItemDecoration.ORIENTATION_VERTICAL);

    /**
     * 广播间隔时间
     */
    private RecyclerView broadcastIntervalRecyclerView;
    /**
     * 被测设备地址
     */
    private String deviceAddress;

    /**
     * BLE 扫描工具
     */
    private BleScanner bleScanner;

    private long lastTime;

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
        getDeviceAddress();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_broadcast_interval_test;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        initBleScanner();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        broadcastIntervalRecyclerView = findViewById(R.id.time_interval_list);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initRecyclerViewData();
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
        bleScanner.startScan();
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

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleScanner.stopScan();
        bleScanner.close();
        bleScanner = null;
        onBleScanStateChangedListener = null;
        broadcastIntervalRecyclerView = null;
        deviceAddress = null;
        lastTime = 0;
    }

    private void getDeviceAddress() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        deviceAddress = intent.getStringExtra(Constants.DEVICE_ADDRESS);

    }

    private void initBleScanner() {
        bleScanner = BleManager.newBleScanner();
        if (bleScanner == null) {
            return;
        }
        bleScanner.init();
        bleScanner.setAutoStartNextScan(true);
        bleScanner.setOnBleScanStateChangedListener(onBleScanStateChangedListener);
    }

    private void initRecyclerViewData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        broadcastIntervalRecyclerView.setLayoutManager(linearLayoutManager);
        broadcastIntervalRecyclerView.addItemDecoration(defaultItemDecoration);
        broadcastIntervalAdapter.bindToRecyclerView(broadcastIntervalRecyclerView);
        broadcastIntervalAdapter.setEmptyView(R.layout.scanning);
    }
}
