package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.tag.R;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.databinding.FragmentTriggerMagneticTagBinding;
import com.moko.bxp.tag.utils.ToastUtils;

public class TriggerMagneticFragment extends Fragment {

    private static final String TAG = TriggerMagneticFragment.class.getSimpleName();

    private FragmentTriggerMagneticTagBinding mBind;


    private SlotDataActivity activity;
    private boolean mIsStart = true;
    private String mDuration = "30";


    public TriggerMagneticFragment() {
    }

    public static TriggerMagneticFragment newInstance() {
        TriggerMagneticFragment fragment = new TriggerMagneticFragment();
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
        mBind = FragmentTriggerMagneticTagBinding.inflate(inflater, container, false);
        activity = (SlotDataActivity) getActivity();
        if (activity.slotData.triggerType == 6) {
            // 霍尔触发
            mIsStart = activity.slotData.triggerAdvStatus == 1;
            mDuration = String.valueOf(activity.slotData.triggerAdvDuration);
        }
        changeTips();
        mBind.etDuration.setText(mDuration);
        mBind.etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String durationStr = s.toString();
                mDuration = durationStr;
                changeTips();
            }
        });
        return mBind.getRoot();
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

    public int getDuration() {
        if (mIsStart) {
            String durationStr = mBind.etDuration.getText().toString();
            if (TextUtils.isEmpty(durationStr)) {
                ToastUtils.showToast(getActivity(), "The advertising can not be empty.");
                return -1;
            }
            int duration = Integer.parseInt(durationStr);
            if (duration > 65535) {
                ToastUtils.showToast(activity, "The advertising range is 0~65535");
                return -1;
            }
            return duration;
        } else {
            return 30;
        }
    }

    public void magneticStart() {
        mIsStart = true;
        changeTips();
    }

    public void magneticStop() {
        mIsStart = false;
        changeTips();
    }


    private void changeTips() {
        String triggerTips = getString(R.string.trigger_magnetic_tips, mIsStart ?
                ("0".equals(mDuration) ? "start advertising" : String.format("start advertising for %ss", mDuration)) :
                "stop advertising", mIsStart ? "stop" : "start");
        mBind.tvTriggerTips.setText(triggerTips);
        mBind.ivStart.setImageResource(mIsStart ? R.drawable.icon_selected : R.drawable.icon_unselected);
        mBind.ivStop.setImageResource(mIsStart ? R.drawable.icon_unselected : R.drawable.icon_selected);
    }

    public int getTriggerAdvStatus() {
        return mIsStart ? 1 : 0;
    }
}
