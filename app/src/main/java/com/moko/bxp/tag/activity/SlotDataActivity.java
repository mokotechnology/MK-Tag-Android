package com.moko.bxp.tag.activity;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoConstants;
import com.moko.ble.lib.event.ConnectStatusEvent;
import com.moko.ble.lib.event.OrderTaskResponseEvent;
import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.task.OrderTaskResponse;
import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.able.ISlotDataAction;
import com.moko.bxp.tag.dialog.BottomDialog;
import com.moko.bxp.tag.dialog.LoadingMessageDialog;
import com.moko.bxp.tag.entity.SlotData;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.fragment.IBeaconFragment;
import com.moko.bxp.tag.fragment.TagInfoFragment;
import com.moko.bxp.tag.fragment.TlmFragment;
import com.moko.bxp.tag.fragment.TriggerMagneticFragment;
import com.moko.bxp.tag.fragment.TriggerMotionFragment;
import com.moko.bxp.tag.fragment.UidFragment;
import com.moko.bxp.tag.fragment.UrlFragment;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.OrderCHAR;
import com.moko.support.entity.ParamsKeyEnum;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.carbswang.android.numberpickerview.library.NumberPickerView;

public class SlotDataActivity extends BaseActivity implements NumberPickerView.OnValueChangeListener {
    private static final int TRIGGER_TYPE_NULL = 0;
    private static final int TRIGGER_TYPE_MOTION = 5;
    private static final int TRIGGER_TYPE_MAGNETIC = 6;

    @BindView(R.id.tv_slot_title)
    TextView tvSlotTitle;
    @BindView(R.id.iv_save)
    ImageView ivSave;
    @BindView(R.id.frame_slot_container)
    FrameLayout frameSlotContainer;
    @BindView(R.id.npv_slot_type)
    NumberPickerView npvSlotType;
    @BindView(R.id.iv_trigger)
    ImageView ivTrigger;
    @BindView(R.id.tv_trigger_type)
    TextView tvTriggerType;
    @BindView(R.id.frame_trigger_container)
    FrameLayout frameTriggerContainer;
    @BindView(R.id.rl_trigger)
    RelativeLayout rlTrigger;
    @BindView(R.id.rl_trigger_switch)
    RelativeLayout rlTriggerSwitch;
    private FragmentManager fragmentManager;
    private UidFragment uidFragment;
    private UrlFragment urlFragment;
    private TlmFragment tlmFragment;
    private IBeaconFragment iBeaconFragment;
    private TagInfoFragment tagInfoFragment;
    public SlotData slotData;
    private ISlotDataAction slotDataActionImpl;
    private TriggerMagneticFragment magneticFragment;
    private TriggerMotionFragment motionFragment;
    private int triggerType;
    private String[] slotTypeArray;
    private ArrayList<String> triggerTypes;
    private int triggerTypeSelected;
    public SlotFrameTypeEnum currentFrameTypeEnum;
    public boolean isConfigError;
    private boolean isTriggerEnable;
    private boolean isSupportAcc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slot_data);
        ButterKnife.bind(this);
        if (getIntent() != null && getIntent().getExtras() != null) {
            slotData = (SlotData) getIntent().getSerializableExtra(AppConstants.EXTRA_KEY_SLOT_DATA);
            isSupportAcc = getIntent().getBooleanExtra(AppConstants.EXTRA_KEY_SUPPORT_ACC, false);
            currentFrameTypeEnum = slotData.frameTypeEnum;
            triggerType = slotData.triggerType;
            XLog.i(slotData.toString());
        }
        fragmentManager = getFragmentManager();
        createFragments();
        triggerTypes = new ArrayList<>();
        slotTypeArray = getResources().getStringArray(R.array.slot_type);
        npvSlotType.setDisplayedValues(slotTypeArray);
        triggerTypes.add("Magnetic detection");
        triggerTypes.add("Motion detection");
        final int length = slotTypeArray.length;
        npvSlotType.setMinValue(0);
        npvSlotType.setMaxValue(5);
        npvSlotType.setOnValueChangedListener(this);
        for (int i = 0; i < length; i++) {
            if (slotData.frameTypeEnum.getShowName().equals(slotTypeArray[i])) {
                npvSlotType.setValue(i);
                showFragment(i);
                break;
            }
        }
        tvSlotTitle.setText(slotData.slotEnum.getTitle());
        if (slotData.frameTypeEnum != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
        if (triggerType > 0) {
            isTriggerEnable = true;
            ivTrigger.setImageResource(R.drawable.ic_checked);
            rlTrigger.setVisibility(View.VISIBLE);
        } else {
            isTriggerEnable = false;
            ivTrigger.setImageResource(R.drawable.ic_unchecked);
            rlTrigger.setVisibility(View.GONE);
        }
        createTriggerFragments();
        showTriggerFragment();
        setTriggerData();
        EventBus.getDefault().register(this);
    }

    private void setTriggerData() {
        switch (triggerType) {
            case TRIGGER_TYPE_MAGNETIC:
                triggerTypeSelected = 0;
                break;
            case TRIGGER_TYPE_MOTION:
                triggerTypeSelected = 1;
                break;

        }
        tvTriggerType.setText(triggerTypes.get(triggerTypeSelected));
    }

    private void showTriggerFragment() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (triggerType) {
            case TRIGGER_TYPE_MAGNETIC:
                fragmentTransaction.show(magneticFragment).hide(motionFragment).commit();
                break;
            case TRIGGER_TYPE_MOTION:
                fragmentTransaction.hide(magneticFragment).show(motionFragment).commit();
                break;
        }
    }

    private void createTriggerFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        motionFragment = TriggerMotionFragment.newInstance();
        magneticFragment = TriggerMagneticFragment.newInstance();
        fragmentTransaction.add(R.id.frame_trigger_container, motionFragment)
                .add(R.id.frame_trigger_container, magneticFragment)
                .show(magneticFragment).hide(motionFragment).commit();
    }

    private void createFragments() {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        uidFragment = UidFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, uidFragment);
        urlFragment = UrlFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, urlFragment);
        tlmFragment = TlmFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, tlmFragment);
        iBeaconFragment = IBeaconFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, iBeaconFragment);
        tagInfoFragment = TagInfoFragment.newInstance();
        fragmentTransaction.add(R.id.frame_slot_container, tagInfoFragment);
        fragmentTransaction.commit();

    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onConnectStatusEvent(ConnectStatusEvent event) {
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_DISCONNECTED.equals(action)) {
                // 设备断开，通知页面更新
                SlotDataActivity.this.finish();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.POSTING, priority = 200)
    public void onOrderTaskResponseEvent(OrderTaskResponseEvent event) {
        EventBus.getDefault().cancelEventDelivery(event);
        final String action = event.getAction();
        runOnUiThread(() -> {
            if (MokoConstants.ACTION_ORDER_TIMEOUT.equals(action)) {
            }
            if (MokoConstants.ACTION_ORDER_FINISH.equals(action)) {
                ToastUtils.showToast(SlotDataActivity.this, isConfigError ? "Error" : "Successfully configure");
                isConfigError = false;
                dismissSyncProgressDialog();
                SlotDataActivity.this.setResult(SlotDataActivity.this.RESULT_OK);
                SlotDataActivity.this.finish();
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
                            if (flag == 0x00)
                                return;
                            if (length > 0) {
                                int result = value[4] & 0xFF;
                                if (result == 0x00) {
                                    isConfigError = true;
                                }
                            }
                        }
                        break;
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

    @Override
    public void onValueChange(NumberPickerView picker, int oldVal, int newVal) {
        XLog.i(newVal + "");
        XLog.i(picker.getContentByCurrValue());
        showFragment(newVal);
        if (slotDataActionImpl != null) {
            slotDataActionImpl.resetParams();
        }
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromShowName(slotTypeArray[newVal]);
        if (slotFrameTypeEnum != SlotFrameTypeEnum.NO_DATA) {
            rlTriggerSwitch.setVisibility(View.VISIBLE);
        } else {
            rlTriggerSwitch.setVisibility(View.GONE);
        }
    }

    private void showFragment(int index) {
        SlotFrameTypeEnum slotFrameTypeEnum = SlotFrameTypeEnum.fromShowName(slotTypeArray[index]);
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        switch (slotFrameTypeEnum) {
            case TLM:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(iBeaconFragment).hide(tagInfoFragment).show(tlmFragment).commit();
                slotDataActionImpl = tlmFragment;
                break;
            case UID:
                fragmentTransaction.hide(urlFragment).hide(iBeaconFragment).hide(tlmFragment).hide(tagInfoFragment).show(uidFragment).commit();
                slotDataActionImpl = uidFragment;
                break;
            case URL:
                fragmentTransaction.hide(uidFragment).hide(iBeaconFragment).hide(tlmFragment).hide(tagInfoFragment).show(urlFragment).commit();
                slotDataActionImpl = urlFragment;
                break;
            case IBEACON:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(tagInfoFragment).show(iBeaconFragment).commit();
                slotDataActionImpl = iBeaconFragment;
                break;
            case TAG:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).show(tagInfoFragment).commit();
                slotDataActionImpl = tagInfoFragment;
                break;
            case NO_DATA:
                fragmentTransaction.hide(uidFragment).hide(urlFragment).hide(tlmFragment).hide(iBeaconFragment).hide(tagInfoFragment).commit();
                slotDataActionImpl = null;
                break;

        }
        slotData.frameTypeEnum = slotFrameTypeEnum;
    }

    public void onTrigger(View view) {
        if (isWindowLocked())
            return;
        isTriggerEnable = !isTriggerEnable;
        if (isTriggerEnable) {
            ivTrigger.setImageResource(R.drawable.ic_unchecked);
            rlTrigger.setVisibility(View.GONE);
        } else {
            ivTrigger.setImageResource(R.drawable.ic_checked);
            rlTrigger.setVisibility(View.VISIBLE);
        }
    }

    public void onBack(View view) {
        finish();
    }

    public void onSave(View view) {
        if (isWindowLocked())
            return;
        OrderTask orderTask = null;
        // 发送触发条件
        switch (triggerType) {
            case TRIGGER_TYPE_NULL:
                orderTask = OrderTaskAssembler.setTriggerClose(slotData.slotEnum.ordinal());
                break;
            case TRIGGER_TYPE_MOTION:
                if (motionFragment.getDuration() < 0 || motionFragment.getStaticDuration() < 0) {
                    return;
                }
                orderTask = OrderTaskAssembler.setSlotTriggerMotionParams(slotData.slotEnum.ordinal(),
                        motionFragment.getTriggerAdvStatus(), motionFragment.getDuration(), motionFragment.getStaticDuration());
                break;
            case TRIGGER_TYPE_MAGNETIC:
                if (magneticFragment.getDuration() < 0) {
                    return;
                }
                orderTask = OrderTaskAssembler.setSlotTriggerMagneticParams(slotData.slotEnum.ordinal(),
                        magneticFragment.getTriggerAdvStatus(), magneticFragment.getDuration());
                break;
        }
        if (slotDataActionImpl == null) {
            ArrayList<OrderTask> orderTasks = new ArrayList<>();
            orderTasks.add(OrderTaskAssembler.setSlotParamsNoData(slotData.slotEnum.ordinal()));
            orderTasks.add(orderTask);
            MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
            return;
        }
        if (!slotDataActionImpl.isValid()) {
            return;
        }
        showSyncingProgressDialog();
        slotDataActionImpl.sendData();
        if (orderTask != null) {
            MokoSupport.getInstance().sendOrder(orderTask);
        }
    }

    public void onTriggerType(View view) {
        if (isWindowLocked())
            return;
        if (!isSupportAcc)
            return;
        // 选择触发条件
        BottomDialog dialog = new BottomDialog();
        dialog.setDatas(triggerTypes, triggerTypeSelected);
        dialog.setListener(value -> {
            triggerTypeSelected = value;
            switch (triggerTypeSelected) {
                case 0:
                    triggerType = TRIGGER_TYPE_MAGNETIC;
                    break;
                case 1:
                    triggerType = TRIGGER_TYPE_MOTION;
                    break;
            }
            showTriggerFragment();
            tvTriggerType.setText(triggerTypes.get(value));
        });
        dialog.show(getSupportFragmentManager());
    }

    public void onSelectUrlScheme(View view) {
        if (isWindowLocked())
            return;
        if (urlFragment != null) {
            urlFragment.selectUrlScheme();
        }
    }

    public void onMotionStart(View view) {
        if (isWindowLocked())
            return;
        if (motionFragment != null)
            motionFragment.motionStart();
    }

    public void onMotionStop(View view) {
        if (isWindowLocked())
            return;
        if (motionFragment != null)
            motionFragment.motionStop();
    }

    public void onMagneticStart(View view) {
        if (isWindowLocked())
            return;
        if (magneticFragment != null)
            magneticFragment.magneticStart();
    }

    public void onMagneticStop(View view) {
        if (isWindowLocked())
            return;
        if (magneticFragment != null)
            magneticFragment.magneticStop();
    }
}
