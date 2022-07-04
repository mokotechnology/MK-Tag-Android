package com.moko.bxp.tag.entity;

import java.io.Serializable;
import java.util.LinkedHashMap;

public class AdvInfo implements Serializable {

    public String name;
    public int rssi;
    public String mac;
    public String scanRecord;
    public int battery;
    public long intervalTime;
    public long scanTime;
    public int txPower;
    public int rangingData;
    public int accX = 0;
    public int accY = 0;
    public int accZ = 0;
    public int accShown = 0;
    public int connectState;
    public int verifyEnable;
    public int deviceType;
    public int deviceInfoFrame;
    public String beaconTemp;
    public String deviceId;
    public LinkedHashMap<Integer, TriggerData> triggerDataHashMap;

    @Override
    public String toString() {
        return "AdvInfo{" +
                "name='" + name + '\'' +
                ", mac='" + mac + '\'' +
                '}';
    }

    public static class TriggerData {
        public int triggerType;
        public int triggerStatus;
        public int triggerCount;
        public byte[] dataBytes;
        public String dataStr;

        @Override
        public String toString() {
            return "TriggerData{" +
                    "triggerType=" + triggerType +
                    ", data='" + dataStr + '\'' +
                    '}';
        }
    }
}
