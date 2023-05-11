package com.moko.bxp.tag.activity;


import android.app.FragmentManager;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothGattCharacteristic;
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
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;

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
import com.moko.bxp.tag.dialog.BottomDialog;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.dialog.ModifyPasswordDialog;
import com.moko.bxp.tag.fragment.DeviceFragment;
import com.moko.bxp.tag.fragment.SettingFragment;
import com.moko.bxp.tag.fragment.SlotFragment;
import com.moko.bxp.tag.utils.FileUtils;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;
import com.moko.support.task.OTADataTask;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceInfoActivity extends BaseActivity implements RadioGroup.OnCheckedChangeListener {
    public static final int REQUEST_CODE_SELECT_FIRMWARE = 0x10;

    @BindView(R.id.frame_container)
    FrameLayout frameContainer;
    @BindView(R.id.radioBtn_slot)
    RadioButton radioBtnSlot;
    @BindView(R.id.radioBtn_setting)
    RadioButton radioBtnSetting;
    @BindView(R.id.radioBtn_device)
    RadioButton radioBtnDevice;
    @BindView(R.id.rg_options)
    RadioGroup rgOptions;
    @BindView(R.id.tv_title)
    TextView tvTitle;
    private FragmentManager fragmentManager;
    private SlotFragment slotFragment;
    private SettingFragment settingFragment;
    private DeviceFragment deviceFragment;
    public String mDeviceMac;
    public String mDeviceName;
    private boolean mIsClose;
    private boolean mReceiverTag = false;
    private int mDisconnectType;
    public boolean isConfigError;
    private boolean isVerifyPassword;
    public boolean isSupportAcc;
    private ArrayList<String> mAdvModeList;
    private int mAdvModeSelected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_info);
        ButterKnife.bind(this);
        fragmentManager = getFragmentManager();
        isVerifyPassword = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, false);
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
            return;
        }
        mAdvModeList = new ArrayList<>();
        mAdvModeList.add("Legacy");
        mAdvModeList.add("Long Range");
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getAllSlot());
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getSensorType());
        //获取固件版本
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 100)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                if (mIsClose)
                    return;
                if (mDisconnectType > 0)
                    return;
                if (MokoSupport.getInstance().isBluetoothOpen()) {
                    if (isUpgrading) {
                        if (!isOTAMode) {
                            reconnectOTADevice();
                        } else {
                            dismissDFUProgressDialog();
                        }
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
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            }
            if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
                if (isUpgrading) {
                    if (mDFUDialog != null && mDFUDialog.isShowing())
                        mDFUDialog.setMessage("EnablingDfuMode...");
                    otaBegin();
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
                OrderTaskResponse response = event.getResponse();
                OrderCHAR orderCHAR = (OrderCHAR) response.orderCHAR;
                int responseType = response.responseType;
                byte[] value = response.responseValue;
                switch (orderCHAR) {
                    case CHAR_OTA_CONTROL:
                    case CHAR_OTA_DATA:
                        ToastUtils.showToast(this, "Error:DFU Failed!");
                        MokoSupport.getInstance().disConnectBle();
                        break;
                }
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
                    case CHAR_OTA_CONTROL:
                        if (value.length == 1) {
                            if (value[0] == 0x00) {
                                // 判断是否包含Data特征
                                BluetoothGattCharacteristic characteristic = MokoSupport.getInstance().getCharacteristic(OrderCHAR.CHAR_OTA_DATA);
                                if (characteristic == null) {
                                    // 重连后发送升级包
                                    MokoSupport.getInstance().disConnectBle();
                                } else {
                                    isOTAMode = true;
                                    // 直接发送升级包
                                    if (mDFUDialog != null && mDFUDialog.isShowing())
                                        mDFUDialog.setMessage("DfuProcessStarting...");
                                    mIndex = 0;
                                    mLastPackage = false;
                                    mPackageCount = 0;
                                    tvTitle.postDelayed(() -> {
                                        writeDataToDevice();
                                    }, 500);
                                }
                            }
                            if (value[0] == 0x03) {
                                // 完成升级
                                XLog.w("onDfuCompleted...");
                                isUpgradeCompleted = true;
                                tvTitle.postDelayed(() -> {
                                    MokoSupport.getInstance().disConnectBle();
                                }, 1000);
                            }
                        } else {
                            ToastUtils.showToast(this, "Error:DFU Failed!");
                        }
                        break;
                    case CHAR_OTA_DATA:
                        if (mLastPackage) {
                            if (mDFUDialog != null && mDFUDialog.isShowing())
                                mDFUDialog.setMessage("Progress:100%");
                            XLog.i("OTA UPLOAD SEND DONE");
                            otaEnd();
                            return;
                        }
                        writeDataToDevice();
                        break;
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
                            byte[] rawDataBytes = Arrays.copyOfRange(value, 4, 4 + length);
                            if (flag == 0x00) {
                                // read
                                switch (configKeyEnum) {
                                    case KEY_ALL_SLOT:
                                        if (length == 6) {
                                            slotFragment.updateSlotType(rawDataBytes);
                                        }
                                        break;
                                    case KEY_SLOT_ADV_PARAMS:
                                        if (length == 8)
                                            slotFragment.setSlotAdvParams(rawDataBytes);
                                        break;
                                    case KEY_SLOT_PARAMS:
                                        if (length > 1) {
                                            slotFragment.setSlotParams(rawDataBytes);
                                        }
                                        break;
                                    case KEY_SLOT_TRIGGER_PARAMS:
                                        if (length > 1) {
                                            slotFragment.setSlotTriggerParams(rawDataBytes);
                                        }
                                        break;
                                    case KEY_DEVICE_MAC:
                                        if (length == 6) {
                                            String mac = MokoUtils.bytesToHexString(rawDataBytes);
                                            StringBuffer stringBuffer = new StringBuffer(mac);
                                            stringBuffer.insert(2, ":");
                                            stringBuffer.insert(5, ":");
                                            stringBuffer.insert(8, ":");
                                            stringBuffer.insert(11, ":");
                                            stringBuffer.insert(14, ":");
                                            mDeviceMac = stringBuffer.toString().toUpperCase();
                                            deviceFragment.setMac(mDeviceMac);
                                        }
                                        break;
                                    case KEY_SENSOR_TYPE:
                                        if (length == 2) {
                                            // bit0 表示带三轴 bit1 表示带温湿度 bit2 表示带光感
                                            isSupportAcc = (value[5] & 0x01) == 0x01;
                                        }
                                        break;
                                    case KEY_BATTERY_VOLTAGE:
                                        if (length == 2) {
                                            int battery = MokoUtils.toInt(rawDataBytes);
                                            deviceFragment.setBattery(battery);
                                        }
                                        break;
                                    case KEY_ADV_MODE:
                                        if (length == 1) {
                                            mAdvModeSelected = value[4];
                                            settingFragment.setAdvMode(mAdvModeList.get(mAdvModeSelected));
                                        }
                                        break;

                                    case KEY_BATTERY_MODE:
                                        if (length == 1) {
                                            if ((value[4] & 0xff) != 0) {
                                                //纽扣电池不支持重置电池计算
                                                settingFragment.visibleResetBattery();
                                            }
                                        }
                                        break;
                                }
                            } else if (flag == 1) {
                                if (configKeyEnum == ParamsKeyEnum.KEY_RESET_BATTERY && length == 1) {
                                    int result = value[4] & 0xff;
                                    if (result == 0xAA) {
                                        ToastUtils.showToast(this, "success");
                                    } else {
                                        ToastUtils.showToast(this, "fail");
                                    }
                                }
                            }
                        }
                        break;

                    case CHAR_MODEL_NUMBER:
                        deviceFragment.setModelNumber(value);
                        break;
                    case CHAR_SOFTWARE_REVISION:
                        deviceFragment.setSoftwareRevision(value);
                        break;
                    case CHAR_FIRMWARE_REVISION:
                        setFirmwareVersion(new String(value).trim());
                        deviceFragment.setFirmwareRevision(value);
                        break;
                    case CHAR_HARDWARE_REVISION:
                        deviceFragment.setHardwareRevision(value);
                        break;
                    case CHAR_SERIAL_NUMBER:
                        deviceFragment.setSerialNumber(value);
                        break;
                    case CHAR_MANUFACTURER_NAME:
                        deviceFragment.setManufacturer(value);
                        break;
                }
            }
        });
    }

    private int firmwareVersion;

    private void setFirmwareVersion(String firmwareVersion) {
        if (TextUtils.isEmpty(firmwareVersion)) return;
        XLog.i("333333**" + firmwareVersion);
        if (firmwareVersion.startsWith("v") || firmwareVersion.startsWith("V")) {
            String result = firmwareVersion.substring(1).replaceAll("\\.", "");
            try {
                this.firmwareVersion = Integer.parseInt(result);
            } catch (Exception e) {
                XLog.i(e);
            }
        }
    }


    public void getAllSlot() {
        showSyncingProgressDialog();
        tvTitle.postDelayed(() -> {
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAllSlot());
        }, 1500);
    }

    private void getDeviceInfo() {
        showSyncingProgressDialog();
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.getBattery());
        orderTasks.add(OrderTaskAssembler.getDeviceMac());
        orderTasks.add(OrderTaskAssembler.getDeviceModel());
        orderTasks.add(OrderTaskAssembler.getSoftwareVersion());
        orderTasks.add(OrderTaskAssembler.getFirmwareVersion());
        orderTasks.add(OrderTaskAssembler.getHardwareVersion());
        orderTasks.add(OrderTaskAssembler.getProductDate());
        orderTasks.add(OrderTaskAssembler.getManufacturer());
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    private void getAdvMode() {
        showSyncingProgressDialog();
        MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getAdvMode());
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
                    try {
                        InputStream in = new FileInputStream(firmwareFile);
                        mFirmwareFileBytes = new byte[in.available()];
                        in.read(mFirmwareFileBytes, 0, in.available());
                        in.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    isUpgrading = true;
                    showDFUProgressDialog("Waiting...");
                    otaBegin();
                } else {
                    Toast.makeText(this, "file is not exists!", Toast.LENGTH_SHORT).show();
                }
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_QUICK_SWITCH) {
            if (resultCode == RESULT_OK) {
                boolean enablePasswordVerify = data.getBooleanExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, false);
                settingFragment.setResetVisibility(enablePasswordVerify);
                // 当连接时不需要密码验证，若启用Password verification，Reset Beacon选项显示，但Modify password需要再次连接时才显示。
                if (isVerifyPassword)
                    settingFragment.setModifyPasswordShown(enablePasswordVerify);
            }
        }
        if (requestCode == AppConstants.REQUEST_CODE_ALARM_MODE) {
            if (resultCode == RESULT_OK) {
                int slotType = data.getIntExtra(AppConstants.EXTRA_KEY_SLOT_TYPE, 0);
                showSyncingProgressDialog();
                tvTitle.postDelayed(() -> {
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
        slotFragment = SlotFragment.newInstance();
        settingFragment = SettingFragment.newInstance();
        deviceFragment = DeviceFragment.newInstance();
        fragmentManager.beginTransaction()
                .add(R.id.frame_container, slotFragment)
                .add(R.id.frame_container, settingFragment)
                .add(R.id.frame_container, deviceFragment)
                .show(slotFragment)
                .hide(settingFragment)
                .hide(deviceFragment)
                .commit();

    }

    private void showSlotFragment() {
        if (slotFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(settingFragment)
                    .hide(deviceFragment)
                    .show(slotFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.slot_title));
    }

    private void showSettingFragment() {
        if (settingFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(slotFragment)
                    .hide(deviceFragment)
                    .show(settingFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.setting_title));
    }

    private void showDeviceFragment() {
        if (deviceFragment != null) {
            fragmentManager.beginTransaction()
                    .hide(slotFragment)
                    .hide(settingFragment)
                    .show(deviceFragment)
                    .commit();
        }
        tvTitle.setText(getString(R.string.device_title));
    }

    @Override
    public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
        if (checkedId == R.id.radioBtn_slot) {
            showSlotFragment();
            getAllSlot();
        } else if (checkedId == R.id.radioBtn_setting) {
            showSettingFragment();
            settingFragment.setFirmwareVersion(firmwareVersion);
            settingFragment.setResetVisibility(isVerifyPassword);
            settingFragment.setModifyPasswordShown(isVerifyPassword);
            getAdvMode();
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

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    public void onSensorConfig(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, SensorConfigActivity.class);
        intent.putExtra(AppConstants.FIRMWARE_VERSION, firmwareVersion);
        startActivity(intent);
    }

    public void onQuickSwitch(View view) {
        if (isWindowLocked()) return;
        Intent intent = new Intent(this, QuickSwitchActivity.class);
        intent.putExtra(AppConstants.FIRMWARE_VERSION, firmwareVersion);
        startActivityForResult(intent, AppConstants.REQUEST_CODE_QUICK_SWITCH);
    }

    public void onResetBeacon(View view) {
        if (isWindowLocked())
            return;
        AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
        resetDeviceDialog.setTitle("Warning！");
        resetDeviceDialog.setMessage("Are you sure to reset the Beacon？");
        resetDeviceDialog.setConfirm(R.string.ok);
        resetDeviceDialog.setOnAlertConfirmListener(() -> resetDevice());
        resetDeviceDialog.show(getSupportFragmentManager());
    }

    public void onRemoteMinder(View view) {
        Intent intent = new Intent(this, RemoteReminderActivity.class);
        startActivity(intent);
    }

    public void onResetBattery(View view) {
        //重置电池计算
        if (isWindowLocked()) return;
        AlertMessageDialog resetDeviceDialog = new AlertMessageDialog();
        resetDeviceDialog.setTitle("Warning！");
        resetDeviceDialog.setMessage("Please ensure you have replaced the new battery for this beacon before reset the Battery");
        resetDeviceDialog.setConfirm(R.string.ok);
        resetDeviceDialog.setOnAlertConfirmListener(() -> {
            showSyncingProgressDialog();
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.resetBattery());
        });
        resetDeviceDialog.show(getSupportFragmentManager());
    }

    ///////////////////////////////////////////////////////////////////////////
    //
    ///////////////////////////////////////////////////////////////////////////

    private int mIndex = 0;
    private boolean mLastPackage = false;
    private int mPackageCount = 0;
    private int MTU = 80;
    private boolean isUpgrading;
    private boolean isUpgradeCompleted;
    private boolean isOTAMode;
    private byte[] mFirmwareFileBytes;

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

    private void reconnectOTADevice() {
        tvTitle.postDelayed(() -> {
            if (mDFUDialog != null && mDFUDialog.isShowing())
                mDFUDialog.setMessage("DeviceConnecting...");
            MokoSupport.getInstance().connDevice(mDeviceMac);
        }, 4000);
    }

    // 1.
    private void otaBegin() {
        //Writing 0x00 to control characteristic to DFU mode  target device begins OTA process
        tvTitle.postDelayed(() -> {
            XLog.i("OTA BEGIN");
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.startDFU());
        }, 500);
    }

    // 2.
    private void writeDataToDevice() {
        byte[] payload = new byte[MTU];
        if (mIndex + MTU >= mFirmwareFileBytes.length) {
            int restSize = mFirmwareFileBytes.length - mIndex;
            System.arraycopy(mFirmwareFileBytes, mIndex, payload, 0, restSize); //copy rest bytes
            mLastPackage = true;
        } else {
            payload = Arrays.copyOfRange(mFirmwareFileBytes, mIndex, mIndex + MTU);
        }
        OTADataTask task = new OTADataTask();
        task.setData(payload);
        MokoSupport.getInstance().sendOrder(task);
        final int progress = (int) (100.0f * mIndex / mFirmwareFileBytes.length);
        if (mDFUDialog != null && mDFUDialog.isShowing())
            mDFUDialog.setMessage(String.format("Progress:%d%%", progress));
        mPackageCount = mPackageCount + 1;
        mIndex = mIndex + MTU;
    }

    // 3.
    private void otaEnd() {
        tvTitle.postDelayed(() -> {
            XLog.i("OTA END");
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.endDFU());
        }, 500);
    }

    public void onDFU(View view) {
        if (isWindowLocked())
            return;
        BluetoothGattCharacteristic characteristic = MokoSupport.getInstance().getCharacteristic(OrderCHAR.CHAR_OTA_CONTROL);
        if (characteristic == null) {
            ToastUtils.showToast(DeviceInfoActivity.this, "Error:Characteristic of OTA is null!");
            return;
        }
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


    public void onAdvMode(View view) {
        if (isWindowLocked())
            return;
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(mAdvModeList, mAdvModeSelected);
        dialog.setListener(value -> {
            mAdvModeSelected = value;
            showSyncingProgressDialog();
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setAdvMode(value));
            orderTasks.add(OrderTaskAssembler.getAdvMode());
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
        });
        dialog.show(getSupportFragmentManager());
    }
}
