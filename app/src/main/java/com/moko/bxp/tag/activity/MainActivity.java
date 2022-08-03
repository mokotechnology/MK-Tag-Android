package com.moko.bxp.tag.activity;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.adapter.DeviceListAdapter;
import com.moko.bxp.tag.dialog.AlertMessageDialog;
import com.moko.bxp.tag.dialog.LoadingDialog;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.dialog.PasswordDialog;
import com.moko.bxp.tag.dialog.ScanFilterDialog;
import com.moko.bxp.tag.entity.AdvInfo;
import com.moko.bxp.tag.utils.AdvInfoAnalysisImpl;
import com.moko.bxp.tag.utils.SPUtiles;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoBleScanner;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.callback.MokoScanDeviceCallback;
import com.moko.support.entity.DeviceInfo;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;


public class MainActivity extends BaseActivity implements MokoScanDeviceCallback, BaseQuickAdapter.OnItemChildClickListener {

    @BindView(R.id.iv_refresh)
    ImageView ivRefresh;
    @BindView(R.id.rv_devices)
    RecyclerView rvDevices;
    @BindView(R.id.tv_device_num)
    TextView tvDeviceNum;
    @BindView(R.id.rl_edit_filter)
    RelativeLayout rl_edit_filter;
    @BindView(R.id.rl_filter)
    RelativeLayout rl_filter;
    @BindView(R.id.tv_filter)
    TextView tv_filter;
    private boolean mReceiverTag = false;
    private ConcurrentHashMap<String, AdvInfo> advInfoHashMap;
    private ArrayList<AdvInfo> advInfoList;
    private DeviceListAdapter adapter;
    private MokoBleScanner mokoBleScanner;
    private Handler mHandler;
    private boolean isPasswordError;
    private boolean isVerifyPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        MokoSupport.getInstance().init(getApplicationContext());
        advInfoHashMap = new ConcurrentHashMap<>();
        advInfoList = new ArrayList<>();
        adapter = new DeviceListAdapter();
        adapter.replaceData(advInfoList);
        adapter.setOnItemChildClickListener(this);
        adapter.openLoadAnimation();
        rvDevices.setLayoutManager(new LinearLayoutManager(this));
        DividerItemDecoration itemDecoration = new DividerItemDecoration(this, DividerItemDecoration.VERTICAL);
        itemDecoration.setDrawable(ContextCompat.getDrawable(this, R.drawable.shape_recycleview_divider));
        rvDevices.addItemDecoration(itemDecoration);
        rvDevices.setAdapter(adapter);

        mHandler = new Handler(Looper.getMainLooper());
        mokoBleScanner = new MokoBleScanner(this);
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
                    ToastUtils.showToast(MainActivity.this, "Connection failed");
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
                            tvDeviceNum.setText(String.format("DEVICE(%d)", advInfoList.size()));
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
        if (advInfo == null)
            return;
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
            ivRefresh.postDelayed(() -> MokoSupport.getInstance().connDevice(advInfo.mac), 500);
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
                ivRefresh.postDelayed(() -> MokoSupport.getInstance().connDevice(mSelectedDeviceMac), 500);
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
            MainActivity.this.filterName = filterName;
            MainActivity.this.filterMac = filterMac;
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
            MainActivity.this.filterRssi = filterRssi;
            if (!TextUtils.isEmpty(filterName)
                    || !TextUtils.isEmpty(showFilterMac)
                    || filterRssi != -100) {
                rl_filter.setVisibility(View.VISIBLE);
                rl_edit_filter.setVisibility(View.GONE);
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
                tv_filter.setText(stringBuilder.toString());
            } else {
                rl_filter.setVisibility(View.GONE);
                rl_edit_filter.setVisibility(View.VISIBLE);
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
        AlertMessageDialog dialog = new AlertMessageDialog();
        dialog.setMessage(R.string.main_exit_tips);
        dialog.setOnAlertConfirmListener(() -> MainActivity.this.finish());
        dialog.show(getSupportFragmentManager());
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
        rl_filter.setVisibility(View.GONE);
        rl_edit_filter.setVisibility(View.VISIBLE);
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
