package com.moko.bxp.tag.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.entity.AdvInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderServices;
import com.moko.support.service.DeviceInfoAnalysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import no.nordicsemi.android.support.v18.scanner.ScanRecord;
import no.nordicsemi.android.support.v18.scanner.ScanResult;


public class AdvInfoAnalysisImpl implements DeviceInfoAnalysis<AdvInfo> {
    private HashMap<String, AdvInfo> beaconXInfoHashMap;

    public AdvInfoAnalysisImpl() {
        this.beaconXInfoHashMap = new HashMap<>();
    }

    @Override
    public AdvInfo parseDeviceInfo(DeviceInfo deviceInfo) {
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        Map<ParcelUuid, byte[]> map = record.getServiceData();
        if (map == null || map.isEmpty()) return null;
        int battery = -1;
        int triggerStatus = -1;
        int triggerCount = -1;
        String deviceId = "";
//        String beaconTemp = "";
        int accX = 0;
        int accY = 0;
        int accZ = 0;
        int accShown = 0;
        int deviceInfoFrame = -1;
        int triggerTypeFrame = -1;
        int rangeData = -1;
        int verifyEnable = 0;
        int deviceType = 0;
        String dataStr = "";
        byte[] dataBytes = new byte[0];
        final Iterator iterator = map.keySet().iterator();
        while (iterator.hasNext()) {
            final ParcelUuid parcelUuid = (ParcelUuid) iterator.next();
            if (parcelUuid.getUuid().equals(OrderServices.SERVICE_ADV_DEVICE.getUuid())) {
                byte[] data = map.get(new ParcelUuid(OrderServices.SERVICE_ADV_DEVICE.getUuid()));
                if (data == null || data.length == 0 || data.length < 21)
                    continue;
                deviceInfoFrame = data[0] & 0xFF;
                accX = MokoUtils.toIntSigned(Arrays.copyOfRange(data, 4, 6));
                accY = MokoUtils.toIntSigned(Arrays.copyOfRange(data, 6, 8));
                accZ = MokoUtils.toIntSigned(Arrays.copyOfRange(data, 8, 10));
//                int tempInteger = data[10];
//                int tempDecimal = data[11] & 0xFF;
//                beaconTemp = String.format("%d.%d", tempInteger, tempDecimal);
                rangeData = data[12];
                battery = MokoUtils.toInt(Arrays.copyOfRange(data, 13, 15));
            }
            if (parcelUuid.getUuid().equals(OrderServices.SERVICE_ADV_TRIGGER.getUuid())) {
                byte[] data = map.get(new ParcelUuid(OrderServices.SERVICE_ADV_TRIGGER.getUuid()));
                if (data == null || data.length == 0 || data.length < 5)
                    continue;
                dataStr = MokoUtils.bytesToHexString(data);
                dataBytes = data;
                triggerTypeFrame = data[0] & 0xFF;
                verifyEnable = (data[1] & 0x01) == 0x01 ? 1 : 0;
                triggerStatus = (data[1] & 0x02) == 0x02 ? 1 : 0;
                triggerCount = MokoUtils.toInt(Arrays.copyOfRange(data, 2, 4));
                deviceId = String.format("0x%s", MokoUtils.bytesToHexString(Arrays.copyOfRange(data, 4, data.length - 2)).toUpperCase());
                deviceType = data[data.length - 2] & 0xFF;
            }

        }
        if (accX != 0 || accY != 0 || accZ != 0) {
            accShown = 1;
        }
        AdvInfo advInfo;
        if (beaconXInfoHashMap.containsKey(deviceInfo.mac)) {
            advInfo = beaconXInfoHashMap.get(deviceInfo.mac);
            if (!TextUtils.isEmpty(deviceInfo.name)) {
                advInfo.name = deviceInfo.name;
            }
            advInfo.rssi = deviceInfo.rssi;
            if (battery >= 0) {
                advInfo.battery = battery;
            }
            if (result.isConnectable())
                advInfo.connectState = 1;
            advInfo.txPower = record.getTxPowerLevel();
            advInfo.rangingData = rangeData;
            advInfo.deviceId = deviceId;
            advInfo.verifyEnable = verifyEnable;
            advInfo.deviceType = deviceType;
            advInfo.scanRecord = deviceInfo.scanRecord;
            long currentTime = SystemClock.elapsedRealtime();
            long intervalTime = currentTime - advInfo.scanTime;
            advInfo.intervalTime = intervalTime;
            advInfo.scanTime = currentTime;
        } else {
            advInfo = new AdvInfo();
            advInfo.name = deviceInfo.name;
            advInfo.mac = deviceInfo.mac;
            advInfo.rssi = deviceInfo.rssi;
            if (battery < 0) {
                advInfo.battery = -1;
            } else {
                advInfo.battery = battery;
            }
            if (result.isConnectable()) {
                advInfo.connectState = 1;
            } else {
                advInfo.connectState = 0;
            }
            advInfo.txPower = record.getTxPowerLevel();
            advInfo.rangingData = rangeData;
            advInfo.deviceId = deviceId;
            advInfo.verifyEnable = verifyEnable;
            advInfo.deviceType = deviceType;
            advInfo.scanRecord = deviceInfo.scanRecord;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.triggerDataHashMap = new LinkedHashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        if (triggerTypeFrame > 0) {
            AdvInfo.TriggerData triggerData = new AdvInfo.TriggerData();
            triggerData.dataStr = dataStr;
            triggerData.dataBytes = dataBytes;
            triggerData.triggerType = triggerTypeFrame;
            triggerData.triggerStatus = triggerStatus;
            triggerData.triggerCount = triggerCount;
            advInfo.triggerDataHashMap.put(triggerTypeFrame, triggerData);
        }
        advInfo.deviceInfoFrame = deviceInfoFrame;
        if (deviceInfoFrame == 0) {
            advInfo.rangingData = rangeData;
            advInfo.accX = accX;
            advInfo.accY = accY;
            advInfo.accZ = accZ;
            advInfo.accShown = accShown;
        }
        return advInfo;
    }
}
