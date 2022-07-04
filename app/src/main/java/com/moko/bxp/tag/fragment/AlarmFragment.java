package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.bxp.tag.R;
import com.moko.bxp.tag.activity.DeviceInfoActivity;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlarmFragment extends Fragment {

    @BindView(R.id.tv_single_press_mode_switch)
    TextView tvSinglePressModeSwitch;
    @BindView(R.id.tv_double_press_mode_switch)
    TextView tvDoublePressModeSwitch;
    @BindView(R.id.tv_long_press_mode_switch)
    TextView tvLongPressModeSwitch;
    @BindView(R.id.tv_abnormal_inactivity_mode_switch)
    TextView tvAbnormalInactivityModeSwitch;
    private DeviceInfoActivity activity;

    public AlarmFragment() {
    }

    public static AlarmFragment newInstance() {
        AlarmFragment fragment = new AlarmFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alarm, container, false);
        ButterKnife.bind(this, view);
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }


    public void setSinglePressModeSwitch(int onOff) {
        tvSinglePressModeSwitch.setText(onOff == 1 ? "ON" : "OFF");
    }

    public void setDoublePressModeSwitch(int onOff) {
        tvDoublePressModeSwitch.setText(onOff == 1 ? "ON" : "OFF");
    }

    public void setLongPressModeSwitch(int onOff) {
        tvLongPressModeSwitch.setText(onOff == 1 ? "ON" : "OFF");
    }

    public void setAbnormalInactivityModeSwitch(int onOff) {
        tvAbnormalInactivityModeSwitch.setText(onOff == 1 ? "ON" : "OFF");
    }
}
