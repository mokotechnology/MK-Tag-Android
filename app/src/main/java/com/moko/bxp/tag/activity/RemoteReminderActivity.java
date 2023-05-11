package com.moko.bxp.tag.activity;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;

import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTaskResponse;
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

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author: jun.liu
 * @date: 2023/5/9 18:25
 * @des: 远程提醒
 */
public class RemoteReminderActivity extends BaseActivity {
    @BindView(R.id.etTime)
    EditText etTime;
    @BindView(R.id.etInterval)
    EditText etInterval;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remote_reminder);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        findViewById(R.id.btnRemind).setOnClickListener(v -> onRemindClick());
    }

    //远程提醒
    private void onRemindClick() {
        if (TextUtils.isEmpty(etInterval.getText()) || TextUtils.isEmpty(etTime.getText())) {
            ToastUtils.showToast(this, "Opps！Please check the input characters and try again.");
            return;
        }
        int interval = Integer.parseInt(etInterval.getText().toString().trim());
        int time = Integer.parseInt(etTime.getText().toString().trim());
        if (interval < 1 || interval > 100) {
            ToastUtils.showToast(this, "Blinking interval should in 1~100");
            return;
        }
        if (time < 1 || time > 600) {
            ToastUtils.showToast(this, "Blinking time should in 1~600");
            return;
        }
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setRemoteReminder(interval * 100, time));
    }

    public void onBack(View view) {
        finish();
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
                        if (header != 0xEB || configKeyEnum == null) return;
                        int length = value[3] & 0xFF;
                        if (flag == 0x01 && length == 0x01 && configKeyEnum == ParamsKeyEnum.KEY_REMOTE_REMINDER) {
                            // write
                            int result = value[4] & 0xFF;
                            if (result == 0xAA) {
                                ToastUtils.showToast(this, "success");
                            } else {
                                ToastUtils.showToast(this, "fail");
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
