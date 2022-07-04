package com.moko.bxp.tag.activity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.entity.TxPowerEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmModeConfigActivity extends BaseActivity implements SeekBar.OnSeekBarChangeListener {


    @BindView(R.id.tv_alarm_title)
    TextView tvAlarmTitle;
    @BindView(R.id.iv_slot_adv_switch)
    ImageView ivSlotAdvSwitch;
    @BindView(R.id.et_adv_interval)
    EditText etAdvInterval;
    @BindView(R.id.sb_adv_range_data)
    SeekBar sbAdvRangeData;
    @BindView(R.id.tv_adv_range_data)
    TextView tvAdvRangeData;
    @BindView(R.id.tx_power)
    TextView txPower;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;
    @BindView(R.id.iv_alarm_mode)
    ImageView ivAlarmMode;
    @BindView(R.id.iv_adv_before_triggered)
    ImageView ivAdvBeforeTriggered;
    @BindView(R.id.et_abnormal_inactivity_time)
    EditText etAbnormalInactivityTime;
    @BindView(R.id.rl_abnormal_inactivity_time)
    RelativeLayout rlAbnormalInactivityTime;
    @BindView(R.id.et_trigger_adv_time)
    EditText etTriggerAdvTime;
    @BindView(R.id.et_trigger_adv_interval)
    EditText etTriggerAdvInterval;
    @BindView(R.id.sb_trigger_tx_power)
    SeekBar sbTriggerTxPower;
    @BindView(R.id.tv_trigger_tx_power)
    TextView tvTriggerTxPower;
    @BindView(R.id.ll_slot_trigger_params)
    LinearLayout llSlotTriggerParams;
    @BindView(R.id.ll_slot_alarm_params)
    LinearLayout llSlotAlarmParams;
    @BindView(R.id.tv_abnormal_inactivity_time_tips)
    TextView tvAbnormalInactivityTimeTips;

    public boolean isConfigError;
    public int slotType;
    private boolean isAdvOpen;
    private boolean isTriggerOpen;
    private boolean isAdvBeforeTriggerOpen;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_mode_config);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 0);
        }
        switch (slotType) {
            case 0:
                tvAlarmTitle.setText("Single press alarm mode");
                break;
            case 1:
                tvAlarmTitle.setText("Double press alarm mode");
                break;
            case 2:
                tvAlarmTitle.setText("Long press alarm mode");
                break;
            case 3:
                tvAlarmTitle.setText("Abnormal inactivity alarm mode");
                break;
        }
        if (slotType == 3) {
            rlAbnormalInactivityTime.setVisibility(View.VISIBLE);
        }
        sbAdvRangeData.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        sbTriggerTxPower.setOnSeekBarChangeListener(this);
        etAbnormalInactivityTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String advTime = etTriggerAdvTime.getText().toString();
                String inactivityTime = editable.toString();
                tvAbnormalInactivityTimeTips.setText(getString(R.string.abnormal_inactivity_time_tips, inactivityTime, advTime));
            }
        });
        etTriggerAdvTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String advTime = editable.toString();
                String inactivityTime = etAbnormalInactivityTime.getText().toString();
                tvAbnormalInactivityTimeTips.setText(getString(R.string.abnormal_inactivity_time_tips, inactivityTime, advTime));
            }
        });
        EventBus.getDefault().register(this);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getSlotParams(slotType));
            orderTasks.add(OrderTaskAssembler.getSlotTriggerParams(slotType));
            orderTasks.add(OrderTaskAssembler.getSlotAdvBeforeTriggerEnable(slotType));
            if (slotType == 3) {
                orderTasks.add(OrderTaskAssembler.getAbnormalInactivityAlarmStaticInterval());
            }
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    AlarmModeConfigActivity.this.finish();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_PARAMS:
                        if (value.length > 4) {
                            int header = value[0] & 0xFF;// 0xEB
                            int flag = value[1] & 0xFF;// read or write
                            int cmd = value[2] & 0xFF;
                            if (header != 0xEB)
                                return;
                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                            if (configKeyEnum == null) {
                                return;
                            }
                            int length = value[3] & 0xFF;
                            if (flag == 0x01 && length == 0x01) {
                                // write
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_SLOT_PARAMS:
                                    case KEY_ABNORMAL_INACTIVITY_ALARM_STATIC_INTERVAL:
                                    case KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        break;
                                    case KEY_SLOT_TRIGGER_PARAMS:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(AlarmModeConfigActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_SLOT_PARAMS:
                                        if (length == 6 && value[4] == slotType) {
                                            int slotEnable = value[5] & 0xFF;
                                            int rangingData = value[6];
                                            int advInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                            int txPower = value[9];
                                            isAdvOpen = slotEnable == 1;
                                            ivSlotAdvSwitch.setImageResource(isAdvOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                            int progress = rangingData + 100;
                                            sbAdvRangeData.setProgress(progress);
                                            etAdvInterval.setText(String.valueOf(advInterval / 20));
                                            TxPowerEnum txPowerEnum = TxPowerEnum.fromTxPower(txPower);
                                            sbTxPower.setProgress(txPowerEnum.ordinal());
                                            llSlotAlarmParams.setVisibility(isAdvOpen ? View.VISIBLE : View.GONE);
                                        }
                                        break;
                                    case KEY_SLOT_TRIGGER_PARAMS:
                                        if (length == 8 && value[4] == slotType) {
                                            int triggerEnable = value[5] & 0xFF;
                                            int triggerAdvInterval = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                            int txPower = value[9];
                                            int triggerAdvTime = MokoUtils.toInt(Arrays.copyOfRange(value, 10, 12));
                                            isTriggerOpen = triggerEnable == 1;
                                            ivAlarmMode.setImageResource(isTriggerOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                            etTriggerAdvInterval.setText(String.valueOf(triggerAdvInterval / 20));
                                            TxPowerEnum txPowerEnum = TxPowerEnum.fromTxPower(txPower);
                                            sbTriggerTxPower.setProgress(txPowerEnum.ordinal());
                                            etTriggerAdvTime.setText(String.valueOf(triggerAdvTime));
                                            llSlotTriggerParams.setVisibility(isTriggerOpen ? View.VISIBLE : View.GONE);
                                        }
                                        break;
                                    case KEY_SLOT_ADV_BEFORE_TRIGGER_ENABLE:
                                        if (length == 2 && value[4] == slotType) {
                                            int advBeforeTriggerEnable = value[5] & 0xFF;
                                            isAdvBeforeTriggerOpen = advBeforeTriggerEnable == 1;
                                            ivAdvBeforeTriggered.setImageResource(isAdvBeforeTriggerOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                        }
                                        break;
                                    case KEY_ABNORMAL_INACTIVITY_ALARM_STATIC_INTERVAL:
                                        if (length == 0x02) {
                                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                            etAbnormalInactivityTime.setText(String.valueOf(interval));
                                        }
                                        break;
                                }
                            }
                        }
                        break;
                }
            }
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    public void showSyncingProgressDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Syncing..");
        mLoadingMessageDialog.show(getSupportFragmentManager());
    }

    public void dismissSyncProgressDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, slotType);
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;

        if (isValid()) {
            showSyncingProgressDialog();
            saveParams();
        } else {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
        }
    }

    private void saveParams() {
        String advIntervalStr = etAdvInterval.getText().toString();
        String triggerAdvTimeStr = etTriggerAdvTime.getText().toString();
        String triggerAdvIntervalStr = etTriggerAdvInterval.getText().toString();
        String abnormalInactivityTimeStr = etAbnormalInactivityTime.getText().toString();
        int advInterval = Integer.parseInt(advIntervalStr);
        int triggerAdvTime = Integer.parseInt(triggerAdvTimeStr);
        int triggerAdvInterval = Integer.parseInt(triggerAdvIntervalStr);

        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (slotType == 3) {
            int abnormalInactivityTime = Integer.parseInt(abnormalInactivityTimeStr);
            orderTasks.add(OrderTaskAssembler.setAbnormalInactivityAlarmStaticInterval(abnormalInactivityTime));
        }
        orderTasks.add(OrderTaskAssembler.setSlotParams(
                slotType,
                isAdvOpen ? 1 : 0,
                sbAdvRangeData.getProgress() - 100,
                advInterval * 20,
                TxPowerEnum.fromOrdinal(sbTxPower.getProgress()).getTxPower()));
        orderTasks.add(OrderTaskAssembler.setSlotAdvBeforeTriggerEnable(slotType, isAdvBeforeTriggerOpen ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.setSlotTriggerParams(
                slotType,
                isTriggerOpen ? 1 : 0,
                sbAdvRangeData.getProgress() - 100,
                triggerAdvInterval * 20,
                TxPowerEnum.fromOrdinal(sbTriggerTxPower.getProgress()).getTxPower(),
                triggerAdvTime));

        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        String advIntervalStr = etAdvInterval.getText().toString();
        String triggerAdvTimeStr = etTriggerAdvTime.getText().toString();
        String triggerAdvIntervalStr = etTriggerAdvInterval.getText().toString();
        String abnormalInactivityTimeStr = etAbnormalInactivityTime.getText().toString();
        if (TextUtils.isEmpty(advIntervalStr)
                || TextUtils.isEmpty(triggerAdvTimeStr)
                || TextUtils.isEmpty(triggerAdvIntervalStr)
                || (slotType == 3 && TextUtils.isEmpty(abnormalInactivityTimeStr))) {
            return false;
        }
        int advInterval = Integer.parseInt(advIntervalStr);
        if (advInterval < 1 || advInterval > 500)
            return false;
        int triggerAdvTime = Integer.parseInt(triggerAdvTimeStr);
        if (triggerAdvTime < 1 || triggerAdvTime > 65535)
            return false;
        int triggerAdvInterval = Integer.parseInt(triggerAdvIntervalStr);
        if (triggerAdvInterval < 1 || triggerAdvInterval > 500)
            return false;
        if (slotType == 3) {
            int abnormalInactivityTime = Integer.parseInt(abnormalInactivityTimeStr);
            if (abnormalInactivityTime < 1 || abnormalInactivityTime > 65535)
                return false;
        }
        return true;
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        switch (seekBar.getId()) {
            case R.id.sb_adv_range_data:
                tvAdvRangeData.setText(String.format("%ddBm", progress - 100));
                break;
            case R.id.sb_tx_power:
                TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
                tvTxPower.setText(String.format("%ddBm", txPowerEnum.getTxPower()));
                break;
            case R.id.sb_trigger_tx_power:
                TxPowerEnum triggerTxPowerEnum = TxPowerEnum.fromOrdinal(progress);
                tvTriggerTxPower.setText(String.format("%ddBm", triggerTxPowerEnum.getTxPower()));
                break;
        }

    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    public void onSlotAdvSwitch(View view) {
        if (isWindowLocked())
            return;
        isAdvOpen = !isAdvOpen;
        ivSlotAdvSwitch.setImageResource(isAdvOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        llSlotAlarmParams.setVisibility(isAdvOpen ? View.VISIBLE : View.GONE);
    }

    public void onSlotAlarmModeSwitch(View view) {
        if (isWindowLocked())
            return;
        isTriggerOpen = !isTriggerOpen;
        ivAlarmMode.setImageResource(isTriggerOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        llSlotTriggerParams.setVisibility(isTriggerOpen ? View.VISIBLE : View.GONE);
    }

    public void onSlotAdvBeforeTriggeredSwitch(View view) {
        if (isWindowLocked())
            return;
        isAdvBeforeTriggerOpen = !isAdvBeforeTriggerOpen;
        ivAdvBeforeTriggered.setImageResource(isAdvBeforeTriggerOpen ? R.drawable.ic_checked : R.drawable.ic_unchecked);
    }

    public void onTriggerNotifyType(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AlarmNotifyTypeActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, slotType);
        startActivity(intent);
    }
}
