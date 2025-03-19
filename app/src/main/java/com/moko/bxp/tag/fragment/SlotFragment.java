package com.moko.bxp.tag.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.activity.DeviceInfoActivity;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.databinding.FragmentSlotTagBinding;
import com.moko.bxp.tag.entity.SlotData;
import com.moko.bxp.tag.entity.SlotEnum;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.UrlSchemeEnum;

import java.util.ArrayList;
import java.util.Arrays;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    private FragmentSlotTagBinding mBind;
    private DeviceInfoActivity activity;
    private SlotData slotData;
    private boolean hallPowerEnable;

    public SlotFragment() {
    }

    public static SlotFragment newInstance() {
        SlotFragment fragment = new SlotFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    public void setHallPowerEnable(boolean hallPowerEnable) {
        this.hallPowerEnable = hallPowerEnable;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentSlotTagBinding.inflate(inflater, container, false);
        activity = (DeviceInfoActivity) getActivity();
        return mBind.getRoot();
    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    public void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData = new SlotData();
        slotData.frameTypeEnum = frameType;
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
                intent.putExtra(AppConstants.EXTRA_KEY_SUPPORT_ACC, activity.isSupportAcc);
                intent.putExtra("hall", hallPowerEnable);
                startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
                break;
            case IBEACON:
            case TLM:
            case URL:
            case UID:
            case TAG:
                getSlotData(slot);
                break;
        }
    }

    private void getSlotData(SlotEnum slotEnum) {
        activity.showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getSlotAdvParams(slotEnum.ordinal()));
        orderTasks.add(OrderTaskAssembler.getSlotParams(slotEnum.ordinal()));
        orderTasks.add(OrderTaskAssembler.getSlotTriggerParams(slotEnum.ordinal()));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    // 10 20 50 40 FF FF
    public void updateSlotType(byte[] rawDataBytes) {
        changeView((int) rawDataBytes[0] & 0xff, mBind.tvSlot1, mBind.rlSlot1);
        changeView((int) rawDataBytes[1] & 0xff, mBind.tvSlot2, mBind.rlSlot2);
        changeView((int) rawDataBytes[2] & 0xff, mBind.tvSlot3, mBind.rlSlot3);
        changeView((int) rawDataBytes[3] & 0xff, mBind.tvSlot4, mBind.rlSlot4);
        changeView((int) rawDataBytes[4] & 0xff, mBind.tvSlot5, mBind.rlSlot5);
        changeView((int) rawDataBytes[5] & 0xff, mBind.tvSlot6, mBind.rlSlot6);
    }

    public void setSlotAdvParams(byte[] rawDataBytes) {
        slotData.advInterval = rawDataBytes[1] & 0xFF;
        slotData.advDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 2, 4));
        slotData.standbyDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 4, 6));
        switch (slotData.frameTypeEnum) {
            case IBEACON:
                slotData.rssi_1m = rawDataBytes[6];
                break;
            default:
                slotData.rssi_0m = rawDataBytes[6];
                break;
        }
        slotData.txPower = rawDataBytes[7];
    }

    private void changeView(int frameType, TextView tvSlot, RelativeLayout rlSlot) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum == null) {
            return;
        }
        tvSlot.setText(slotFrameTypeEnum.getShowName());
        rlSlot.setTag(slotFrameTypeEnum);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AppConstants.REQUEST_CODE_SLOT_DATA) {
                Log.i(TAG, "onActivityResult: ");
                activity.getAllSlot();
            }
        }
    }


    // 不同类型的数据长度不同
    public void setSlotParams(byte[] rawDataBytes) {
        int frameType = rawDataBytes[1] & 0xFF;
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromFrameType(frameType);
        if (slotFrameTypeEnum != null) {
            switch (slotFrameTypeEnum) {
                case URL:
                    int urlType = (int) rawDataBytes[2] & 0xff;
                    slotData.urlSchemeEnum = UrlSchemeEnum.fromUrlType(urlType);
                    slotData.urlContentHex = MokoUtils.bytesToHexString(rawDataBytes).substring(6);
                    break;
                case UID:
                    slotData.namespace = MokoUtils.bytesToHexString(rawDataBytes).substring(4, 24);
                    slotData.instanceId = MokoUtils.bytesToHexString(rawDataBytes).substring(24);
                    break;
                case TAG:
                    int deviceNameLength = rawDataBytes[2] & 0xFF;
                    byte[] deviceName = Arrays.copyOfRange(rawDataBytes, 3, 3 + deviceNameLength);
                    slotData.deviceName = new String(deviceName);
                    int tagIdLength = rawDataBytes[3 + deviceNameLength] & 0xFF;
                    byte[] tagId = Arrays.copyOfRange(rawDataBytes, 4 + deviceNameLength, rawDataBytes.length);
                    slotData.tagId = MokoUtils.bytesToHexString(tagId);
                    break;
                case IBEACON:
                    byte[] major = Arrays.copyOfRange(rawDataBytes, 2, 4);
                    byte[] minor = Arrays.copyOfRange(rawDataBytes, 4, 6);
                    byte[] uuid = Arrays.copyOfRange(rawDataBytes, 6, 22);
                    slotData.major = MokoUtils.bytesToHexString(major);
                    slotData.minor = MokoUtils.bytesToHexString(minor);
                    slotData.iBeaconUUID = MokoUtils.bytesToHexString(uuid);
                    break;
            }
        }
    }


    public void setSlotTriggerParams(byte[] rawDataBytes) {
        slotData.triggerType = rawDataBytes[1] & 0xff;
        if (slotData.triggerType == 5) {
            // 移动触发
            slotData.triggerAdvStatus = rawDataBytes[2] & 0xFF;
            slotData.triggerAdvDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 3, 5));
            slotData.staticDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 5, 7));
        }
        if (slotData.triggerType == 6) {
            // 霍尔触发
            slotData.triggerAdvStatus = rawDataBytes[2] & 0xFF;
            slotData.triggerAdvDuration = MokoUtils.toInt(Arrays.copyOfRange(rawDataBytes, 3, 5));
            slotData.staticDuration = 60;
        }
        Intent intent = new Intent(getActivity(), SlotDataActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
        intent.putExtra(AppConstants.EXTRA_KEY_SUPPORT_ACC, activity.isSupportAcc);
        intent.putExtra("hall", hallPowerEnable);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
    }
}
