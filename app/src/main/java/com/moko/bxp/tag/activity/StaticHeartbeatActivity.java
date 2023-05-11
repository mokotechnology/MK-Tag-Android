package com.moko.bxp.tag.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.constraintlayout.widget.Group;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
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

import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: jun.liu
 * @date: 2023/5/9 9:40
 * @des:
 */
public class StaticHeartbeatActivity extends BaseActivity {
    @BindView(R.id.ivSwitch)
    ImageView ivSwitch;
    @BindView(R.id.etStaticTime)
    EditText etStaticTime;
    @BindView(R.id.etDuration)
    EditText etDuration;
    @BindView(R.id.group)
    Group group;
    @BindView(R.id.tvTips)
    TextView tvTips;
    private boolean isSwitch;
    private final String checkedText = "*Please ensure that all active SLOTs have enabled the Motion detection trigger function.\n*Please ensure that the configured static cycle time value is greater than the maximum keep static time value parameter configured for all enabled SLOTs' Motion detection trigger function parameters.";
    private final String uncheckedText = "*Before enabling the static heartbeat function, please ensure that all active SLOTs have enabled the Motion detection trigger function.";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_static_heartbeat);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getStaticHeartbeat());
        ivSwitch.setOnClickListener(v -> onSwitchClick());
    }

    private void onSwitchClick() {
        isSwitch = !isSwitch;
        group.setVisibility(isSwitch ? View.VISIBLE : View.GONE);
        ivSwitch.setImageResource(isSwitch ? R.drawable.ic_checked : R.drawable.ic_unchecked);
        tvTips.setText(isSwitch ? checkedText : uncheckedText);
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked()) return;
        int time = 1;
        int duration = 1;
        if (isSwitch) {
            if (TextUtils.isEmpty(etStaticTime.getText()) || TextUtils.isEmpty(etDuration.getText())) {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                return;
            }
            time = Integer.parseInt(etStaticTime.getText().toString());
            duration = Integer.parseInt(etDuration.getText().toString());
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
                                    ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
                                }
                            }
                        }
                        if (flag == 0x00) {
                            if (configKeyEnum == ParamsKeyEnum.KEY_STATIC_HEARTBEAT && length == 5) {
                                int enable = value[4] & 0xff;
                                group.setVisibility(enable == 1 ? View.VISIBLE : View.GONE);
                                isSwitch = enable == 1;
                                if (enable == 1) {
                                    int time = MokoUtils.toInt(Arrays.copyOfRange(value, 5, 7));
                                    int duration = MokoUtils.toInt(Arrays.copyOfRange(value, 7, 9));
                                    etStaticTime.setText(String.valueOf(time));
                                    etStaticTime.setSelection(etStaticTime.getText().length());
                                    etDuration.setText(String.valueOf(duration));
                                    etDuration.setSelection(etDuration.getText().length());
                                    tvTips.setText(checkedText);
                                } else {
                                    tvTips.setText(uncheckedText);
                                }
                            }
                        }
                    }
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

}
