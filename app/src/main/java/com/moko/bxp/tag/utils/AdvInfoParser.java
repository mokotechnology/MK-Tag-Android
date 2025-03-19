package com.moko.bxp.tag.utils;

import android.text.TextUtils;

import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.entity.AdvIBeacon;
import com.moko.bxp.tag.entity.AdvInfo;
import com.moko.bxp.tag.entity.AdvTLM;
import com.moko.bxp.tag.entity.AdvTag;
import com.moko.bxp.tag.entity.AdvUID;
import com.moko.bxp.tag.entity.AdvURL;
import com.moko.support.tag.entity.UrlExpansionEnum;
import com.moko.support.tag.entity.UrlSchemeEnum;


public class AdvInfoParser {

    public static AdvUID getUID(String data) {
        // 00ee0102030405060708090a0102030405060000
        AdvUID uid = new AdvUID();
        int rssi = Integer.parseInt(data.substring(2, 4), 16);
        uid.rssi = (byte) rssi + "";
        uid.namespaceId = data.substring(4, 24).toUpperCase();
        uid.instanceId = data.substring(24, 36).toUpperCase();
        return uid;
    }

    public static AdvURL getURL(String data) {
        // 100c0141424344454609
        AdvURL url = new AdvURL();
        int rssi = Integer.parseInt(data.substring(2, 4), 16);
        url.rssi = (byte) rssi + "";

        UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.parseInt(data.substring(4, 6), 16));
        String urlSchemeStr = "";
        if (urlSchemeEnum != null) {
            urlSchemeStr = urlSchemeEnum.getUrlDesc();
        }
        String urlExpansionStr = "";
        UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanType(Integer.parseInt(data.substring(data.length() - 2), 16));
        if (urlExpansionEnum != null) {
            urlExpansionStr = urlExpansionEnum.getUrlExpanDesc();
        }
        String urlStr;
        if (TextUtils.isEmpty(urlExpansionStr)) {
            urlStr = urlSchemeStr + MokoUtils.hex2String(data.substring(6));
        } else {
            urlStr = urlSchemeStr + MokoUtils.hex2String(data.substring(6, data.length() - 2)) + urlExpansionStr;
        }
        url.url = urlStr;
        return url;
    }

    public static AdvTLM getTLM(String data) {
        // 20000d18158000017eb20002e754
        AdvTLM tlm = new AdvTLM();
        tlm.vbatt = Integer.parseInt(data.substring(4, 8), 16) + "";
        int temp1 = Integer.parseInt(data.substring(8, 10), 16);
        int temp2 = Integer.parseInt(data.substring(10, 12), 16);
        int tempInt = temp1 > 128 ? temp1 - 256 : temp1;
        float tempDecimal = temp2 / 256.0f;
        float temperature = tempInt + tempDecimal;
        String tempStr = MokoUtils.getDecimalFormat("0.##").format(temperature);
        tlm.temp = String.format("%s°C", tempStr);
        tlm.adv_cnt = Long.parseLong(data.substring(12, 20), 16) + "";
        long seconds = Long.parseLong(data.substring(20, 28), 16) / 10;
        int day = 0, hours = 0, minutes = 0;
        day = (int) (seconds / (60 * 60 * 24));
        seconds -= day * 60 * 60 * 24;
        hours = (int) (seconds / (60 * 60));
        seconds -= hours * 60 * 60;
        minutes = (int) (seconds / 60);
        seconds -= minutes * 60;
        tlm.sec_cnt = String.format("%dd%dh%dm%ds", day, hours, minutes, seconds);
        return tlm;
    }

    public static AdvIBeacon getIBeacon(int rssi, String data,int type) {
        // 50ee0c0102030405060708090a0b0c0d0e0f1000010002
        AdvIBeacon iBeacon = new AdvIBeacon();
        if (type == AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
            int rssi_1m = Integer.parseInt(data.substring(2, 4), 16);
            iBeacon.rssi = (byte) rssi_1m + "";
            String uuid = data.substring(6, 38).toLowerCase();
            StringBuilder stringBuilder = new StringBuilder(uuid);
            stringBuilder.insert(8, "-");
            stringBuilder.insert(13, "-");
            stringBuilder.insert(18, "-");
            stringBuilder.insert(23, "-");
            iBeacon.uuid = stringBuilder.toString();
            iBeacon.major = Integer.parseInt(data.substring(38, 42), 16) + "";
            iBeacon.minor = Integer.parseInt(data.substring(42, 46), 16) + "";
            double distance = MokoUtils.getDistance(rssi, Math.abs((byte) rssi_1m));
            String distanceDesc = "Unknown";
            if (distance <= 0.1) {
                distanceDesc = "Immediate";
            } else if (distance > 0.1 && distance <= 1.0) {
                distanceDesc = "Near";
            } else if (distance > 1.0) {
                distanceDesc = "Far";
            }
            iBeacon.distanceDesc = distanceDesc;
        }else if (type == AdvInfo.VALID_DATA_TYPE_IBEACON_APPLE){
            String uuid = data.substring(4, 36).toLowerCase();
            StringBuilder stringBuilder = new StringBuilder(uuid);
            stringBuilder.insert(8, "-");
            stringBuilder.insert(13, "-");
            stringBuilder.insert(18, "-");
            stringBuilder.insert(23, "-");
            iBeacon.uuid = stringBuilder.toString();
            iBeacon.major = Integer.parseInt(data.substring(36, 40), 16) + "";
            iBeacon.minor = Integer.parseInt(data.substring(40, 44), 16) + "";
            int rssi_1m = Integer.parseInt(data.substring(44, 46), 16);
            iBeacon.rssi = (byte) rssi_1m + "";
            double distance = MokoUtils.getDistance(rssi, Math.abs((byte) rssi_1m));
            String distanceDesc = "Unknown";
            if (distance <= 0.1) {
                distanceDesc = "Immediate";
            } else if (distance > 0.1 && distance <= 1.0) {
                distanceDesc = "Near";
            } else if (distance > 1.0) {
                distanceDesc = "Far";
            }
            iBeacon.distanceDesc = distanceDesc;
        }
        return iBeacon;
    }

    public static AdvTag getTagInfo(String data) {
        AdvTag advTag = new AdvTag();
        advTag.hallStatus = (Integer.parseInt(data.substring(2, 4), 16) & 0x01) == 1 ? "Absent" : "Present";
        advTag.hallTriggerCount = String.valueOf(Integer.parseInt(data.substring(4, 8), 16));
        advTag.isAccEnable = (Integer.parseInt(data.substring(2, 4), 16) & 0x04) == 4;
        if (advTag.isAccEnable) {
            advTag.motionStatus = (Integer.parseInt(data.substring(2, 4), 16) & 0x02) == 2 ? "In progress" : "No Movement";
            advTag.motionTriggerCount = String.valueOf(Integer.parseInt(data.substring(8, 12), 16));
            advTag.accX = String.format("X:%dmg", MokoUtils.toIntSigned(MokoUtils.hex2bytes(data.substring(12, 16))));
            advTag.accY = String.format("Y:%dmg", MokoUtils.toIntSigned(MokoUtils.hex2bytes(data.substring(16, 20))));
            advTag.accZ = String.format("Z:%dmg", MokoUtils.toIntSigned(MokoUtils.hex2bytes(data.substring(20, 24))));
        }
        return advTag;
    }
}
