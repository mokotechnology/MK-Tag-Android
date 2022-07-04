package com.moko.bxp.tag.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.dialog.AlertMessageDialog;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class QuickSwitchActivity extends BaseActivity {

    @BindView(R.id.iv_connectable)
    ImageView ivConnectable;
    @BindView(R.id.tv_connectable_status)
    TextView tvConnectableStatus;
    @BindView(R.id.iv_button_power)
    ImageView ivButtonPower;
    @BindView(R.id.tv_button_power)
    TextView tvButtonPower;
    @BindView(R.id.iv_password_verify)
    ImageView ivPasswordVerify;
    @BindView(R.id.tv_password_verify)
    TextView tvPasswordVerify;
    @BindView(R.id.iv_hw_reset)
    ImageView ivHwReset;
    @BindView(R.id.tv_hw_reset)
    TextView tvHwReset;
    @BindView(R.id.iv_scan_response_packet)
    ImageView ivScanResponsePacket;
    @BindView(R.id.tv_scan_response_packet)
    TextView tvScanResponsePacket;
    @BindView(R.id.iv_dismiss_alarm_enable)
    ImageView ivDismissAlarmEnable;
    @BindView(R.id.tv_dismiss_alarm_enable)
    TextView tvDismissAlarmEnable;
    public boolean isConfigError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quick_switch);
        ButterKnife.bind(this);

        EventBus.getDefault().register(this);

        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getConnectable());
            orderTasks.add(OrderTaskAssembler.getButtonPowerEnable());
            orderTasks.add(OrderTaskAssembler.getVerifyPasswordEnable());
            orderTasks.add(OrderTaskAssembler.getButtonResetEnable());
            orderTasks.add(OrderTaskAssembler.getScanResponseEnable());
            orderTasks.add(OrderTaskAssembler.getDismissAlarmEnable());
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
                    finish();
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
                                    case KEY_BLE_CONNECTABLE:
                                    case KEY_BUTTON_POWER_ENABLE:
                                    case KEY_BUTTON_RESET_ENABLE:
                                    case KEY_SCAN_RESPONSE_ENABLE:
                                    case KEY_DISMISS_ALARM_ENABLE:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(QuickSwitchActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00 && length == 1) {
                                // read
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_BLE_CONNECTABLE:
                                        setConnectable(result);
                                        break;
                                    case KEY_BUTTON_POWER_ENABLE:
                                        setButtonPower(result);
                                        break;
                                    case KEY_VERIFY_PASSWORD_ENABLE:
                                        setPasswordVerify(result);
                                        break;
                                    case KEY_BUTTON_RESET_ENABLE:
                                        setButtonResetEnable(result);
                                        break;
                                    case KEY_SCAN_RESPONSE_ENABLE:
                                        setScanResponseEnable(result);
                                        break;
                                    case KEY_DISMISS_ALARM_ENABLE:
                                        setDismissAlarmEnable(result);
                                        break;

                                }
                            }
                        }
                        break;
                    case CHAR_PASSWORD:
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
                                    case KEY_VERIFY_PASSWORD_ENABLE:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(QuickSwitchActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00 && length == 1) {
                                // read
                                int result = value[4] & 0xFF;
                                switch (configKeyEnum) {
                                    case KEY_VERIFY_PASSWORD_ENABLE:
                                        setPasswordVerify(result);
                                        break;

                                }
                            }
                        }
                        break;
                }
            }
        });
    }

    private boolean enablePasswordVerify;

    public void setPasswordVerify(int enable) {
        this.enablePasswordVerify = enable == 1;
        ivPasswordVerify.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvPasswordVerify.setText(enablePasswordVerify ? "Enable" : "Disable");
        tvPasswordVerify.setEnabled(enablePasswordVerify);
    }

    boolean enableConnected;

    public void setConnectable(int enable) {
        enableConnected = enable == 1;
        ivConnectable.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvConnectableStatus.setText(enableConnected ? "Enable" : "Disable");
        tvConnectableStatus.setEnabled(enableConnected);
    }

    private boolean enableButtonPower;

    public void setButtonPower(int enable) {
        this.enableButtonPower = enable == 1;
        ivButtonPower.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvButtonPower.setText(enableButtonPower ? "Enable" : "Disable");
        tvButtonPower.setEnabled(enableButtonPower);
    }

    private boolean enableHWReset;

    public void setButtonResetEnable(int enable) {
        this.enableHWReset = enable == 1;
        ivHwReset.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvHwReset.setText(enableHWReset ? "Enable" : "Disable");
        tvHwReset.setEnabled(enableHWReset);
    }

    private boolean enableScanResponse;

    public void setScanResponseEnable(int enable) {
        this.enableScanResponse = enable == 1;
        ivScanResponsePacket.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvScanResponsePacket.setText(enableScanResponse ? "Enable" : "Disable");
        tvScanResponsePacket.setEnabled(enableScanResponse);
    }

    private boolean enableDismissAlarm;

    public void setDismissAlarmEnable(int enable) {
        this.enableDismissAlarm = enable == 1;
        ivDismissAlarmEnable.setImageResource(enable == 1 ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvDismissAlarmEnable.setText(enableDismissAlarm ? "Enable" : "Disable");
        tvDismissAlarmEnable.setEnabled(enableDismissAlarm);
    }

    public void onChangeConnectable(View view) {
        if (isWindowLocked())
            return;
        if (enableConnected) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("Are you sure to set the Beacon non-connectable？");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setConnectable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setConnectable(true);
        }
    }

    public void onChangeScanResponsePacket(View view) {
        if (isWindowLocked())
            return;
        setScanResponsePacket(!enableScanResponse);
    }

    public void onChangeButtonPower(View view) {
        if (isWindowLocked())
            return;
        if (enableButtonPower) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If this function is disabled, you cannot power off the Beacon by tag.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setButtonPower(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setButtonPower(true);
        }
    }

    public void onChangeHWReset(View view) {
        if (isWindowLocked())
            return;
        if (enableHWReset) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If Button reset is disabled, you cannot reset the Beacon by tag operation.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setButtonResetEnable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setButtonResetEnable(true);
        }
    }

    public void onChangePasswordVerify(View view) {
        if (isWindowLocked())
            return;
        if (enablePasswordVerify) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If Password verification is disabled, it will not need password to connect the Beacon.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setVerifyPasswordEnable(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setVerifyPasswordEnable(true);
        }
    }


    public void onChangeDismissAlarm(View view) {
        if (isWindowLocked())
            return;
        if (enableDismissAlarm) {
            final AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setTitle("Warning！");
            dialog.setMessage("If this function is disabled, you cannot dismiss alarm by tag.");
            dialog.setConfirm(R.string.ok);
            dialog.setOnAlertConfirmListener(() -> {
                setDismissAlarm(false);
            });
            dialog.show(getSupportFragmentManager());
        } else {
            setDismissAlarm(true);
        }
    }


    public void setConnectable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setConnectable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getConnectable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    public void setButtonPower(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setButtonPowerEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getButtonPowerEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setVerifyPasswordEnable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setVerifyPasswordEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getVerifyPasswordEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setButtonResetEnable(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setButtonResetEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getButtonResetEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setScanResponsePacket(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setScanResponseEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getScanResponseEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setDismissAlarm(boolean enable) {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setDismissAlarmEnable(enable ? 1 : 0));
        orderTasks.add(OrderTaskAssembler.getDismissAlarmEnable());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            dismissSyncProgressDialog();
                            finish();
                            break;
                    }
                }
            }
        }
    };


    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 注销广播
        unregisterReceiver(mReceiver);
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
        back();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void back() {
        Intent intent = new Intent();
        intent.putExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, enablePasswordVerify);
        setResult(RESULT_OK, intent);
        finish();
    }

}
