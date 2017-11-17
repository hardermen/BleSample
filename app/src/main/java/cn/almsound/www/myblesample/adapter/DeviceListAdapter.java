package cn.almsound.www.myblesample.adapter;

import android.content.Context;

import com.jackiepenghe.baselibrary.AllPurposeAdapter;
import com.jackiepenghe.baselibrary.ViewHolder;
import com.jackiepenghe.blelibrary.BleDevice;

import java.util.ArrayList;
import java.util.List;


/**
 * @author jackie
 * 自定义适配器(显示自定义BLE设备列表)
 * Created by jackie on 2017/1/6 0006.
 */
public class DeviceListAdapter extends AllPurposeAdapter<BleDevice> {


    /**
     * 构造方法
     *
     * @param context      上下文
     * @param mDatas       数据
     */
    public DeviceListAdapter(Context context, ArrayList<BleDevice> mDatas) {
        super(context, mDatas, android.R.layout.simple_list_item_2);
    }

    @Override
    protected void convert(ViewHolder viewHolder, int position, BleDevice item) {
        viewHolder.setText(android.R.id.text1,item.getBluetoothDevice().getName());
        viewHolder.setText(android.R.id.text2,item.getBluetoothDevice().getAddress());
    }
}
