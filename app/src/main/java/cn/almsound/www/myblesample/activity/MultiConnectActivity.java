package cn.almsound.www.myblesample.activity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cn.almsound.www.baselibrary.BaseAppcompatActivity;
import cn.almsound.www.blelibrary.BleDeviceController;
import cn.almsound.www.blelibrary.BleManager;
import cn.almsound.www.blelibrary.BleMultiConnector;
import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.callback.Device1Callback;
import cn.almsound.www.myblesample.callback.Device2Callback;

/**
 * @author alm
 */
public class MultiConnectActivity extends BaseAppcompatActivity {


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
    private Button openSocket1Btn, openSocket2Btn;
    private Button closeSocket1Btn, closeSocket2Btn;
    private Device1Callback device1BleCallback = new Device1Callback();
    private Device2Callback device2BleCallback = new Device2Callback();


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
                default:
                    break;
            }
        }
    };

    private boolean first = true;
    private String device1Address = "00:02:5B:00:15:A4";
    private String device2Address = "00:02:5B:00:15:A2";

    private void doConnect() {
        if (!first) {
            return;
        }
        first = false;


        //使用默认的回调连接
//        bleMultiConnector.connect(device1Address);
//        bleMultiConnector.connect(device2Address);

        //断开后自动连接（此函数调用的是系统的API，由系统自动连接设备）
        bleMultiConnector.connect(device1Address,true);
        bleMultiConnector.connect(device2Address,true);

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
        connectButton.setOnClickListener(onClickListener);
        openSocket1Btn.setOnClickListener(onClickListener);
        openSocket2Btn.setOnClickListener(onClickListener);
        closeSocket1Btn.setOnClickListener(onClickListener);
        closeSocket2Btn.setOnClickListener(onClickListener);
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
}
