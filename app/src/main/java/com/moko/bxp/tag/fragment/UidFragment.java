package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.ReplacementTransformationMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.able.ISlotDataAction;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.TxPowerEnum;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UidFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UidFragment";
    @BindView(R.id.et_namespace)
    EditText etNamespace;
    @BindView(R.id.et_instance_id)
    EditText etInstanceId;
    @BindView(R.id.sb_rssi)
    SeekBar sbRssi;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.tv_adv_tx_power)
    TextView tvRssi;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;
    @BindView(R.id.et_adv_interval)
    EditText etAdvInterval;
    @BindView(R.id.et_adv_duration)
    EditText etAdvDuration;
    @BindView(R.id.et_standby_duration)
    EditText etStandbyDuration;

    private SlotDataActivity activity;

    public UidFragment() {
    }

    public static UidFragment newInstance() {
        UidFragment fragment = new UidFragment();
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
        View view = inflater.inflate(R.layout.fragment_uid, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbRssi.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        etNamespace.setTransformationMethod(new A2bigA());
        etInstanceId.setTransformationMethod(new A2bigA());
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvDuration.setText("10");
            etStandbyDuration.setText("0");
            sbRssi.setProgress(100);
            sbTxPower.setProgress(6);
        } else {
            etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbRssi.setProgress(100);
                mRssi = 0;
                tvRssi.setText(String.format("%ddBm", mRssi));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                sbRssi.setProgress(advTxPowerProgress);
                mRssi = activity.slotData.rssi_0m;
                tvRssi.setText(String.format("%ddBm", mRssi));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            mTxPower = activity.slotData.txPower;
            tvTxPower.setText(String.format("%ddBm", mTxPower));
        }
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.UID) {
            etNamespace.setText(activity.slotData.namespace);
            etInstanceId.setText(activity.slotData.instanceId);
            etNamespace.setSelection(etNamespace.getText().toString().length());
            etInstanceId.setSelection(etInstanceId.getText().toString().length());
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
    private String mNamespaceIdHex;
    private String mInstanceIdHex;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    public void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_rssi) {
            int rssi = progress - 100;
            tvRssi.setText(String.format("%ddBm", rssi));
            mRssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            int txPower = txPowerEnum.getTxPower();
            tvTxPower.setText(String.format("%ddBm", txPower));
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
        String namespace = etNamespace.getText().toString();
        String instanceId = etInstanceId.getText().toString();
        String advInterval = etAdvInterval.getText().toString();
        String advDuration = etAdvDuration.getText().toString();
        String standbyDuration = etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(namespace) || TextUtils.isEmpty(instanceId)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        if (namespace.length() != 20 || instanceId.length() != 12) {
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
        mNamespaceIdHex = namespace;
        mInstanceIdHex = instanceId;
        return true;
    }

    @Override
    public void sendData() {
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlotAdvParams(activity.slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, mRssi, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsUID(activity.slotData.slotEnum.ordinal(),
                mNamespaceIdHex, mInstanceIdHex));
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
            etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            int rssiProgress = activity.slotData.rssi_0m + 100;
            sbRssi.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);

            etNamespace.setText(activity.slotData.namespace);
            etInstanceId.setText(activity.slotData.instanceId);
        } else {
            etAdvInterval.setText("10");
            etAdvDuration.setText("10");
            etStandbyDuration.setText("0");
            sbRssi.setProgress(100);
            sbTxPower.setProgress(6);

            etNamespace.setText("");
            etInstanceId.setText("");
        }
    }
}
