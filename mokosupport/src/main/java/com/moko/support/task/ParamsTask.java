package com.moko.support.task;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.TimeZone;

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
//            case KEY_MODIFY_PASSWORD:
            case KEY_EFFECTIVE_CLICK_INTERVAL:
            case KEY_BUTTON_POWER_ENABLE:
            case KEY_SCAN_RESPONSE_ENABLE:
            case KEY_CHANGE_PASSWORD_DISCONNECT_ENABLE:
            case KEY_BUTTON_RESET_ENABLE:
            case KEY_SLOT_ADV_ENABLE:
//            case KEY_SLOT_ACTIVE:
//            case KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE:
//            case KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE:
            case KEY_ABNORMAL_INACTIVITY_ALARM_STATIC_INTERVAL:
            case KEY_POWER_SAVING_ENABLE:
            case KEY_POWER_SAVING_STATIC_TRIGGER_TIME:
//            case KEY_SLOT_LED_NOTIFY_ALARM_PARAMS:
//            case KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS:
//            case KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS:
            case KEY_REMOTE_LED_NOTIFY_ALARM_PARAMS:
            case KEY_REMOTE_VIBRATION_NOTIFY_ALARM_PARAMS:
            case KEY_REMOTE_BUZZER_NOTIFY_ALARM_PARAMS:
            case KEY_DISMISS_ALARM_ENABLE:
            case KEY_DISMISS_LED_NOTIFY_ALARM_PARAMS:
            case KEY_DISMISS_VIBRATION_NOTIFY_ALARM_PARAMS:
            case KEY_DISMISS_BUZZER_NOTIFY_ALARM_PARAMS:
            case KEY_DISMISS_ALARM_TYPE:
            case KEY_BATTERY_VOLTAGE:
            case KEY_SYSTEM_TIME:
            case KEY_DEVICE_ID:
            case KEY_DEVICE_NAME:
            case KEY_SINGLE_PRESS_EVENTS:
            case KEY_DOUBLE_PRESS_EVENTS:
            case KEY_LONG_PRESS_EVENTS:
                createGetParamsData(key.getParamsKey());
                break;
        }
    }

    public void setData(ParamsKeyEnum key) {
        switch (key) {
            case KEY_CLOSE:
            case KEY_DEFAULT:
            case KEY_RESET:
            case KEY_SINGLE_PRESS_EVENT_CLEAR:
            case KEY_DOUBLE_PRESS_EVENT_CLEAR:
            case KEY_LONG_PRESS_EVENT_CLEAR:
            case KEY_DISMISS_ALARM:
                createSetParamsData(key.getParamsKey());
                break;
        }
    }


    private void createGetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x00, (byte) paramsKey, (byte) 0x00};
    }

    private void createSetParamsData(int paramsKey) {
        data = new byte[]{(byte) 0xEA, (byte) 0x01, (byte) paramsKey, (byte) 0x00};
    }

    public void setAxisParams(@IntRange(from = 0, to = 4) int rate,
                              @IntRange(from = 0, to = 3) int scale,
                              @IntRange(from = 1, to = 2048) int sensitivity) {
        byte[] paramsBytes = MokoUtils.toByteArray(sensitivity, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_AXIS_PARAMS.getParamsKey(),
                (byte) 0x04,
                (byte) rate,
                (byte) scale,
                paramsBytes[0],
                paramsBytes[1],
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

    public void setEffectiveClickInterval(@IntRange(from = 500, to = 1500) int interval) {
        byte[] paramsBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_EFFECTIVE_CLICK_INTERVAL.getParamsKey(),
                (byte) 0x02,
                paramsBytes[0],
                paramsBytes[1],
        };
        response.responseValue = data;
    }

    public void setButtonPowerEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUTTON_POWER_ENABLE.getParamsKey(),
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

    public void setChangePasswordDisconnectEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_CHANGE_PASSWORD_DISCONNECT_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }


    public void setButtonResetEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_BUTTON_RESET_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void getSlotParams(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotParams(@IntRange(from = 0, to = 3) int slot,
                              @IntRange(from = 0, to = 1) int enable,
                              @IntRange(from = -100, to = 0) int rssi,
                              @IntRange(from = 20, to = 10000) int interval,
                              @IntRange(from = -40, to = 4) int txPower) {
        byte[] paramsBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_PARAMS.getParamsKey(),
                (byte) 0x06,
                (byte) slot,
                (byte) enable,
                (byte) rssi,
                paramsBytes[0],
                paramsBytes[1],
                (byte) txPower
        };
        response.responseValue = data;
    }

    public void getSlotTriggerParams(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotTriggerParams(@IntRange(from = 0, to = 3) int slot,
                                     @IntRange(from = 0, to = 1) int enable,
                                     @IntRange(from = -100, to = 0) int rssi,
                                     @IntRange(from = 20, to = 10000) int interval,
                                     @IntRange(from = -40, to = 4) int txPower,
                                     @IntRange(from = 1, to = 65535) int triggerAdvInterval) {
        byte[] paramsBytes = MokoUtils.toByteArray(interval, 2);
        byte[] triggerAdvIntervalBytes = MokoUtils.toByteArray(triggerAdvInterval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_PARAMS.getParamsKey(),
                (byte) 0x08,
                (byte) slot,
                (byte) enable,
                (byte) rssi,
                paramsBytes[0],
                paramsBytes[1],
                (byte) txPower,
                triggerAdvIntervalBytes[0],
                triggerAdvIntervalBytes[1]
        };
        response.responseValue = data;
    }

    public void getSlotAdvBeforeTriggerEnable(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotAdvBeforeTriggerEnable(@IntRange(from = 0, to = 3) int slot,
                                              @IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void getSlotTriggerAlarmNotifyType(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotTriggerAlarmNotifyType(@IntRange(from = 0, to = 3) int slot,
                                              @IntRange(from = 0, to = 5) int type) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE.getParamsKey(),
                (byte) 0x02,
                (byte) slot,
                (byte) type
        };
        response.responseValue = data;
    }

    public void setAbnormalInactivityAlarmStaticInterval(@IntRange(from = 1, to = 65535) int time) {
        byte[] intervalBytes = MokoUtils.toByteArray(time, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_ABNORMAL_INACTIVITY_ALARM_STATIC_INTERVAL.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1]
        };
        response.responseValue = data;
    }

    public void setPowerSavingEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_POWER_SAVING_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setPowerSavingStaticTriggerTime(@IntRange(from = 1, to = 65535) int time) {
        byte[] intervalBytes = MokoUtils.toByteArray(time, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME.getParamsKey(),
                (byte) 0x02,
                intervalBytes[0],
                intervalBytes[1]
        };
        response.responseValue = data;
    }

    public void getSlotLEDNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotLEDNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                            @IntRange(from = 1, to = 6000) int time,
                                            @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x05,
                (byte) slot,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void getSlotVibrationNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotVibrationNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                                  @IntRange(from = 1, to = 6000) int time,
                                                  @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x05,
                (byte) slot,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void getSlotBuzzerNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x00,
                (byte) ParamsKeyEnum.KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x01,
                (byte) slot
        };
        response.responseValue = data;
    }

    public void setSlotBuzzerNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                               @IntRange(from = 1, to = 6000) int time,
                                               @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x05,
                (byte) slot,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setRemoteLEDNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                              @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setRemoteVibrationNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                    @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_VIBRATION_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setRemoteBuzzerNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                 @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_REMOTE_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setDismissAlarmEnable(@IntRange(from = 0, to = 1) int enable) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_ALARM_ENABLE.getParamsKey(),
                (byte) 0x01,
                (byte) enable
        };
        response.responseValue = data;
    }

    public void setDismissLEDNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                               @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_LED_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setDismissVibrationNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                     @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_VIBRATION_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setDismissBuzzerNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                  @IntRange(from = 100, to = 10000) int interval) {
        byte[] timeBytes = MokoUtils.toByteArray(time, 2);
        byte[] intervalBytes = MokoUtils.toByteArray(interval, 2);
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_BUZZER_NOTIFY_ALARM_PARAMS.getParamsKey(),
                (byte) 0x04,
                timeBytes[0],
                timeBytes[1],
                intervalBytes[0],
                intervalBytes[1],
        };
        response.responseValue = data;
    }

    public void setDismissAlarmType(@IntRange(from = 0, to = 5) int type) {
        data = new byte[]{
                (byte) 0xEA,
                (byte) 0x01,
                (byte) ParamsKeyEnum.KEY_DISMISS_ALARM_TYPE.getParamsKey(),
                (byte) 0x01,
                (byte) type
        };
        response.responseValue = data;
    }

    public void setSystemTime() {
        Calendar calendar = Calendar.getInstance();
        TimeZone timeZone = TimeZone.getTimeZone("GMT");
        calendar.setTimeZone(timeZone);
        long unixTime = calendar.getTimeInMillis();
        byte[] unixTimeBytes = ByteBuffer.allocate(Long.SIZE / Byte.SIZE).putLong(unixTime).array();
        int length = unixTimeBytes.length;
        data = new byte[length + 4];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_SYSTEM_TIME.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < unixTimeBytes.length; i++) {
            data[i + 4] = unixTimeBytes[i];
        }
        response.responseValue = data;
    }

    public void setDeviceId(String deviceId) {
        byte[] deviceIdBytes = MokoUtils.hex2bytes(deviceId);
        int length = deviceIdBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_DEVICE_ID.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = deviceIdBytes[i];
        }
        response.responseValue = data;
    }

    public void setDeviceName(String deviceName) {
        byte[] deviceNameBytes = deviceName.getBytes();
        int length = deviceNameBytes.length;
        data = new byte[4 + length];
        data[0] = (byte) 0xEA;
        data[1] = 0x01;
        data[2] = (byte) ParamsKeyEnum.KEY_DEVICE_NAME.getParamsKey();
        data[3] = (byte) length;
        for (int i = 0; i < length; i++) {
            data[i + 4] = deviceNameBytes[i];
        }
        response.responseValue = data;
    }
}
