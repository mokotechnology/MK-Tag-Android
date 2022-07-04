package com.moko.bxp.tag.adapter;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.elvishew.xlog.XLog;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.entity.AdvInfo;

import java.util.Iterator;

public class DeviceListAdapter extends BaseQuickAdapter<AdvInfo, BaseViewHolder> {
    public DeviceListAdapter() {
        super(R.layout.list_item_device);
    }

    @Override
    protected void convert(BaseViewHolder helper, AdvInfo item) {
        helper.setText(R.id.tv_name, TextUtils.isEmpty(item.name) ? "N/A" : item.name);
        helper.setText(R.id.tv_mac, "MAC:" + item.mac);
        helper.setText(R.id.tv_rssi, String.format("%ddBm", item.rssi));
        helper.setText(R.id.tv_interval_time, item.intervalTime == 0 ? "<->N/A" : String.format("<->%dms", item.intervalTime));
        helper.setText(R.id.tv_battery, item.battery < 0 ? "N/A" : String.format("%dmV", item.battery));
        helper.addOnClickListener(R.id.tv_connect);
        helper.setGone(R.id.tv_connect, item.connectState > 0);
        helper.setText(R.id.tv_device_id, String.format("Device ID:%s", item.deviceId));
        helper.setVisible(R.id.tv_tx_power, item.txPower != Integer.MIN_VALUE);
        helper.setText(R.id.tv_tx_power, String.format("Tx power:%ddBm", item.txPower));
        LinearLayout parent = helper.getView(R.id.ll_adv_info);
        parent.removeAllViews();
        Iterator<Integer> iterator = item.triggerDataHashMap.keySet().iterator();
        while (iterator.hasNext()) {
            AdvInfo.TriggerData triggerData = item.triggerDataHashMap.get(iterator.next());
            XLog.i(triggerData.toString());
            String triggerTypeStr = "";
            switch (triggerData.triggerType) {
                case 0x20:
                    triggerTypeStr = "Single press alarm mode";
                    break;
                case 0x21:
                    triggerTypeStr = "Double press alarm mode";
                    break;
                case 0x22:
                    triggerTypeStr = "Long press alarm mode";
                    break;
                case 0x23:
                    triggerTypeStr = "Abnormal inactivity alarm mode";
                    break;
            }
            View view = LayoutInflater.from(mContext).inflate(R.layout.adv_item_trigger, null);
            TextView tvTriggerType = view.findViewById(R.id.tv_trigger_type);
            TextView tvTriggerStatus = view.findViewById(R.id.tv_trigger_status);
            TextView tvTriggerCount = view.findViewById(R.id.tv_trigger_count);
            RelativeLayout rlTriggerCount = view.findViewById(R.id.rl_trigger_count);
            tvTriggerType.setText(triggerTypeStr);
            tvTriggerStatus.setText(triggerData.triggerStatus == 0 ? "Standby" : "Triggered");
            tvTriggerCount.setText(String.valueOf(triggerData.triggerCount));
            rlTriggerCount.setVisibility(triggerData.triggerType == 0x23 ? View.GONE : View.VISIBLE);
            parent.addView(view);
        }
        if (item.deviceInfoFrame == 0) {
            View view = LayoutInflater.from(mContext).inflate(R.layout.adv_item_device, null);
            TextView tvRssi = view.findViewById(R.id.tv_rssi);
            RelativeLayout rlAcc = view.findViewById(R.id.rl_acc);
            TextView tvAcc = view.findViewById(R.id.tv_acc);
            tvRssi.setText(String.format("%ddBm", item.rangingData));
            rlAcc.setVisibility(item.accShown == 1 ? View.VISIBLE : View.GONE);
            tvAcc.setText(String.format("X:%smg Y:%smg Z:%smg", item.accX, item.accY, item.accZ));
            parent.addView(view);
        }
    }
}
