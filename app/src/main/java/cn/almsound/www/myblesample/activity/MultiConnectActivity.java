package cn.almsound.www.myblesample.activity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cn.almsound.www.baselibrary.BaseAppcompatActivity;
import cn.almsound.www.blelibrary.BleManager;
import cn.almsound.www.blelibrary.BleMultiConnector;
import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.callback.Device1BleCallback;
import cn.almsound.www.myblesample.callback.Device2BleCallback;

/**
 * @author alm
 */
public class MultiConnectActivity extends BaseAppcompatActivity {

    private BleMultiConnector bleMultiConnector;
    private Button connectButton;
    private Button openSocket1Btn, openSocket2Btn;
    private Button closeSocket1Btn, closeSocket2Btn;
    private Device1BleCallback device1BleCallback;
    private Device2BleCallback device2BleCallback;


    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.connect_button:
                    doConnect();
                    break;
                case R.id.open_socket1:
                    device1BleCallback.open();
                    break;
                case R.id.open_socket2:
                    device2BleCallback.open();
                    break;
                case R.id.close_socket1:
                    device1BleCallback.close();
                    break;
                case R.id.close_socket2:
                    device2BleCallback.close();
                    break;
                default:
                    break;
            }
        }
    };
    private boolean first = true;

    private void doConnect() {
        if (!first){
            return;
        }
        first = false;
        String device1Address = "00:02:5B:00:15:A4";
        String device2Address = "00:02:5B:00:15:A2";
        bleMultiConnector.connect(device1Address, device1BleCallback);
        bleMultiConnector.connect(device2Address, device2BleCallback);
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
        device1BleCallback = new Device1BleCallback(bleMultiConnector);
        device2BleCallback = new Device2BleCallback(bleMultiConnector);
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
}
