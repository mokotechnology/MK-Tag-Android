package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.OrderCHAR;


public class GetMagnetStatusTask extends OrderTask {

    public byte[] data;

    public GetMagnetStatusTask() {
        super(OrderCHAR.CHAR_HALL, OrderTask.RESPONSE_TYPE_READ);
    }

    @Override
    public byte[] assemble() {
        return data;
    }
}
