package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
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
import com.moko.bxp.tag.databinding.FragmentIbeaconBinding;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.TxPowerEnum;

import java.util.ArrayList;

public class IBeaconFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {
    private static final String TAG = "IBeaconFragment";

    private FragmentIbeaconBinding mBind;
    private SlotDataActivity activity;

    public IBeaconFragment() {
    }

    public static IBeaconFragment newInstance() {
        IBeaconFragment fragment = new IBeaconFragment();
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
        mBind = FragmentIbeaconBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        //限制只输入大写，自动小写转大写
        mBind.etUuid.setTransformationMethod(new A2bigA());
        setDefault();
        return mBind.getRoot();
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(41);
            mBind.sbTxPower.setProgress(5);
        } else {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
                int rssiProgress = activity.slotData.rssi_1m + 100;
                mBind.sbRssi.setProgress(rssiProgress);
                mRssi = activity.slotData.rssi_1m;
                mBind.tvAdvTxPower.setText(String.format("%ddBm", mRssi));
            } else if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                mBind.sbRssi.setProgress(41);
                mRssi = -59;
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
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.IBEACON) {
            mBind.etMajor.setText(String.valueOf(Integer.parseInt(activity.slotData.major, 16)));
            mBind.etMinor.setText(String.valueOf(Integer.parseInt(activity.slotData.minor, 16)));
            mBind.etUuid.setText(activity.slotData.iBeaconUUID.toUpperCase());
            mBind.etMajor.setSelection(mBind.etMajor.getText().toString().length());
            mBind.etMinor.setSelection(mBind.etMinor.getText().toString().length());
            mBind.etUuid.setSelection(mBind.etUuid.getText().toString().length());
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
    private int mMajor;
    private int mMinor;
    private String mUUIDHex;

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
        String majorStr = mBind.etMajor.getText().toString();
        String minorStr = mBind.etMinor.getText().toString();
        String uuidStr = mBind.etUuid.getText().toString();
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(majorStr) || TextUtils.isEmpty(minorStr) || TextUtils.isEmpty(uuidStr)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (Integer.valueOf(majorStr) > 65535 || Integer.valueOf(minorStr) > 65535 || uuidStr.length() != 32) {
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
        mMajor = Integer.valueOf(majorStr);
        mMinor = Integer.valueOf(minorStr);
        mUUIDHex = uuidStr;
        return true;
    }

    @Override
    public void sendData() {
        // 切换通道，保证通道是在当前设置通道里
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlotAdvParams(activity.slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, mRssi, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsIBeacon(activity.slotData.slotEnum.ordinal(),
                mMajor, mMinor, mUUIDHex));
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

            int rssiProgress = activity.slotData.rssi_1m + 100;
            mBind.sbRssi.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);

            mBind.etMajor.setText(Integer.parseInt(activity.slotData.major, 16) + "");
            mBind.etMinor.setText(Integer.parseInt(activity.slotData.minor, 16) + "");
            mBind.etUuid.setText(activity.slotData.iBeaconUUID.toUpperCase());
            mBind.etMajor.setSelection(mBind.etMajor.getText().toString().length());
            mBind.etMinor.setSelection(mBind.etMinor.getText().toString().length());
            mBind.etUuid.setSelection(mBind.etUuid.getText().toString().length());
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(41);
            mBind.sbTxPower.setProgress(5);

            mBind.etMajor.setText("");
            mBind.etMinor.setText("");
            mBind.etUuid.setText("");
        }
    }
}
