package com.moko.support;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.task.GetFirmwareRevisionTask;
import com.moko.support.task.GetHardwareRevisionTask;
import com.moko.support.task.GetManufacturerNameTask;
import com.moko.support.task.GetModelNumberTask;
import com.moko.support.task.GetSerialNumberTask;
import com.moko.support.task.GetSoftwareRevisionTask;
import com.moko.support.task.ParamsTask;
import com.moko.support.task.PasswordTask;

import androidx.annotation.IntRange;

public class OrderTaskAssembler {

    /**
     * @Description 获取制造商
     */
    public static OrderTask getManufacturer() {
        GetManufacturerNameTask task = new GetManufacturerNameTask();
        return task;
    }

    /**
     * @Description 获取设备型号
     */
    public static OrderTask getDeviceModel() {
        GetModelNumberTask task = new GetModelNumberTask();
        return task;
    }

    /**
     * @Description 获取生产日期
     */
    public static OrderTask getProductDate() {
        GetSerialNumberTask task = new GetSerialNumberTask();
        return task;
    }

    /**
     * @Description 获取硬件版本
     */
    public static OrderTask getHardwareVersion() {
        GetHardwareRevisionTask task = new GetHardwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取固件版本
     */
    public static OrderTask getFirmwareVersion() {
        GetFirmwareRevisionTask task = new GetFirmwareRevisionTask();
        return task;
    }

    /**
     * @Description 获取软件版本
     */
    public static OrderTask getSoftwareVersion() {
        GetSoftwareRevisionTask task = new GetSoftwareRevisionTask();
        return task;
    }


    public static OrderTask getDeviceMac() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DEVICE_MAC);
        return task;
    }

    public static OrderTask getAxisParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_AXIS_PARAMS);
        return task;
    }

    public static OrderTask setAxisParams(@IntRange(from = 0, to = 4) int rate,
                                          @IntRange(from = 0, to = 3) int scale,
                                          @IntRange(from = 1, to = 2048) int sensitivity) {
        ParamsTask task = new ParamsTask();
        task.setAxisParams(rate, scale, sensitivity);
        return task;
    }

    /**
     * @Description 获取连接状态
     */
    public static OrderTask getConnectable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BLE_CONNECTABLE);
        return task;
    }

    /**
     * @Description 设置连接状态
     */
    public static OrderTask setConnectable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setBleConnectable(enable);
        return task;
    }

    public static OrderTask getVerifyPasswordEnable() {
        PasswordTask task = new PasswordTask();
        task.setData(ParamsKeyEnum.KEY_VERIFY_PASSWORD_ENABLE);
        return task;
    }

    public static OrderTask setVerifyPasswordEnable(@IntRange(from = 0, to = 1) int enable) {
        PasswordTask task = new PasswordTask();
        task.setVerifyPasswordEnable(enable);
        return task;
    }

    public static OrderTask setPassword(String password) {
        PasswordTask task = new PasswordTask();
        task.setPassword(password);
        return task;
    }

    public static OrderTask setNewPassword(String password) {
        PasswordTask task = new PasswordTask();
        task.setNewPassword(password);
        return task;
    }

    public static OrderTask getEffectiveClickInterval() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_EFFECTIVE_CLICK_INTERVAL);
        return task;
    }

    public static OrderTask setEffectiveClickInterval(@IntRange(from = 500, to = 1500) int interval) {
        ParamsTask task = new ParamsTask();
        task.setEffectiveClickInterval(interval);
        return task;
    }

    public static OrderTask getScanResponseEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SCAN_RESPONSE_ENABLE);
        return task;
    }

    public static OrderTask setScanResponseEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setScanResponseEnable(enable);
        return task;
    }

    public static OrderTask getChangePasswordDisconnectEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_CHANGE_PASSWORD_DISCONNECT_ENABLE);
        return task;
    }

    public static OrderTask setChangePasswordDisconnectEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setChangePasswordDisconnectEnable(enable);
        return task;
    }

    /**
     * @Description 获取UTC0时区时间
     */
    public static OrderTask getSystemTime() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SYSTEM_TIME);
        return task;
    }

    /**
     * @Description 设置UTC0时区时间
     */
    public static OrderTask setSystemTime() {
        ParamsTask task = new ParamsTask();
        task.setSystemTime();
        return task;
    }


    /**
     * @Description 获取按键关键
     */
    public static OrderTask getButtonPowerEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BUTTON_POWER_ENABLE);
        return task;
    }

    /**
     * @Description 设置按键关键
     */
    public static OrderTask setButtonPowerEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setButtonPowerEnable(enable);
        return task;
    }

    public static OrderTask getButtonResetEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BUTTON_RESET_ENABLE);
        return task;
    }

    public static OrderTask setButtonResetEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setButtonResetEnable(enable);
        return task;
    }

    /**
     * @Description 获取电池电量
     */
    public static OrderTask getBattery() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_BATTERY_VOLTAGE);
        return task;
    }

    /**
     * @Description 关机
     */
    public static OrderTask setClose() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_CLOSE);
        return task;
    }

    /**
     * @Description 保存为默认值
     */
    public static OrderTask setDefault() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DEFAULT);
        return task;
    }

    /**
     * @Description 恢复出厂设置
     */
    public static OrderTask resetDevice() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_RESET);
        return task;
    }

    public static OrderTask setSinglePressEventClear() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_SINGLE_PRESS_EVENT_CLEAR);
        return task;
    }

    public static OrderTask setDoublePressEventClear() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DOUBLE_PRESS_EVENT_CLEAR);
        return task;
    }

    public static OrderTask setLongPressEventClear() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_LONG_PRESS_EVENT_CLEAR);
        return task;
    }

    public static OrderTask getSlotParams(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotParams(slot);
        return task;
    }

    public static OrderTask setSlotParams(@IntRange(from = 0, to = 3) int slot,
                                          @IntRange(from = 0, to = 1) int enable,
                                          @IntRange(from = -100, to = 0) int rssi,
                                          @IntRange(from = 20, to = 10000) int interval,
                                          @IntRange(from = -40, to = 4) int txPower) {
        ParamsTask task = new ParamsTask();
        task.setSlotParams(slot, enable, rssi, interval, txPower);
        return task;
    }

    public static OrderTask getSlotTriggerParams(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotTriggerParams(slot);
        return task;
    }

    public static OrderTask setSlotTriggerParams(@IntRange(from = 0, to = 3) int slot,
                                                 @IntRange(from = 0, to = 1) int enable,
                                                 @IntRange(from = -100, to = 0) int rssi,
                                                 @IntRange(from = 20, to = 10000) int interval,
                                                 @IntRange(from = -40, to = 4) int txPower,
                                                 @IntRange(from = 1, to = 65535) int triggerAdvTime) {
        ParamsTask task = new ParamsTask();
        task.setSlotTriggerParams(slot, enable, rssi, interval, txPower, triggerAdvTime);
        return task;
    }

    public static OrderTask getSlotAdvBeforeTriggerEnable(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotAdvBeforeTriggerEnable(slot);
        return task;
    }

    public static OrderTask setSlotAdvBeforeTriggerEnable(@IntRange(from = 0, to = 3) int slot,
                                                          @IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setSlotAdvBeforeTriggerEnable(slot, enable);
        return task;
    }

    public static OrderTask getAbnormalInactivityAlarmStaticInterval() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ABNORMAL_INACTIVITY_ALARM_STATIC_INTERVAL);
        return task;
    }

    public static OrderTask setAbnormalInactivityAlarmStaticInterval(@IntRange(from = 1, to = 65535) int interval) {
        ParamsTask task = new ParamsTask();
        task.setAbnormalInactivityAlarmStaticInterval(interval);
        return task;
    }

    public static OrderTask getSlotTriggerAlarmNotifyType(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotTriggerAlarmNotifyType(slot);
        return task;
    }

    public static OrderTask setSlotTriggerAlarmNotifyType(@IntRange(from = 0, to = 3) int slot,
                                                          @IntRange(from = 0, to = 5) int type) {
        ParamsTask task = new ParamsTask();
        task.setSlotTriggerAlarmNotifyType(slot, type);
        return task;
    }

    public static OrderTask getPowerSavingEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_POWER_SAVING_ENABLE);
        return task;
    }

    public static OrderTask setPowerSavingEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setPowerSavingEnable(enable);
        return task;
    }

    public static OrderTask getPowerSavingStaticTriggerTime() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_POWER_SAVING_STATIC_TRIGGER_TIME);
        return task;
    }

    public static OrderTask setPowerSavingStaticTriggerTime(@IntRange(from = 1, to = 65535) int time) {
        ParamsTask task = new ParamsTask();
        task.setPowerSavingStaticTriggerTime(time);
        return task;
    }

    public static OrderTask getSlotLEDNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotLEDNotifyAlarmParams(slot);
        return task;
    }

    public static OrderTask setSlotLEDNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                                        @IntRange(from = 1, to = 6000) int time,
                                                        @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setSlotLEDNotifyAlarmParams(slot, time, interval);
        return task;
    }

    public static OrderTask getSlotVibrationNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotVibrationNotifyAlarmParams(slot);
        return task;
    }

    public static OrderTask setSlotVibrationNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                                              @IntRange(from = 1, to = 6000) int time,
                                                              @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setSlotVibrationNotifyAlarmParams(slot, time, interval);
        return task;
    }

    public static OrderTask getSlotBuzzerNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotBuzzerNotifyAlarmParams(slot);
        return task;
    }

    public static OrderTask setSlotBuzzerNotifyAlarmParams(@IntRange(from = 0, to = 3) int slot,
                                                           @IntRange(from = 1, to = 6000) int time,
                                                           @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setSlotBuzzerNotifyAlarmParams(slot, time, interval);
        return task;
    }

    public static OrderTask getRemoteLEDNotifyAlarmParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_REMOTE_LED_NOTIFY_ALARM_PARAMS);
        return task;
    }

    public static OrderTask setRemoteLEDNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                          @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setRemoteLEDNotifyAlarmParams(time, interval);
        return task;
    }

    public static OrderTask getRemoteVibrationNotifyAlarmParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_REMOTE_VIBRATION_NOTIFY_ALARM_PARAMS);
        return task;
    }

    public static OrderTask setRemoteVibrationNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                                @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setRemoteVibrationNotifyAlarmParams(time, interval);
        return task;
    }

    public static OrderTask getRemoteBuzzerNotifyAlarmParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_REMOTE_BUZZER_NOTIFY_ALARM_PARAMS);
        return task;
    }

    public static OrderTask setRemoteBuzzerNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                             @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setRemoteBuzzerNotifyAlarmParams(time, interval);
        return task;
    }

    public static OrderTask setDismissAlarm() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_DISMISS_ALARM);
        return task;
    }

    public static OrderTask setDismissAlarmEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setDismissAlarmEnable(enable);
        return task;
    }

    public static OrderTask getDismissAlarmEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DISMISS_ALARM_ENABLE);
        return task;
    }

    public static OrderTask getDismissLEDNotifyAlarmParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DISMISS_LED_NOTIFY_ALARM_PARAMS);
        return task;
    }

    public static OrderTask setDismissLEDNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                           @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setDismissLEDNotifyAlarmParams(time, interval);
        return task;
    }

    public static OrderTask getDismissVibrationNotifyAlarmParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DISMISS_VIBRATION_NOTIFY_ALARM_PARAMS);
        return task;
    }

    public static OrderTask setDismissVibrationNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                                 @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setDismissVibrationNotifyAlarmParams(time, interval);
        return task;
    }

    public static OrderTask getDismissBuzzerNotifyAlarmParams() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DISMISS_BUZZER_NOTIFY_ALARM_PARAMS);
        return task;
    }

    public static OrderTask setDismissBuzzerNotifyAlarmParams(@IntRange(from = 1, to = 6000) int time,
                                                              @IntRange(from = 100, to = 10000) int interval) {
        ParamsTask task = new ParamsTask();
        task.setDismissBuzzerNotifyAlarmParams(time, interval);
        return task;
    }

    public static OrderTask getDismissAlarmType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DISMISS_ALARM_TYPE);
        return task;
    }

    public static OrderTask setDismissAlarmType(@IntRange(from = 0, to = 5) int type) {
        ParamsTask task = new ParamsTask();
        task.setDismissAlarmType(type);
        return task;
    }

    public static OrderTask getDeviceId() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DEVICE_ID);
        return task;
    }

    public static OrderTask setDeviceId(String deviceId) {
        ParamsTask task = new ParamsTask();
        task.setDeviceId(deviceId);
        return task;
    }

    public static OrderTask getDeviceName() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DEVICE_NAME);
        return task;
    }

    public static OrderTask setDeviceName(String deviceId) {
        ParamsTask task = new ParamsTask();
        task.setDeviceName(deviceId);
        return task;
    }

    public static OrderTask getSinglePressEventCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SINGLE_PRESS_EVENTS);
        return task;
    }

    public static OrderTask getDoublePressEventCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_DOUBLE_PRESS_EVENTS);
        return task;
    }

    public static OrderTask getLongPressEventCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_LONG_PRESS_EVENTS);
        return task;
    }
}
