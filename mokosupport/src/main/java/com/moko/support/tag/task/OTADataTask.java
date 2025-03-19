package com.moko.support.tag.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.tag.entity.OrderCHAR;


public class OTADataTask extends OrderTask {
    public byte[] data;

    public OTADataTask() {
        super(OrderCHAR.CHAR_OTA_DATA, OrderTask.RESPONSE_TYPE_WRITE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }


    public void setData(byte[] data) {
        this.data = data;
    }
}
