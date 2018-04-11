package cn.almsound.www.myblesample.adapter.entity.services_characteristics_list_entity;

import android.bluetooth.BluetoothGattCharacteristic;

import com.chad.library.adapter.base.entity.MultiItemEntity;

import cn.almsound.www.myblesample.adapter.ServicesCharacteristicsListAdapter;

/**
 * @author jacke
 * @date 2018/1/22 0022
 */

public class CharacteristicUuidItem implements MultiItemEntity {

    private String uuid;
    private boolean canRead;
    private boolean canWrite;
    private boolean canNotify;

    public CharacteristicUuidItem(String uuid, boolean canRead, boolean canWrite, boolean canNotify) {
        this.uuid = uuid;
        this.canRead = canRead;
        this.canWrite = canWrite;
        this.canNotify = canNotify;
    }

    public String getUuid() {
        return uuid;
    }

    public void setBluetoothGattCharacteristic(String uuid) {
        this.uuid = uuid;
    }

    public boolean isCanRead() {
        return canRead;
    }

    public void setCanRead(boolean canRead) {
        this.canRead = canRead;
    }

    public boolean isCanWrite() {
        return canWrite;
    }

    public void setCanWrite(boolean canWrite) {
        this.canWrite = canWrite;
    }

    public boolean isCanNotify() {
        return canNotify;
    }

    public void setCanNotify(boolean canNotify) {
        this.canNotify = canNotify;
    }

    @Override
    public int getItemType() {
        return ServicesCharacteristicsListAdapter.TYPE_CHARACTERISTIC_UUID;
    }
}
