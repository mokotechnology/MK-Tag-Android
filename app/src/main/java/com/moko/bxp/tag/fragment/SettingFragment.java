package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.moko.bxp.tag.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingFragment extends Fragment {

    @BindView(R.id.ll_reset)
    LinearLayout llReset;
    @BindView(R.id.ll_modify_password)
    LinearLayout llModifyPassword;

    public SettingFragment() {
    }

    public static SettingFragment newInstance() {
        SettingFragment fragment = new SettingFragment();
        return fragment;
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
}
