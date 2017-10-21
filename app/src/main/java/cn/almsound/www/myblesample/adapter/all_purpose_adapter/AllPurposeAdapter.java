package cn.almsound.www.myblesample.adapter.all_purpose_adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * 自定义万能适配器
 * <p>
 * Created by ALM on 2016/6/28.
 */
@SuppressWarnings("unused")
public abstract class AllPurposeAdapter<T> extends BaseAdapter {

    /**
     * 上下文对象
     */
    private Context mContext;

    /**
     * 适配器数据
     */
    private List<T> mDatas = new ArrayList<>();

    /**
     * 适配器每一个单独的item的布局
     */
    private final int mItemLayoutId;

    /**
     * 数据总数
     */
    private int countSum = -1;

    /**
     * 构造器
     *
     * @param context      上下文对象
     * @param mDatas       适配器数据
     * @param itemLayoutId 适配器每一个单独的item的布局
     */
    protected AllPurposeAdapter(Context context, List<T> mDatas, int itemLayoutId) {
        this.mContext = context;
        // 布局加载器
        LayoutInflater mInflater = LayoutInflater.from(mContext);
        this.mDatas = mDatas;
        this.mItemLayoutId = itemLayoutId;
    }


    /**
     * 替换元素并刷新
     *
     * @param mDatas 要替换的元素列表
     */
    public void refresh(List<T> mDatas) {
        this.mDatas = mDatas;
        this.notifyDataSetChanged();
    }

    /**
     * 删除元素并更新
     *
     * @param position 要删除的元素所在的位置
     */
    public void deleteListByPosition(int position) {
        this.mDatas.remove(position);
        this.notifyDataSetChanged();
    }

    /**
     * 定义item的总数
     *
     * @param i 要设置的item总数
     * @return 返回一个AllPurposeAdapter对象
     */
    public AllPurposeAdapter setCount(int i) {
        countSum = i;
        this.notifyDataSetChanged();
        return this;
    }

    /**
     * 获取数据列表总数
     *
     * @return 数据列表总数
     */
    @Override
    public int getCount() {
        if (countSum == -1) {
            return mDatas.size();
        } else {
            return countSum;
        }
    }

    /**
     * 获取对应位置的某一项
     *
     * @param position 对应位置
     * @return 对应位置的某一项
     */
    @Override
    public T getItem(int position) {
        if (countSum == -1) {
            return mDatas.get(position);
        } else {
            return mDatas.get(countSum % mDatas.size());
        }

    }

    /**
     * 获取对应位置的id
     *
     * @param position 对应位置
     * @return id
     */
    @Override
    public long getItemId(int position) {
        return position;
    }

    /**
     * 获取适配器每一个单独的item的布局
     *
     * @param position    对应位置
     * @param convertView 布局缓存
     * @param parent      适配器每一个单独的item的布局父布局
     * @return 布局控件
     */
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder viewHolder = getViewHolder(position, convertView,
                parent);
        convert(viewHolder, position, getItem(position));
        return viewHolder.getConvertView();

    }

    /**
     * 将item中的布局进行对应的设置
     *
     * @param holder   ViewHolder
     * @param position 对应位置
     * @param item     当前项的数据内容
     */
    public abstract void convert(ViewHolder holder, int position, T item);

    /**
     * 获取ViewHolder
     *
     * @param position    对应位置
     * @param convertView 布局缓存
     * @param parent      父布局
     * @return ViewHolder
     */
    private ViewHolder getViewHolder(int position, View convertView, ViewGroup parent) {
        return ViewHolder.get(mContext, convertView, parent, mItemLayoutId,
                position);
    }
}
