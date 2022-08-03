package com.moko.bxp.tag.entity;


import com.moko.support.entity.UrlSchemeEnum;

import java.io.Serializable;

public class SlotData implements Serializable {
    public SlotEnum slotEnum;
    public SlotFrameTypeEnum frameTypeEnum;
    // iBeacon
    public String iBeaconUUID;
    public String major;
    public String minor;
    public int rssi_1m;
    // URL
    public UrlSchemeEnum urlSchemeEnum;
    public String urlContentHex;
    // UID
    public String namespace;
    public String instanceId;
    // TLM
    // No data
    // Tag
    public String deviceName;
    public String tagId;

    // BaseParam
    public int rssi_0m;
    public int txPower;
    public int advInterval;
    public int advDuration;
    public int standbyDuration;

    // Trigger
    public int triggerType;
    public int triggerAdvStatus;
    public int triggerAdvDuration;
    public int staticDuration;


    @Override
    public String toString() {
        return "SlotData{" +
                "slotEnum=" + slotEnum.getTitle() +
                ", frameTypeEnum=" + frameTypeEnum.getShowName() +
                ", iBeaconUUID='" + iBeaconUUID + '\'' +
                ", major='" + major + '\'' +
                ", minor='" + minor + '\'' +
                ", rssi_1m='" + rssi_1m + '\'' +
                ", urlContent='" + urlContentHex + '\'' +
                ", namespace='" + namespace + '\'' +
                ", instanceId='" + instanceId + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", tagId='" + tagId + '\'' +
                ", rssi_0m='" + rssi_0m + '\'' +
                ", txPower=" + txPower +
                ", advInterval=" + advInterval +
                ", advDuration=" + advDuration +
                ", standbyDuration=" + standbyDuration +
                ", triggerType=" + triggerType +
                ", triggerAdvStatus=" + triggerAdvStatus +
                ", triggerAdvDuration=" + triggerAdvDuration +
                ", staticDuration=" + staticDuration +
                '}';
    }
}
