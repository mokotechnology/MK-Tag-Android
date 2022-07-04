package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import com.moko.ble.lib.task.OrderTask;
import com.moko.bxp.tag.R;
import com.moko.bxp.tag.activity.DeviceInfoActivity;
import com.moko.support.MokoSupport;
import com.moko.support.OrderTaskAssembler;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SettingFragment extends Fragment {

    @BindView(R.id.et_effective_click_interval)
    EditText etEffectiveClickInterval;
    private DeviceInfoActivity activity;

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
        activity = (DeviceInfoActivity) getActivity();
        return view;
    }


    public void setEffectiveClickInterval(int interval) {
        etEffectiveClickInterval.setText(String.valueOf(interval / 100));
    }

    public boolean isValid() {
        String intervalStr = etEffectiveClickInterval.getText().toString();
        if (TextUtils.isEmpty(intervalStr))
            return false;
        int interval = Integer.parseInt(intervalStr);
        if (interval < 5 || interval > 15)
            return false;
        return true;
    }

    public void saveParams() {
        String intervalStr = etEffectiveClickInterval.getText().toString();
        int interval = Integer.parseInt(intervalStr) * 100;
        List<OrderTask> orderTasks = new ArrayList<>();
        orderTasks.add(OrderTaskAssembler.setEffectiveClickInterval(interval));
        MokoSupport.getInstance().sendOrder(orderTasks.toArray(new OrderTask[]{}));
    }
}
