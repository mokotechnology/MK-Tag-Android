package com.moko.bxp.tag.activity;

import android.content.Intent;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.bxp.tag.databinding.ActivitySensorConfigTagBinding;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import androidx.annotation.Nullable;

public class SensorConfigActivity extends BaseActivity<ActivitySensorConfigTagBinding> {

    @Override
    protected void onCreate() {
        EventBus.getDefault().register(this);
//        showSyncingProgressDialog();
//        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getSensorType());
        boolean isSupportAcc = getIntent().getBooleanExtra("acc", false);
        boolean isHallPowerEnable = getIntent().getBooleanExtra("hall", false);
        mBind.tvAccConfig.setVisibility(isSupportAcc ? View.VISIBLE : View.GONE);
        mBind.tvHall.setVisibility(isHallPowerEnable ? View.GONE : View.VISIBLE);
    }

    @Override
    protected ActivitySensorConfigTagBinding getViewBinding() {
        return ActivitySensorConfigTagBinding.inflate(getLayoutInflater());
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                SensorConfigActivity.this.finish();
            }
        });
    }

//    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
//    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
//        EventBus.getDefault().cancelEventDelivery(event);
//        final String action = event.getAction();
//        runOnUiThread(() -> {
//            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
//            }
//            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
//                dismissSyncProgressDialog();
//            }
//            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
//                OrderTaskResponse response = event.getResponse();
//                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
//                int responseType = response.responseType;
//                byte[] value = response.responseValue;
//                switch (orderCHAR) {
//                    case CHAR_PARAMS:
//                        if (value.length > 4) {
//                            int header = value[0] & 0xFF;// 0xEB
//                            int flag = value[1] & 0xFF;// read or write
//                            int cmd = value[2] & 0xFF;
//                            if (header != 0xEB)
//                                return;
//                            ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
//                            if (configKeyEnum == null) {
//                                return;
//                            }
//                            int length = value[3] & 0xFF;
//                            if (flag == 0x00) {
//                                // read
//                                switch (configKeyEnum) {
//                                    case KEY_SENSOR_TYPE:
//                                        if (length == 2) {
//                                            // bit0 表示带三轴 bit1 表示带温湿度 bit2 表示带光感
//                                            tvAccConfig.setVisibility((value[5] & 0x01) == 0x01 ? View.VISIBLE : View.GONE);
//                                        }
//                                        break;
//                                }
//                            }
//                        }
//                        break;
//                }
//            }
//        });
//    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (EventBus.getDefault().isRegistered(this))
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
        intent.putExtra("status", status);
        setResult(RESULT_OK, intent);
        EventBus.getDefault().unregister(this);
        finish();
    }

    public void onAccConfig(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, AccDataActivity.class);
        startActivity(intent);
    }

    public void onHallSensorConfig(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, HallSensorConfigActivity.class);
        startActivityForResult(intent, 200);
    }

    private int status;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 200 && resultCode == RESULT_OK) {
            if (null != data) {
                status = data.getIntExtra("status", 0);
                if (status == 1) {
                    mBind.tvHall.setVisibility(View.GONE);
                }
            }
        }
    }
}
