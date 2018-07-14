package cn.almsound.www.myblesample.adapter;

import android.view.View;

import com.chad.library.adapter.base.BaseMultiItemQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.chad.library.adapter.base.entity.MultiItemEntity;
import com.jackiepenghe.baselibrary.Tool;

import java.util.List;

import cn.almsound.www.myblesample.R;
import cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity.CharacteristicUuidItem;
import cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity.ServiceUuidItem;

/**
 * @author jacke
 * @date 2018/1/22 0022
 */

public class ServicesCharacteristicsListAdapter extends BaseMultiItemQuickAdapter<MultiItemEntity, BaseViewHolder> {

    public static final int TYPE_SERVICE_UUID = 0;
    public static final int TYPE_CHARACTERISTIC_UUID = 1;
    public static final int LEVEL_SERVICE_UUID = 1;

    private OnCharacteristicClickListener onCharacteristicClickListener;

    /**
     * Same as QuickAdapter#QuickAdapter(Context,int) but with
     * some initialization data.
     *
     * @param data A new list is created out of this one to avoid mutable list
     */
    public ServicesCharacteristicsListAdapter(List<MultiItemEntity> data) {
        super(data);
        addItemType(TYPE_SERVICE_UUID, R.layout.item_expandable_service_uuid);
        addItemType(TYPE_CHARACTERISTIC_UUID, R.layout.item_expandable_characteristic_uuid);
    }

    /**
     * Implement this method and use the helper to adapt the view to the given item.
     *
     * @param holder A fully initialized helper.
     * @param item   The item that needs to be displayed.
     */
    @Override
    protected void convert(final BaseViewHolder holder, final MultiItemEntity item) {
        switch (holder.getItemViewType()) {
            case TYPE_SERVICE_UUID:
                final ServiceUuidItem serviceUuidItem = (ServiceUuidItem) item;
                holder.setText(android.R.id.text1,serviceUuidItem.getName())
                        .setText(android.R.id.text2, serviceUuidItem.getUuid())
                        .setImageResource(R.id.expanded, serviceUuidItem.isExpanded() ? R.drawable.arrow_b : R.drawable.arrow_r);

                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tool.warnOut(TAG,"service");
                        int adapterPosition = holder.getAdapterPosition();
                        if (serviceUuidItem.isExpanded()) {
                            collapse(adapterPosition);
                        } else {
                            int expand = expand(adapterPosition);
                            if (expand <= 0){
                                Tool.toastL(mContext,R.string.nothing_to_expand);
                            }
                        }
                    }
                });
                break;
            case TYPE_CHARACTERISTIC_UUID:
                final CharacteristicUuidItem characteristicUuidItem = (CharacteristicUuidItem) item;
                holder .setText(android.R.id.text1, characteristicUuidItem.getName())
                        .setText(android.R.id.text2, characteristicUuidItem.getUuid())
                .setText(R.id.properties,getProperties(characteristicUuidItem.isCanRead(),characteristicUuidItem.isCanWrite(),characteristicUuidItem.isCanNotify()));
                holder.itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Tool.warnOut(TAG,"characteristic");
                        if (onCharacteristicClickListener != null) {
                            int parentPosition = getParentPosition(item);
                            ServiceUuidItem serviceUuidItem1 = (ServiceUuidItem) getItem(parentPosition);
                            if (serviceUuidItem1 != null) {
                                onCharacteristicClickListener.onCharacteristicClick(serviceUuidItem1.getUuid(), characteristicUuidItem.getUuid());
                            }
                        }
                    }
                });
                break;
            default:
                break;
        }
    }

    private String getProperties(boolean canRead, boolean canWrite, boolean canNotify) {
        if (!canRead && !canWrite && !canNotify){
            return mContext.getString(R.string.null_);
        }
        StringBuilder stringBuilder = new StringBuilder();

        if (canRead){
            stringBuilder.append(mContext.getString(R.string.can_read));
        }
        if (canWrite){
            stringBuilder.append(mContext.getString(R.string.can_write));
        }
        if (canNotify){
            stringBuilder.append(mContext.getString(R.string.can_notify));
        }
        return stringBuilder.toString();
    }

    /**
     * 设置点击监听
     *
     * @param onCharacteristicClickListener 特征值点击监听
     */
    public void setOnCharacteristicClickListener(OnCharacteristicClickListener onCharacteristicClickListener) {
        this.onCharacteristicClickListener = onCharacteristicClickListener;
    }

    /**
     * 接口 监听特征UUID被点击时的事件
     */
    public interface OnCharacteristicClickListener {
        /**
         * 监听特征UUID被点击时的事件
         *
         * @param serviceUUID        服务UUID
         * @param characteristicUUID 特征UUID
         */
        void onCharacteristicClick(String serviceUUID, String characteristicUUID);
    }
}
