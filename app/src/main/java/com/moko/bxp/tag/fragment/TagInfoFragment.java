package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.able.ISlotDataAction;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.databinding.FragmentTagInfoTagBinding;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.TxPowerEnum;

import java.util.ArrayList;

public class TagInfoFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "TagInfoFragment";
    private final String FILTER_ASCII = "[ -~]*";
    private FragmentTagInfoTagBinding mBind;


    private SlotDataActivity activity;

    public TagInfoFragment() {
    }

    public static TagInfoFragment newInstance() {
        TagInfoFragment fragment = new TagInfoFragment();
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
        mBind = FragmentTagInfoTagBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        //限制只输入大写，自动小写转大写
        mBind.etTagId.setTransformationMethod(new A2bigA());
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        mBind.etDeviceName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(20), filter});
        setDefault();
        return mBind.getRoot();
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);
        } else {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                mBind.sbRssi.setProgress(100);
                mRssi = 0;
                mBind.tvAdvTxPower.setText(String.format("%ddBm", mRssi));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                mBind.sbRssi.setProgress(advTxPowerProgress);
                mRssi = activity.slotData.rssi_0m;
                mBind.tvAdvTxPower.setText(String.format("%ddBm", mRssi));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
            mTxPower = activity.slotData.txPower;
            mBind.tvTxPower.setText(String.format("%ddBm", mTxPower));
        }

        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TAG) {
            mBind.etDeviceName.setText(activity.slotData.deviceName);
            mBind.etTagId.setText(activity.slotData.tagId);
        }

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

    private int mAdvInterval;
    private int mAdvDuration;
    private int mStandbyDuration;
    private int mRssi;
    private int mTxPower;
    private String mDeviceName;
    private String mTagIdHex;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    public void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_rssi) {
            int rssi = progress - 100;
            mBind.tvAdvTxPower.setText(String.format("%ddBm", rssi));
            mRssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            int txPower = txPowerEnum.getTxPower();
            mBind.tvTxPower.setText(String.format("%ddBm", txPower));
            mTxPower = txPower;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean isValid() {
        String deviceName = mBind.etDeviceName.getText().toString();
        String tagId = mBind.etTagId.getText().toString();
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(deviceName)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (TextUtils.isEmpty(tagId) || tagId.length() % 2 != 0) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (TextUtils.isEmpty(advInterval)) {
            ToastUtils.showToast(activity, "The Adv interval can not be empty.");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv interval range is 1~100");
            return false;
        }
        if (TextUtils.isEmpty(advDuration)) {
            ToastUtils.showToast(activity, "The Adv duration can not be empty.");
            return false;
        }
        int advDurationInt = Integer.parseInt(advDuration);
        if (advDurationInt < 1 || advDurationInt > 65535) {
            ToastUtils.showToast(activity, "The Adv duration range is 1~65535");
            return false;
        }
        if (TextUtils.isEmpty(standbyDuration)) {
            ToastUtils.showToast(activity, "The Standby duration can not be empty.");
            return false;
        }
        int standbyDurationInt = Integer.parseInt(standbyDuration);
        if (standbyDurationInt > 65535) {
            ToastUtils.showToast(activity, "The Standby duration range is 0~65535");
            return false;
        }
        mAdvInterval = advIntervalInt;
        mAdvDuration = advDurationInt;
        mStandbyDuration = standbyDurationInt;
        mDeviceName = deviceName;
        mTagIdHex = tagId;
        return true;
    }

    @Override
    public void sendData() {
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlotAdvParams(activity.slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, mRssi, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsTagInfo(activity.slotData.slotEnum.ordinal(),
                mDeviceName, mTagIdHex));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public class A2bigA extends ReplacementTransformationMethod {

        @Override
        protected char[] getOriginal() {
            char[] aa = {'a', 'b', 'c', 'd', 'e', 'f'};
            return aa;
        }

        @Override
        protected char[] getReplacement() {
            char[] cc = {'A', 'B', 'C', 'D', 'E', 'F'};
            return cc;
        }
    }

    @Override
    public void resetParams() {
        if (activity.slotData.frameTypeEnum == activity.currentFrameTypeEnum) {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            int rssiProgress = activity.slotData.rssi_0m + 100;
            mBind.sbRssi.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);

            mBind.etDeviceName.setText(activity.slotData.deviceName);
            mBind.etTagId.setText(activity.slotData.tagId);
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);
            mBind.etDeviceName.setText("");
            mBind.etTagId.setText("");
        }
    }
}
