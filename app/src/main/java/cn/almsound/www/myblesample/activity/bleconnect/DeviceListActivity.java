package cn.almsound.www.myblesample.activity.bleconnect;

import android.Manifest;
import android.bluetooth.le.ScanResult;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.DefaultItemDecoration;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.BleDevice;
import com.jackiepenghe.blelibrary.BleInterface;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.BleScanner;

import java.util.ArrayList;
import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.DeviceListAdapter;
import cn.almsound.www.myblesample.utils.Constants;

/**
 * 扫描设备列表的界面
 *
 * @author alm
 * Created by jackie on 2017/1/12 0012.
 */
public class DeviceListActivity extends BaseAppCompatActivity {

    /**
     * TAG
     */
    private static final String TAG = "DeviceListActivity";

    /**
     * 权限请求的requestCode
     */
    private static final int REQUEST_CODE_ASK_ACCESS_COARSE_LOCATION = 1;

    /**
     * 扫描到的所有设备列表
     */
    private ArrayList<BleDevice> scanList;
    /**
     * 适配器添加的设备列表
     */
    private ArrayList<BleDevice> adapterList;
    private RecyclerView recyclerView;
    private Button button;
    private DeviceListAdapter adapter;
    private int clickCount;
    /**
     * BLE扫描器
     */
    private BleScanner bleScanner;
    private static final int TWO = 2;
    private BaseQuickAdapter.OnItemClickListener onItemClickListener = new BaseQuickAdapter.OnItemClickListener() {
        @Override
        public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
            doListViewItemClick(position);
        }
    };
    /**
     * 发现一个新设备（在此之前该设备没有被发现过）时触发此回调
     */
    private BleInterface.OnScanFindOneNewDeviceListener onScanFindOneNewDeviceListener = new BleInterface.OnScanFindOneNewDeviceListener() {
        @Override
        public void onScanFindOneNewDevice(BleDevice bleDevice) {
//                byte[] manufacturerSpecificData = bleDevice.getManufacturerSpecificData(-1);\
//                Tool.warnOut(TAG, Tool.bytesToHexStr(manufacturerSpecificData));\
            //可以在此处过滤一些不需要的设备
            /*if(bleDevice.getBluetoothDevice().getAddress().equalsIgnoreCase("00:00:00:AA:SS:BB")){
                return;
            }*/

//                adapterList.add(bleDevice);'
//                adapter.notifyItemInserted(adapterList.size() - 1);'
        }
    };

    /**
     * 扫描结束后会触发此回调
     */
    private BleInterface.OnScanCompleteListener onScanCompleteListener = new BleInterface.OnScanCompleteListener() {
        @Override
        public void onScanComplete() {
            button.setText(R.string.start_scan);
            clickCount--;
        }
    };
    /**
     * 在扫描过程中发现一个设备就会触发一次此回调，不论该设备是否被发现过。在安卓5.0之前此回调效果完全等同于BleInterface.OnScanFindOneNewDeviceListener
     */
    private BleInterface.OnScanFindOneDeviceListener onScanFindOneDeviceListener = new BleInterface.OnScanFindOneDeviceListener() {
        @Override
        public void onScanFindOneDevice(BleDevice bleDevice) {
            //只要发现一个设备就会回调此函数
            if (!adapterList.contains(bleDevice)) {
                adapterList.add(bleDevice);
                adapter.notifyItemInserted(adapterList.size() - 1);
            } else {
                int indexOf = adapterList.indexOf(bleDevice);
                adapterList.set(indexOf, bleDevice);
                adapter.notifyItemChanged(indexOf);
            }
        }
    };

    /**
     * 点击事件的监听
     */
    private View.OnClickListener onClickListener = new View.OnClickListener() {
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
    };

    /**
     * 安卓5.0以上的API才拥有的接口
     */
    private BleInterface.On21ScanCallback on21ScanCallback = new BleInterface.On21ScanCallback() {
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            Tool.warnOut(TAG, "onBatchScanResults");
            if (results == null) {
                return;
            }
            for (int i = 0; i < results.size(); i++) {
                ScanResult scanResult = results.get(i);
               Tool.warnOut(TAG, "scanResult[" + i + "] = " + scanResult.toString());
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Tool.warnOut(TAG, "onScanFailed:errorCode = " + errorCode);
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
        scanList = new ArrayList<>();
        adapterList = new ArrayList<>();
        //初始化BLE扫描器
        initBleScanner();
        adapter = new DeviceListAdapter(adapterList);
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
        recyclerView = findViewById(R.id.device_list);
        button = findViewById(R.id.button);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY, ViewGroup.LayoutParams.MATCH_PARENT, 2, -1);
        recyclerView.addItemDecoration(defaultItemDecoration);
        adapter.setOnItemClickListener(onItemClickListener);
        recyclerView.setAdapter(adapter);
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
        button.setOnClickListener(onClickListener);
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
        button = null;
        adapter = null;
        clickCount = 0;
        bleScanner = null;

        //解除输入法内存泄漏
        Tool.releaseInputMethodManagerMemory(this);
    }

    /**
     * 初始化扫描器
     */
    private void initBleScanner() {

        //创建扫描器实例
        bleScanner = BleManager.getBleScannerInstance(DeviceListActivity.this);
        //如果手机不支持蓝牙的话，这里得到的是null,所以需要进行判空
        if (bleScanner == null) {
            Tool.toastL(DeviceListActivity.this, R.string.ble_not_supported);
            return;
        }
        /*
         * 打开扫描器，并设置相关回调
         * @param scanResults                  扫描到的设备结果存放列表
         * @param onScanFindOneNewDeviceListener 发现一个新设备的回调
         * @param scanPeriod                   扫描持续时间
         * @param scanContinueFlag             是否在扫描完成后立即进行下一次扫描的标志
         *                                     为true表示一直扫描，永远不会调用BleInterface.OnScanCompleteListener，
         *                                     为false，在时间到了之后回调BleInterface.OnScanCompleteListener，然后结束
         * @param onScanCompleteListener       扫描完成的回调
         * @return true表示打开成功
         */
        bleScanner.open(scanList, onScanFindOneNewDeviceListener, 10000, false, onScanCompleteListener);

        //设置其他回调
        bleScanner.setOnScanFindOneDeviceListener(onScanFindOneDeviceListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            bleScanner.setOn21ScanCallback(on21ScanCallback);
        }
//        //bleScanner进阶设置
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            bleScannerSettings();
//        }
    }

//    /**
//     * 设置BleScanner的扫描参数与过滤条件
//     */
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void bleScannerSettings() {
//        setScanFilters();
//        setScanSettings();
//    }

//    /**
//     * 设置扫描器的扫描参数
//     */
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void setScanSettings() {
//        ScanSettings scanSettings = new ScanSettings.Builder()
//                //设置回调触发方式（需要API23及以上）(set callback type(API 23 supported))
////                    .setCallbackType(ScanSettings.CALLBACK_TYPE_ALL_MATCHES)
//                //如果只有传统（我猜测是经典蓝牙，并不确定）的广播，是否回调callback函数(需要API26及以上)
//                // Set whether only legacy advertisments should be returned in scan results.
//                //Legacy advertisements include advertisements as specified by the
//                //Bluetooth core specification 4.2 and below. This is true by default
//                //for compatibility with older apps.
//                //true if only legacy advertisements will be returned
////                    .setLegacy(false)
//                //设置扫描匹配方式（需要API23及以上）(set match mode(API 23 supported))
////                    .setMatchMode(ScanSettings.MATCH_MODE_AGGRESSIVE)
//                //设置扫描匹配次数（需要API23及以上）(set num of matches(API 23 supported))
////                    .setNumOfMatches(2)
//                //在扫描过程中设置物理层(需要API23及以上)(set phy(API 23 supported))
////                    .setPhy(BluetoothDevice.PHY_LE_1M)
//                //设置报告延迟时间(set report delay)
//                .setReportDelay(100)
//                //设置扫描模式(set scan mode(default mode:ScanSettings.SCAN_MODE_LOW_LATENCY))
//                .setScanMode(ScanSettings.SCAN_MODE_BALANCED)
//                //构建
//                .build();
//        //设置扫描参数(set scan settings)
//        bleScanner.setScanSettings(scanSettings);
//    }

//    /**
//     * 设置扫描器的过滤条件
//     */
//    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
//    private void setScanFilters() {
//        //声明过滤集合,可同时设置多组过滤条件(Declaring a filter list to set multiple set of filtering conditions at the same time)
//        ArrayList<ScanFilter> scanFilters = new ArrayList<>();
//        //声明服务UUID(Declaring service uuid)
//        String serviceUUID = "C3E6FEA0-E966-1000-8000-BE99C223DF6A";
//        ScanFilter scanFilter = new ScanFilter.Builder()
//                //设置过滤设备地址(Device address filtering setting)
//                .setDeviceAddress("00:02:5B:00:15:AA")
//                //设置过滤设备名称(Device name filtering setting)
//                .setDeviceName("Y11-")
//                //根据厂商自定义的广播id和广播内容过滤(Device manufacturer data filtering setting)
//                .setManufacturerData(2, new byte[]{0, 2})
//                //根据服务数据进行过滤(Device service uuid filtering setting)
//                .setServiceUuid(new ParcelUuid(UUID.fromString(serviceUUID)))
//                //构建(build filter)
//                .build();
//        //添加一个过滤到过滤集合中(add a filter to filter list)
//        scanFilters.add(scanFilter);
//        //设置过滤条件(set scanner filters)
//        bleScanner.setScanFilters(scanFilters);
//    }


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
        BleDevice bleDevice = adapterList.get(position);
        Intent intent = new Intent(DeviceListActivity.this, ConnectActivity.class);
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.DEVICE, bleDevice);
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
            bleScanner.startScan();
        } else {
            button.setText(R.string.start_scan);
            bleScanner.stopScan();
        }
        clickCount++;
    }
}
