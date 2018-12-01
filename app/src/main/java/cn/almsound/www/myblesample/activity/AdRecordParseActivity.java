package cn.almsound.www.myblesample.activity;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.EditText;

import com.jackiepenghe.baselibrary.BaseAppCompatActivity;
import com.jackiepenghe.baselibrary.DefaultItemDecoration;
import com.jackiepenghe.baselibrary.Tool;
import com.jackiepenghe.blelibrary.AdRecord;
import com.jackiepenghe.blelibrary.BleDevice;

import java.io.Serializable;
import java.util.ArrayList;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.AdRecordRecyclerAdapter;
import cn.almsound.www.myblesample.utils.Constants;

/**
 * @author jackie
 */
public class AdRecordParseActivity extends BaseAppCompatActivity {

    /**
     * 蓝牙设备
     */
    private BleDevice bleDevice;
    /**
     * 广播包数据
     */
    private EditText scanRecordEditText;
    /**
     * 响应包数据
     */
    private EditText responseRecordEditText;
    /**
     * 将广播包按照AdType分开显示的列表
     */
    private RecyclerView recyclerView;
    /**
     * 适配器数据源
     */
    private ArrayList<AdRecord> adRecords = new ArrayList<>();
    /**
     * 适配器
     */
    private AdRecordRecyclerAdapter adRecordRecyclerAdapter = new AdRecordRecyclerAdapter(adRecords);
    /**
     * 默认的分割线
     */
    private DefaultItemDecoration defaultItemDecoration = new DefaultItemDecoration(Color.GRAY,ViewGroup.LayoutParams.MATCH_PARENT,2,-1);

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
        getBleDevice();
    }

    /**
     * 设置布局
     *
     * @return 布局id
     */
    @Override
    protected int setLayout() {
        return R.layout.activity_ad_record_parse;
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
        scanRecordEditText = findViewById(R.id.scan_record_et);
        responseRecordEditText = findViewById(R.id.response_record_et);
        recyclerView = findViewById(R.id.scan_record_rv);
    }

    /**
     * 初始化控件数据
     */
    @Override
    protected void initViewData() {
        initEditText();
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

    }

    /**
     * 在最后进行的操作
     */
    @Override
    protected void doAfterAll() {
        refreshRecyclerViewData();
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

    private void getBleDevice() {
        Intent intent = getIntent();
        if (intent == null) {
            return;
        }
        Serializable serializableExtra = intent.getSerializableExtra(Constants.DEVICE);
        if (serializableExtra == null) {
            return;
        }
        if (!(serializableExtra instanceof BleDevice)) {
            return;
        }
        bleDevice = (BleDevice) serializableExtra;
    }

    private void initEditText() {
        if (bleDevice == null){
            return;
        }
        byte[] scanRecordBytes = bleDevice.getScanRecordBytes();
        if (scanRecordBytes == null){
            return;
        }
        if (scanRecordBytes.length > 31){
            initScanRecordAndResponseCord(scanRecordBytes);
        }else {
            initScanRecord(scanRecordBytes);
        }
    }

    private void initScanRecord(byte[] scanRecordBytes) {
        byte[] scanRecord = new byte[scanRecordBytes.length];
        System.arraycopy(scanRecordBytes,0,scanRecord,0,scanRecord.length);
        byte[] responseRecord = new byte[31];
        scanRecordEditText.setText(Tool.bytesToHexStr(scanRecord));
        responseRecordEditText.setText(Tool.bytesToHexStr(responseRecord));
    }

    private void initScanRecordAndResponseCord(byte[] scanRecordBytes) {
        byte[] scanRecord = new byte[31];
        byte[] responseRecord = new byte[scanRecordBytes.length - 31];
        System.arraycopy(scanRecordBytes,0,scanRecord,0,scanRecord.length);
        System.arraycopy(scanRecordBytes,31,responseRecord,0,responseRecord.length);
        scanRecordEditText.setText(Tool.bytesToHexStr(scanRecord));
        responseRecordEditText.setText(Tool.bytesToHexStr(responseRecord));
    }

    private void initRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(defaultItemDecoration);
        recyclerView.setAdapter(adRecordRecyclerAdapter);
    }

    private void refreshRecyclerViewData() {
        if (bleDevice == null){
            return;
        }
        ArrayList<AdRecord> adRecords = bleDevice.getAdRecords();
        this.adRecords.clear();
        if (adRecords != null ) {
            this.adRecords.addAll(adRecords);
        }
        adRecordRecyclerAdapter.notifyDataSetChanged();
    }
}
