package com.moko.bxp.tag.activity;


import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.dialog.BottomDialog;
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

import butterknife.BindView;
import butterknife.ButterKnife;

public class AccDataActivity extends BaseActivity {

    @BindView(R.id.iv_sync)
    ImageView ivSync;
    @BindView(R.id.tv_sync)
    TextView tvSync;
    @BindView(R.id.tv_x_data)
    TextView tvXData;
    @BindView(R.id.tv_y_data)
    TextView tvYData;
    @BindView(R.id.tv_z_data)
    TextView tvZData;
    @BindView(R.id.tv_axis_scale)
    TextView tvAxisScale;
    @BindView(R.id.tv_axis_data_rate)
    TextView tvAxisDataRate;
    @BindView(R.id.et_motion_threshold)
    EditText etMotionThreshold;
    @BindView(R.id.tv_motion_threshold_unit)
    TextView tvMotionThresholdUnit;
    @BindView(R.id.tv_trigger_count)
    TextView tvTriggerCount;
    @BindView(R.id.layoutStaticHeart)
    LinearLayout layoutStaticHeart;
    private boolean mReceiverTag = false;
    private ArrayList<String> axisDataRates;
    private ArrayList<String> axisScales;
    private boolean isSync;
    private int mSelectedRate;
    private int mSelectedScale;
    public boolean isConfigError;
    private int accType = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_acc_data);
        ButterKnife.bind(this);
        axisDataRates = new ArrayList<>();
        axisDataRates.add("1Hz");
        axisDataRates.add("10Hz");
        axisDataRates.add("25Hz");
        axisDataRates.add("50Hz");
        axisDataRates.add("100Hz");
        axisScales = new ArrayList<>();
        axisScales.add("±2g");
        axisScales.add("±4g");
        axisScales.add("±8g");
        axisScales.add("±16g");

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
            orderTasks.add(OrderTaskAssembler.getMotionTriggerCount());
//            orderTasks.add(OrderTaskAssembler.getAxisParams());
            //先获取三轴传感器类型
            orderTasks.add(OrderTaskAssembler.getAccType());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        }
        layoutStaticHeart.setOnClickListener(v -> startActivity(new Intent(this, StaticHeartbeatActivity.class)));
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
                                    case KEY_AXIS_PARAMS:
                                    case KEY_MOTION_TRIGGER_COUNT:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(AccDataActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        } else {
                                            ToastUtils.showToast(this, "Success");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_MOTION_TRIGGER_COUNT:
                                        if (length != 2) return;
                                        int count = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                        tvTriggerCount.setText(String.valueOf(count));
                                        break;
                                    case KEY_AXIS_PARAMS:
                                        if (length == 3) {
                                            mSelectedRate = value[4] & 0xFF;
                                            mSelectedScale = value[5] & 0xFF;
                                            int threshold = value[6] & 0xFF;
                                            tvAxisDataRate.setText(axisDataRates.get(mSelectedRate));
                                            tvAxisScale.setText(axisScales.get(mSelectedScale));
                                            etMotionThreshold.setText(String.valueOf(threshold));
                                            if (mSelectedScale == 0) {
                                                tvMotionThresholdUnit.setText(accType == 1 ? "x3.91mg" : "x16mg");
                                            } else if (mSelectedScale == 1) {
                                                tvMotionThresholdUnit.setText(accType == 1 ? "x7.81mg" : "x32mg");
                                            } else if (mSelectedScale == 2) {
                                                tvMotionThresholdUnit.setText(accType == 1 ? "x15.63mg" : "x62mg");
                                            } else if (mSelectedScale == 3) {
                                                tvMotionThresholdUnit.setText(accType == 1 ? "x31.25mg" : "x186mg");
                                            }
                                        }
                                        break;

                                    case KEY_ACC_TYPE:
                                        //三轴传感器类型
                                        if (length == 1) accType = value[4] & 0xff;
                                        XLog.i("333333===="+Arrays.toString(value));
                                        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAxisParams());
                                        break;

                                }
                            }
                        }
                        break;
                }
            }
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_ACC:
                        if (value.length > 9) {
                            tvXData.setText(String.format("X-axis:%dmg", MokoUtils.toIntSigned(Arrays.copyOfRange(value, 4, 6))));
                            tvYData.setText(String.format("Y-axis:%dmg", MokoUtils.toIntSigned(Arrays.copyOfRange(value, 6, 8))));
                            tvZData.setText(String.format("Z-axis:%dmg", MokoUtils.toIntSigned(Arrays.copyOfRange(value, 8, 10))));
                        }
                        break;
                }
            }
        });
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {

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
        MokoSupport.getInstance().disableAccNotify();
        finish();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    public void onBack(View view) {
        back();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        String thresholdStr = etMotionThreshold.getText().toString();
        if (TextUtils.isEmpty(thresholdStr)) {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            return;
        }
        int threshold = Integer.parseInt(thresholdStr);
        if (threshold < 1 || threshold > 255) {
            ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            return;
        }
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setAxisParams(mSelectedRate, mSelectedScale, threshold));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }


    public void onClear(View view) {
        if (isWindowLocked())
            return;
        // 保存
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.clearMotionTriggerCount());
        orderTasks.add(OrderTaskAssembler.getMotionTriggerCount());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void onSync(View view) {
        if (isWindowLocked())
            return;
        if (!isSync) {
            isSync = true;
            MokoSupport.getInstance().enableAccNotify();
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            ivSync.startAnimation(animation);
            tvSync.setText("Stop");
        } else {
            MokoSupport.getInstance().disableAccNotify();
            isSync = false;
            ivSync.clearAnimation();
            tvSync.setText("Sync");
        }
    }

    public void onAxisScale(View view) {
        if (isWindowLocked()) return;
        BottomDialog scaleDialog = new BottomDialog();
        scaleDialog.setDatas(axisScales, mSelectedScale);
        scaleDialog.setListener(value -> {
            mSelectedScale = value;
            if (mSelectedScale == 0) {
                tvMotionThresholdUnit.setText(accType == 1 ? "x3.91mg" : "x16mg");
            } else if (mSelectedScale == 1) {
                tvMotionThresholdUnit.setText(accType == 1 ? "x7.81mg" : "x32mg");
            } else if (mSelectedScale == 2) {
                tvMotionThresholdUnit.setText(accType == 1 ? "x15.63mg" : "x62mg");
            } else if (mSelectedScale == 3) {
                tvMotionThresholdUnit.setText(accType == 1 ? "x31.25mg" : "x186mg");
            }
            tvAxisScale.setText(axisScales.get(value));
        });
        scaleDialog.show(getSupportFragmentManager());
    }

    public void onAxisDataRate(View view) {
        if (isWindowLocked()) return;
        BottomDialog dataRateDialog = new BottomDialog();
        dataRateDialog.setDatas(axisDataRates, mSelectedRate);
        dataRateDialog.setListener(value -> {
            mSelectedRate = value;
            tvAxisDataRate.setText(axisDataRates.get(value));
        });
        dataRateDialog.show(getSupportFragmentManager());
    }
}
