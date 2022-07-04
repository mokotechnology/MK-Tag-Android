package com.moko.bxp.tag.activity;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.dialog.AlertMessageDialog;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.dialog.ModifyPasswordDialog;
import com.moko.bxp.tag.fragment.AlarmFragment;
import com.moko.bxp.tag.fragment.DeviceFragment;
import com.moko.bxp.tag.fragment.SettingFragment;
import com.moko.bxp.tag.service.DfuService;
import com.moko.bxp.tag.utils.FileUtils;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import androidx.annotation.IdRes;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import no.nordicsemi.android.dfu.DfuProgressListener;
import no.nordicsemi.android.dfu.DfuProgressListenerAdapter;
import no.nordicsemi.android.dfu.DfuServiceInitiator;
import no.nordicsemi.android.dfu.DfuServiceListenerHelper;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;
    @BindView(R.id.radioBtn_alarm)
    RadioButton radioBtnAlarm;
    @BindView(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    @BindView(R.id.rg_options)
    RadioGroup rgOptions;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    @BindView(R.id.iv_save)
    ImageView ivSave;
    private FragmentManager fragmentManager;
    private AlarmFragment alarmFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mPassword;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;
    private boolean mReceiverTag = false;
    private int mDisconnectType;
    public boolean isConfigError;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        mPassword = getIntent().getStringExtra(AppConstants.EXTRA_KEY_PASSWORD);
        fragmentManager = getFragmentManager();
        initFragment();
        rgOptions.setOnCheckedChangeListener(this);
        EventBus.getDefault().register(this);
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        }
        getAlarmSwitch();
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                if (MokoSupport.getInstance().exportSingleEvents != null) {
                    MokoSupport.getInstance().exportSingleEvents.clear();
                    MokoSupport.getInstance().storeSingleEventString = null;
                }
                if (MokoSupport.getInstance().exportDoubleEvents != null) {
                    MokoSupport.getInstance().exportDoubleEvents.clear();
                    MokoSupport.getInstance().storeDoubleEventString = null;
                }
                if (MokoSupport.getInstance().exportLongEvents != null) {
                    MokoSupport.getInstance().exportLongEvents.clear();
                    MokoSupport.getInstance().storeLongEventString = null;
                }
                // 设备断开，通知页面更新
                if (mIsClose)
                    return;
                if (mDisconnectType > 0)
                    return;
                if (MokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        tvTitle.postDelayed(() -> {
                            dismissDFUProgressDialog();
                        }, 2000);
                    } else {
                        AlertMessageDialog dialog = new AlertMessageDialog();
                        dialog.setTitle("Dismiss");
                        dialog.setMessage("The device disconnected!");
                        dialog.setConfirm("Exit");
                        dialog.setCancelGone();
                        dialog.setOnAlertConfirmListener(() -> {
                            setResult(RESULT_OK);
                            finish();
                        });
                        dialog.show(getSupportFragmentManager());
                    }
                }
            }
        });

    }


    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_CURRENT_DATA.equals(action)) {
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_DISCONNECT:
                        if (value.length == 5) {
                            mDisconnectType = value[4] & 0xff;
                            if (mDisconnectType == 2 && isModifyPassword) {
                                isModifyPassword = false;
                                dismissSyncProgressDialog();
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setMessage("Modify password success!\nPlease reconnect the Device.");
                                dialog.setCancelGone();
                                dialog.setConfirm(R.string.ok);
                                dialog.setOnAlertConfirmListener(() -> {
                                    setResult(RESULT_OK);
                                    finish();
                                });
                                dialog.show(getSupportFragmentManager());
                            } else if (mDisconnectType == 3) {
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setMessage("Reset success!\nBeacon is disconnected.");
                                dialog.setCancelGone();
                                dialog.setConfirm(R.string.ok);
                                dialog.setOnAlertConfirmListener(() -> {
                                    setResult(RESULT_OK);
                                    finish();
                                });
                                dialog.show(getSupportFragmentManager());
                            } else if (mDisconnectType == 4) {
                                AlertMessageDialog dialog = new AlertMessageDialog();
                                dialog.setTitle("Dismiss");
                                dialog.setMessage("The device disconnected!");
                                dialog.setConfirm("Exit");
                                dialog.setCancelGone();
                                dialog.setOnAlertConfirmListener(() -> {
                                    setResult(RESULT_OK);
                                    finish();
                                });
                                dialog.show(getSupportFragmentManager());
                            }
                        }
                        break;
                }
            }
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
                    case CHAR_PASSWORD:
                        if (value.length == 5) {
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
                                    case KEY_MODIFY_PASSWORD:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(DeviceInfoActivity.this, "Opps！Save failed. Please check the input characters and try again.");
                                        }
                                        break;
                                }
                            }
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_VERIFY_PASSWORD_ENABLE:
                                        if (length > 0) {
                                            int enable = value[4] & 0xFF;
                                            deviceFragment.setViewShown(enable == 1);
                                        }
                                        break;
                                }
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
                                    case KEY_DEVICE_NAME:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        break;
                                    case KEY_EFFECTIVE_CLICK_INTERVAL:
                                    case KEY_DEVICE_ID:
                                        if (result == 0) {
                                            isConfigError = true;
                                        }
                                        if (isConfigError) {
                                            ToastUtils.showToast(DeviceInfoActivity.this, "Opps！Save failed. Please check the input characters and try again.");
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
                                        if (length == 6) {
                                            int slot = value[4];
                                            int slotEnable = value[5] & 0xFF;
                                            if (slot == 0) {
                                                alarmFragment.setSinglePressModeSwitch(slotEnable);
                                            } else if (slot == 1) {
                                                alarmFragment.setDoublePressModeSwitch(slotEnable);
                                            } else if (slot == 2) {
                                                alarmFragment.setLongPressModeSwitch(slotEnable);
                                            } else if (slot == 3) {
                                                alarmFragment.setAbnormalInactivityModeSwitch(slotEnable);
                                            }
                                        }
                                        break;
                                    case KEY_EFFECTIVE_CLICK_INTERVAL:
                                        if (length == 2) {
                                            int interval = MokoUtils.toInt(Arrays.copyOfRange(value, 4, 6));
                                            settingFragment.setEffectiveClickInterval(interval);
                                        }
                                        break;
                                    case KEY_DEVICE_NAME:
                                        if (length > 0) {
                                            String deviceName = new String(Arrays.copyOfRange(value, 4, 4 + length));
                                            mDeviceName = deviceName;
                                            deviceFragment.setDeviceName(deviceName);
                                        }
                                        break;
                                    case KEY_DEVICE_MAC:
                                        if (length == 6) {
                                            String mac = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 10));
                                            StringBuffer stringBuffer = new StringBuffer(mac);
                                            stringBuffer.insert(2, ":");
                                            stringBuffer.insert(5, ":");
                                            stringBuffer.insert(8, ":");
                                            stringBuffer.insert(11, ":");
                                            stringBuffer.insert(14, ":");
                                            mDeviceMac = stringBuffer.toString().toUpperCase();
                                        }
                                        break;
                                    case KEY_DEVICE_ID:
                                        if (length > 0) {
                                            String deviceIdHex = MokoUtils.bytesToHexString(Arrays.copyOfRange(value, 4, 4 + length));
                                            deviceFragment.setDeviceId(deviceIdHex);
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


    private void getAlarmSwitch() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getSlotParams(0));
        orderTasks.add(OrderTaskAssembler.getSlotParams(1));
        orderTasks.add(OrderTaskAssembler.getSlotParams(2));
        orderTasks.add(OrderTaskAssembler.getSlotParams(3));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void getDeviceInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getDeviceName());
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getVerifyPasswordEnable());
        orderTasks.add(OrderTaskAssembler.getDeviceId());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void getSetting() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getEffectiveClickInterval());
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
                            AlertMessageDialog dialog = new AlertMessageDialog();
                            dialog.setTitle("Dismiss");
                            dialog.setCancelGone();
                            dialog.setMessage("The current system of bluetooth is not available!");
                            dialog.setConfirm(R.string.ok);
                            dialog.setOnAlertConfirmListener(() -> {
                                finish();
                            });
                            dialog.show(getSupportFragmentManager());
                            break;

                    }
                }
            }
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_SELECT_FIRMWARE) {
            if (resultCode == RESULT_OK) {
                //得到uri，后面就是将uri转化成file的过程。
                Uri uri = data.getData();
                String firmwareFilePath = FileUtils.getPath(this, uri);
                if (TextUtils.isEmpty(firmwareFilePath)) {
                    return;
                }
                final File firmwareFile = new File(firmwareFilePath);
                if (firmwareFile.exists()) {
                    final DfuServiceInitiator starter = new DfuServiceInitiator(mDeviceMac)
                            .setDeviceName(mDeviceName)
                            .setKeepBond(false)
                            .setDisableNotification(true);
                    starter.setZip(null, firmwareFilePath);
                    starter.start(this, DfuService.class);
                    showDFUProgressDialog("Waiting...");
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_QUICK_SWITCH) {
            if (resultCode == RESULT_OK) {
                boolean enablePasswordVerify = data.getBooleanExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, false);
                if (enablePasswordVerify)
                    deviceFragment.setResetShown();
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ALARM_MODE) {
            if (resultCode == RESULT_OK) {
                int slotType = data.getIntExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 0);
                showSyncingProgressDialog();
                ivSave.postDelayed(() -> {
                    ArrayList<OrderTask> orderTasks = new ArrayList<>();
                    if (slotType == 0)
                        orderTasks.add(OrderTaskAssembler.getSlotParams(0));
                    else if (slotType == 1)
                        orderTasks.add(OrderTaskAssembler.getSlotParams(1));
                    else if (slotType == 2)
                        orderTasks.add(OrderTaskAssembler.getSlotParams(2));
                    else if (slotType == 3)
                        orderTasks.add(OrderTaskAssembler.getSlotParams(3));
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                }, 500);
            }
        }
    }

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
        MokoSupport.getInstance().disConnectBle();
        mIsClose = false;
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private void initFragment() {
        alarmFragment = AlarmFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, alarmFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(alarmFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();

    }

    private void showSlotFragment() {
        if (alarmFragment != null) {
            ivSave.setVisibility(View.GONE);
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(alarmFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.alarm_title));
    }

    private void showSettingFragment() {
        if (settingFragment != null) {
            ivSave.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction()
                    .hide(alarmFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        if (deviceFragment != null) {
            ivSave.setVisibility(View.VISIBLE);
            fragmentManager.beginTransaction()
                    .hide(alarmFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtn_alarm) {
            showSlotFragment();
            getAlarmSwitch();
        } else if (checkedId == R.id.radioBtn_setting) {
            showSettingFragment();
            getSetting();
        } else if (checkedId == R.id.radioBtn_device) {
            showDeviceFragment();
            getDeviceInfo();
        }
    }

    private boolean isModifyPassword;

    public void modifyPassword(String password) {
        isModifyPassword = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setNewPassword(password));
    }

    public void resetDevice() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetDevice());
    }

    public void setClose() {
        mIsClose = true;
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.setClose());
    }

    public void chooseFirmwareFile() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");//设置类型，我这里是任意类型，任意后缀的可以这样写。
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        try {
            startActivityForResult(Intent.createChooser(intent, "select file first!"), REQUEST_CODE_SELECT_FIRMWARE);
        } catch (ActivityNotFoundException ex) {
            ToastUtils.showToast(this, "install file manager app");
        }
    }

    public void onBack(View view) {
        back();
    }


    public void onSave(View view) {
        if (isWindowLocked())
            return;
        if (radioBtnSetting.isChecked()) {
            if (settingFragment.isValid()) {
                showSyncingProgressDialog();
                settingFragment.saveParams();
            } else {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            }
        }
        if (radioBtnDevice.isChecked()) {
            if (deviceFragment.isValid()) {
                showSyncingProgressDialog();
                deviceFragment.saveParams();
            } else {
                ToastUtils.showToast(this, "Opps！Save failed. Please check the input characters and try again.");
            }
        }
    }

    public void onSinglePressMode(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AlarmModeConfigActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 0);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_ALARM_MODE);
    }

    public void onDoublePressMode(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AlarmModeConfigActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 1);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_ALARM_MODE);
    }

    public void onLongPressMode(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AlarmModeConfigActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 2);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_ALARM_MODE);
    }

    public void onAbnormalInactivityMode(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AlarmModeConfigActivity.class);
        intent.putExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 3);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_ALARM_MODE);
    }

    public void onAlarmEvent(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AlarmEventActivity.class);
        startActivity(intent);
    }

    public void onDismissAlarmConfig(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, DismissAlarmNotifyTypeActivity.class);
        startActivity(intent);
    }

    public void onRemoteReminder(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, RemoteReminderActivity.class);
        startActivity(intent);
    }

    public void onAcc(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, AccDataActivity.class);
        startActivity(intent);
    }

    public void onPowerSavingConfig(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, PowerSavingConfigActivity.class);
        startActivity(intent);
    }

    public void onQuickSwitch(View view) {
        if (isWindowLocked())
            return;
        startActivityForResult(new Intent(this, QuickSwitchActivity.class), AppConstants.REQUEST_CODE_QUICK_SWITCH);
    }

    public void onTurnOffBeacon(View view) {
        if (isWindowLocked())
            return;
        AlertMessageDialog powerAlertDialog = new AlertMessageDialog();
        powerAlertDialog.setTitle("Warning！");
        powerAlertDialog.setMessage("Are you sure to turn off the Beacon?Please make sure the Beacon has a tag to turn on!");
        powerAlertDialog.setConfirm(R.string.ok);
        powerAlertDialog.setOnAlertConfirmListener(() -> {
            setClose();
        });
        powerAlertDialog.show(getSupportFragmentManager());
    }

    public void onResetBeacon(View view) {
        if (isWindowLocked())
            return;
        AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
        resetDeviceDialog.setTitle("Warning！");
        resetDeviceDialog.setMessage("Are you sure to reset the Beacon？");
        resetDeviceDialog.setConfirm(R.string.ok);
        resetDeviceDialog.setOnAlertConfirmListener(() -> {
            resetDevice();
        });
        resetDeviceDialog.show(getSupportFragmentManager());
    }


    public void onSystemInfo(View view) {
        if (isWindowLocked())
            return;
        Intent intent = new Intent(this, SystemInfoActivity.class);
        startActivity(intent);
    }

    private ProgressDialog mDFUDialog;

    private void showDFUProgressDialog(String tips) {
        mDFUDialog = new ProgressDialog(DeviceInfoActivity.this);
        mDFUDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mDFUDialog.setCanceledOnTouchOutside(false);
        mDFUDialog.setCancelable(false);
        mDFUDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        mDFUDialog.setMessage(tips);
        if (!isFinishing() && mDFUDialog != null && !mDFUDialog.isShowing()) {
            mDFUDialog.show();
        }
    }

    private void dismissDFUProgressDialog() {
        mDeviceConnectCount = 0;
        if (!isFinishing() && mDFUDialog != null && mDFUDialog.isShowing()) {
            mDFUDialog.dismiss();
        }
        AlertMessageDialog dialog = new AlertMessageDialog();
        if (isUpgradeCompleted) {
            dialog.setMessage("DFU Successfully!\nPlease reconnect the device.");
        } else {
            dialog.setMessage("Opps!DFU Failed.\nPlease try again!");
        }
        dialog.setCancelGone();
        dialog.setConfirm(R.string.ok);
        dialog.setOnAlertConfirmListener(() -> {
            isUpgrading = false;
            setResult(RESULT_OK);
            finish();
        });
        dialog.show(getSupportFragmentManager());
    }


    @Override
    protected void onResume() {
        super.onResume();
        DfuServiceListenerHelper.registerProgressListener(this, mDfuProgressListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        DfuServiceListenerHelper.unregisterProgressListener(this, mDfuProgressListener);
    }

    private int mDeviceConnectCount;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;

    private final DfuProgressListener mDfuProgressListener = new DfuProgressListenerAdapter() {
        @Override
        public void onDeviceConnecting(String deviceAddress) {
            XLog.w("onDeviceConnecting...");
            mDeviceConnectCount++;
            if (mDeviceConnectCount > 3) {
                ToastUtils.showToast(DeviceInfoActivity.this, "Error:DFU Failed");
                MokoSupport.getInstance().disConnectBle();
                final LocalBroadcastManager manager = LocalBroadcastManager.getInstance(DeviceInfoActivity.this);
                final Intent abortAction = new Intent(DfuService.BROADCAST_ACTION);
                abortAction.putExtra(DfuService.EXTRA_ACTION, DfuService.ACTION_ABORT);
                manager.sendBroadcast(abortAction);
            }
        }

        @Override
        public void onDeviceDisconnecting(String deviceAddress) {
            XLog.w("onDeviceDisconnecting...");
        }

        @Override
        public void onDfuProcessStarting(String deviceAddress) {
            isUpgrading = true;
            mDFUDialog.setMessage("DfuProcessStarting...");
        }


        @Override
        public void onEnablingDfuMode(String deviceAddress) {
            mDFUDialog.setMessage("EnablingDfuMode...");
        }

        @Override
        public void onFirmwareValidating(String deviceAddress) {
            mDFUDialog.setMessage("FirmwareValidating...");
        }

        @Override
        public void onDfuCompleted(String deviceAddress) {
            XLog.w("onDfuCompleted...");
            isUpgradeCompleted = true;
        }

        @Override
        public void onDfuAborted(String deviceAddress) {
            mDFUDialog.setMessage("DfuAborted...");
        }

        @Override
        public void onProgressChanged(String deviceAddress, int percent, float speed, float avgSpeed, int currentPart, int partsTotal) {
            String progress = String.format("Progress:%d%%", percent);
            XLog.i(progress);
            mDFUDialog.setMessage(progress);
        }

        @Override
        public void onError(String deviceAddress, int error, int errorType, String message) {
            XLog.i("DFU Error:" + message);
        }
    };

    public void onDFU(View view) {
        if (isWindowLocked())
            return;
        chooseFirmwareFile();
    }

    public void onModifyPassword(View view) {
        if (isWindowLocked())
            return;
        final ModifyPasswordDialog modifyPasswordDialog = new ModifyPasswordDialog();
        modifyPasswordDialog.setOnModifyPasswordClicked(new ModifyPasswordDialog.ModifyPasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                modifyPassword(password);
            }

            @Override
            public void onPasswordNotMatch() {
                AlertMessageDialog dialog = new AlertMessageDialog();
                dialog.setMessage("Password do not match!\nPlease try again.");
                dialog.setConfirm(R.string.ok);
                dialog.setCancelGone();
                dialog.show(getSupportFragmentManager());
            }
        });
        modifyPasswordDialog.show(getSupportFragmentManager());
    }
}
