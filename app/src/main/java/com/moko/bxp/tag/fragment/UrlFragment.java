package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.able.ISlotDataAction;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.databinding.FragmentUrlTagBinding;
import com.moko.bxp.tag.dialog.UrlSchemeDialog;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.tag.MokoSupport;
import com.moko.support.tag.OrderTaskAssembler;
import com.moko.support.tag.entity.TxPowerEnum;
import com.moko.support.tag.entity.UrlExpansionEnum;
import com.moko.support.tag.entity.UrlSchemeEnum;

import java.util.ArrayList;

public class UrlFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UrlFragment";
    private final String FILTER_ASCII = "[!-~]*";
    private FragmentUrlTagBinding mBind;

    private SlotDataActivity activity;

    public UrlFragment() {
    }

    public static UrlFragment newInstance() {
        UrlFragment fragment = new UrlFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate: ");
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.i(TAG, "onCreateView: ");
        mBind = FragmentUrlTagBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        mBind.sbRssi.setOnSeekBarChangeListener(this);
        mBind.sbTxPower.setOnSeekBarChangeListener(this);
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        mBind.etUrl.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32), filter});
        setDefault();
        return mBind.getRoot();
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);
        } else {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                mBind.sbRssi.setProgress(100);
                mRssi = 0;
                mBind.tvAdvTxPower.setText(String.format("%ddBm", mRssi));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                mBind.sbRssi.setProgress(advTxPowerProgress);
                mRssi = activity.slotData.rssi_0m;
                mBind.tvAdvTxPower.setText(String.format("%ddBm", mRssi));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);
            mTxPower = activity.slotData.txPower;
            mBind.tvTxPower.setText(String.format("%ddBm", mTxPower));
        }
        mUrlScheme = UrlSchemeEnum.HTTP_WWW.getUrlType();
        mBind.tvUrlScheme.setText(UrlSchemeEnum.HTTP_WWW.getUrlDesc());
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.URL) {
            mUrlScheme = activity.slotData.urlSchemeEnum.getUrlType();
            mBind.tvUrlScheme.setText(activity.slotData.urlSchemeEnum.getUrlDesc());
            String url = activity.slotData.urlContentHex;
            String urlExpansionStr = url.substring(url.length() - 2);
            int urlExpansionType = Integer.parseInt(urlExpansionStr, 16);
            UrlExpansionEnum urlEnum = UrlExpansionEnum.fromUrlExpanType(urlExpansionType);
            if (urlEnum == null) {
                mBind.etUrl.setText(MokoUtils.hex2String(url));
            } else {
                mBind.etUrl.setText(MokoUtils.hex2String(url.substring(0, url.length() - 2)) + urlEnum.getUrlExpanDesc());
            }
        }

    }

    @Override
    public void onResume() {
        Log.i(TAG, "onResume: ");
        super.onResume();
    }

    @Override
    public void onPause() {
        Log.i(TAG, "onPause: ");
        super.onPause();
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "onDestroy: ");
        super.onDestroy();
    }

    private int mAdvInterval;
    private int mAdvDuration;
    private int mStandbyDuration;
    private int mRssi;
    private int mTxPower;
    private int mUrlScheme;
    private String mUrlContent;

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        updateData(seekBar.getId(), progress);
    }

    public void updateData(int viewId, int progress) {
        if (viewId == R.id.sb_rssi) {
            int rssi = progress - 100;
            mBind.tvAdvTxPower.setText(String.format("%ddBm", rssi));
            mRssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            int txPower = txPowerEnum.getTxPower();
            mBind.tvTxPower.setText(String.format("%ddBm", txPower));
            mTxPower = txPower;
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public boolean isValid() {
        String urlContent = mBind.etUrl.getText().toString();
        String advInterval = mBind.etAdvInterval.getText().toString();
        String advDuration = mBind.etAdvDuration.getText().toString();
        String standbyDuration = mBind.etStandbyDuration.getText().toString();
        if (TextUtils.isEmpty(urlContent)) {
            ToastUtils.showToast(activity, "Data format incorrect!");
            return false;
        }
        int advIntervalInt = Integer.parseInt(advInterval);
        if (advIntervalInt < 1 || advIntervalInt > 100) {
            ToastUtils.showToast(activity, "The Adv interval range is 1~100");
            return false;
        }
        if (TextUtils.isEmpty(advDuration)) {
            ToastUtils.showToast(activity, "The Adv duration can not be empty.");
            return false;
        }
        int advDurationInt = Integer.parseInt(advDuration);
        if (advDurationInt < 1 || advDurationInt > 65535) {
            ToastUtils.showToast(activity, "The Adv duration range is 1~65535");
            return false;
        }
        if (TextUtils.isEmpty(standbyDuration)) {
            ToastUtils.showToast(activity, "The Standby duration can not be empty.");
            return false;
        }
        int standbyDurationInt = Integer.parseInt(standbyDuration);
        if (standbyDurationInt > 65535) {
            ToastUtils.showToast(activity, "The Standby duration range is 0~65535");
            return false;
        }
        if (urlContent.indexOf(".") >= 0) {
            String urlExpansion = urlContent.substring(urlContent.lastIndexOf("."));
            UrlExpansionEnum urlExpansionEnum = UrlExpansionEnum.fromUrlExpanDesc(urlExpansion);
            if (urlExpansionEnum == null) {
                // url中有点，但不符合eddystone结尾格式，内容长度不能超过17个字符
                if (urlContent.length() < 2 || urlContent.length() > 17) {
                    ToastUtils.showToast(activity, "Data format incorrect!");
                    return false;
                }
            } else {
                String content = urlContent.substring(0, urlContent.lastIndexOf("."));
                if (content.length() < 1 || content.length() > 16) {
                    ToastUtils.showToast(activity, "Data format incorrect!");
                    return false;
                }
            }
        } else {
            // url中没有有点，内容长度不能超过17个字符
            if (urlContent.length() < 2 || urlContent.length() > 17) {
                ToastUtils.showToast(activity, "Data format incorrect!");
                return false;
            }
        }
        mUrlContent = urlContent;
        mAdvInterval = advIntervalInt;
        mAdvDuration = advDurationInt;
        mStandbyDuration = standbyDurationInt;
        return true;
    }

    @Override
    public void sendData() {
        ArrayList<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setSlotAdvParams(activity.slotData.slotEnum.ordinal(),
                mAdvInterval, mAdvDuration, mStandbyDuration, mRssi, mTxPower));
        orderTasks.add(OrderTaskAssembler.setSlotParamsURL(activity.slotData.slotEnum.ordinal(),
                mUrlScheme, mUrlContent));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    @Override
    public void resetParams() {
        if (activity.slotData.frameTypeEnum == activity.currentFrameTypeEnum) {
            mBind.etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            mBind.etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            mBind.etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            int rssiProgress = activity.slotData.rssi_0m + 100;
            mBind.sbRssi.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            mBind.sbTxPower.setProgress(txPowerProgress);

            mUrlScheme = activity.slotData.urlSchemeEnum.getUrlType();
            mBind.tvUrlScheme.setText(activity.slotData.urlSchemeEnum.getUrlDesc());
            String url = activity.slotData.urlContentHex;
            String urlExpansionStr = url.substring(url.length() - 2);
            int urlExpansionType = Integer.parseInt(urlExpansionStr, 16);
            UrlExpansionEnum urlEnum = UrlExpansionEnum.fromUrlExpanType(urlExpansionType);
            if (urlEnum == null) {
                mBind.etUrl.setText(MokoUtils.hex2String(url));
            } else {
                mBind.etUrl.setText(MokoUtils.hex2String(url.substring(0, url.length() - 2)) + urlEnum.getUrlExpanDesc());
            }
            mBind.etUrl.setSelection(mBind.etUrl.getText().toString().length());
        } else {
            mBind.etAdvInterval.setText("10");
            mBind.etAdvDuration.setText("10");
            mBind.etStandbyDuration.setText("0");
            mBind.sbRssi.setProgress(100);
            mBind.sbTxPower.setProgress(5);

            mBind.etUrl.setText("");
        }
    }

    public void selectUrlScheme() {
        UrlSchemeDialog dialog = new UrlSchemeDialog(activity);
        dialog.setUrlScheme(mBind.tvUrlScheme.getText().toString());
        dialog.setUrlSchemeClickListener(urlType -> {
            UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.valueOf(urlType));
            mBind.tvUrlScheme.setText(urlSchemeEnum.getUrlDesc());
            mUrlScheme = Integer.valueOf(urlType);
        });
        dialog.show();
    }
}
