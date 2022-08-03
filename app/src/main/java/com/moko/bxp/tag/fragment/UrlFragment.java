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
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.moko.ble.lib.task.OrderTask;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.able.ISlotDataAction;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.dialog.UrlSchemeDialog;
import com.moko.bxp.tag.entity.SlotFrameTypeEnum;
import com.moko.bxp.tag.utils.ToastUtils;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;
import com.moko.support.entity.TxPowerEnum;
import com.moko.support.entity.UrlExpansionEnum;
import com.moko.support.entity.UrlSchemeEnum;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class UrlFragment extends Fragment implements SeekBar.OnSeekBarChangeListener, ISlotDataAction {

    private static final String TAG = "UrlFragment";
    private final String FILTER_ASCII = "[!-~]*";
    @BindView(R.id.et_url)
    EditText etUrl;
    @BindView(R.id.sb_rssi)
    SeekBar sbRssi;
    @BindView(R.id.sb_tx_power)
    SeekBar sbTxPower;
    @BindView(R.id.tv_url_scheme)
    TextView tvUrlScheme;
    @BindView(R.id.tv_adv_tx_power)
    TextView tvRssi;
    @BindView(R.id.tv_tx_power)
    TextView tvTxPower;
    @BindView(R.id.et_adv_interval)
    EditText etAdvInterval;
    @BindView(R.id.et_adv_duration)
    EditText etAdvDuration;
    @BindView(R.id.et_standby_duration)
    EditText etStandbyDuration;


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
        View view = inflater.inflate(R.layout.fragment_url, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        sbRssi.setOnSeekBarChangeListener(this);
        sbTxPower.setOnSeekBarChangeListener(this);
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etUrl.setFilters(new InputFilter[]{new InputFilter.LengthFilter(32), filter});
        setDefault();
        return view;
    }

    private void setDefault() {
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.NO_DATA) {
            etAdvInterval.setText("10");
            etAdvDuration.setText("10");
            etStandbyDuration.setText("0");
            sbRssi.setProgress(100);
            sbTxPower.setProgress(6);
        } else {
            etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.TLM) {
                sbRssi.setProgress(100);
                mRssi = 0;
                tvRssi.setText(String.format("%ddBm", mRssi));
            } else {
                int advTxPowerProgress = activity.slotData.rssi_0m + 100;
                sbRssi.setProgress(advTxPowerProgress);
                mRssi = activity.slotData.rssi_0m;
                tvRssi.setText(String.format("%ddBm", mRssi));
            }

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);
            mTxPower = activity.slotData.txPower;
            tvTxPower.setText(String.format("%ddBm", mTxPower));
        }
        mUrlScheme = UrlSchemeEnum.HTTP_WWW.getUrlType();
        tvUrlScheme.setText(UrlSchemeEnum.HTTP_WWW.getUrlDesc());
        if (activity.slotData.frameTypeEnum == SlotFrameTypeEnum.URL) {
            mUrlScheme = activity.slotData.urlSchemeEnum.getUrlType();
            tvUrlScheme.setText(activity.slotData.urlSchemeEnum.getUrlDesc());
            String url = activity.slotData.urlContentHex;
            String urlExpansionStr = url.substring(url.length() - 2);
            int urlExpansionType = Integer.parseInt(urlExpansionStr, 16);
            UrlExpansionEnum urlEnum = UrlExpansionEnum.fromUrlExpanType(urlExpansionType);
            if (urlEnum == null) {
                etUrl.setText(MokoUtils.hex2String(url));
            } else {
                etUrl.setText(MokoUtils.hex2String(url.substring(0, url.length() - 2)) + urlEnum.getUrlExpanDesc());
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
            tvRssi.setText(String.format("%ddBm", rssi));
            mRssi = rssi;
        } else if (viewId == R.id.sb_tx_power) {
            TxPowerEnum txPowerEnum = TxPowerEnum.fromOrdinal(progress);
            int txPower = txPowerEnum.getTxPower();
            tvTxPower.setText(String.format("%ddBm", txPower));
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
        String urlContent = etUrl.getText().toString();
        String advInterval = etAdvInterval.getText().toString();
        String advDuration = etAdvDuration.getText().toString();
        String standbyDuration = etStandbyDuration.getText().toString();
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
            etAdvInterval.setText(String.valueOf(activity.slotData.advInterval));
            etAdvDuration.setText(String.valueOf(activity.slotData.advDuration));
            etStandbyDuration.setText(String.valueOf(activity.slotData.standbyDuration));

            int rssiProgress = activity.slotData.rssi_0m + 100;
            sbRssi.setProgress(rssiProgress);

            int txPowerProgress = TxPowerEnum.fromTxPower(activity.slotData.txPower).ordinal();
            sbTxPower.setProgress(txPowerProgress);

            mUrlScheme = activity.slotData.urlSchemeEnum.getUrlType();
            tvUrlScheme.setText(activity.slotData.urlSchemeEnum.getUrlDesc());
            String url = activity.slotData.urlContentHex;
            String urlExpansionStr = url.substring(url.length() - 2);
            int urlExpansionType = Integer.parseInt(urlExpansionStr, 16);
            UrlExpansionEnum urlEnum = UrlExpansionEnum.fromUrlExpanType(urlExpansionType);
            if (urlEnum == null) {
                etUrl.setText(MokoUtils.hex2String(url));
            } else {
                etUrl.setText(MokoUtils.hex2String(url.substring(0, url.length() - 2)) + urlEnum.getUrlExpanDesc());
            }
            etUrl.setSelection(etUrl.getText().toString().length());
        } else {
            etAdvInterval.setText("10");
            etAdvDuration.setText("10");
            etStandbyDuration.setText("0");
            sbRssi.setProgress(100);
            sbTxPower.setProgress(6);

            etUrl.setText("");
        }
    }

    public void selectUrlScheme() {
        UrlSchemeDialog dialog = new UrlSchemeDialog(getActivity());
        dialog.setData(tvUrlScheme.getText().toString());
        dialog.setUrlSchemeClickListener(urlType -> {
            UrlSchemeEnum urlSchemeEnum = UrlSchemeEnum.fromUrlType(Integer.valueOf(urlType));
            tvUrlScheme.setText(urlSchemeEnum.getUrlDesc());
            mUrlScheme = Integer.valueOf(urlType);
        });
        dialog.show();
    }
}
