package com.moko.support.entity;

import java.io.Serializable;
import java.util.UUID;

public enum OrderCHAR implements Serializable {
    // 180F
    CHAR_BATTERY(UUID.fromString("00002A19-0000-1000-8000-00805F9B34FB")),
    // 180A
    CHAR_MODEL_NUMBER(UUID.fromString("00002A24-0000-1000-8000-00805F9B34FB")),
    CHAR_SERIAL_NUMBER(UUID.fromString("00002A25-0000-1000-8000-00805F9B34FB")),
    CHAR_MANUFACTURER_NAME(UUID.fromString("00002A29-0000-1000-8000-00805F9B34FB")),
    CHAR_FIRMWARE_REVISION(UUID.fromString("00002A26-0000-1000-8000-00805F9B34FB")),
    CHAR_HARDWARE_REVISION(UUID.fromString("00002A27-0000-1000-8000-00805F9B34FB")),
    CHAR_SOFTWARE_REVISION(UUID.fromString("00002A28-0000-1000-8000-00805F9B34FB")),
    // AA00
    CHAR_PARAMS(UUID.fromString("0000AA01-0000-1000-8000-00805F9B34FB")),
    CHAR_DISCONNECT(UUID.fromString("0000AA02-0000-1000-8000-00805F9B34FB")),
    CHAR_ACC(UUID.fromString("0000AA06-0000-1000-8000-00805F9B34FB")),
    CHAR_PASSWORD(UUID.fromString("0000AA07-0000-1000-8000-00805F9B34FB")),
    CHAR_HALL(UUID.fromString("0000AA08-0000-1000-8000-00805F9B34FB")),
    // OTA
    CHAR_OTA_CONTROL(UUID.fromString("F7BF3564-FB6D-4E53-88A4-5E37E0326063")),
    CHAR_OTA_DATA(UUID.fromString("984227F3-34FC-4045-A5D0-2C581F81A153")),
    ;

    private UUID uuid;

    OrderCHAR(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }
}
