package cn.almsound.www.myblesample.adapter.others;

import android.content.Context;

import java.util.List;

import cn.almsound.www.blelibrary.BleDevice;
import cn.almsound.www.myblesample.adapter.base.BasePurposeAdapter;
import cn.almsound.www.myblesample.adapter.base.ViewHolder;

/**
 * @author jackie
 * 自定义适配器(显示自定义BLE设备列表)
 * Created by jackie on 2017/1/6 0006.
 */
public class DeviceListAdapter extends BasePurposeAdapter<BleDevice> {


    /**
     * 构造方法
     *
     * @param context      上下文
     * @param mDatas       数据
     */
    public DeviceListAdapter(Context context, List<BleDevice> mDatas) {
        super(context, mDatas, android.R.layout.simple_list_item_2);
    }

    @Override
    public void convert(ViewHolder holder, int position, BleDevice item) {
        holder.setText(android.R.id.text1,item.getBluetoothDevice().getName());
        holder.setText(android.R.id.text2,item.getBluetoothDevice().getAddress());
    }
}
