package cn.almsound.www.myblesample.activity.blemulticonnect;

import android.bluetooth.BluetoothDevice;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.BleMultiConnector;

import java.io.Serializable;
import java.util.ArrayList;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.callback.Device1Callback;
import cn.almsound.www.myblesample.callback.Device2Callback;
import cn.almsound.www.myblesample.callback.Device3Callback;
import cn.almsound.www.myblesample.callback.Device4Callback;
import cn.almsound.www.myblesample.callback.Device5Callback;
import cn.almsound.www.myblesample.utils.Constants;
import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 * @author alm
 */
public class MultiConnectActivity extends BaseAppCompatActivity {

    private static final String TAG = MultiConnectActivity.class.getSimpleName();

    private BleMultiConnector bleMultiConnector;
    private Button connectButton;
    private CustomTextCircleView customTextCircleView1, customTextCircleView2, customTextCircleView3, customTextCircleView4, customTextCircleView5;
    private TextView deviceAddressTv1, deviceAddressTv2, deviceAddressTv3, deviceAddressTv4, deviceAddressTv5;
    private boolean first = true;

    private BluetoothDevice device1;
    private BluetoothDevice device2;
    private BluetoothDevice device3;
    private BluetoothDevice device4;
    private BluetoothDevice device5;

    /**
     * 用于监测连接状态的回调
     */
    private Device1Callback device1BleCallback;
    private Device2Callback device2BleCallback;
    private Device3Callback device3BleCallback;
    private Device4Callback device4BleCallback;
    private Device5Callback device5BleCallback;


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_button:
                    doConnect();
                    break;
                default:
                    break;
            }
        }
    };
    private ArrayList<BluetoothDevice> bluetoothDevices;

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
        Serializable serializableExtra = getIntent().getSerializableExtra(Constants.DEVICE_LIST);
        if (serializableExtra instanceof ArrayList) {
            //noinspection unchecked
            bluetoothDevices = (ArrayList<BluetoothDevice>) serializableExtra;
        }
        if (bluetoothDevices == null || bluetoothDevices.size() == 0) {
            return;
        }
        for (int i = 0; i < bluetoothDevices.size(); i++) {
            BluetoothDevice bluetoothDevice = bluetoothDevices.get(i);
            switch (i) {
                case 0:
                    device1 = bluetoothDevice;
                    break;
                case 1:
                    device2 = bluetoothDevice;
                    break;
                case 2:
                    device3 = bluetoothDevice;
                    break;
                case 3:
                    device4 = bluetoothDevice;
                    break;
                case 4:
                    device5 = bluetoothDevice;
                    break;
                default:
                    break;
            }
        }
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_multi_connect;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        bleMultiConnector = BleManager.getBleMultiConnectorInstance(this);
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        connectButton = findViewById(R.id.connect_button);

        customTextCircleView1 = findViewById(R.id.circle_device1);
        customTextCircleView2 = findViewById(R.id.circle_device2);
        customTextCircleView3 = findViewById(R.id.circle_device3);
        customTextCircleView4 = findViewById(R.id.circle_device4);
        customTextCircleView5 = findViewById(R.id.circle_device5);

        deviceAddressTv1 = findViewById(R.id.device1_address_tv);
        deviceAddressTv2 = findViewById(R.id.device2_address_tv);
        deviceAddressTv3 = findViewById(R.id.device3_address_tv);
        deviceAddressTv4 = findViewById(R.id.device4_address_tv);
        deviceAddressTv5 = findViewById(R.id.device5_address_tv);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        if (device1 != null) {
            deviceAddressTv1.setText(device1.getAddress());
        }else {
            deviceAddressTv1.setText(R.string.null_);
        }
        if (device2 != null) {
            deviceAddressTv2.setText(device2.getAddress());
        }else {
            deviceAddressTv2.setText(R.string.null_);
        }
        if (device3 != null) {
            deviceAddressTv3.setText(device3.getAddress());
        }else {
            deviceAddressTv3.setText(R.string.null_);
        }
        if (device4 != null) {
            deviceAddressTv4.setText(device4.getAddress());
        }else {
            deviceAddressTv4.setText(R.string.null_);
        }
        if (device5 != null) {
            deviceAddressTv5.setText(device5.getAddress());
        }else {
            deviceAddressTv5.setText(R.string.null_);
        }
    }

    /**
     * 初始化其他数据
     */
    @Override
    protected void initOtherData() {
        device1BleCallback = new Device1Callback(customTextCircleView1);
        device2BleCallback = new Device2Callback(customTextCircleView2);
        device3BleCallback = new Device3Callback(customTextCircleView3);
        device4BleCallback = new Device4Callback(customTextCircleView4);
        device5BleCallback = new Device5Callback(customTextCircleView5);
    }

    /**
     * 初始化事件
     */
    @Override
    protected void initEvents() {
        connectButton.setOnClickListener(onClickListener);
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
    public void onBackPressed() {
        super.onBackPressed();
        bleMultiConnector.refreshAllGattCache();
        bleMultiConnector.closeAll();
    }

    /**
     * 开始连接
     */
    private void doConnect() {
        if (!first) {
            return;
        }
        first = false;

        //使用默认的回调连接
//        bleMultiConnector.connect(device1Address);
//        bleMultiConnector.connect(device2Address);

        //断开后自动连接（此函数调用的是系统的API，由系统自动连接设备)
//        bleMultiConnector.connect(device1Address, device1BleCallback, true);
//        bleMultiConnector.connect(device2Address, device2BleCallback, true);
//        bleMultiConnector.connect(device3Address, device3BleCallback, true);
//        bleMultiConnector.connect(device4Address, device4BleCallback, true);
//        bleMultiConnector.connect(device5Address, device5BleCallback, true);

        //我在此处使用线程顺序发起连接
        new Thread() {
            @Override
            public void run() {
                if (device1 != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bleMultiConnector.connect(device1, device1BleCallback, true);
                                }
                            });
                        }
                    }).start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (device2 != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bleMultiConnector.connect(device2, device2BleCallback, true);
                                }
                            });
                        }
                    }).start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (device3 != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bleMultiConnector.connect(device3, device3BleCallback, true);
                                }
                            });
                        }
                    }).start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (device4 != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bleMultiConnector.connect(device4, device4BleCallback, true);
                                }
                            });
                        }
                    }).start();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (device5 != null) {
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    bleMultiConnector.connect(device5, device5BleCallback, true);
                                }
                            });
                        }
                    }).start();
                }
            }
        }.start();


        //连接时传入对应的回调，方便进行操作,通常使用这个就行了
//        bleMultiConnector.connect(device1Address, device1BleCallback);
//        bleMultiConnector.connect(device2Address, device2BleCallback);


        //连接时传入对应的回调，方便进行操作,并且在连接断开之后自动尝试连接（系统会默认自动去连接该设备，这是系统自身的重连参数，推荐用这个参数进行重连）
//        bleMultiConnector.connect(device1Address,device1BleCallback,true);
//        bleMultiConnector.connect(device2Address,device2BleCallback,true);
    }
}
