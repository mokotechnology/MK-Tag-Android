package com.moko.bxp.tag.activity;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
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

public class PowerSavingConfigActivity extends BaseActivity {


    @BindView(R.id.iv_power_saving_mode)
    ImageView ivPowerSavingMode;
    @BindView(R.id.et_static_trigger_time)
    EditText etStaticTriggerTime;
    @BindView(R.id.cl_static_trigger_time)
    ConstraintLayout clStaticTriggerTime;
    @BindView(R.id.tv_static_trigger_time_tips)
    TextView tvStaticTriggerTimeTips;
    public boolean isConfigError;
    public boolean isEnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_power_saving_config);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        etStaticTriggerTime.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String triggerTime = editable.toString();
                tvStaticTriggerTimeTips.setText(getString(R.string.static_trigger_time_tips, triggerTime));
            }
        });
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getPowerSavingEnable());
            orderTasks.add(OrderTaskAssembler.getPowerSavingStaticTriggerTime());
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
                    PowerSavingConfigActivity.this.finish();
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
                                    case KEY_POWER_SAVING_STATIC_TRIGGER_TIME:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        break;
                                    case KEY_POWER_SAVING_ENABLE:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(PowerSavingConfigActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_POWER_SAVING_ENABLE:
                                        if (length == 1) {
                                            isEnable = value[4] == 1;
                                            ivPowerSavingMode.setImageResource(isEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                            clStaticTriggerTime.setVisibility(isEnable ? View.VISIBLE : View.GONE);
                                        }
                                        break;
                                    case KEY_POWER_SAVING_STATIC_TRIGGER_TIME:
                                        if (length == 2) {
                                            int time = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                            etStaticTriggerTime.setText(String.valueOf(time));
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

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        if (isValid()) {
            showSyncingProgressDialog();
            String timeStr = etStaticTriggerTime.getText().toString();
            int time = Integer.parseInt(timeStr);
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setPowerSavingStaticTriggerTime(time));
            orderTasks.add(OrderTaskAssembler.setPowerSavingEnable(isEnable ? 1 : 0));
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        } else {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
        }
    }

    public void onBack(View view) {
        finish();
    }

    private boolean isValid() {
        String timeStr = etStaticTriggerTime.getText().toString();
        if (TextUtils.isEmpty(timeStr)) {
            return false;
        }
        int time = Integer.parseInt(timeStr);
        if (time < 1 || time > 65535)
            return false;
        return true;
    }

    public void onPowerSavingMode(View view) {
        if (isWindowLocked())
            return;
        isEnable = !isEnable;
        ivPowerSavingMode.setImageResource(isEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        clStaticTriggerTime.setVisibility(isEnable ? View.VISIBLE : View.GONE);
    }
}
