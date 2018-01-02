package cn.almsound.www.myblesample.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.BleDevice;
import com.jackiepenghe.blelibrary.BleInterface;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.BleScanner;

import java.util.ArrayList;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.DeviceListAdapter;
import cn.almsound.www.myblesample.utils.Constants;

/**
 * 扫描设备列表的界面
 *
 * @author alm
 *         Created by jackie on 2017/1/12 0012.
 */
public class DeviceListActivity extends BaseAppCompatActivity implements View.OnClickListener, AdapterView.OnItemClickListener {

    /**
     * TAG
     */
    private static final String TAG = "DeviceListActivity";

    /**
     * 权限请求的requestCode
     */
    private static final int REQUEST_CODE_ASK_ACCESS_COARSE_LOCATION = 1;

    /**
     * 设备名最短字符数
     */
    private static final int DEVICE_NAME_MIN_LENGTH = 5;
    private static final String DEVICE_NAME = "Y11-";

    /**
     * 扫描到的所有设备列表
     */
    private ArrayList<BleDevice> scanList;
    /**
     * 适配器添加的设备列表
     */
    private ArrayList<BleDevice> adapterList;
    private ListView listView;
    private Button button;
    private DeviceListAdapter adapter;
    private int clickCount;
    /**
     * BLE扫描器
     */
    private BleScanner bleScanner;
    private static final int TWO = 2;

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
        scanList = new ArrayList<>();
        adapterList = new ArrayList<>();
        //初始化BLE扫描器
        initBleScan();
        adapter = new DeviceListAdapter(DeviceListActivity.this, adapterList);
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_device_list;
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
        listView = findViewById(R.id.device_list);
        button = findViewById(R.id.button);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        listView.setAdapter(adapter);
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
        button.setOnClickListener(this);
        listView.setOnItemClickListener(this);
    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {

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
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button:
                checkAPIVersion();
                break;
            default:
                break;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()) {
            case R.id.device_list:
                doListViewItemClick(position);
                break;
            default:
                break;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_ACCESS_COARSE_LOCATION:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    doButtonClick();
                } else {
                    Tool.toastL(DeviceListActivity.this, R.string.no_permission_for_local);
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    /**
     * 在activity被销毁的时候关闭扫描器
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();
        //关闭扫描器
        bleScanner.close();
        scanList = null;
        adapterList = null;
        listView.setAdapter(null);
        listView = null;
        button = null;
        adapter = null;
        clickCount = 0;
        bleScanner = null;
    }

    /**
     * 初始化扫描器
     */
    private void initBleScan() {

        //创建扫描器实例
        bleScanner = BleManager.newBleScanner(DeviceListActivity.this);
        //发现一个新设备（在此之前该设备没有被发现过）时触发此回调
        BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener = new BleInterface.OnScanFindOneNewDeviceListener() {
            @Override
            public void scanFindOneNewDevice(BleDevice bleDevice) {
                //可以在此处过滤一些不需要的设备
            /*if(bleDevice.getBluetoothDevice().getAddress().equalsIgnoreCase("00:00:00:AA:SS:BB")){
                return;
            }*/

                adapterList.add(bleDevice);
                adapter.notifyDataSetChanged();
            }
        };

        //扫描结束后会触发此回调
        BleInterface.OnScanCompleteListener onScanCompleteListener = new BleInterface.OnScanCompleteListener() {
            @Override
            public void scanComplete() {
                button.setText(R.string.start_scan);
                clickCount--;
            }
        };
        //在扫描过程中发现一个设备就会触发一次此回调，不论该设备是否被发现过。在安卓5.0之前此回调效果完全等同于BleInterface.OnScanFindOneNewDeviceListener
        BleInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener = new BleInterface.OnScanFindOneDeviceListener() {
            @Override
            public void scanFindOneDevice(BluetoothDevice bluetoothDevice, int rssi, byte[] scanRecord) {
                //只要发现一个设备就会回调此函数
            }
        };

        /*
         * 打开扫描器，并设置相关回调
         * @param scanResults                  扫描设备结果存放列表
         * @param onScanFindOneNewDeviceListener 发现一个新设备的回调
         * @param scanPeriod                   扫描持续时间
         * @param scanContinueFlag             是否在扫描完成后立即进行下一次扫描的标志
         *                                     为true表示一直扫描，永远不会调用BleInterface.OnScanCompleteListener，
         *                                     为false，在时间到了之后回调BleInterface.OnScanCompleteListener，然后结束
         * @param onScanCompleteListener       扫描完成的回调
         * @return true表示打开成功
         */
        bleScanner.open(scanList, onScanFindOneNewDeviceListener, 10000, true, onScanCompleteListener);
        //设置回调
        bleScanner.setOnScanFindOneDeviceListener(onScanFindOneDeviceListener);
    }


    /**
     * 判断安卓版本执行权限请求
     */
    private void checkAPIVersion() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            int checkAccessCoarseLocationPermission = ContextCompat.checkSelfPermission(DeviceListActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION);
            if (checkAccessCoarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(DeviceListActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, REQUEST_CODE_ASK_ACCESS_COARSE_LOCATION);
            } else {
                doButtonClick();
            }
        } else {
            doButtonClick();
        }
    }

    /**
     * ListView的Item被点击时调用
     *
     * @param position ListView被点击的位置
     */
    private void doListViewItemClick(int position) {
        if (bleScanner.isScanning()) {
            bleScanner.stopScan();
            button.setText(R.string.start_scan);
            clickCount--;
        }
        BleDevice customBleDeviceInfo = adapterList.get(position);
        Intent intent = new Intent(DeviceListActivity.this, ConnectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.DEVICE, customBleDeviceInfo);
        intent.putExtra(Constants.BUNDLE, bundle);
        startActivity(intent);
    }

    /**
     * 扫描/停止扫描
     */
    private void doButtonClick() {
        if (clickCount % TWO == 0) {
            button.setText(R.string.stop_scan);
            bleScanner.clearScanResults();
            adapterList.clear();
            adapter.notifyDataSetChanged();
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
//                ScanFilter scanFilter = new ScanFilter.Builder()
//                        .build();
//                ArrayList<ScanFilter> scanFilters = new ArrayList<>();
//                scanFilters.add(scanFilter);
//                ScanSettings scanSettings = new ScanSettings.Builder()
//                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
//                        .build();
//                bleScanner.setScanFilters(scanFilters);
//                bleScanner.setScanSettings(scanSettings);
//                bleScanner.startScan();
//            }else {
            bleScanner.startScan();
//            }
        } else {
            button.setText(R.string.start_scan);
            bleScanner.stopScan();
        }
        clickCount++;
    }
}
