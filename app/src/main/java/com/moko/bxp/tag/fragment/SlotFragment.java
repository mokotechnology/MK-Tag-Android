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
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.activity.DeviceInfoActivity;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.entity.SlotData;
import com.moko.bxp.tag.entity.SlotEnum;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.UrlSchemeEnum;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class SlotFragment extends Fragment {

    private static final String TAG = "SlotFragment";
    @BindView(R.id.tv_slot1)
    TextView tvSlot1;
    @BindView(R.id.rl_slot1)
    RelativeLayout rlSlot1;
    @BindView(R.id.tv_slot2)
    TextView tvSlot2;
    @BindView(R.id.rl_slot2)
    RelativeLayout rlSlot2;
    @BindView(R.id.tv_slot3)
    TextView tvSlot3;
    @BindView(R.id.rl_slot3)
    RelativeLayout rlSlot3;
    @BindView(R.id.tv_slot4)
    TextView tvSlot4;
    @BindView(R.id.rl_slot4)
    RelativeLayout rlSlot4;
    @BindView(R.id.tv_slot5)
    TextView tvSlot5;
    @BindView(R.id.rl_slot5)
    RelativeLayout rlSlot5;
    @BindView(R.id.tv_slot6)
    TextView tvSlot6;
    @BindView(R.id.rl_slot6)
    RelativeLayout rlSlot6;

    private DeviceInfoActivity activity;
    private SlotData slotData;

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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        View view = inflater.inflate(R.layout.fragment_slot, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
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

    @OnClick({R.id.rl_slot1, R.id.rl_slot2, R.id.rl_slot3, R.id.rl_slot4, R.id.rl_slot5, R.id.rl_slot6})
    public void onViewClicked(View view) {
        slotData = new SlotData();
        SlotFrameTypeEnum frameType = (SlotFrameTypeEnum) view.getTag();
        slotData.frameTypeEnum = frameType;
        // No data直接跳转
        switch (view.getId()) {
            case R.id.rl_slot1:
                createData(frameType, SlotEnum.SLOT1);
                break;
            case R.id.rl_slot2:
                createData(frameType, SlotEnum.SLOT2);
                break;
            case R.id.rl_slot3:
                createData(frameType, SlotEnum.SLOT3);
                break;
            case R.id.rl_slot4:
                createData(frameType, SlotEnum.SLOT4);
                break;
            case R.id.rl_slot5:
                createData(frameType, SlotEnum.SLOT5);
                break;
            case R.id.rl_slot6:
                createData(frameType, SlotEnum.SLOT6);
                break;
        }
    }

    private void createData(SlotFrameTypeEnum frameType, SlotEnum slot) {
        slotData.slotEnum = slot;
        switch (frameType) {
            case NO_DATA:
                Intent intent = new Intent(getActivity(), SlotDataActivity.class);
                intent.putExtra(AppConstants.EXTRA_KEY_SLOT_DATA, slotData);
                intent.putExtra(AppConstants.EXTRA_KEY_SUPPORT_ACC, activity.isSupportAcc);
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
        changeView((int) rawDataBytes[0] & 0xff, tvSlot1, rlSlot1);
        changeView((int) rawDataBytes[1] & 0xff, tvSlot2, rlSlot2);
        changeView((int) rawDataBytes[2] & 0xff, tvSlot3, rlSlot3);
        changeView((int) rawDataBytes[3] & 0xff, tvSlot4, rlSlot4);
        changeView((int) rawDataBytes[4] & 0xff, tvSlot5, rlSlot5);
        changeView((int) rawDataBytes[5] & 0xff, tvSlot6, rlSlot6);
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
        startActivityForResult(intent, AppConstants.REQUEST_CODE_SLOT_DATA);
    }
}
