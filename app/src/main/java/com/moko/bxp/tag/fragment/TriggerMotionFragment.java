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
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.moko.bxp.tag.R;
import com.moko.bxp.tag.activity.SlotDataActivity;
import com.moko.bxp.tag.utils.ToastUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

public class TriggerMotionFragment extends Fragment {

    private static final String TAG = TriggerMotionFragment.class.getSimpleName();

    @BindView(R.id.iv_start)
    ImageView ivStart;
    @BindView(R.id.et_duration)
    EditText etDuration;
    @BindView(R.id.et_static)
    EditText etStatic;
    @BindView(R.id.iv_stop)
    ImageView ivStop;
    @BindView(R.id.et_stop_static)
    EditText etStopStatic;
    @BindView(R.id.tv_trigger_tips)
    TextView tvTriggerTips;


    private SlotDataActivity activity;
    private boolean mIsStart = true;
    private int mDuration = 30;
    private int mStatic = 60;


    public TriggerMotionFragment() {
    }

    public static TriggerMotionFragment newInstance() {
        TriggerMotionFragment fragment = new TriggerMotionFragment();
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
        View view = inflater.inflate(R.layout.fragment_trigger_motion, container, false);
        ButterKnife.bind(this, view);
        activity = (SlotDataActivity) getActivity();
        if (activity.slotData.triggerType == 5) {
            // 移动触发
            mIsStart = activity.slotData.triggerAdvStatus == 1;
            mDuration = activity.slotData.triggerAdvDuration;
            mStatic = activity.slotData.staticDuration;
        }
        changeTips();
        etDuration.setText(String.valueOf(mDuration));
        etStatic.setText(String.valueOf(mStatic));
        etStopStatic.setText(String.valueOf(mStatic));
        etDuration.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String durationStr = s.toString();
                if (!TextUtils.isEmpty(durationStr)) {
                    mDuration = Integer.parseInt(durationStr);
                    changeTips();
                }
            }
        });
        etStatic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String staticStr = s.toString();
                if (mIsStart && !TextUtils.isEmpty(staticStr)) {
                    mStatic = Integer.parseInt(staticStr);
                    changeTips();
                }
            }
        });
        etStopStatic.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String staticStr = s.toString();
                if (!mIsStart && !TextUtils.isEmpty(staticStr)) {
                    mStatic = Integer.parseInt(staticStr);
                    changeTips();
                }
            }
        });
        return view;
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

    public int getStaticDuration() {
        String staticStr = mIsStart ? etStatic.getText().toString() : etStopStatic.getText().toString();
        if (TextUtils.isEmpty(staticStr)) {
            ToastUtils.showToast(getActivity(), "The static duration can not be empty.");
            return -1;
        }
        int staticDuration = Integer.parseInt(staticStr);
        if (staticDuration < 1 || staticDuration > 65535) {
            ToastUtils.showToast(activity, "The static duration range is 1~65535");
            return -1;
        }
        return staticDuration;
    }

    public int getDuration() {
        if (mIsStart) {
            String durationStr = etDuration.getText().toString();
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

    public void motionStart() {
        mIsStart = true;
        changeTips();
    }

    public void motionStop() {
        mIsStart = false;
        changeTips();
    }


    private void changeTips() {
        String triggerTips = getString(R.string.trigger_motion_tips, mIsStart ?
                        (mDuration == 0 ?
                                "start advertising"
                                : String.format("start advertising for %ds", mDuration))
                        : "stop advertising", mStatic,
                mIsStart ? "stop" : "start");
        tvTriggerTips.setText(triggerTips);
        ivStart.setImageResource(mIsStart ? R.drawable.icon_selected : R.drawable.icon_unselected);
        ivStop.setImageResource(mIsStart ? R.drawable.icon_unselected : R.drawable.icon_selected);
    }

    public int getTriggerAdvStatus() {
        return mIsStart ? 1 : 0;
    }
}
