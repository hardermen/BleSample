package cn.almsound.www.myblesample.activity.bleconnect;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
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
import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.DefaultItemDecoration;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.BleConnector;
import com.jackiepenghe.blelibrary.BleDevice;
import com.jackiepenghe.blelibrary.BleInterface;
import com.jackiepenghe.blelibrary.BleManager;

import java.util.ArrayList;
import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.ServicesCharacteristicsListAdapter;
import cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity.CharacteristicUuidItem;
import cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity.ServiceUuidItem;
import cn.almsound.www.myblesample.utils.Constants;
import cn.almsound.www.myblesample.watcher.EditTextWatcherForHexData;
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

    /**
     * 连接成功的回调（在设备连接成功之后会触发此回调）
     */
    private BleInterface.OnConnectedListener onConnectedListener = new BleInterface.OnConnectedListener() {
        @Override
        public void onConnected() {
            //记录是否连接成功的标志，同时也记录本方法是否已经被回调,避免部分手机多次回调
            if (isLinked) {
                return;
            }
            isLinked = true;
            //连接成功，将指示标志设置为蓝色
            customTextCircleView.setColor(Color.BLUE);
        }
    };
    /**
     * 连接成功后会扫描远端设备的服务，在服务扫描完成之后会触发此回调
     */
    private BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener = new BleInterface.OnServicesDiscoveredListener() {
        @Override
        public void onServicesDiscovered() {
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

                    ServiceUuidItem serviceUuidItem = new ServiceUuidItem(serviceUuidString);
                    List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                    for (int j = 0; j < characteristics.size(); j++) {
                        BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristics.get(j);
                        String characteristicUuidString = bluetoothGattCharacteristic.getUuid().toString();
                        boolean canRead = bleConnector.canRead(serviceUuidString, characteristicUuidString);
                        boolean canWrite = bleConnector.canWrite(serviceUuidString, characteristicUuidString);
                        boolean canNotify = bleConnector.canNotify(serviceUuidString, characteristicUuidString);
                        CharacteristicUuidItem characteristicUuidItem = new CharacteristicUuidItem(characteristicUuidString, canRead, canWrite, canNotify);
                        serviceUuidItem.addSubItem(characteristicUuidItem);
                    }
                    adapterData.add(serviceUuidItem);
                }

                servicesCharacteristicsListAdapter.notifyDataSetChanged();
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
    };
    /**
     * 与远端设备断开连接后触发此回调
     */
    private BleInterface.OnDisconnectedListener onDisconnectedListener = new BleInterface.OnDisconnectedListener() {
        @Override
        public void onDisconnected() {
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
        }
    };
    /**
     * 读取到远端设备数据后会触发此回调,返回的数据是一个byte数组
     */
    private BleInterface.OnCharacteristicReadListener onCharacteristicReadListener = new BleInterface.OnCharacteristicReadListener() {
        @Override
        public void onCharacteristicRead(String uuid, byte[] values) {
            String hexStr = Tool.bytesToHexStr(values);
            String str = new String(values);
            Tool.warnOut(TAG, "读取到的数据 = " + hexStr);
            showReadDataResultDialog(hexStr, str);
        }
    };
    /**
     * 正在连接时触发此回调（不过此回调从来没有被触发过，我也不知道为何）
     */
    private BleInterface.OnConnectingListener onConnectingListener = new BleInterface.OnConnectingListener() {
        @Override
        public void onConnecting() {
            customTextCircleView.setColor(Color.YELLOW);
        }
    };
    /**
     * 收到远端设备的主动通知时，触发此回调
     */
    private BleInterface.OnReceiveNotificationListener onReceiveNotificationListener = new BleInterface.OnReceiveNotificationListener() {
        @Override
        public void onReceiveNotification(String uuid,byte[] values) {
            String hexStr = Tool.bytesToHexStr(values);
            String str = new String(values);
            Tool.warnOut("ConnectActivity", "value = " + hexStr);
            showReceiveNotificationDialog(hexStr, str);
        }
    };
    /**
     * 取到远端设备的RSSI值时触发此回调
     */
    private BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener = new BleInterface.OnReadRemoteRssiListener() {
        @Override
        public void onReadRemoteRssi(int rssi) {
            Tool.warnOut("ConnectActivity", "rssi = " + rssi);
        }
    };
    /**
     * 当连接工具调用Close方法之后，在连接工具彻底关闭时会触发此回调
     * 最好是屏蔽onBackPressed()方法，onBackPressed()方法中只调用bleConnector.closeAll().然后在这个方法中回调super.onBackPressed()结束activity
     * 一定要在这个回调中做结束activity的操作，不要直接在onDestroy中调用close避免连接工具还没有彻底关闭，activity就结束造成内存泄漏
     */
    BleInterface.OnCloseCompleteListener onCloseCompleteListener = new BleInterface.OnCloseCompleteListener() {
        @Override
        public void onCloseComplete() {
            ConnectActivity.super.onBackPressed();
        }
    };
    /**
     * 设备的绑定(也可以说配对)状态改变后触发此回调
     */
    BleInterface.OnDeviceBondStateChangedListener onBondStateChangedListener = new BleInterface.OnDeviceBondStateChangedListener() {
        /**
         * 正在绑定设备
         */
        @Override
        public void onDeviceBinding() {
            Tool.warnOut(TAG, "绑定中");
            Tool.toastL(ConnectActivity.this, "绑定中");
        }

        /**
         * 绑定完成
         */
        @Override
        public void onDeviceBonded() {
            Tool.warnOut(TAG, "绑定成功");
            Tool.toastL(ConnectActivity.this, "绑定成功");
            //发起连接
            startConnect();
        }

        /**
         * 取消绑定或者绑定失败
         */
        @Override
        public void onDeviceBindNone() {
            Tool.warnOut(TAG, "绑定失败");
            Tool.toastL(ConnectActivity.this, "绑定失败");
            startConnect();
        }
    };
    BleInterface.OnMtuChangedListener onMtuChangedListener = new BleInterface.OnMtuChangedListener() {
        @Override
        public void onMtuChanged(int mtu) {
            Tool.warnOut(TAG, "onMtuChanged:mtu = " + mtu);
        }
    };
    private BleInterface.OnDisconnectingListener onDisconnectingListener = new BleInterface.OnDisconnectingListener() {
        @Override
        public void onDisconnecting() {
            Tool.warnOut(TAG, "onDisconnecting");
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
        //获取Bundle
        Bundle bundleExtra = intent.getBundleExtra(Constants.BUNDLE);
        //获取BleDevice对象
        BleDevice bleDevice = bundleExtra.getParcelable(Constants.DEVICE);
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
//        switch (bleConnector.startBound(address)) {
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
        //关闭连接工具,如果返回false,直接调用super.onBackPressed()，否则在close的回调中调用返回
        if (!bleConnector.close()) {
            super.onBackPressed();
        }
    }

    /**
     * 初始化连接工具
     */
    private void initBleConnector() {
        //创建BLE连接器实例
        bleConnector = BleManager.getBleConnectorInstance(ConnectActivity.this);
        //如果手机不支持蓝牙的话，这里得到的是null,所以需要进行判空
        if (bleConnector == null) {
            Tool.toastL(ConnectActivity.this, R.string.ble_not_supported);
            return;
        }
        //bleConnector.setOnConnectingListener(onConnectingListener);//
        //设置连接设备成功的回调
        bleConnector.setOnConnectedListener(onConnectedListener);
        //设置连接之后，服务发现完成的回调
        bleConnector.setOnServicesDiscoveredListener(onServicesDiscoveredListener);
        //bleConnector.setOnDisconnectingListener(onDisconnectingListener);//
        //设置连接被断开的回调
        bleConnector.setOnDisconnectedListener(onDisconnectedListener);
        //设置 读取到设备的数据时的回调
        bleConnector.setOnCharacteristicReadListener(onCharacteristicReadListener);
        //设置 收到设备发来的通知时的回调
        bleConnector.setOnReceiveNotificationListener(onReceiveNotificationListener);
        //设置 获取设备的RSSI的回调
        bleConnector.setOnReadRemoteRssiListener(onReadRemoteRssiListener);
        //设置 连接器关闭时的回调
        bleConnector.setOnCloseCompleteListener(onCloseCompleteListener);
        //设置 绑定状态被更改时的回调
        bleConnector.setOnBondStateChangedListener(onBondStateChangedListener);
        //设置 Mtu参数被更改时的回调
        bleConnector.setOnMtuChangedListener(onMtuChangedListener);
    }

    /**
     * 显示收到的通知
     *
     * @param hexStr 收到的通知（十六进制字符串）
     * @param str    收到的通知
     */
    private void showReceiveNotificationDialog(String hexStr, String str) {
        EditText editText = (EditText) View.inflate(this, R.layout.edit_text, null);
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
        EditText editText = (EditText) View.inflate(this, R.layout.edit_text, null);
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
        //先设置要连接的设备
        if (bleConnector.checkAndSetDevice(bluetoothDevice)) {
            //发起连接
            if (bleConnector.startConnect()) {
                Tool.warnOut("开始连接");
                Tool.toastL(ConnectActivity.this, "发起连接");
                customTextCircleView.setColor(Color.YELLOW);
            } else {
                Tool.warnOut("发起连接失败");
            }
        }
    }

    /**
     * 初始化RecyclerView的数据
     */
    private void initRecyclerView() {
        DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY, ViewGroup.LayoutParams.MATCH_PARENT, 2, -1);
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
                                }
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
                                    Tool.toastL(ConnectActivity.this, R.string.write_not_notify);
                                    return;
                                }
                                boolean openNotification = bleConnector.enableNotification(serviceUUID, characteristicUUID, true);
                                if (!openNotification) {
                                    Tool.toastL(ConnectActivity.this, R.string.open_notification_failed);
                                } else {
                                    Tool.toastL(ConnectActivity.this, R.string.open_notification_success);
                                }
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

    private void showWriteDataDialog(final String serviceUUID, final String characteristicUUID) {
        final EditText editText = (EditText) View.inflate(this, R.layout.edit_text, null);
        EditTextWatcherForHexData editTextWatcherForHexData = new EditTextWatcherForHexData(editText);
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
