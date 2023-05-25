package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.entity.UrlExpansionEnum;

import androidx.annotation.FontRes;
import androidx.annotation.IntRange;


public class ParamsTask extends OrderTask {
    public byte[] data;

    public ParamsTask() {
        super(OrderCHAR.CHAR_PARAMS, OrderTask.RESPONSE_TYPE_WRITE_NO_RESPONSE);
    }

    @Override
    public byte[] assemble() {
        return data;
    }

    public void getData(ParamsKeyEnum key) {
        switch (key) {
            case KEY_DEVICE_MAC:
            case KEY_AXIS_PARAMS:
            case KEY_BLE_CONNECTABLE:
            case KEY_HALL_POWER_ENABLE:
            case KEY_SCAN_RESPONSE_ENABLE:
            case KEY_BATTERY_VOLTAGE:
            case KEY_ALL_SLOT:
            case KEY_SENSOR_TYPE:
            case KEY_ACC_TYPE:
            case KEY_MOTION_TRIGGER_COUNT:
            case KEY_MAGNETIC_TRIGGER_COUNT:
            case KEY_TRIGGER_LED_INDICATOR_ENABLE:
            case KEY_ADV_MODE:
            case KEY_STATIC_HEARTBEAT:
            case KEY_BATTERY_MODE:
                createGetParamsData(key.getParamsKey());
                break;
        }
    }

    public void setData(ParamsKeyEnum key) {
        switch (key) {
            case KEY_CLOSE:
            case KEY_DEFAULT:
            case KEY_RESET:
            case KEY_MOTION_TRIGGER_COUNT:
            case KEY_MAGNETIC_TRIGGER_COUNT:
                createSetParamsData(key.getParamsKey());
                break;
        }
    }

    public void setStaticHeartbeat(@IntRange(from = 1, to = 65535) int staticTime,
                                   @IntRange(from = 1, to = 65535) int advDuration,
                                   @IntRange(from = 0, to = 1) int enable) {
        byte[] bytesTime = MokoUtils.toByteArray(staticTime, 2);
        byte[] bytesDuration = MokoUtils.toByteArray(advDuration, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_STATIC_HEARTBEAT.getParamsKey(),
                (byte) 0x05,
                (byte) enable,
                bytesTime[0],
                bytesTime[1],
                bytesDuration[0],
                bytesDuration[1]
        };
    }

    //设置远程控制led
    public void setRemoteReminder(@IntRange(from = 100, to = 10000) int interval,
                                  @IntRange(from = 1, to = 600) int time) {
        byte[] bytesInterval = MokoUtils.toByteArray(interval, 2);
        byte[] bytesTime = MokoUtils.toByteArray(time, 2);
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_REMINDER.getParamsKey(),
                (byte) 0x05,
                (byte) 0x03,
                bytesInterval[0],
                bytesInterval[1],
                bytesTime[0],
                bytesTime[1]
        };
    }

    public void resetBattery() {
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_RESET_BATTERY.getParamsKey(),
                (byte) 0x01,
                (byte) 0x01
        };
    }

    public void setBatteryMode(int mode){
        response.responseValue = data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BATTERY_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) mode
        };
    }


    private void createGetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x00, (byte) paramsKey, (byte) 0x00};
    }

    private void createSetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x01, (byte) paramsKey, (byte) 0x00};
    }

    public void setAxisParams(@IntRange(from = 0, to = 4) int rate,
                              @IntRange(from = 0, to = 3) int scale,
                              @IntRange(from = 1, to = 255) int sensitivity) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_PARAMS.getParamsKey(),
                (byte) 0x03,
                (byte) rate,
                (byte) scale,
                (byte) sensitivity
        };
        response.responseValue = data;
    }

    public void setBleConnectable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BLE_CONNECTABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }


    public void setHallPowerEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_HALL_POWER_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setTriggerLEDIndicatorEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_TRIGGER_LED_INDICATOR_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setScanResponseEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SCAN_RESPONSE_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void getSlotAdvParams(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void getSlotParams(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotParamsNoData(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) 0xFF // No Data
        };
        response.responseValue = data;
    }

    public void setSlotParamsUID(@IntRange(from = 0, to = 5) int slot,
                                 String namespaceId, String instanceId) {
        byte[] namespaceIdBytes = MokoUtils.hex2bytes(namespaceId);
        byte[] instanceIdBytes = MokoUtils.hex2bytes(instanceId);
        data = new byte[22];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey();
        data[3] = (byte) 0x12;
        data[4] = (byte) slot;
        data[5] = (byte) 0x00; // UID
        for (int i = 0; i < namespaceIdBytes.length; i++) {
            data[6 + i] = namespaceIdBytes[i];
        }
        for (int i = 0; i < instanceIdBytes.length; i++) {
            data[16 + i] = instanceIdBytes[i];
        }
        response.responseValue = data;
    }

    public void setSlotParamsURL(@IntRange(from = 0, to = 5) int slot,
                                 int urlScheme, String urlContent) {
        String urlContentHex;
        if (urlContent.indexOf(".") >= 0) {
            String urlExpansion = urlContent.substring(urlContent.lastIndexOf("."));
            UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanDesc(urlExpansion);
            if (urlExpansionEnum == null) {
                urlContentHex = MokoUtils.string2Hex(urlContent);
            } else {
                String content = urlContent.substring(0, urlContent.lastIndexOf("."));
                urlContentHex = MokoUtils.string2Hex(content) + MokoUtils.int2HexString(urlExpansionEnum.getUrlExpanType());
            }
        } else {
            urlContentHex = MokoUtils.string2Hex(urlContent);
        }
        byte[] urlContentBytes = MokoUtils.hex2bytes(urlContentHex);
        int urlLength = urlContentBytes.length;
        data = new byte[7 + urlLength];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey();
        data[3] = (byte) (3 + urlLength);
        data[4] = (byte) slot;
        data[5] = (byte) 0x10;// URL
        data[6] = (byte) urlScheme;
        for (int i = 0; i < urlContentBytes.length; i++) {
            data[7 + i] = urlContentBytes[i];
        }
        response.responseValue = data;
    }

    public void setSlotParamsTagInfo(@IntRange(from = 0, to = 5) int slot,
                                     String deviceName, String tagId) {
        byte[] deviceNameBytes = deviceName.getBytes();
        byte[] tagIdBytes = MokoUtils.hex2bytes(tagId);
        int deviceNameLength = deviceNameBytes.length;
        int tagIdLength = tagIdBytes.length;
        data = new byte[8 + deviceNameLength + tagIdLength];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey();
        data[3] = (byte) (4 + deviceNameLength + tagIdLength);
        data[4] = (byte) slot;
        data[5] = (byte) 0x80; // TAG
        data[6] = (byte) deviceNameLength;
        for (int i = 0; i < deviceNameLength; i++) {
            data[7 + i] = deviceNameBytes[i];
        }
        data[7 + deviceNameLength] = (byte) tagIdLength;
        for (int i = 0; i < tagIdLength; i++) {
            data[8 + deviceNameLength + i] = tagIdBytes[i];
        }
        response.responseValue = data;
    }

    public void setSlotParamsIBeacon(@IntRange(from = 0, to = 5) int slot,
                                     int major, int minor, String uuid) {
        byte[] majorBytes = MokoUtils.toByteArray(major, 2);
        byte[] minorBytes = MokoUtils.toByteArray(minor, 2);
        byte[] uuidBytes = MokoUtils.hex2bytes(uuid);
        data = new byte[26];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey();
        data[3] = (byte) 0x16;
        data[4] = (byte) slot;
        data[5] = (byte) 0x50; // iBeacon
        for (int i = 0; i < majorBytes.length; i++) {
            data[6 + i] = majorBytes[i];
        }
        for (int i = 0; i < minorBytes.length; i++) {
            data[8 + i] = minorBytes[i];
        }
        for (int i = 0; i < uuidBytes.length; i++) {
            data[10 + i] = uuidBytes[i];
        }
        response.responseValue = data;
    }

    public void setSlotParamsTLM(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[6];
        data[0] = (byte) 0xEA;
        data[1] = (byte) 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey();
        data[3] = (byte) 0x02;
        data[4] = (byte) slot;
        data[5] = (byte) 0x20; // TLM
        response.responseValue = data;
    }

    public void setSlotAdvParams(@IntRange(from = 0, to = 5) int slot,
                                 @IntRange(from = 1, to = 100) int interval,
                                 @IntRange(from = 1, to = 65535) int duration,
                                 @IntRange(from = 0, to = 65535) int standbyDuration,
                                 @IntRange(from = -100, to = 0) int rssi,
                                 @IntRange(from = -40, to = 4) int txPower) {
        byte[] durationBytes = MokoUtils.toByteArray(duration, 2);
        byte[] standbyDurationBytes = MokoUtils.toByteArray(standbyDuration, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_PARAMS.getParamsKey(),
                (byte) 0x08,
                (byte) slot,
                (byte) interval,
                durationBytes[0],
                durationBytes[1],
                standbyDurationBytes[0],
                standbyDurationBytes[1],
                (byte) rssi,
                (byte) txPower
        };
        response.responseValue = data;
    }

    public void getSlotTriggerParams(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotTriggerClose(@IntRange(from = 0, to = 5) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) 0x00
        };
        response.responseValue = data;
    }

    public void setSlotTriggerMotionParams(@IntRange(from = 0, to = 5) int slot,
                                           @IntRange(from = 0, to = 1) int status,
                                           @IntRange(from = 0, to = 65535) int duration,
                                           @IntRange(from = 1, to = 65535) int staticDuration) {
        byte[] durationBytes = MokoUtils.toByteArray(duration, 2);
        byte[] staticDurationBytes = MokoUtils.toByteArray(staticDuration, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x07,
                (byte) slot,
                (byte) 0x05,
                (byte) status,
                durationBytes[0],
                durationBytes[1],
                staticDurationBytes[0],
                staticDurationBytes[1]
        };
        response.responseValue = data;
    }

    public void setSlotTriggerMagneticParams(@IntRange(from = 0, to = 5) int slot,
                                             @IntRange(from = 0, to = 1) int status,
                                             @IntRange(from = 0, to = 65535) int duration) {
        byte[] durationBytes = MokoUtils.toByteArray(duration, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x05,
                (byte) slot,
                (byte) 0x06,
                (byte) status,
                durationBytes[0],
                durationBytes[1]
        };
        response.responseValue = data;
    }

    public void setAdvMode(int advMode) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ADV_MODE.getParamsKey(),
                (byte) 0x01,
                (byte) advMode
        };
        response.responseValue = data;
    }
}
