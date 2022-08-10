package com.moko.support;

import com.moko.ble.lib.task.OrderTask;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.task.GetFirmwareRevisionTask;
import com.moko.support.task.GetHardwareRevisionTask;
import com.moko.support.task.GetMagnetStatusTask;
import com.moko.support.task.GetManufacturerNameTask;
import com.moko.support.task.GetModelNumberTask;
import com.moko.support.task.GetSerialNumberTask;
import com.moko.support.task.GetSoftwareRevisionTask;
import com.moko.support.task.OTAControlTask;
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
                                          @IntRange(from = 1, to = 255) int sensitivity) {
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


    /**
     * @Description 获取霍尔关机功能
     */
    public static OrderTask getHallPowerEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_HALL_POWER_ENABLE);
        return task;
    }

    /**
     * @Description 设置霍尔关机功能
     */
    public static OrderTask setHallPowerEnable(int enable) {
        ParamsTask task = new ParamsTask();
        task.setHallPowerEnable(enable);
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

    public static OrderTask getAllSlot() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ALL_SLOT);
        return task;
    }

    public static OrderTask getSensorType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_SENSOR_TYPE);
        return task;
    }

    public static OrderTask getAccType() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_ACC_TYPE);
        return task;
    }

    public static OrderTask getMotionTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MOTION_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask clearMotionTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MOTION_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask getMagneticTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_MAGNETIC_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask clearMagneticTriggerCount() {
        ParamsTask task = new ParamsTask();
        task.setData(ParamsKeyEnum.KEY_MAGNETIC_TRIGGER_COUNT);
        return task;
    }

    public static OrderTask getMagnetStatus() {
        GetMagnetStatusTask task = new GetMagnetStatusTask();
        return task;
    }

    public static OrderTask getTriggerLEDIndicatorEnable() {
        ParamsTask task = new ParamsTask();
        task.getData(ParamsKeyEnum.KEY_TRIGGER_LED_INDICATOR_ENABLE);
        return task;
    }

    public static OrderTask setTriggerLEDIndicatorEnable(@IntRange(from = 0, to = 1) int enable) {
        ParamsTask task = new ParamsTask();
        task.setTriggerLEDIndicatorEnable(enable);
        return task;
    }

    public static OrderTask getSlotAdvParams(@IntRange(from = 0, to = 5) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotAdvParams(slot);
        return task;
    }

    public static OrderTask getSlotParams(@IntRange(from = 0, to = 5) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotParams(slot);
        return task;
    }

    public static OrderTask setSlotAdvParams(@IntRange(from = 0, to = 5) int slot,
                                             @IntRange(from = 1, to = 100) int interval,
                                             @IntRange(from = 1, to = 65535) int duration,
                                             @IntRange(from = 0, to = 65535) int standbyDuration,
                                             @IntRange(from = -100, to = 0) int rssi,
                                             @IntRange(from = -40, to = 4) int txPower) {
        ParamsTask task = new ParamsTask();
        task.setSlotAdvParams(slot, interval, duration, standbyDuration, rssi, txPower);
        return task;
    }

    public static OrderTask setSlotParamsNoData(@IntRange(from = 0, to = 5) int slot) {
        ParamsTask task = new ParamsTask();
        task.setSlotParamsNoData(slot);
        return task;
    }

    public static OrderTask setSlotParamsUID(@IntRange(from = 0, to = 5) int slot,
                                             String namespaceId, String instanceId) {
        ParamsTask task = new ParamsTask();
        task.setSlotParamsUID(slot, namespaceId, instanceId);
        return task;
    }

    public static OrderTask setSlotParamsURL(@IntRange(from = 0, to = 5) int slot,
                                             int urlScheme, String urlContent) {
        ParamsTask task = new ParamsTask();
        task.setSlotParamsURL(slot, urlScheme, urlContent);
        return task;
    }

    public static OrderTask setSlotParamsTagInfo(@IntRange(from = 0, to = 5) int slot,
                                                 String deviceName, String tagId) {
        ParamsTask task = new ParamsTask();
        task.setSlotParamsTagInfo(slot, deviceName, tagId);
        return task;
    }

    public static OrderTask setSlotParamsIBeacon(@IntRange(from = 0, to = 5) int slot,
                                                 int major, int minor, String uuid) {
        ParamsTask task = new ParamsTask();
        task.setSlotParamsIBeacon(slot, major, minor, uuid);
        return task;
    }

    public static OrderTask setSlotParamsTLM(@IntRange(from = 0, to = 5) int slot) {
        ParamsTask task = new ParamsTask();
        task.setSlotParamsTLM(slot);
        return task;
    }


    public static OrderTask setTriggerClose(@IntRange(from = 0, to = 5) int slot) {
        ParamsTask task = new ParamsTask();
        task.setSlotTriggerClose(slot);
        return task;
    }

    public static OrderTask getSlotTriggerParams(@IntRange(from = 0, to = 5) int slot) {
        ParamsTask task = new ParamsTask();
        task.getSlotTriggerParams(slot);
        return task;
    }

    public static OrderTask setSlotTriggerMotionParams(@IntRange(from = 0, to = 5) int slot,
                                                       @IntRange(from = 0, to = 1) int status,
                                                       @IntRange(from = 0, to = 65535) int duration,
                                                       @IntRange(from = 1, to = 65535) int staticDuration) {
        ParamsTask task = new ParamsTask();
        task.setSlotTriggerMotionParams(slot, status, duration, staticDuration);
        return task;
    }

    public static OrderTask setSlotTriggerMagneticParams(@IntRange(from = 0, to = 5) int slot,
                                                         @IntRange(from = 0, to = 1) int status,
                                                         @IntRange(from = 0, to = 65535) int duration) {
        ParamsTask task = new ParamsTask();
        task.setSlotTriggerMagneticParams(slot, status, duration);
        return task;
    }

    public static OrderTask startDFU() {
        OTAControlTask task = new OTAControlTask();
        task.startDFU();
        return task;
    }

    public static OrderTask endDFU() {
        OTAControlTask task = new OTAControlTask();
        task.endDFU();
        return task;
    }
}
