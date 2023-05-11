package com.moko.bxp.tag.utils;

import android.os.ParcelUuid;
import android.os.SystemClock;
import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.entity.AdvInfo;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.service.DeviceInfoAnalysis;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
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
        int battery = -1;
        ScanResult result = deviceInfo.scanResult;
        ScanRecord record = result.getScanRecord();
        Map<ParcelUuid, byte[]> map = record.getServiceData();
        // filter
        boolean isEddystone = false;
        boolean isTagInfo = false;
        byte[] values = null;
        int type = -1;
        if (map != null && !map.isEmpty()) {
            Iterator iterator = map.keySet().iterator();
            while (iterator.hasNext()) {
                ParcelUuid parcelUuid = (ParcelUuid) iterator.next();
                if (parcelUuid.toString().startsWith("0000feaa")) {
                    isEddystone = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case AdvInfo.VALID_DATA_FRAME_TYPE_UID:
                                if (bytes.length != 20)
                                    return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_UID;
                                // 00ee0102030405060708090a0102030405060000
                                break;
                            case AdvInfo.VALID_DATA_FRAME_TYPE_URL:
                                if (bytes.length > 20)
                                    return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_URL;
                                // 100c0141424344454609
                                break;
                            case AdvInfo.VALID_DATA_FRAME_TYPE_TLM:
                                if (bytes.length != 14)
                                    return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_TLM;
                                // 20000d18158000017eb20002e754
                                break;
                        }
                    }
                    values = bytes;
                    break;
                } else if (parcelUuid.toString().startsWith("0000feab")) {
                    isTagInfo = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON:
                                if (bytes.length != 23)
                                    return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON;
                                // 50ee0c0102030405060708090a0b0c0d0e0f1000010002
                                break;
                        }
                    }
                    values = bytes;
                    break;
                } else if (parcelUuid.toString().startsWith("0000ea01")) {
                    isTagInfo = true;
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case AdvInfo.VALID_DATA_FRAME_TYPE_TAG_INFO:
                                if (bytes.length < 19)
                                    return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_TAG_INFO;
                                battery = MokoUtils.toInt(Arrays.copyOfRange(bytes, 16, 18));
                                break;
                        }
                    }
                    values = bytes;
                    break;
                }else if (parcelUuid.toString().startsWith("0000eb01")){
                    byte[] bytes = map.get(parcelUuid);
                    if (bytes != null) {
                        switch (bytes[0] & 0xff) {
                            case AdvInfo.VALID_DATA_FRAME_TYPE_PRODUCTION_TEST:
                                if (bytes.length < 13) return null;
                                type = AdvInfo.VALID_DATA_FRAME_TYPE_PRODUCTION_TEST;
                                break;
                        }
                    }
                }

            }
        }
        if ((!isEddystone && !isTagInfo) || values == null || type == -1) {
            return null;
        }
        // avoid repeat
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
            advInfo.scanRecord = deviceInfo.scanRecord;
            advInfo.scanTime = SystemClock.elapsedRealtime();
            advInfo.validDataHashMap = new HashMap<>();
            beaconXInfoHashMap.put(deviceInfo.mac, advInfo);
        }
        String data = MokoUtils.bytesToHexString(values);
        if (advInfo.validDataHashMap.containsKey(data)) {
            return advInfo;
        } else {
            AdvInfo.ValidData validData = new AdvInfo.ValidData();
            validData.data = data;
            validData.type = type;
            validData.txPower = record.getTxPowerLevel();
            if (type == AdvInfo.VALID_DATA_FRAME_TYPE_TLM) {
                advInfo.validDataHashMap.put(String.valueOf(type), validData);
                return advInfo;
            }
            if (type == AdvInfo.VALID_DATA_FRAME_TYPE_TAG_INFO) {
                advInfo.validDataHashMap.put(String.valueOf(type), validData);
                return advInfo;
            }
            advInfo.validDataHashMap.put(String.valueOf(type), validData);
        }
        return advInfo;
    }
}
