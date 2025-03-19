package com.moko.bxp.tag.activity;

import android.text.TextUtils;
import android.view.View;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.databinding.ActivityStaticHeartbeatBinding;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.OrderCHAR;
import com.moko.support.tag.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.Arrays;

/**
 * @author: jun.liu
 * @date: 2023/5/9 9:40
 * @des:
 */
public class StaticHeartbeatActivity extends BaseActivity<ActivityStaticHeartbeatBinding> {

    private boolean isSwitch;
    private final String checkedText = "*Please ensure that all active SLOTs have enabled the Motion detection trigger function.\n\n*Please ensure that the configured static cycle time value is greater than the maximum keep static time value parameter configured for all enabled SLOTs' Motion detection trigger function parameters.";
    private final String uncheckedText = "*Before enabling the static heartbeat function, please ensure that all active SLOTs have enabled the Motion detection trigger function.";

    @Override
    protected void onCreate() {
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getStaticHeartbeat());
        mBind.ivSwitch.setOnClickListener(v -> onSwitchClick());
    }

    @Override
    protected ActivityStaticHeartbeatBinding getViewBinding() {
        return ActivityStaticHeartbeatBinding.inflate(getLayoutInflater());
    }

    private void onSwitchClick() {
        isSwitch = !isSwitch;
        mBind.group.setVisibility(isSwitch ? View.VISIBLE : View.GONE);
        mBind.ivSwitch.setImageResource(isSwitch ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        mBind.tvTips.setText(isSwitch ? checkedText : uncheckedText);
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        int time = TextUtils.isEmpty(mBind.etStaticTime.getText()) ? 1 : Integer.parseInt(mBind.etStaticTime.getText().toString());
        int duration = TextUtils.isEmpty(mBind.etDuration.getText()) ? 1 : Integer.parseInt(mBind.etDuration.getText().toString());
        if (isSwitch) {
            if (TextUtils.isEmpty(mBind.etStaticTime.getText()) || TextUtils.isEmpty(mBind.etDuration.getText())) {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                return;
            }
            time = Integer.parseInt(mBind.etStaticTime.getText().toString());
            duration = Integer.parseInt(mBind.etDuration.getText().toString());
            if (!isValid(time)) {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                return;
            }
            if (!isValid(duration)) {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                return;
            }
        }
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setStaticHeartbeat(time, duration, isSwitch ? 1 : 0));
    }

    private boolean isValid(int result) {
        return result >= 1 && result <= 65535;
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 400)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                dismissSyncProgressDialog();
            }
            if (MokoConstants.ACTION_ORDER_RESULT.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                byte[] value = response.responseValue;
                if (orderCHAR == OrderCHAR.CHAR_PARAMS) {
                    if (value.length > 4) {
                        int header = value[0] & 0xFF;// 0xEB
                        int flag = value[1] & 0xFF;// read or write
                        int cmd = value[2] & 0xFF;
                        ParamsKeyEnum configKeyEnum = ParamsKeyEnum.fromParamKey(cmd);
                        if (header != 0xEB || configKeyEnum == null) {
                            return;
                        }
                        int length = value[3] & 0xFF;
                        if (flag == 0x01 && length == 0x01) {
                            // write
                            if (configKeyEnum == ParamsKeyEnum.KEY_STATIC_HEARTBEAT) {
                                int result = value[4] & 0xFF;
                                if (result == 0xAA) {
                                    ToastUtils.showToast(this, "Success");
                                } else {
                                    ToastUtils.showToast(this, "Opps！Save failed!");
                                }
                            }
                        }
                        if (flag == 0x00) {
                            if (configKeyEnum == ParamsKeyEnum.KEY_STATIC_HEARTBEAT && length == 5) {
                                int enable = value[4] & 0xff;
                                mBind.group.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
                                isSwitch = enable == 1;
                                if (enable == 1) {
                                    mBind.tvTips.setText(checkedText);
                                } else {
                                    mBind.tvTips.setText(uncheckedText);
                                }
                                mBind.ivSwitch.setImageResource(isSwitch ? R.drawable.ic_checked : R.drawable.ic_unchecked);
                                staticTime = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                staticDuration = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                mBind.etStaticTime.setText(String.valueOf(staticTime));
                                mBind.etStaticTime.setSelection(mBind.etStaticTime.getText().length());
                                mBind.etDuration.setText(String.valueOf(staticDuration));
                                mBind.etDuration.setSelection(mBind.etDuration.getText().length());
                            }
                        }
                    }
                }
            }
        });
    }

    private int staticTime;
    private int staticDuration;

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

}
