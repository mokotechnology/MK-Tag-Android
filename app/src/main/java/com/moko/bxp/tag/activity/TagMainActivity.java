package com.moko.bxp.tag.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.BuildConfig;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.adapter.DeviceListAdapter;
import com.moko.bxp.tag.databinding.ActivityMainTagBinding;
import com.moko.bxp.tag.dialog.AlertMessageDialog;
import com.moko.bxp.tag.dialog.LoadingDialog;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.dialog.PasswordDialog;
import com.moko.bxp.tag.dialog.ScanFilterDialog;
import com.moko.bxp.tag.entity.AdvInfo;
import com.moko.bxp.tag.utils.AdvInfoAnalysisImpl;
import com.moko.bxp.tag.utils.SPUtiles;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.bxp.tag.utils.Utils;
import com.moko.support.tag.MokoBleScanner;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.callback.MokoScanDeviceCallback;
import com.moko.support.tag.entity.DeviceInfo;
import com.moko.support.tag.entity.OrderCHAR;
import com.moko.support.tag.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;


public class TagMainActivity extends BaseActivity<ActivityMainTagBinding> implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemChildClickListener {

    private boolean mReceiverTag = false;
    private ConcurrentHashMap<String, AdvInfo> advInfoHashMap;
    private ArrayList<AdvInfo> advInfoList;
    private DeviceListAdapter adapter;
    private MokoBleScanner mokoBleScanner;
    private Handler mHandler;
    private boolean isPasswordError;
    private boolean isVerifyPassword;

    public static String PATH_LOGCAT;


    @Override
    protected void onCreate() {
        // 初始化Xlog
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            // 优先保存到SD卡中
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                PATH_LOGCAT = getExternalFilesDir(null).getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "MOKOTAG");
            } else {
                PATH_LOGCAT = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "MOKOTAG");
            }
        } else {
            // 如果SD卡不存在，就保存到本应用的目录下
            PATH_LOGCAT = getFilesDir().getAbsolutePath() + File.separator + (BuildConfig.IS_LIBRARY ? "mokoBeaconXPro" : "MOKOTAG");
        }
        MokoSupport.getInstance().init(getApplicationContext());
        advInfoHashMap = new ConcurrentHashMap<>();
        advInfoList = new ArrayList<>();
        adapter = new DeviceListAdapter();
        adapter.replaceData(advInfoList);
        adapter.setOnItemChildClickListener(this);
        adapter.openLoadAnimation();
        mBind.rvDevices.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.shape_recycleview_divider));
        mBind.rvDevices.addItemDecoration(itemDecoration);
        mBind.rvDevices.setAdapter(adapter);

        mHandler = new Handler(Looper.getMainLooper());
        mokoBleScanner = new MokoBleScanner(this);
        if (!BuildConfig.IS_LIBRARY) {
            StringBuffer buffer = new StringBuffer();
            // 记录机型
            buffer.append("机型：");
            buffer.append(android.os.Build.MODEL);
            buffer.append("=====");
            // 记录版本号
            buffer.append("手机系统版本：");
            buffer.append(android.os.Build.VERSION.RELEASE);
            buffer.append("=====");
            // 记录APP版本
            buffer.append("APP版本：");
            buffer.append(Utils.getVersionInfo(this));
            XLog.d(buffer.toString());
        }
        EventBus.getDefault().register(this);
        mSavedPassword = SPUtiles.getStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD, "");
        // 注册广播接收器
        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        mReceiverTag = true;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
        } else {
            if (animation == null) {
                startScan();
            }
        }
    }

    @Override
    protected ActivityMainTagBinding getViewBinding() {
        return ActivityMainTagBinding.inflate(getLayoutInflater());
    }

    private String unLockResponse;
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                String action = intent.getAction();
                if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
                    int blueState = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    switch (blueState) {
                        case BluetoothAdapter.STATE_TURNING_OFF:
                            if (animation != null) {
                                mHandler.removeMessages(0);
                                mokoBleScanner.stopScanDevice();
                                onStopScan();
                            }
                            break;
                        case BluetoothAdapter.STATE_ON:
                            if (animation == null) {
                                startScan();
                            }
                            break;

                    }
                }
            }
        }
    };

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        String action = event.getAction();
        if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
            mPassword = "";
            // 设备断开，通知页面更新
            dismissLoadingProgressDialog();
            dismissLoadingMessageDialog();
            if (isVerifyPassword) {
                isVerifyPassword = false;
                showPasswordDialog();
                return;
            }
            if (animation == null) {
                if (isPasswordError) {
                    isPasswordError = false;
                } else {
                    ToastUtils.showToast(TagMainActivity.this, "Connection failed");
                }
                startScan();
            }
        }
        if (MokoConstants.ACTION_DISCOVER_SUCCESS.equals(action)) {
            // 设备连接成功，通知页面更新
            dismissLoadingProgressDialog();
            if (!TextUtils.isEmpty(mPassword)) {
                showLoadingMessageDialog();
                mHandler.postDelayed(() -> {
                    ArrayList<OrderTask> orderTasks = new ArrayList<>();
                    orderTasks.add(OrderTaskAssembler.setPassword(mPassword));
                    MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
                }, 500);
                return;
            }
            showLoadingProgressDialog();
            mHandler.postDelayed(() -> {
                ArrayList<OrderTask> orderTasks = new ArrayList<>();
                orderTasks.add(OrderTaskAssembler.getVerifyPasswordEnable());
                MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            }, 500);
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        final String action = event.getAction();
        if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
        }
        if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
            dismissLoadingMessageDialog();
            dismissLoadingProgressDialog();
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
                        if (flag == 0x00 && length == 0x01) {
                            // read
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_VERIFY_PASSWORD_ENABLE:
                                    if (result == 0x01) {
                                        isVerifyPassword = true;
                                        MokoSupport.getInstance().disConnectBle();
                                    } else {
                                        Intent i = new Intent(this, DeviceInfoActivity.class);
                                        i.putExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, false);
                                        startActivityForResult(i, AppConstants.REQUEST_CODE_DEVICE_INFO);
                                    }
                                    break;
                            }
                        }
                        if (flag == 0x01 && length == 0x01) {
                            // write
                            int result = value[4] & 0xFF;
                            switch (configKeyEnum) {
                                case KEY_PASSWORD:
                                    if (result == 0xAA) {
                                        mSavedPassword = mPassword;
                                        SPUtiles.setStringValue(this, AppConstants.SP_KEY_SAVED_PASSWORD, mSavedPassword);
                                        XLog.i("Success");
                                        Intent i = new Intent(this, DeviceInfoActivity.class);
                                        i.putExtra(AppConstants.EXTRA_KEY_PASSWORD_VERIFICATION, true);
                                        startActivityForResult(i, AppConstants.REQUEST_CODE_DEVICE_INFO);
                                    } else {
                                        isPasswordError = true;
                                        ToastUtils.showToast(this, "Password incorrect！");
                                        MokoSupport.getInstance().disConnectBle();
                                    }
                                    break;
                            }
                        }
                    }
                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case AppConstants.REQUEST_CODE_DEVICE_INFO:
                    mPassword = "";
                    if (animation == null) {
                        startScan();
                    }
                    break;

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


    @Override
    public void onStartScan() {
        advInfoHashMap.clear();
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (animation != null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            adapter.replaceData(advInfoList);
                            mBind.tvDeviceNum.setText(String.format("DEVICE(%d)", advInfoList.size()));
                        }
                    });
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    updateDevices();
                }
            }
        }).start();
    }

    private AdvInfoAnalysisImpl advInfoAnalysisImpl;

    @Override
    public void onScanDevice(DeviceInfo deviceInfo) {
        AdvInfo advInfo = advInfoAnalysisImpl.parseDeviceInfo(deviceInfo);
        if (advInfo == null) return;
        advInfoHashMap.put(advInfo.mac, advInfo);
    }

    @Override
    public void onStopScan() {
        findViewById(R.id.iv_refresh).clearAnimation();
        animation = null;
    }

    private void updateDevices() {
        advInfoList.clear();
        if (!TextUtils.isEmpty(filterName)
                || !TextUtils.isEmpty(filterMac)
                || filterRssi != -100) {
            ArrayList<AdvInfo> advInfoListFilter = new ArrayList<>(advInfoHashMap.values());
            Iterator<AdvInfo> iterator = advInfoListFilter.iterator();
            while (iterator.hasNext()) {
                AdvInfo advInfo = iterator.next();
                if (advInfo.rssi > filterRssi) {
                    if (TextUtils.isEmpty(filterName) && TextUtils.isEmpty(filterMac)) {
                        continue;
                    } else {
                        if (!TextUtils.isEmpty(filterMac) && TextUtils.isEmpty(advInfo.mac)) {
                            iterator.remove();
                        } else if (!TextUtils.isEmpty(filterMac) && advInfo.mac.toLowerCase().replaceAll(":", "").contains(filterMac.toLowerCase())) {
                            continue;
                        } else if (!TextUtils.isEmpty(filterName) && TextUtils.isEmpty(advInfo.name)) {
                            iterator.remove();
                        } else if (!TextUtils.isEmpty(filterName) && advInfo.name.toLowerCase().contains(filterName.toLowerCase())) {
                            continue;
                        } else {
                            iterator.remove();
                        }
                    }
                } else {
                    iterator.remove();
                }
            }
            advInfoList.addAll(advInfoListFilter);
        } else {
            advInfoList.addAll(advInfoHashMap.values());
        }
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true");
        Collections.sort(advInfoList, new Comparator<AdvInfo>() {
            @Override
            public int compare(AdvInfo lhs, AdvInfo rhs) {
                if (lhs.rssi > rhs.rssi) {
                    return -1;
                } else if (lhs.rssi < rhs.rssi) {
                    return 1;
                }
                return 0;
            }
        });
    }

    private Animation animation = null;
    public String filterName;
    public String filterMac;
    public int filterRssi = -100;

    private void startScan() {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        animation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
        findViewById(R.id.iv_refresh).startAnimation(animation);
        advInfoAnalysisImpl = new AdvInfoAnalysisImpl();
        mokoBleScanner.startScanDevice(this);
    }


    private LoadingDialog mLoadingDialog;

    private void showLoadingProgressDialog() {
        mLoadingDialog = new LoadingDialog();
        mLoadingDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingProgressDialog() {
        if (mLoadingDialog != null)
            mLoadingDialog.dismissAllowingStateLoss();
    }

    private LoadingMessageDialog mLoadingMessageDialog;

    private void showLoadingMessageDialog() {
        mLoadingMessageDialog = new LoadingMessageDialog();
        mLoadingMessageDialog.setMessage("Verifying..");
        mLoadingMessageDialog.show(getSupportFragmentManager());

    }

    private void dismissLoadingMessageDialog() {
        if (mLoadingMessageDialog != null)
            mLoadingMessageDialog.dismissAllowingStateLoss();
    }

    @Override
    public void onBackPressed() {
        back();
    }

    private String mPassword;
    private String mSavedPassword;
    private String mSelectedDeviceMac;

    @Override
    public void onItemChildClick(BaseQuickAdapter adapter, View view, int position) {
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        final AdvInfo advInfo = (AdvInfo) adapter.getItem(position);
        if (advInfo != null && !isFinishing()) {
            if (animation != null) {
                mHandler.removeMessages(0);
                mokoBleScanner.stopScanDevice();
            }
            mSelectedDeviceMac = advInfo.mac;
            showLoadingProgressDialog();
            mBind.ivRefresh.postDelayed(() -> MokoSupport.getInstance().connDevice(advInfo.mac), 500);
        }
    }

    private void showPasswordDialog() {
        // show password
        final PasswordDialog dialog = new PasswordDialog();
        dialog.setPassword(mSavedPassword);
        dialog.setOnPasswordClicked(new PasswordDialog.PasswordClickListener() {
            @Override
            public void onEnsureClicked(String password) {
                if (!MokoSupport.getInstance().isBluetoothOpen()) {
                    MokoSupport.getInstance().enableBluetooth();
                    return;
                }
                XLog.i(password);
                mPassword = password;
                showLoadingProgressDialog();
                mBind.ivRefresh.postDelayed(() -> MokoSupport.getInstance().connDevice(mSelectedDeviceMac), 500);
            }

            @Override
            public void onDismiss() {
                if (animation == null)
                    startScan();
            }
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onBack(View view) {
        if (isWindowLocked())
            return;
        back();
    }

    public void onAbout(View view) {
        if (isWindowLocked())
            return;
        startActivity(new Intent(this, AboutActivity.class));
    }

    public void onFilter(View view) {
        if (isWindowLocked())
            return;
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        ScanFilterDialog scanFilterDialog = new ScanFilterDialog(this);
        scanFilterDialog.setFilterName(filterName);
        scanFilterDialog.setFilterMac(filterMac);
        scanFilterDialog.setFilterRssi(filterRssi);
        scanFilterDialog.setOnScanFilterListener((filterName, filterMac, filterRssi) -> {
            TagMainActivity.this.filterName = filterName;
            TagMainActivity.this.filterMac = filterMac;
            String showFilterMac = "";
            if (filterMac.length() == 12) {
                StringBuffer stringBuffer = new StringBuffer(filterMac);
                stringBuffer.insert(2, ":");
                stringBuffer.insert(5, ":");
                stringBuffer.insert(8, ":");
                stringBuffer.insert(11, ":");
                stringBuffer.insert(14, ":");
                showFilterMac = stringBuffer.toString();
            } else {
                showFilterMac = filterMac;
            }
            TagMainActivity.this.filterRssi = filterRssi;
            if (!TextUtils.isEmpty(filterName)
                    || !TextUtils.isEmpty(showFilterMac)
                    || filterRssi != -100) {
                mBind.rlFilter.setVisibility(View.VISIBLE);
                mBind.rlEditFilter.setVisibility(View.GONE);
                StringBuilder stringBuilder = new StringBuilder();
                if (!TextUtils.isEmpty(filterName)) {
                    stringBuilder.append(filterName);
                    stringBuilder.append(";");
                }
                if (!TextUtils.isEmpty(showFilterMac)) {
                    stringBuilder.append(showFilterMac);
                    stringBuilder.append(";");
                }
                if (filterRssi != -100) {
                    stringBuilder.append(String.format("%sdBm", filterRssi + ""));
                    stringBuilder.append(";");
                }
                mBind.tvFilter.setText(stringBuilder.toString());
            } else {
                mBind.rlFilter.setVisibility(View.GONE);
                mBind.rlEditFilter.setVisibility(View.VISIBLE);
            }
            if (isWindowLocked())
                return;
            if (animation == null) {
                startScan();
            }
        });
        scanFilterDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (isWindowLocked())
                    return;
                if (animation == null) {
                    startScan();
                }
            }
        });
        scanFilterDialog.show();
    }

    private void back() {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        if (BuildConfig.IS_LIBRARY) {
            finish();
        } else {
            AlertMessageDialog dialog = new AlertMessageDialog();
            dialog.setMessage(R.string.main_exit_tips);
            dialog.setOnAlertConfirmListener(() -> TagMainActivity.this.finish());
            dialog.show(getSupportFragmentManager());
        }
    }

    public void onRefresh(View view) {
        if (isWindowLocked())
            return;
        if (!MokoSupport.getInstance().isBluetoothOpen()) {
            // 蓝牙未打开，开启蓝牙
            MokoSupport.getInstance().enableBluetooth();
            return;
        }
        if (animation == null) {
            startScan();
        } else {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
    }

    public void onFilterDelete(View view) {
        if (animation != null) {
            mHandler.removeMessages(0);
            mokoBleScanner.stopScanDevice();
        }
        mBind.rlFilter.setVisibility(View.GONE);
        mBind.rlEditFilter.setVisibility(View.VISIBLE);
        filterName = "";
        filterMac = "";
        filterRssi = -100;
        if (isWindowLocked())
            return;
        if (animation == null) {
            startScan();
        }
    }
}
