package com.moko.bxp.tag.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

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

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

import androidx.constraintlayout.widget.ConstraintLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class AlarmNotifyTypeActivity extends BaseActivity {


    @BindView(R.id.npv_notify_type)
    NumberPickerView npvNotifyType;
    @BindView(R.id.et_blinking_time)
    EditText etBlinkingTime;
    @BindView(R.id.et_blinking_interval)
    EditText etBlinkingInterval;
    @BindView(R.id.cl_led_notify)
    ConstraintLayout clLedNotify;
    @BindView(R.id.et_vibrating_time)
    EditText etVibratingTime;
    @BindView(R.id.et_vibrating_interval)
    EditText etVibratingInterval;
    @BindView(R.id.cl_vibration_notify)
    ConstraintLayout clVibrationNotify;
    @BindView(R.id.et_ringing_time)
    EditText etRingingTime;
    @BindView(R.id.et_ringing_interval)
    EditText etRingingInterval;
    @BindView(R.id.cl_buzzer_notify)
    ConstraintLayout clBuzzerNotify;
    public boolean isConfigError;
    public int slotType;
    private String[] alarmNotifyTypeArray;
    public int notifyType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_notify_type);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotType = getIntent().getIntExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 0);
        }

        alarmNotifyTypeArray = getResources().getStringArray(R.array.alarm_notify_type);
        npvNotifyType.setDisplayedValues(alarmNotifyTypeArray);
        npvNotifyType.setMinValue(0);
        npvNotifyType.setMaxValue(alarmNotifyTypeArray.length - 1);
        npvNotifyType.setValue(notifyType);
        npvNotifyType.setOnValueChangedListener((picker, oldVal, newVal) -> {
            notifyType = newVal;
            if (notifyType == 1 || notifyType == 4 || notifyType == 5) {
                // LED/LED+Vibration/LED+Buzzer
                clLedNotify.setVisibility(View.VISIBLE);
            } else {
                clLedNotify.setVisibility(View.GONE);
            }
            if (notifyType == 2 || notifyType == 4) {
                // Vibration/LED+Vibration
                clVibrationNotify.setVisibility(View.VISIBLE);
            } else {
                clVibrationNotify.setVisibility(View.GONE);
            }
            if (notifyType == 3 || notifyType == 5) {
                // Buzzer/LED+Buzzer
                clBuzzerNotify.setVisibility(View.VISIBLE);
            } else {
                clBuzzerNotify.setVisibility(View.GONE);
            }
        });

        EventBus.getDefault().register(this);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getSlotTriggerAlarmNotifyType(slotType));
            orderTasks.add(OrderTaskAssembler.getSlotLEDNotifyAlarmParams(slotType));
            orderTasks.add(OrderTaskAssembler.getSlotBuzzerNotifyAlarmParams(slotType));
            orderTasks.add(OrderTaskAssembler.getSlotVibrationNotifyAlarmParams(slotType));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                    // 设备断开，通知页面更新
                    AlarmNotifyTypeActivity.this.finish();
                }
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
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
                                    case KEY_SLOT_LED_NOTIFY_ALARM_PARAMS:
                                    case KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS:
                                    case KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        break;
                                    case KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(AlarmNotifyTypeActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_SLOT_TRIGGER_ALARM_NOTIFY_TYPE:
                                        if (length == 2 && value[4] == slotType) {
                                            notifyType = value[5] & 0xFF;
                                            npvNotifyType.setValue(notifyType);
                                            if (notifyType == 1 || notifyType == 4 || notifyType == 5) {
                                                // LED/LED+Vibration/LED+Buzzer
                                                clLedNotify.setVisibility(View.VISIBLE);
                                            } else {
                                                clLedNotify.setVisibility(View.GONE);
                                            }
                                            if (notifyType == 2 || notifyType == 4) {
                                                // Vibration/LED+Vibration
                                                clVibrationNotify.setVisibility(View.VISIBLE);
                                            } else {
                                                clVibrationNotify.setVisibility(View.GONE);
                                            }
                                            if (notifyType == 3 || notifyType == 5) {
                                                // Buzzer/LED+Buzzer
                                                clBuzzerNotify.setVisibility(View.VISIBLE);
                                            } else {
                                                clBuzzerNotify.setVisibility(View.GONE);
                                            }
                                        }
                                        break;
                                    case KEY_SLOT_LED_NOTIFY_ALARM_PARAMS:
                                        if (length == 5 && value[4] == slotType) {
                                            int time = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                            etBlinkingTime.setText(String.valueOf(time));
                                            etBlinkingInterval.setText(String.valueOf(interval / 100));
                                        }
                                        break;
                                    case KEY_SLOT_VIBRATION_NOTIFY_ALARM_PARAMS:
                                        if (length == 5 && value[4] == slotType) {
                                            int time = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                            etVibratingTime.setText(String.valueOf(time));
                                            etVibratingInterval.setText(String.valueOf(interval / 100));
                                        }
                                        break;
                                    case KEY_SLOT_BUZZER_NOTIFY_ALARM_PARAMS:
                                        if (length == 5 && value[4] == slotType) {
                                            int time = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                            etRingingTime.setText(String.valueOf(time));
                                            etRingingInterval.setText(String.valueOf(interval / 100));
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


    public void onBack(View view) {
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
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        if (notifyType == 1 || notifyType == 4 || notifyType == 5) {
            String ledTimeStr = etBlinkingTime.getText().toString();
            String ledIntervalStr = etBlinkingInterval.getText().toString();
            int ledTime = Integer.parseInt(ledTimeStr);
            int ledInterval = Integer.parseInt(ledIntervalStr) * 100;
            // LED/LED+Vibration/LED+Buzzer
            orderTasks.add(OrderTaskAssembler.setSlotLEDNotifyAlarmParams(slotType, ledTime, ledInterval));
        }
        if (notifyType == 2 || notifyType == 4) {
            String vibrationTimeStr = etVibratingTime.getText().toString();
            String vibrationIntervalStr = etVibratingInterval.getText().toString();
            int vibrationTime = Integer.parseInt(vibrationTimeStr);
            int vibrationInterval = Integer.parseInt(vibrationIntervalStr) * 100;
            // Vibration/LED+Vibration
            orderTasks.add(OrderTaskAssembler.setSlotVibrationNotifyAlarmParams(slotType, vibrationTime, vibrationInterval));
        }
        if (notifyType == 3 || notifyType == 5) {
            String buzzerTimeStr = etRingingTime.getText().toString();
            String buzzerIntervalStr = etRingingInterval.getText().toString();
            int buzzerTime = Integer.parseInt(buzzerTimeStr);
            int buzzerInterval = Integer.parseInt(buzzerIntervalStr) * 100;
            // Buzzer/LED+Buzzer
            orderTasks.add(OrderTaskAssembler.setSlotBuzzerNotifyAlarmParams(slotType, buzzerTime, buzzerInterval));
        }
        orderTasks.add(OrderTaskAssembler.setSlotTriggerAlarmNotifyType(slotType, notifyType));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private boolean isValid() {
        if (notifyType == 0)
            return true;
        String ledTimeStr = etBlinkingTime.getText().toString();
        String ledIntervalStr = etBlinkingInterval.getText().toString();
        String vibrationTimeStr = etVibratingTime.getText().toString();
        String vibrationIntervalStr = etVibratingInterval.getText().toString();
        String buzzerTimeStr = etRingingTime.getText().toString();
        String buzzerIntervalStr = etRingingInterval.getText().toString();
        if (notifyType == 1 || notifyType == 4 || notifyType == 5) {
            if (TextUtils.isEmpty(ledTimeStr) || TextUtils.isEmpty(ledIntervalStr)) {
                return false;
            }
            int ledTime = Integer.parseInt(ledTimeStr);
            if (ledTime < 1 || ledTime > 6000)
                return false;
            int ledInterval = Integer.parseInt(ledIntervalStr);
            if (ledInterval < 1 || ledInterval > 100)
                return false;
        }
        if (notifyType == 2 || notifyType == 4) {
            if (TextUtils.isEmpty(vibrationTimeStr) || TextUtils.isEmpty(vibrationIntervalStr)) {
                return false;
            }
            int vibrationTime = Integer.parseInt(vibrationTimeStr);
            if (vibrationTime < 1 || vibrationTime > 6000)
                return false;
            int vibrationInterval = Integer.parseInt(vibrationIntervalStr);
            if (vibrationInterval < 1 || vibrationInterval > 100)
                return false;
        }
        if (notifyType == 3 || notifyType == 5) {
            if (TextUtils.isEmpty(buzzerTimeStr) || TextUtils.isEmpty(buzzerIntervalStr)) {
                return false;
            }
            int buzzerTime = Integer.parseInt(buzzerTimeStr);
            if (buzzerTime < 1 || buzzerTime > 6000)
                return false;
            int buzzerInterval = Integer.parseInt(buzzerIntervalStr);
            if (buzzerInterval < 1 || buzzerInterval > 100)
                return false;
        }
        return true;
    }
}
