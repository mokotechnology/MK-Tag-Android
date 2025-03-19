package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.tag.databinding.FragmentSettingTagBinding;

public class SettingFragment extends Fragment {
    private FragmentSettingTagBinding mBind;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
    }

    public void visibleResetBattery() {
        mBind.tvResetBattery.setVisibility(View.VISIBLE);
        mBind.lineBattery.setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBind = FragmentSettingTagBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    public void setResetVisibility(boolean enablePasswordVerify) {
        mBind.llReset.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setModifyPasswordShown(boolean enablePasswordVerify) {
        mBind.llModifyPassword.setVisibility(enablePasswordVerify ? View.VISIBLE : View.GONE);
    }

    public void setAdvMode(String advMode) {
        mBind.tvAdvMode.setText(advMode);
    }

    public void setSensorGone() {
        mBind.layoutSensor.setVisibility(View.GONE);
    }
}
