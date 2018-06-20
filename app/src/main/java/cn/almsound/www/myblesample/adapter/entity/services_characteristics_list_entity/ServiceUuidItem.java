package cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity;

import com.chad.library.adapter.base.entity.AbstractExpandableItem;
import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.almsound.www.myblesample.adapter.ServicesCharacteristicsListAdapter;

/**
 *
 * @author jacke
 * @date 2018/1/22 0022
 */

public class ServiceUuidItem extends AbstractExpandableItem<CharacteristicUuidItem> implements MultiItemEntity{

    private String uuid;

    public ServiceUuidItem(String uuid) {
        this.uuid = uuid;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * Get the level of this item. The level start from 0.
     * If you don't care about the level, just return a negative.
     */
    @Override
    public int getLevel() {
        return ServicesCharacteristicsListAdapter.LEVEL_SERVICE_UUID;
    }

    @Override
    public int getItemType() {
        return ServicesCharacteristicsListAdapter.TYPE_SERVICE_UUID;
    }
}
