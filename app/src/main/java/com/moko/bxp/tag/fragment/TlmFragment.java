package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.able.ISlotDataAction;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.databinding.FragmentTlmTagBinding;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.TxPowerEnum;

import java.util.ArrayList;

public class TlmFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "TlmFragment";
    private FragmentTlmTagBinding mBind;
    private SlotDataActivity activity;

    public TlmFragment() {
    }

    public static TlmFragment newInstance() {
        TlmFragment fragment = new TlmFragment();
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
        mBind = FragmentTlmTagBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        setDefault();
        return mBind.getRoot();
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbTxPower.setProgress(5);
        } else {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
            mTxPower = activity.slotData.txPower;
            mBind.tvTxPower.setText(String.format("%ddBm", mTxPower));
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
    private int mTxPower;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        upgdateData(seekBar.getId(), progress);
    }

    private void upgdateData(int viewId, int progress) {
        if (viewId == R.id.sb_tx_power) {
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
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
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
        return true;
    }

    @Override
    public void sendData() {
        byte[] tlmBytes = MokoUtils.hex2bytes(SlotFrameTypeEnum.TLM.getFrameType());
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlotAdvParams(activity.slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, activity.slotData.rssi_0m, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsTLM(activity.slotData.slotEnum.ordinal()));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    public void resetParams() {
        if (activity.slotData.frameTypeEnum == activity.currentFrameTypeEnum) {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbTxPower.setProgress(5);
        }
    }
}
