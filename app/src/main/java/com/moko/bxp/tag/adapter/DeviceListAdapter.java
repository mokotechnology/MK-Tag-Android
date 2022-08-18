package com.moko.bxp.tag.adapter;

import android.content.Intent;
import android.graphics.Paint;
import android.net.Uri;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.elvishew.xlog.XLog;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.entity.AdvIBeacon;
import com.moko.bxp.tag.entity.AdvInfo;
import com.moko.bxp.tag.entity.AdvTLM;
import com.moko.bxp.tag.entity.AdvTag;
import com.moko.bxp.tag.entity.AdvUID;
import com.moko.bxp.tag.entity.AdvURL;
import com.moko.bxp.tag.utils.AdvInfoParser;

import java.util.ArrayList;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.list_item_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, item.rssi + "");
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.setText(R.id.tv_battery, item.battery < 0 ? "N/A" : String.format("%dmV", item.battery));
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState > 0);
        helper.setVisible(R.id.tv_tag_id, false);
        LinearLayout parent = helper.getView(R.id.ll_adv_info);
        parent.removeAllViews();
        ArrayList<AdvInfo.ValidData> validDataList = new ArrayList<>(item.validDataHashMap.values());
        for (AdvInfo.ValidData validData : validDataList) {
            XLog.i(validData.toString());
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_UID) {
                parent.addView(createUIDView(AdvInfoParser.getUID(validData.data)));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_URL) {
                parent.addView(createURLView(AdvInfoParser.getURL(validData.data)));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_TLM) {
                parent.addView(createTLMView(AdvInfoParser.getTLM(validData.data)));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_IBEACON) {
                AdvIBeacon beaconXiBeacon = AdvInfoParser.getIBeacon(item.rssi, validData.data);
                beaconXiBeacon.txPower = validData.txPower + "";
                parent.addView(createIBeaconView(beaconXiBeacon));
            }
            if (validData.type == AdvInfo.VALID_DATA_FRAME_TYPE_TAG_INFO) {
                parent.addView(createTagView(AdvInfoParser.getTagInfo(validData.data)));
                helper.setVisible(R.id.tv_tag_id, true);
                helper.setText(R.id.tv_tag_id, String.format("Tag ID:0x%s", validData.data.substring(36)));
            }
        }
    }

    private View createUIDView(AdvUID uid) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_uid, null);
        TextView tvRSSI0M = view.findViewById(R.id.tv_rssi_0m);
        TextView tvNameSpace = view.findViewById(R.id.tv_namespace);
        TextView tvInstanceId = view.findViewById(R.id.tv_instance_id);
        tvRSSI0M.setText(String.format("%sdBm", uid.rssi));
        tvNameSpace.setText("0x" + uid.namespaceId.toUpperCase());
        tvInstanceId.setText("0x" + uid.instanceId.toUpperCase());
        return view;
    }

    private View createURLView(final AdvURL url) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_url, null);
        TextView tvRSSI0M = view.findViewById(R.id.tv_rssi_0m);
        TextView tvUrl = view.findViewById(R.id.tv_url);
        tvRSSI0M.setText(String.format("%sdBm", url.rssi));
        tvUrl.setText(url.url);
        tvUrl.getPaint().setFlags(Paint.UNDERLINE_TEXT_FLAG); //下划线
        tvUrl.getPaint().setAntiAlias(true);//抗锯齿
        tvUrl.setOnClickListener(v -> {
            Uri uri = Uri.parse(url.url);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            mContext.startActivity(intent);
        });
        return view;
    }

    private View createTLMView(AdvTLM tlm) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_tlm, null);
        TextView tv_vbatt = view.findViewById(R.id.tv_vbatt);
        TextView tv_temp = view.findViewById(R.id.tv_temp);
        TextView tv_adv_cnt = view.findViewById(R.id.tv_adv_cnt);
        TextView tv_sec_cnt = view.findViewById(R.id.tv_sec_cnt);
        tv_vbatt.setText(String.format("%smV", tlm.vbatt));
        tv_temp.setText(tlm.temp);
        tv_adv_cnt.setText(tlm.adv_cnt);
        tv_sec_cnt.setText(tlm.sec_cnt);
        return view;
    }

    private View createIBeaconView(AdvIBeacon iBeacon) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_ibeacon, null);
        TextView tv_tx_power = view.findViewById(R.id.tv_tx_power);
        TextView tv_rssi_1m = view.findViewById(R.id.tv_rssi_1m);
        TextView tv_uuid = view.findViewById(R.id.tv_uuid);
        TextView tv_major = view.findViewById(R.id.tv_major);
        TextView tv_minor = view.findViewById(R.id.tv_minor);
        TextView tv_proximity_state = view.findViewById(R.id.tv_proximity_state);

        tv_rssi_1m.setText(String.format("%sdBm", iBeacon.rssi));
        tv_tx_power.setText(String.format("%sdBm", iBeacon.txPower));
        tv_proximity_state.setText(iBeacon.distanceDesc);
        tv_uuid.setText(iBeacon.uuid.toUpperCase());
        tv_major.setText(iBeacon.major);
        tv_minor.setText(iBeacon.minor);
        return view;
    }

    private View createTagView(AdvTag tag) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.adv_slot_tag_info, null);
        TextView tvMagneticStatus = view.findViewById(R.id.tv_magnetic_status);
        TextView tvMagneticTriggerCount = view.findViewById(R.id.tv_magnetic_trigger_count);
        TextView tvMotionStatus = view.findViewById(R.id.tv_motion_status);
        TextView tvMotionTriggerCount = view.findViewById(R.id.tv_motion_trigger_count);
        TextView tvAcc = view.findViewById(R.id.tv_acc);
        tvMagneticStatus.setText(tag.hallStatus);
        tvMagneticTriggerCount.setText(tag.hallTriggerCount);
        tvMotionStatus.setText(tag.motionStatus);
        tvMotionTriggerCount.setText(tag.motionTriggerCount);
        tvAcc.setText(String.format("%s;%s;%s", tag.accX, tag.accY, tag.accZ));
        return view;
    }
}
