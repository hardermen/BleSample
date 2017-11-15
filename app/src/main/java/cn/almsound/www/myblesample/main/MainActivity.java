package cn.almsound.www.myblesample.main;

import android.content.Intent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import cn.almsound.www.baselibrary.BaseAppcompatActivity;
import cn.almsound.www.blelibrary.Tool;
import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.activity.DeviceListActivity;
import cn.almsound.www.myblesample.activity.MultiConnectActivity;

/**
 * @author alm
 */
public class MainActivity extends BaseAppcompatActivity {

    private Button simpleUseBtn;
    private Button multiConnectBtn;
    private View.OnClickListener onClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            switch (view.getId()) {
                case R.id.simple_use_button:
                    toDeviceListActivity();
                    break;
                case R.id.multi_connect_button:
                    toMultiConnectActivity();
                    break;
                default:
                    break;
            }
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
        Tool.setDebugFlag(true);
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_main;
    }

    /**
     * 在设置布局之后，进行其他操作之前，所需要初始化的数据
     */
    @Override
    protected void doBeforeInitOthers() {
        hideTitleBackButton();
    }

    /**
     * 初始化布局控件
     */
    @Override
    protected void initViews() {
        simpleUseBtn = findViewById(R.id.simple_use_button);
        multiConnectBtn = findViewById(R.id.multi_connect_button);
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
        simpleUseBtn.setOnClickListener(onClickListener);
        multiConnectBtn.setOnClickListener(onClickListener);
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
    protected void onDestroy() {
        super.onDestroy();
        simpleUseBtn.setOnClickListener(null);
        simpleUseBtn = null;
    }

    private void toDeviceListActivity() {
        Intent intent = new Intent(MainActivity.this, DeviceListActivity.class);
        startActivity(intent);
    }

    private void toMultiConnectActivity() {
        Intent intent = new Intent(MainActivity.this,MultiConnectActivity.class);
        startActivity(intent);
    }
}
