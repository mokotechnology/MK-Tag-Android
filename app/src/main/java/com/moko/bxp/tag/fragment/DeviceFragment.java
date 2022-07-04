package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.InputFilter;
import android.text.Spanned;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.tag.R;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceFragment extends Fragment {
    private final String FILTER_ASCII = "[ -~]*";

    @BindView(R.id.et_device_name)
    EditText etDeviceName;
    @BindView(R.id.rl_reset_factory)
    RelativeLayout rlResetFactory;
    @BindView(R.id.rl_password)
    RelativeLayout rlPassword;
    @BindView(R.id.et_device_id)
    EditText etDeviceId;

    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_device, container, false);
        ButterKnife.bind(this, view);
        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                if (!(source + "").matches(FILTER_ASCII)) {
                    return "";
                }

                return null;
            }
        };
        etDeviceName.setFilters(new InputFilter[]{new InputFilter.LengthFilter(10), filter});
        return view;
    }

    public void setDeviceName(String deviceName) {
        etDeviceName.setText(deviceName);
    }

    public boolean isValid() {
        String deviceNameStr = etDeviceName.getText().toString();
        if (TextUtils.isEmpty(deviceNameStr))
            return false;
        int length = deviceNameStr.length();
        if (length < 1 || length > 10)
            return false;
        String deviceIdStr = etDeviceId.getText().toString();
        if (TextUtils.isEmpty(deviceIdStr))
            return false;
        if (deviceIdStr.length() % 2 != 0)
            return false;
        return true;
    }

    public void saveParams() {
        String deviceName = etDeviceName.getText().toString();
        String deviceStr = etDeviceId.getText().toString();
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setDeviceName(deviceName));
        orderTasks.add(OrderTaskAssembler.setDeviceId(deviceStr));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }

    public void setViewShown(boolean enablePasswordVerify) {
        rlResetFactory.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
        rlPassword.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setResetShown() {
        rlResetFactory.setVisibility(View.VISIBLE);
    }

    public void setDeviceId(String deviceIdHex) {
        etDeviceId.setText(deviceIdHex);
    }
}
