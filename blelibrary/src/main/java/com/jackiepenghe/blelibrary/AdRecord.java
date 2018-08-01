package com.jackiepenghe.blelibrary;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 保存广播包的解析结果
 *
 * @author jacke
 */
@SuppressWarnings("unused")
public class AdRecord {

    /*-------------------------成员变量-------------------------*/

    /**
     * 数据长度
     */
    private int length;
    /**
     * 数据类型
     */
    private int type;
    /**
     * 数据内容
     */
    private byte[] data;

    /*-------------------------构造函数-------------------------*/

    /**
     * 构造函数
     *
     * @param length 数据长度
     * @param type   数据类型
     * @param data   数内容
     */
    private AdRecord(int length, int type, byte[] data) {
        this.length = length;
        this.type = type;
        this.data = data;
    }

    /*-------------------------getter-------------------------*/

    /**
     * 获取数据长度
     *
     * @return 数据长度
     */
    public int getLength() {
        return length;
    }

    /**
     * 获取数据类型
     *
     * @return 数据类型
     */
    public int getType() {
        return type;
    }

    /**
     * 获取数据内容
     *
     * @return 数据内容
     */
    public byte[] getData() {
        return data;
    }

    /**
     * 将完整的广播包解析成AdRecord集合
     *
     * @param scanRecord 完整的广播包
     * @return AdRecord集合
     */
    static ArrayList<AdRecord> parseScanRecord(byte[] scanRecord) {
        ArrayList<AdRecord> records = new ArrayList<>();

        if (scanRecord == null) {
            return records;
        }

        byte length;
        for (int index = 0; index < scanRecord.length; index += length) {
            length = scanRecord[index++];
            if (length == 0) {
                break;
            }

            int type = scanRecord[index];
            if (type == 0) {
                break;
            }

            byte[] data = Arrays.copyOfRange(scanRecord, index + 1, index + length);
            records.add(new AdRecord(length, type, data));
        }
        return records;
    }
}