package com.moko.support.entity;


import java.io.Serializable;

public enum ParamsKeyEnum implements Serializable {
    KEY_DEVICE_MAC(0x20),
    KEY_AXIS_PARAMS(0x21),
    KEY_SLOT_ADV_PARAMS(0x22),
    KEY_SLOT_PARAMS(0x23),
    KEY_SLOT_TRIGGER_PARAMS(0x24),
    KEY_BLE_CONNECTABLE(0x25),
    KEY_CLOSE(0x26),
    KEY_DEFAULT(0x27),
    KEY_RESET(0x28),
    KEY_HALL_POWER_ENABLE(0x29),
//    KEY_MANUFACTURER(0x2A),
//    KEY_SERIAL_NUMBER(0x2B),
//    KEY_SOFTWARE_VERSION(0x2C),
//    KEY_HARDWARE_VERSION(0x2D),
//    KEY_MODEL_NUMBER(0x2E),
    KEY_SCAN_RESPONSE_ENABLE(0x2F),
    KEY_ALL_SLOT(0x31),
    KEY_TRIGGER_LED_INDICATOR_ENABLE(0x32),
    KEY_BATTERY_VOLTAGE(0x4A),
    KEY_LOW_POWER_THRESHOLD(0x4C),
    KEY_MOTION_TRIGGER_COUNT(0x4D),
    KEY_MAGNETIC_TRIGGER_COUNT(0x4E),
    KEY_SENSOR_TYPE(0x4F),
    KEY_ACC_TYPE(0x50),
    KEY_PASSWORD(0x51),
    KEY_MODIFY_PASSWORD(0x52),
    KEY_VERIFY_PASSWORD_ENABLE(0x53),
    KEY_ADV_MODE(0x57),
    ;

    private int paramsKey;

    ParamsKeyEnum(int paramsKey) {
        this.paramsKey = paramsKey;
    }


    public int getParamsKey() {
        return paramsKey;
    }

    public static ParamsKeyEnum fromParamKey(int paramsKey) {
        for (ParamsKeyEnum paramsKeyEnum : ParamsKeyEnum.values()) {
            if (paramsKeyEnum.getParamsKey() == paramsKey) {
                return paramsKeyEnum;
            }
        }
        return null;
    }
}
