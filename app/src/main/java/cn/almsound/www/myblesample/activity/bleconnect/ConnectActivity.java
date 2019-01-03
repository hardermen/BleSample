package cn.almsound.www.myblesample.activity.bleconnect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.jackiepenghe.baselibrary.activity.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.tools.Tool;
import com.jackiepenghe.baselibrary.view.utils.DefaultItemDecoration;
import com.jackiepenghe.blelibrary.BleConnector;
import com.jackiepenghe.blelibrary.BleDevice;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.BleUtils;
import com.jackiepenghe.blelibrary.interfaces.OnBleConnectStateChangedListener;
import com.jackiepenghe.blelibrary.interfaces.OnBleDescriptorWriteListener;
import com.jackiepenghe.blelibrary.interfaces.OnDeviceBondStateChangedListener;
import com.jackiepenghe.blelibrary.interfaces.implementations.DefaultLargeDataWriteWithNotificationSendStateChangedListener;
import com.jackiepenghe.blelibrary.interfaces.implementations.DefaultOnLargeDataSendStateChangedListener;

import java.util.ArrayList;
import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.ServicesCharacteristicsListAdapter;
import cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity.CharacteristicUuidItem;
import cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity.ServiceUuidItem;
import cn.almsound.www.myblesample.utils.Constants;
import cn.almsound.www.myblesample.watcher.EditTextWatcherForHexData;
import cn.almsound.www.myblesample.watcher.EditTextWatcherForHexDataWithin20;
import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 * 连接设备的界面
 *
 * @author jacke
 */
public class ConnectActivity extends BaseAppCompatActivity {

    private static final String TAG = "ConnectActivity";

    /**
     * 连接状态指示，设置其颜色表示不同的连接状态
     * 红色：未连接或连接被断开
     * 黄色：发起连接了
     * 蓝色：连接上
     * 绿色：连接上并且将远端设备服务扫描完毕
     */
    private CustomTextCircleView customTextCircleView;

    /**
     * BLE连接器
     */
    private BleConnector bleConnector;

    /**
     * 用于记录连接状态
     */
    private boolean isLinked;

    /**
     * 用于记录服务是否已经扫描完毕，避免某些手机重复回调
     */
    private boolean serviceDiscovered;

    /**
     * 设备名，设备地址
     */
    private TextView nameTv, addressTv;

    /**
     * 蓝牙设备对象
     */
    private BluetoothDevice bluetoothDevice;

    /**
     * 显示设备的服务与特征的列表
     */
    private RecyclerView recyclerView;

    /**
     * RecyclerView默认的装饰
     */
    private DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY, ViewGroup.LayoutParams.MATCH_PARENT, 2, -1);

    /**
     * adapter的数据
     */
    private ArrayList<MultiItemEntity> adapterData = new ArrayList<>();

    /**
     * 用于显示服务UUID和特征UUID的Adapter
     */
    private ServicesCharacteristicsListAdapter servicesCharacteristicsListAdapter;

    private ServicesCharacteristicsListAdapter.OnCharacteristicClickListener onCharacteristicClickListener = new ServicesCharacteristicsListAdapter.OnCharacteristicClickListener() {
        @Override
        public void onCharacteristicClick(String serviceUUID, String characteristicUUID) {
            Tool.warnOut(TAG, "serviceUUID = " + serviceUUID + ",characteristicUUID = " + characteristicUUID);
            showOptionsDialog(serviceUUID, characteristicUUID);
        }
    };

    private int toastKeepTime = 500;

    private DefaultOnLargeDataSendStateChangedListener defaultOnLargeDataSendStateChangedListener = new DefaultOnLargeDataSendStateChangedListener() {
        /**
         * 传输开始
         */
        @Override
        public void sendStarted() {
            super.sendStarted();
            Tool.toast(ConnectActivity.this, "sendStarted", toastKeepTime);
        }

        /**
         * 传输完成
         */
        @Override
        public void sendFinished() {
            super.sendFinished();
            Tool.toast(ConnectActivity.this, "sendFinished", toastKeepTime);
        }

        /**
         * 数据发送进度更改
         *
         * @param currentPackageCount 当前发送成功的包数
         * @param pageCount           总包数
         * @param data                本包发送的数据
         */
        @Override
        public void packageSendProgressChanged(int currentPackageCount, int pageCount, @NonNull byte[] data) {
            super.packageSendProgressChanged(currentPackageCount, pageCount, data);
            Tool.toast(ConnectActivity.this, "packageSendProgressChanged " + currentPackageCount + " / " + pageCount, toastKeepTime);
            Tool.warnOut(TAG, "data = " + Tool.bytesToHexStr(data));
        }

        /**
         * 数据发送失败
         *
         * @param currentPackageCount 当前发送失败的包数
         * @param pageCount           总包数
         * @param data                本包发送的数据
         */
        @Override
        public void packageSendFailed(int currentPackageCount, int pageCount, @NonNull byte[] data) {
            super.packageSendFailed(currentPackageCount, pageCount, data);
            Tool.toast(ConnectActivity.this, "packageSendFailed " + currentPackageCount + " / " + pageCount, toastKeepTime);
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
        public void packageSendFailedAndRetry(int currentPackageCount, int pageCount, int tryCount, @NonNull byte[] data) {
            super.packageSendFailedAndRetry(currentPackageCount, pageCount, tryCount, data);
//            Tool.toast(ConnectActivity.this, "packageSendFailedAndRetry: tryCount = " + tryCount + " " + currentPackageCount + " / " + pageCount, toastKeepTime);
        }

        /**
         * 数据发送超时进行的回调
         *
         * @param currentPackageIndex 当前发送超时的包数
         * @param pageCount           总包数
         * @param data                发送超时的数据
         */
        @Override
        public void onSendTimeOut(int currentPackageIndex, int pageCount, @NonNull byte[] data) {
            super.onSendTimeOut(currentPackageIndex, pageCount, data);
            Tool.toast(ConnectActivity.this, "onSendTimeOut: " + currentPackageIndex + " / " + pageCount, toastKeepTime);
        }

        /**
         * 数据发送超时,尝试重发数据时进行的回调
         *
         * @param tryCount            重发次数
         * @param currentPackageIndex 当前重发的包数
         * @param pageCount           总包数
         * @param data                重发的数据内容
         */
        @Override
        public void onSendTimeOutAndRetry(int tryCount, int currentPackageIndex, int pageCount, @NonNull byte[] data) {
            super.onSendTimeOutAndRetry(tryCount, currentPackageIndex, pageCount, data);
            Tool.warnOut(TAG, "onSendTimeOut: tryCount = " + tryCount + " " + currentPackageIndex + " / " + pageCount);
        }
    };
    private DefaultLargeDataWriteWithNotificationSendStateChangedListener defaultLargeDataWriteWithNotificationSendStateChangedListener = new DefaultLargeDataWriteWithNotificationSendStateChangedListener() {
        /**
         * 收到远端设备的通知时进行的回调
         *
         * @param currentPackageData 当前包数据
         * @param currentPackageCount 当前包数
         * @param packageCount 总包数
         * @param values 远端设备的通知内容
         * @return true表示可以继续下一包发送，false表示传输出错
         */
        @Override
        public boolean onReceiveNotification(byte[] currentPackageData, int currentPackageCount, int packageCount, @NonNull byte[] values) {
            super.onReceiveNotification(currentPackageData, currentPackageCount, packageCount, values);
            return true;
        }

        /**
         * 数据发送完成
         */
        @Override
        public void onDataSendFinished() {
            super.onDataSendFinished();
            Tool.toastL(ConnectActivity.this, "onDataSendFinished");
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
            super.onDataSendFailed(currentPackageCount, pageCount, data);
            Tool.toastL(ConnectActivity.this, "onDataSendFailed");
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
        public void onDataSendFailedAndRetry(int currentPackageCount, int pageCount, @NonNull byte[] data, int tryCount) {
            super.onDataSendFailedAndRetry(currentPackageCount, pageCount, data, tryCount);
        }

        /**
         * 数据发送进度有更改
         *
         * @param currentPackageCount 当前包数
         * @param pageCount           总包数
         * @param data                当前包数据内容
         */
        @Override
        public void onDataSendProgressChanged(int currentPackageCount, int pageCount, @NonNull byte[] data) {
            super.onDataSendProgressChanged(currentPackageCount, pageCount, data);
            Tool.toast(ConnectActivity.this, currentPackageCount + " / " + pageCount, toastKeepTime);
        }

        /**
         * 因为通知返回的数据出错而导致的传输失败
         */
        @Override
        public void onSendFailedWithWrongNotifyData() {
            super.onSendFailedWithWrongNotifyData();
            Tool.toast(ConnectActivity.this, "onSendFailedWithWrongNotifyData", toastKeepTime);
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
        public void onSendFailedWithWrongNotifyDataAndRetry(int tryCount, int currentPackageIndex, int packageCount, @NonNull byte[] data) {
            super.onSendFailedWithWrongNotifyDataAndRetry(tryCount, currentPackageIndex, packageCount, data);
        }

        /**
         * 在一段时间内没有收到通知回复时，判定为超时
         *
         * @param currentPackageIndex 当前发送超时的包数
         * @param packageCount        总包数
         * @param data                发送超时的包数据
         */
        @Override
        public void onDataSendTimeOut(int currentPackageIndex, int packageCount, @NonNull byte[] data) {
            super.onDataSendTimeOut(currentPackageIndex, packageCount, data);
            Tool.toast(ConnectActivity.this, "onDataSendTimeOut", toastKeepTime);
        }

        /**
         * 通知回复超时时，进行重发尝试时的回调
         *
         * @param data                重发的数据
         * @param tryCount            重试次数
         * @param currentPackageIndex 当前尝试重发的包数
         * @param packageCount        总包数
         */
        @Override
        public void onDataSendTimeOutAndRetry(@NonNull byte[] data, int tryCount, int currentPackageIndex, int packageCount) {
            super.onDataSendTimeOutAndRetry(data, tryCount, currentPackageIndex, packageCount);
        }
    };
    private OnBleConnectStateChangedListener onBleConnectStateChangedListener = new OnBleConnectStateChangedListener() {
        @Override
        public void connected() {
            //记录是否连接成功的标志，同时也记录本方法是否已经被回调,避免部分手机多次回调
            if (isLinked) {
                return;
            }
            isLinked = true;
            //连接成功，将指示标志设置为蓝色
            customTextCircleView.setColor(Color.BLUE);
        }

        @Override
        public void disconnected() {
            //如果还没有连接上，不做任何操作
            if (!isLinked) {
                return;
            }
            //重置连接标志
            isLinked = false;
            //重置服务扫描状态标志
            serviceDiscovered = false;
            //断开连接，将指示标志设置为红色
            customTextCircleView.setColor(Color.RED);
            Tool.toastL(ConnectActivity.this, R.string.disconnect);
        }

        @Override
        public void gattStatusError(int errorStatus) {
            Tool.warnOut(TAG, "连接出错，状态码：" + errorStatus);
            Tool.toastL(ConnectActivity.this, "连接出错，状态码：" + errorStatus);
            bleConnector.close();
            onBackPressed();
        }

        @Override
        public void connecting() {
            customTextCircleView.setColor(Color.YELLOW);
        }

        @Override
        public void autoDiscoverServicesFailed() {
            Tool.warnOut(TAG, "远端设备服务列表扫描失败");
        }

        @Override
        public void disconnecting() {
            Tool.warnOut(TAG, "onDisconnecting");
        }

        @Override
        public void unknownStatus(int statusCode) {

        }

        @Override
        public void gattPerformTaskFailed(int errorStatus, String methodName) {

        }

        @Override
        public void servicesDiscovered() {
            //记录本方法是否已经被回调，避免部分手机多次回调
            if (serviceDiscovered) {
                return;
            }
            serviceDiscovered = true;

            //服务发现完成，将指示标志设置为绿色（对BLE远端设备的所有操作都在服务扫描完成之后）
            customTextCircleView.setColor(Color.GREEN);

            Tool.toastL(ConnectActivity.this, R.string.connect_success);

            //获取服务列表
            List<BluetoothGattService> deviceServices = bleConnector.getServices();

            if (deviceServices != null) {

                for (int i = 0; i < deviceServices.size(); i++) {
                    BluetoothGattService bluetoothGattService = deviceServices.get(i);
                    String serviceUuidString = bluetoothGattService.getUuid().toString();
                    Tool.warnOut(TAG, "bluetoothGattService UUID = " + serviceUuidString);

                    ServiceUuidItem serviceUuidItem = new ServiceUuidItem(BleUtils.getServiceUuidName(serviceUuidString), serviceUuidString);
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristics.get(j);
                        String characteristicUuidString = bluetoothGattCharacteristic.getUuid().toString();
                        boolean canRead = bleConnector.canRead(serviceUuidString, characteristicUuidString);
                        boolean canWrite = bleConnector.canWrite(serviceUuidString, characteristicUuidString);
                        boolean canNotify = bleConnector.canNotify(serviceUuidString, characteristicUuidString);
                        CharacteristicUuidItem characteristicUuidItem = new CharacteristicUuidItem(BleUtils.getServiceUuidName(characteristicUuidString), characteristicUuidString, canRead, canWrite, canNotify);
                        serviceUuidItem.addSubItem(characteristicUuidItem);
                    }
                    adapterData.add(serviceUuidItem);
                }

                servicesCharacteristicsListAdapter.notifyDataSetChanged();
                if (bleConnector.refreshGattCache()) {
                    Tool.toastL(ConnectActivity.this, R.string.uuid_refresh_success);
                } else {
                    Tool.toastL(ConnectActivity.this, R.string.uuid_refresh_failed);
                }
            }

            //提取设备名与设备地址
            nameTv.setText(bluetoothDevice.getName());
            addressTv.setText(bluetoothDevice.getAddress());

            //请求更改mtu
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                int mtu = 24;
                if (bleConnector.requestMtu(mtu)) {
                    Tool.warnOut(TAG, mtu + " 字节MTU请求成功");
                } else {
                    Tool.warnOut(TAG, mtu + " 字节MTU请求失败");
                }
            } else {
                Tool.warnOut(TAG, "系统版本过低，无法请求更新MTU");
            }
        }

        @Override
        public void readCharacteristicData(BluetoothGattCharacteristic characteristic, byte[] data) {
            String hexStr = Tool.bytesToHexStr(data);
            String str = new String(data);
            Tool.warnOut(TAG, "读取到的数据 = " + hexStr);
            showReadDataResultDialog(hexStr, str);
        }

        @Override
        public void writeCharacteristicData(BluetoothGattCharacteristic characteristic, byte[] data) {
            String hexStr = Tool.bytesToHexStr(data);
            Tool.warnOut(TAG, "onCharacteristicWrite hexStr = " + hexStr);
        }

        @Override
        public void receivedNotification(BluetoothGattCharacteristic characteristic, byte[] data) {
            String hexStr = Tool.bytesToHexStr(data);
            String str = new String(data);
            Tool.warnOut("ConnectActivity", "value = " + hexStr);
            showReceiveNotificationDialog(hexStr, str);
        }

        @Override
        public void readDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor, byte[] data) {

        }

        @Override
        public void writeDescriptor(BluetoothGattDescriptor bluetoothGattDescriptor, byte[] data) {

        }

        @Override
        public void reliableWriteCompleted() {

        }

        @Override
        public void readRemoteRssi(int rssi) {
            Tool.warnOut("ConnectActivity", "rssi = " + rssi);
        }

        @Override
        public void mtuChanged(int mtu) {
            Tool.warnOut(TAG, "onMtuChanged:mtu = " + mtu);
        }

        @Override
        public void phyUpdate(int txPhy, int rxPhy) {

        }

        @Override
        public void readPhy(int txPhy, int rxPhy) {

        }

        @Override
        public void onCloseComplete() {
            BleManager.releaseBleConnector();
            finish();
        }

        @Override
        public void onConnectTimeOut() {
            Tool.toastL(ConnectActivity.this, R.string.connect_time_out);
            onBackPressed();
        }
    };
    private OnDeviceBondStateChangedListener onDeviceBondStateChangedListener = new OnDeviceBondStateChangedListener() {
        @Override
        public void onDeviceBinding() {
            Tool.warnOut(TAG, "绑定中");
            Tool.toastL(ConnectActivity.this, "绑定中");
        }

        @Override
        public void onDeviceBonded() {
            Tool.warnOut(TAG, "绑定成功");
            Tool.toastL(ConnectActivity.this, "绑定成功");
            //发起连接
            startConnect();
        }

        @Override
        public void onDeviceBindNone() {
            Tool.warnOut(TAG, "绑定失败");
            Tool.toastL(ConnectActivity.this, "绑定失败");
            onBackPressed();
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
        //获取intent,因为蓝牙的对象从上一个activity通过intent传递
        Intent intent = getIntent();
        //获取BleDevice对象
        BleDevice bleDevice = (BleDevice) intent.getSerializableExtra(Constants.DEVICE);
        if (bleDevice == null) {
            Tool.toastL(ConnectActivity.this, R.string.device_info_error);
            finish();
            return;
        }
        //获取蓝牙实例
        bluetoothDevice = bleDevice.getBluetoothDevice();
        //初始化BLE连接工具
        initBleConnector();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_connect;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        servicesCharacteristicsListAdapter = new ServicesCharacteristicsListAdapter(adapterData);
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        customTextCircleView = findViewById(R.id.custom_text_circle_view);
        nameTv = findViewById(R.id.device_name);
        addressTv = findViewById(R.id.device_address);
        recyclerView = findViewById(R.id.services_characteristics_list);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initRecyclerView();
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
        servicesCharacteristicsListAdapter.setOnCharacteristicClickListener(onCharacteristicClickListener);
    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
        //发起连接
        startConnect();

//        /*
//         * 调用绑定的方法（如果需要绑定)，否则请直接调用连接的方法
//         * 注意：如果该设备不支持绑定，会直接回调绑定成功的回调，在绑定成功的回调中发起连接即可
//         * 第一次绑定某一个设备会触发回调，之后再次绑定，可根据绑定时的函数的返回值来判断绑定状态，以进行下一步操作
//         */
//        switch (bleConnector.startBound(bluetoothDevice.getAddress())) {
//            case BleConstants.DEVICE_BOND_START_SUCCESS:
//                Tool.warnOut(TAG, "开始绑定");
//                Tool.toastL(this, "开始绑定");
//                break;
//            case BleConstants.DEVICE_BOND_START_FAILED:
//                Tool.warnOut(TAG, "发起绑定失败");
//                Tool.toastL(this, "发起绑定失败");
//                break;
//            case BleConstants.DEVICE_BOND_BONDED:
//                Tool.warnOut(TAG, "此设备已经被绑定了");
//                Tool.toastL(this, "此设备已经被绑定了");
//                startConnect();
//                break;
//            case BleConstants.DEVICE_BOND_BONDING:
//                Tool.warnOut(TAG, "此设备正在绑定中");
//                Tool.toastL(this, "此设备正在绑定中");
//                break;
//            case BleConstants.BLUETOOTH_ADAPTER_NULL:
//                Tool.warnOut(TAG, "没有蓝牙适配器存在");
//                Tool.toastL(this, "没有蓝牙适配器存在");
//                break;
//            case BleConstants.BLUETOOTH_ADDRESS_INCORRECT:
//                Tool.warnOut(TAG, "蓝牙地址错误");
//                Tool.toastL(this, "蓝牙地址错误");
//                break;
//            case BleConstants.BLUETOOTH_MANAGER_NULL:
//                Tool.warnOut(TAG, "没有蓝牙管理器存在");
//                Tool.toastL(this, "没有蓝牙管理器存在");
//                break;
//            default:
//                Tool.warnOut(TAG, "default");
//                break;
//        }
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
    public void onBackPressed() {
//        if (bleConnector.unBound()) {
//            Tool.warnOut(TAG, "解绑成功");/
//        } else {
//            Tool.warnOut(TAG, "解绑失败");/
//        }

        //屏蔽返回键
        /*super.onBackPressed();*/
        if (bleConnector == null) {
            super.onBackPressed();
            return;
        }
        //关闭连接工具,如果返回false,直接调用super.onBackPressed()，否则在close的回调中调用返回
        if (!bleConnector.close()) {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        customTextCircleView = null;
        bleConnector = null;
        isLinked = false;
        serviceDiscovered = false;
        nameTv = null;
        addressTv = null;
        bluetoothDevice = null;
        recyclerView.setAdapter(null);
        recyclerView.setLayoutManager(null);
        recyclerView.removeItemDecoration(defaultItemDecoration);
        defaultItemDecoration = null;
        recyclerView = null;
        adapterData = null;
        servicesCharacteristicsListAdapter = null;
        onCharacteristicClickListener = null;
        defaultOnLargeDataSendStateChangedListener = null;
        defaultLargeDataWriteWithNotificationSendStateChangedListener = null;
        BleManager.releaseBleConnector();
    }

    /**
     * 初始化连接工具
     */
    private void initBleConnector() {
        //创建BLE连接器实例
        bleConnector = BleManager.getBleConnectorInstance();
        //如果手机不支持蓝牙的话，这里得到的是null,所以需要进行判空
        if (bleConnector == null) {
            Tool.toastL(ConnectActivity.this, R.string.ble_not_supported);
            return;
        }
        bleConnector.setConnectTimeOut(60000);
        setConnectListener();
    }

    /**
     * 设置相关的回调
     */
    private void setConnectListener() {
        //设置 连接 相关的回调
        bleConnector.setOnBleConnectStateChangedListener(onBleConnectStateChangedListener);
        //设置 绑定状态被更改时的回调
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            bleConnector.setOnDeviceBondStateChangedListener(onDeviceBondStateChangedListener);
        }
        //设置发送分包数据时，每一包数据之间的延时
        bleConnector.setSendLargeDataPackageDelayTime(500);
        //设置发送分包数据时，每一包数据发送超时的时间
        bleConnector.setSendLargeDataTimeOut(10000);
    }

    /**
     * 显示收到的通知
     *
     * @param hexStr 收到的通知（十六进制字符串）
     * @param str    收到的通知
     */
    private void showReceiveNotificationDialog(String hexStr, String str) {
        EditText editText = (EditText) View.inflate(this, R.layout.dialog_show_notifycation_data, null);
        String s = hexStr + "(" + str + ")";
        editText.setText(s);
        new AlertDialog.Builder(this)
                .setTitle(R.string.notification_data)
                .setView(editText)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /**
     * 读到数据后显示数据内容
     *
     * @param hexStr 读到的数据（十六进制字符串）
     * @param str    读到的数据
     */
    private void showReadDataResultDialog(String hexStr, String str) {
        EditText editText = (EditText) View.inflate(this, R.layout.dialog_show_read_data, null);
        String s = hexStr + "(" + str + ")";
        editText.setText(s);
        new AlertDialog.Builder(this)
                .setTitle(R.string.read_data)
                .setView(editText)
                .setCancelable(true)
                .setPositiveButton(R.string.ok, null)
                .show();
    }

    /**
     * 发起连接
     */
    private void startConnect() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (bleConnector.isInitialized()) {
                        break;
                    }
                }
                //先设置要连接的设备
                if (bleConnector.connect(bluetoothDevice, true)) {
                    Tool.warnOut("开始连接");
                    BleManager.getHANDLER().post(new Runnable() {
                        @Override
                        public void run() {
                            Tool.toastL(ConnectActivity.this, "发起连接");
                            customTextCircleView.setColor(Color.YELLOW);
                        }
                    });
                } else {
                    Tool.warnOut("发起连接失败");
                }
            }
        };
        BleManager.getThreadFactory().newThread(runnable).start();

    }

    /**
     * 初始化RecyclerView的数据
     */
    private void initRecyclerView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.addItemDecoration(defaultItemDecoration);
        recyclerView.setAdapter(servicesCharacteristicsListAdapter);
    }

    /**
     * 显示操作方式的对话框
     *
     * @param serviceUUID        服务UUID
     * @param characteristicUUID 特征UUID
     */
    private void showOptionsDialog(final String serviceUUID, final String characteristicUUID) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.select_options)
                .setItems(R.array.bleOptions, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        switch (which) {
                            //读
                            case 0:
                                if (!bleConnector.canRead(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.read_not_support);
                                    return;
                                }
                                boolean readData = bleConnector.readData(serviceUUID, characteristicUUID);
                                if (!readData) {
                                    Tool.toastL(ConnectActivity.this, R.string.read_failed);
                                    return;
                                }
                                Tool.toastL(ConnectActivity.this, R.string.request_sent);
                                break;
                            //写
                            case 1:
                                if (!bleConnector.canWrite(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.write_not_support);
                                    return;
                                }
                                showWriteDataDialog(serviceUUID, characteristicUUID);
                                break;
                            //打开通知
                            case 2:
                                if (!bleConnector.canNotify(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.notify_not_support);
                                    return;
                                }
                                bleConnector.addOnBleDescriptorWriteListener(new OnBleDescriptorWriteListener() {

                                    /**
                                     * descriptor data write successful
                                     *
                                     * @param bluetoothGattDescriptor BluetoothGattDescriptor
                                     * @param data                    descriptor
                                     */
                                    @Override
                                    public void onBleDescriptorWrite(BluetoothGattDescriptor bluetoothGattDescriptor, byte[] data) {
                                        Tool.toastL(ConnectActivity.this, R.string.open_notification_success);
                                        bleConnector.removeOnBleDescriptorWriteListener(this);
//                                        bleConnector.addOnBleReceiveNotificationListener(onReceiveNotificationListener);
                                    }
                                });
                                boolean openNotification = bleConnector.enableNotification(serviceUUID, characteristicUUID, true);
                                if (!openNotification) {
                                    Tool.toastL(ConnectActivity.this, R.string.open_notification_failed);
                                    return;
                                }
                                Tool.toastL(ConnectActivity.this, R.string.request_sent);
                                break;
                            //写入超长数据,自动格式化（分包传输）
                            case 3:
                                if (!bleConnector.canWrite(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.write_not_support);
                                    return;
                                }
                                showWriteBigDataDialog(serviceUUID, characteristicUUID, true);
                                break;
                            //写入超长数据，自动格式化（分包传输且需要通知处理）
                            case 4:
                                if (!bleConnector.canWrite(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.write_not_support);
                                    return;
                                }
                                if (!bleConnector.canNotify(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.notify_not_support);
                                    return;
                                }
                                showWriteBigDataWithNotifyDialog(serviceUUID, characteristicUUID, true);
                                break;
                            //写入超长数据,不自动格式化（分包传输）
                            case 5:
                                if (!bleConnector.canWrite(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.write_not_support);
                                    return;
                                }
                                showWriteBigDataDialog(serviceUUID, characteristicUUID, false);
                                break;
                            //写入超长数据，不自动格式化（分包传输且需要通知处理）
                            case 6:
                                if (!bleConnector.canWrite(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.write_not_support);
                                    return;
                                }
                                if (!bleConnector.canNotify(serviceUUID, characteristicUUID)) {
                                    Tool.toastL(ConnectActivity.this, R.string.notify_not_support);
                                    return;
                                }
                                showWriteBigDataWithNotifyDialog(serviceUUID, characteristicUUID, false);
                                break;
                            default:
                                break;
                        }
                    }
                })
                .setCancelable(false)
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showWriteBigDataWithNotifyDialog(final String serviceUUID, final String characteristicUUID, final boolean autoFormat) {
        final EditText editText = (EditText) View.inflate(this, R.layout.dialog_show_write_big_data_with_notify, null);
        EditTextWatcherForHexData editTextWatcherForHexData = new EditTextWatcherForHexData(editText);
        editText.addTextChangedListener(editTextWatcherForHexData);
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        editText.setText(Tool.bytesToHexStr(bytes));
        new AlertDialog.Builder(this)
                .setTitle(R.string.input_data)
                .setView(editText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString();
                        if ("".equals(text)) {
                            Tool.toastL(ConnectActivity.this, R.string.set_nothing);
                            showWriteDataDialog(serviceUUID, characteristicUUID);
                            return;
                        }
                        text = text.replace(" ", "");
                        byte[] bytes = Tool.hexStrToBytes(text);
                        bleConnector.writeLargeDataWithNotification(serviceUUID, characteristicUUID, bytes, defaultLargeDataWriteWithNotificationSendStateChangedListener, autoFormat);

                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show();
    }

    private void showWriteBigDataDialog(final String serviceUUID, final String characteristicUUID, final boolean autoFormat) {
        final EditText editText = (EditText) View.inflate(this, R.layout.dialog_show_write_big_data, null);
        EditTextWatcherForHexData editTextWatcherForHexData = new EditTextWatcherForHexData(editText);
        editText.addTextChangedListener(editTextWatcherForHexData);
        byte[] bytes = new byte[256];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) i;
        }
        editText.setText(Tool.bytesToHexStr(bytes));
        new AlertDialog.Builder(this)
                .setTitle(R.string.input_data)
                .setView(editText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString();
                        if ("".equals(text)) {
                            Tool.toastL(ConnectActivity.this, R.string.set_nothing);
                            showWriteDataDialog(serviceUUID, characteristicUUID);
                            return;
                        }
                        text = text.replace(" ", "");
                        byte[] bytes = Tool.hexStrToBytes(text);
                        bleConnector.writeLargeData(serviceUUID, characteristicUUID, bytes, defaultOnLargeDataSendStateChangedListener, autoFormat);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show();
    }

    private void showWriteDataDialog(final String serviceUUID, final String characteristicUUID) {
        final EditText editText = (EditText) View.inflate(this, R.layout.dialog_show_write_data, null);
        EditTextWatcherForHexDataWithin20 editTextWatcherForHexData = new EditTextWatcherForHexDataWithin20(editText);
        editText.addTextChangedListener(editTextWatcherForHexData);
        new AlertDialog.Builder(this)
                .setTitle(R.string.input_data)
                .setView(editText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString();
                        if ("".equals(text)) {
                            Tool.toastL(ConnectActivity.this, R.string.set_nothing);
                            showWriteDataDialog(serviceUUID, characteristicUUID);
                            return;
                        }
                        text = text.replace(" ", "");
                        byte[] bytes = Tool.hexStrToBytes(text);
                        boolean b = bleConnector.writeData(serviceUUID, characteristicUUID, bytes);
                        if (b) {
                            Tool.toastL(ConnectActivity.this, R.string.write_success);
                        } else {
                            Tool.toastL(ConnectActivity.this, R.string.write_failed);
                        }
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show();
    }
}
