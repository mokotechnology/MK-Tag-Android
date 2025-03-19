package com.moko.bxp.tag.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.databinding.ActivityHallConfigBinding;
import com.moko.bxp.tag.dialog.AlertMessageDialog;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.OrderCHAR;
import com.moko.support.tag.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * 霍尔传感器
 */
public class HallSensorConfigActivity extends BaseActivity<ActivityHallConfigBinding> {
    private boolean mReceiverTag = false;

    private boolean mIsHallPowerEnable;

    @Override
    protected void onCreate() {
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.getMagnetStatus());
            orderTasks.add(OrderTaskAssembler.getMagneticTriggerCount());
//            orderTasks.add(OrderTaskAssembler.getHallPowerEnable());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
//        MokoSupport.getInstance().enableHallStatusNotify();
    }

    @Override
    protected ActivityHallConfigBinding getViewBinding() {
        return ActivityHallConfigBinding.inflate(getLayoutInflater());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 300)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
//            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
//                OrderTaskResponse response = event.getResponse();
//                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
//                int responseType = response.responseType;
//                byte[] value = response.responseValue;
//                switch (orderCHAR) {
//                    case CHAR_HALL: {
//                        if (value.length == 5) {
//                            int status = value[4] & 0xFF;
//                            tvMagnetStatus.setText(status == 0 ? "Present" : "Absent");
//                        }
//                    }
//                    break;
//                }
//            }
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
                    case CHAR_HALL: {
                        if (value.length == 5) {
                            int status = value[4] & 0xFF;
                            mBind.tvMagnetStatus.setText(status == 0 ? "Present" : "Absent");
                        }
                    }
                    break;
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
                                    case KEY_HALL_POWER_ENABLE:
                                        if (result == 0xAA) {
                                            ToastUtils.showToast(this, "Success");
                                            Intent intent = new Intent();
                                            intent.putExtra("status", 1);
                                            setResult(RESULT_OK, intent);
                                            back();
                                        } else {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        }
                                        break;

                                    case KEY_MAGNETIC_TRIGGER_COUNT:
                                        if (result == 0xAA) {
                                            ToastUtils.showToast(this, "Success");
                                        } else {
                                            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_MAGNETIC_TRIGGER_COUNT:
                                        if (length != 2)
                                            return;
                                        int count = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        mBind.tvTriggerCount.setText(String.valueOf(count));
                                        MokoSupport.getInstance().enableHallStatusNotify();
                                        break;
//                                    case KEY_HALL_POWER_ENABLE:
//                                        if (length == 1) {
//                                            mIsHallPowerEnable = (value[4] & 0xFF) == 1;
//                                            ivHallSensorEnable.setImageResource(mIsHallPowerEnable ? R.drawable.ic_checked : R.drawable.ic_unchecked);
//                                            MokoSupport.getInstance().enableHallStatusNotify();
//                                        }
//                                        break;

                                }
                            }
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                byte[] value = response.responseValue;
                if (null != value && value.length == 5) {
                    int header = value[0] & 0xFF;// 0xEB
                    int flag = value[1] & 0xFF;// read or write
                    int cmd = value[2] & 0xFF;
                    if (header != 0xEB) return;
                    int length = value[3] & 0xFF;
                    if (length == 1 && flag == 2 && cmd == 5) {
                        int status = value[4] & 0xFF;
                        mBind.tvMagnetStatus.setText(status == 0 ? "Present" : "Absent");
                    }
                }
//                switch (orderCHAR) {
//                    case CHAR_HALL:
//                        int header = value[0] & 0xFF;// 0xEB
//                        int flag = value[1] & 0xFF;// read or write
//                        int cmd = value[2] & 0xFF;
//                        if (header != 0xEB) return;
////                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
////                        if (configKeyEnum == null) {
////                            return;
////                        }
//                        int length = value[3] & 0xFF;
//                        if (length == 1&& flag == 2&& cmd == 5) {
//                            int status = value[4] & 0xFF;
//                            tvMagnetStatus.setText(status == 0 ? "Present" : "Absent");
//                        }
//                        break;
//                }
            }
        });
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
        if (mReceiverTag) {
            mReceiverTag = false;
            // 注销广播
            unregisterReceiver(mReceiver);
        }
        MokoSupport.getInstance().disableHallStatusNotify();
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

    private void back() {
        // 关闭通知
        MokoSupport.getInstance().disableHallStatusNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    public void onClear(View view) {
        if (isWindowLocked())
            return;
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.clearMagneticTriggerCount());
        orderTasks.add(OrderTaskAssembler.getMagneticTriggerCount());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onHallSensorEnable(View view) {
        if (isWindowLocked()) return;
        //能到这里霍尔关机功能是禁用的
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setTitle("Warning！");
        dialog.setMessage("*If you enable it, you will not be able to use the Hall trigger and count functions");
        dialog.setConfirm(R.string.ok);
        dialog.setCancel(R.string.cancel);
        dialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setHallPowerEnable(1));
        });
        dialog.show(getSupportFragmentManager());


//        mIsHallPowerEnable = !mIsHallPowerEnable;
//        // 保存
//        showSyncingProgressDialog();
//        ArrayList<OrderTask> orderTasks = new ArrayList<>();
//        orderTasks.add(OrderTaskAssembler.setHallPowerEnable(mIsHallPowerEnable ? 1 : 0));
//        orderTasks.add(OrderTaskAssembler.getHallPowerEnable());
//        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
