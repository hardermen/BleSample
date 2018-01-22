package cn.almsound.www.myblesample.activity.blemulticonnect;

import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.blelibrary.BleDeviceController;
import com.jackiepenghe.blelibrary.BleManager;
import com.jackiepenghe.blelibrary.BleMultiConnector;
import com.jackiepenghe.blelibrary.Tool;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.callback.Device1Callback;
import cn.almsound.www.myblesample.callback.Device2Callback;
import cn.almsound.www.myblesample.callback.Device3Callback;
import cn.almsound.www.myblesample.callback.Device4Callback;
import cn.almsound.www.myblesample.callback.Device5Callback;
import cn.almsound.www.myblesample.watcher.EditTextWatcherForMacAddress;
import cn.almsound.www.myblesample.wideget.CustomTextCircleView;

/**
 * @author alm
 */
public class MultiConnectActivity extends BaseAppCompatActivity {

    private static final String TAG = MultiConnectActivity.class.getSimpleName();

    private static final byte[] OPEN_SOCKET_BYTE_ARRAY = new byte[]{0x00, 0x00};
    private static final byte[] CLOSE_SOCKET_BYTE_ARRAY = new byte[]{0x00, 0x01};
    /**
     * 手机直控插座时，需要用到的服务UUID
     */
    private static final String SOCKET_SERVICE_UUID = "0000FFF0-0000-1000-8000-00805f9b34fb";
    /**
     * 手机直控插座时，进行开启或关闭操作的特征UUID
     */
    private static final String CHARACTERISTIC_PHONE_CONTROL = "0000fff3-0000-1000-8000-00805f9b34fb";

    private BleMultiConnector bleMultiConnector;
    private Button connectButton;
    private Button openSocket1Btn, openSocket2Btn, openSocket3Btn, openSocket4Btn, openSocket5Btn;
    private Button closeSocket1Btn, closeSocket2Btn, closeSocket3Btn, closeSocket4Btn, closeSocket5Btn;
    private CustomTextCircleView customTextCircleView1, customTextCircleView2, customTextCircleView3, customTextCircleView4, customTextCircleView5;
    private TextView deviceAaddressTv1, deviceAaddressTv2, deviceAaddressTv3, deviceAaddressTv4, deviceAaddressTv5;
    private boolean first = true;
    private String device1Address = "00:02:5B:00:15:A4";
    private String device2Address = "00:02:5B:00:15:A2";
    private String device3Address = "00:02:5B:00:15:A9";
    private String device4Address = "00:02:5B:00:15:A1";
    private String device5Address = "00:02:5B:00:15:A8";
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
                case R.id.open_socket1:
                    openSocket1();
                    break;
                case R.id.open_socket2:
                    openSocket2();
                    break;
                case R.id.close_socket1:
                    closeSocket1();
                    break;
                case R.id.close_socket2:
                    closeSocket2();
                    break;
                case R.id.open_socket3:
                    openSocket3();
                    break;
                case R.id.close_socket3:
                    closeSocket3();
                    break;
                case R.id.open_socket4:
                    openSocket4();
                    break;
                case R.id.close_socket4:
                    closeSocket4();
                    break;
                case R.id.open_socket5:
                    openSocket5();
                    break;
                case R.id.close_socket5:
                    closeSocket5();
                    break;
                default:
                    showSetAddressDialog(view.getId());
                    break;
            }
        }
    };

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

        // 使用县城 顺序发起连接
        new Thread() {
            @Override
            public void run() {
                bleMultiConnector.connect(device1Address, device1BleCallback, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bleMultiConnector.connect(device2Address, device2BleCallback, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bleMultiConnector.connect(device3Address, device3BleCallback, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bleMultiConnector.connect(device4Address, device4BleCallback, true);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                bleMultiConnector.connect(device5Address, device5BleCallback, true);
            }
        }.start();


        //连接时传入对应的回调，方便进行操作,通常使用这个就行了
//        bleMultiConnector.connect(device1Address, device1BleCallback);
//        bleMultiConnector.connect(device2Address, device2BleCallback);


        //连接时传入对应的回调，方便进行操作,并且在连接断开之后自动尝试连接（系统会默认自动去连接该设备，这是系统自身的重连参数，推荐用这个参数进行重连）
//        bleMultiConnector.connect(device1Address,device1BleCallback,true);
//        bleMultiConnector.connect(device2Address,device2BleCallback,true);
    }

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
        bleMultiConnector = BleManager.getBleMultiConnector(this);
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

    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        connectButton = findViewById(R.id.connect_button);
        openSocket1Btn = findViewById(R.id.open_socket1);
        openSocket2Btn = findViewById(R.id.open_socket2);
        closeSocket1Btn = findViewById(R.id.close_socket1);
        closeSocket2Btn = findViewById(R.id.close_socket2);
        openSocket3Btn = findViewById(R.id.open_socket3);
        closeSocket3Btn = findViewById(R.id.close_socket3);
        openSocket4Btn = findViewById(R.id.open_socket4);
        closeSocket4Btn = findViewById(R.id.close_socket4);
        openSocket5Btn = findViewById(R.id.open_socket5);
        closeSocket5Btn = findViewById(R.id.close_socket5);

        customTextCircleView1 = findViewById(R.id.circle_device1);
        customTextCircleView2 = findViewById(R.id.circle_device2);
        customTextCircleView3 = findViewById(R.id.circle_device3);
        customTextCircleView4 = findViewById(R.id.circle_device4);
        customTextCircleView5 = findViewById(R.id.circle_device5);

        deviceAaddressTv1 = findViewById(R.id.device1_address_tv);
        deviceAaddressTv2 = findViewById(R.id.device2_address_tv);
        deviceAaddressTv3 = findViewById(R.id.device3_address_tv);
        deviceAaddressTv4 = findViewById(R.id.device4_address_tv);
        deviceAaddressTv5 = findViewById(R.id.device5_address_tv);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        deviceAaddressTv1.setText(device1Address);
        deviceAaddressTv2.setText(device2Address);
        deviceAaddressTv3.setText(device3Address);
        deviceAaddressTv4.setText(device4Address);
        deviceAaddressTv5.setText(device5Address);
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
        openSocket1Btn.setOnClickListener(onClickListener);
        openSocket2Btn.setOnClickListener(onClickListener);
        closeSocket1Btn.setOnClickListener(onClickListener);
        closeSocket2Btn.setOnClickListener(onClickListener);
        openSocket3Btn.setOnClickListener(onClickListener);
        closeSocket3Btn.setOnClickListener(onClickListener);
        openSocket4Btn.setOnClickListener(onClickListener);
        closeSocket4Btn.setOnClickListener(onClickListener);
        openSocket5Btn.setOnClickListener(onClickListener);
        closeSocket5Btn.setOnClickListener(onClickListener);

        customTextCircleView1.setOnClickListener(onClickListener);
        customTextCircleView2.setOnClickListener(onClickListener);
        customTextCircleView3.setOnClickListener(onClickListener);
        customTextCircleView4.setOnClickListener(onClickListener);
        customTextCircleView5.setOnClickListener(onClickListener);
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

    private void open(BleDeviceController bleDeviceController) {
        if (bleDeviceController == null) {
            return;
        }
        bleDeviceController.writeData(SOCKET_SERVICE_UUID, CHARACTERISTIC_PHONE_CONTROL, OPEN_SOCKET_BYTE_ARRAY);
    }

    private void close(BleDeviceController bleDeviceController) {
        if (bleDeviceController == null) {
            return;
        }
        bleDeviceController.writeData(SOCKET_SERVICE_UUID, CHARACTERISTIC_PHONE_CONTROL, CLOSE_SOCKET_BYTE_ARRAY);
    }

    private void openSocket1() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device1Address);
        open(bleDeviceController);
    }

    private void openSocket2() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device2Address);
        open(bleDeviceController);
    }

    private void closeSocket1() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device1Address);
        close(bleDeviceController);
    }

    private void closeSocket2() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device2Address);
        close(bleDeviceController);
    }

    private void openSocket3() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device3Address);
        open(bleDeviceController);
    }

    private void closeSocket3() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device3Address);
        close(bleDeviceController);
    }

    private void openSocket4() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device4Address);
        open(bleDeviceController);
    }

    private void closeSocket4() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device4Address);
        close(bleDeviceController);
    }

    private void openSocket5() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device5Address);
        open(bleDeviceController);
    }

    private void closeSocket5() {
        BleDeviceController bleDeviceController = bleMultiConnector.getBleDeviceController(device5Address);
        close(bleDeviceController);
    }

    private void showSetAddressDialog(final int id) {
        View viewById = findViewById(id);
        if (!(viewById instanceof CustomTextCircleView)) {
            return;
        }

        final EditText editText = (EditText) View.inflate(this, R.layout.edit_text, null);
        EditTextWatcherForMacAddress editTextWatcherForMacAddress = new EditTextWatcherForMacAddress(editText);
        editText.addTextChangedListener(editTextWatcherForMacAddress);
        new AlertDialog.Builder(this)
                .setTitle(R.string.set_address)
                .setView(editText)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String text = editText.getText().toString();
                        if (text.length() != 17) {
                            Tool.toastL(MultiConnectActivity.this, R.string.address_error);
                            showSetAddressDialog(id);
                            return;
                        }
                        if (!BluetoothAdapter.checkBluetoothAddress(text)) {
                            Tool.toastL(MultiConnectActivity.this, R.string.address_error);
                            showSetAddressDialog(id);
                            return;
                        }
                        setAddress(id, text);
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .setCancelable(false)
                .show();
    }

    private void setAddress(int id, String text) {
        View viewById = findViewById(id);
        if (!(viewById instanceof CustomTextCircleView)) {
            return;
        }
        switch (id) {
            case R.id.circle_device1:
                device1Address = text;
                deviceAaddressTv1.setText(text);
                break;
            case R.id.circle_device2:
                device2Address = text;
                deviceAaddressTv2.setText(text);
                break;
            case R.id.circle_device3:
                device3Address = text;
                deviceAaddressTv3.setText(text);
                break;
            case R.id.circle_device4:
                device4Address = text;
                deviceAaddressTv4.setText(text);
                break;
            case R.id.circle_device5:
                device5Address = text;
                deviceAaddressTv5.setText(text);
                break;
            default:
                break;
        }
    }
}
