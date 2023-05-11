package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.bxp.tag.AppConstants;
import com.moko.bxp.tag.R;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingFragment extends Fragment {

    @BindView(R.id.ll_reset)
    LinearLayout llReset;
    @BindView(R.id.ll_modify_password)
    LinearLayout llModifyPassword;
    @BindView(R.id.tv_adv_mode)
    TextView tvAdvMode;
    @BindView(R.id.layoutBattery)
    LinearLayout layoutBattery;
    @BindView(R.id.tvResetBattery)
    TextView tvResetBattery;
    @BindView(R.id.lineBattery)
    View viewLine;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    public void setFirmwareVersion(int firmwareVersion) {
        if (firmwareVersion > AppConstants.BASE_VERSION) {
            layoutBattery.setVisibility(View.VISIBLE);
            MokoSupport.getInstance().sendOrder(OrderTaskAssembler.getBatteryMode());
        }
    }

    public void visibleResetBattery(){
        tvResetBattery.setVisibility(View.VISIBLE);
        viewLine.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    public void setResetVisibility(boolean enablePasswordVerify) {
        llReset.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setModifyPasswordShown(boolean enablePasswordVerify) {
        llModifyPassword.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setAdvMode(String advMode) {
        tvAdvMode.setText(advMode);
    }
}
