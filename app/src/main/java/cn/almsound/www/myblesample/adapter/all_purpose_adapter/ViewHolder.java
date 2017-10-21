package cn.almsound.www.myblesample.adapter.all_purpose_adapter;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;


/**
 * 为万能适配器创建的通用的ViewHolder
 * Created by ALM on 2016/7/7.
 */
@SuppressWarnings("unused")
public class ViewHolder {
    /**
     * 缓存控件的集合
     */
    private final SparseArray<View> views;

    /**
     * 记录当前的位置
     */
    private int position;

    /**
     * 缓存布局
     */
    private View convertView;

    /**
     * 图片加载工具
     */
    private ImageLoader imageLoader;

    /**
     * 构造器
     * @param context 上下文
     * @param parent 父布局
     * @param layoutId item布局id
     * @param position 当前的位置
     */
    private ViewHolder(Context context, ViewGroup parent, int layoutId,
                       int position) {
        this.position = position;
        this.views = new SparseArray<>();
        convertView = LayoutInflater.from(context).inflate(layoutId, parent,
                false);
        // setTag
        convertView.setTag(this);
        imageLoader = ImageLoader.getInstance(context);
    }

    /**
     * 拿到一个ViewHolder对象
     *
     * @param context 上下文
     * @param convertView 容器
     * @param parent 父容器
     * @param layoutId 布局的id
     * @param position 选中的位置
     * @return viewHolder
     */
    static ViewHolder get(Context context, View convertView,
                          ViewGroup parent, int layoutId, int position) {
        if (convertView == null) {
            return new ViewHolder(context, parent, layoutId, position);
        }
        return (ViewHolder) convertView.getTag();
    }

    /**
     * 获取item的总布局
     * @return item的总布局
     */
    @SuppressWarnings("WeakerAccess")
    public View getConvertView() {
        return convertView;
    }

    /**
     * 通过控件的Id获取对应的控件，如果没有则加入views
     *
     * @param viewId 控件的Id
     * @return 控件
     */
    private <T extends View> T getView(int viewId) {
        View view = views.get(viewId);
        if (view == null) {
            view = convertView.findViewById(viewId);
            views.put(viewId, view);
        }
        //noinspection unchecked
        return (T) view;
    }

    /**
     * 为TextView设置字符串
     *
     * @param viewId 控件的Id
     * @param string 要设置的字符串
     * @return  当前ViewHolder对象
     */
    public ViewHolder setText(int viewId, CharSequence string) {
        TextView view = getView(viewId);
        view.setText(string);
        return this;
    }

    /**
     * 获取edit文本
     *
     * @param viewId 控件的Id
     * @return  edit文本
     */
    public String getEditText(int viewId) {
        EditText ed = getView(viewId);
        return ed.getText().toString();
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId 控件的Id
     * @param drawableId 图片的id
     * @return 当前ViewHolder对象
     */
    public ViewHolder setImageResource(int viewId, int drawableId) {
        ImageView view = getView(viewId);
        view.setImageResource(drawableId);

        return this;
    }

    /**
     * 为ImageView设置图片
     *
     * @param viewId 控件的Id
     * @return 当前ViewHolder对象
     */
    public ViewHolder setImageBitmap(int viewId, Bitmap bm) {
        ImageView view = getView(viewId);
        view.setImageBitmap(bm);
        return this;
    }

    /**
     * 为ImageView设置图片
     * @param viewId 控件的Id
     * @param url   文件url
     * @param circle 是否是圆形
     * @return 当前ViewHolder对象
     */
    public ViewHolder setImageByUrl(int viewId, String url, boolean circle) {
        ImageView imageView =  getView(viewId);
        imageLoader.displayImage(url, imageView, circle);
        return this;
    }

    /**
     * 给view设置背景色
     *
     * @param viewId 控件的Id
     * @param color 要设置的背景色
     * @return 当前ViewHolder对象
     */
    public ViewHolder setBackgroundColor(int viewId, int color) {
        View view = getView(viewId);
        view.setBackgroundColor(color);
        return this;
    }

    /**
     * 获得当前的位置
     * @return 当前ViewHolder对象
     */
    public int getPosition() {
        return position;
    }

}
