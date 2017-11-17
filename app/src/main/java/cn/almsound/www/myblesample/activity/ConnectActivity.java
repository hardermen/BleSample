package cn.almsound.www.myblesample.activity;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.jackiepenghe.baselibrary.BaseAppcompatActivity;
import com.jackiepenghe.blelibrary.BleConnector;
import com.jackiepenghe.blelibrary.BleDevice;
import com.jackiepenghe.blelibrary.BleInterface;
import com.jackiepenghe.blelibrary.BleManager;

import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.utils.Constants;
import cn.almsound.www.myblesample.utils.ConversionUtil;
import cn.almsound.www.myblesample.utils.LogUtil;
import cn.almsound.www.myblesample.utils.ToastUtil;
import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 * 连接设备的界面
 *
 * @author jacke
 */
public class ConnectActivity extends BaseAppcompatActivity {

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
     * 设备地址
     */
    private String address;
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
            ToastUtil.l(ConnectActivity.this, R.string.device_info_error);
            finish();
            return;
        }
        //获取蓝牙实例
        bluetoothDevice = bleDevice.getBluetoothDevice();
        //获取蓝牙地址
        address = bluetoothDevice.getAddress();
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

    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        customTextCircleView =  findViewById(R.id.custom_text_circle_view);
        nameTv =  findViewById(R.id.device_name);
        addressTv =  findViewById(R.id.device_address);
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
        //发起连接
        startConnect();

//        /*
//         * 调用绑定的方法（如果需要绑定)，否则请直接调用连接的方法
//         * 注意：如果该设备不支持绑定，会直接回调绑定成功的回调，在绑定成功的回调中发起连接即可
//         * 第一次绑定某一个设备会触发回调，之后再次绑定，可根据绑定时的函数的返回值来判断绑定状态，以进行下一步操作
//         */
//        switch (bleConnector.startBound(address)) {
//            case BleConstants.DEVICE_BOND_START_SUCCESS:
//                LogUtil.w(TAG, "开始绑定");
//                break;
//            case BleConstants.DEVICE_BOND_START_FAILED:
//                LogUtil.w(TAG, "发起绑定失败");
//                break;
//            case BleConstants.DEVICE_BOND_BONDED:
//                LogUtil.w(TAG, "此设备已经被绑定了");
//                startConnect();
//                break;
//            case BleConstants.DEVICE_BOND_BONDING:
//                LogUtil.w(TAG, "此设备正在绑定中");
//                break;
//            case BleConstants.BLUETOOTH_ADAPTER_NULL:
//                LogUtil.w(TAG, "没有蓝牙适配器存在");
//                break;
//            case BleConstants.BLUETOOTH_ADDRESS_INCORRECT:
//                LogUtil.w(TAG, "蓝牙地址错误");
//                break;
//            case BleConstants.BLUETOOTH_MANAGER_NULL:
//                LogUtil.w(TAG, "没有蓝牙管理器存在");
//                break;
//            default:
//                LogUtil.w(TAG, "default");
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
        bleConnector = BleManager.newBleConnector(ConnectActivity.this);
        //创建连接成功的回调（在设备连接成功之后会触发此回调）
        BleInterface.OnConnectedListener onConnectedListener = new BleInterface.OnConnectedListener() {
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
        //连接成功后会扫描远端设备的服务，在服务扫描完成之后会触发此回调
        BleInterface.OnServicesDiscoveredListener onServicesDiscoveredListener = new BleInterface.OnServicesDiscoveredListener() {
            @Override
            public void onServicesDiscovered() {
                //记录本方法是否已经被回调，避免部分手机多次回调
                if (serviceDiscovered) {
                    return;
                }
                serviceDiscovered = true;

                //服务发现完成，将指示标志设置为绿色（对BLE远端设备的所有操作都在服务扫描完成之后）
                customTextCircleView.setColor(Color.GREEN);

                ToastUtil.l(ConnectActivity.this, R.string.connect_success);

                //获取服务列表
                List<BluetoothGattService> deviceServices = bleConnector.getServices();

                if (deviceServices != null) {

                    for (int i = 0; i < deviceServices.size(); i++) {
                        BluetoothGattService bluetoothGattService = deviceServices.get(i);
                        LogUtil.w(TAG, "service UUID = " + bluetoothGattService.getUuid().toString());

                        List<BluetoothGattCharacteristic> characteristics = bluetoothGattService.getCharacteristics();
                        for (int j = 0; j < characteristics.size(); j++) {
                            BluetoothGattCharacteristic bluetoothGattCharacteristic = characteristics.get(j);
                            LogUtil.w(TAG, "bluetoothGattCharacteristic UUID = " + bluetoothGattCharacteristic.getUuid().toString());
                        }
                    }
                }

                //提取设备名与设备地址
                nameTv.setText(bluetoothDevice.getName());
                addressTv.setText(bluetoothDevice.getAddress());
                String serviceUUID = "C3E6FEA0-E966-1000-8000-BE99C223DF6A";
                String chaUUID = "C3E6FEA2-E966-1000-8000-BE99C223DF6A";
                if (!bleConnector.openNotification(serviceUUID, chaUUID)) {
                    LogUtil.w(TAG, "open notification failed");
                } else {
                    LogUtil.w(TAG, "open notification succeed");
                }
            }
        };
        //与远端设备断开连接后触发此回调
        BleInterface.OnDisconnectedListener onDisconnectedListener = new BleInterface.OnDisconnectedListener() {
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
        //读取到远端设备数据后会触发此回调,返回的数据是一个byte数组
        BleInterface.OnCharacteristicReadListener onCharacteristicReadListener = new BleInterface.OnCharacteristicReadListener() {
            @Override
            public void onCharacteristicRead(byte[] values) {
                LogUtil.w(TAG, "读取到的数据 = " + ConversionUtil.bytesToHexStr(values));
            }
        };
        //正在连接时触发此回调（不过此回调从来没有被触发过，我也不知道为何）
        BleInterface.OnConnectingListener onConnectingListener =new BleInterface.OnConnectingListener() {
            @Override
            public void onConnecting() {
                customTextCircleView.setColor(Color.YELLOW);
            }
        };
        //收到远端设备的主动通知时，触发此回调
        BleInterface.OnReceiveNotificationListener onReceiveNotificationListener = new BleInterface.OnReceiveNotificationListener() {
            @Override
            public void onReceiveNotification(byte[] values) {
                LogUtil.w("ConnectActivity", "value = " + ConversionUtil.bytesToHexStr(values));
            }
        };
        //读取到远端设备的RSSI值时触发此回调
        BleInterface.OnReadRemoteRssiListener onReadRemoteRssiListener = new BleInterface.OnReadRemoteRssiListener() {
            @Override
            public void onReadRemoteRssi(int rssi) {
                LogUtil.w("ConnectActivity", "rssi = " + rssi);
            }
        };
        /*当连接工具调用Close方法之后，在连接工具彻底关闭时会触发此回调
         *最好是屏蔽onBackPressed()方法，onBackPressed()方法中只调用bleConnector.closeAll().然后在这个方法中回调super.onBackPressed()结束activity
         *一定要在这个回调中做结束activity的操作，不要直接在onDestroy中调用close避免连接工具还没有彻底关闭，activity就结束造成内存泄漏
         */
        BleInterface.OnCloseCompleteListener onCloseCompleteListener = new BleInterface.OnCloseCompleteListener() {
            @Override
            public void onCloseComplete() {
                ConnectActivity.super.onBackPressed();
            }
        };
        //设备的绑定(也可以说配对)状态改变后触发此回调
        BleInterface.OnDeviceBondStateChangedListener onBondStateChangedListener = new BleInterface.OnDeviceBondStateChangedListener() {
            /**
             * 正在绑定设备
             */
            @Override
            public void deviceBinding() {

            }

            /**
             * 绑定完成
             */
            @Override
            public void deviceBonded() {
                //发起连接
                startConnect();
            }

            /**
             * 取消绑定或者绑定失败
             */
            @Override
            public void deviceBindNone() {

            }
        };

        /*设置连接工具一系列的监听事件*/
        bleConnector.setOnServicesDiscoveredListener(onServicesDiscoveredListener);
        bleConnector.setOnDisconnectedListener(onDisconnectedListener);
        bleConnector.setOnCharacteristicReadListener(onCharacteristicReadListener);
        bleConnector.setOnConnectedListener(onConnectedListener);
        bleConnector.setOnConnectingListener(onConnectingListener);
        bleConnector.setOnReceiveNotificationListener(onReceiveNotificationListener);
        bleConnector.setOnReadRemoteRssiListener(onReadRemoteRssiListener);
        bleConnector.setOnCloseCompleteListener(onCloseCompleteListener);
        bleConnector.setOnBondStateChangedListener(onBondStateChangedListener);
    }

    /**
     * 发起连接
     */
    private void startConnect() {
        //先设置地址
        if (bleConnector.checkAndSetAddress(address)) {
            //发起连接
            if (bleConnector.startConnect(true)) {
                LogUtil.w("开始连接");
                customTextCircleView.setColor(Color.YELLOW);
            } else {
                LogUtil.w("发起连接失败");
            }
        }
    }
}
