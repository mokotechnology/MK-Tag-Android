package com.moko.bxp.tag.dialog;

import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.ViewGroup;


import com.moko.bxp.tag.databinding.DialogBottomTagBinding;

import java.util.ArrayList;

public class BottomDialog extends MokoBaseDialog<DialogBottomTagBinding> {


    private ArrayList<String> mDatas;
    private int mIndex;

    @Override
    protected DialogBottomTagBinding getViewBind(LayoutInflater inflater, ViewGroup container) {
        return DialogBottomTagBinding.inflate(inflater, container, false);
    }

    @Override
    protected void onCreateView() {
        mBind.wvBottom.setData(mDatas);
        mBind.wvBottom.setDefault(mIndex);
        mBind.tvCancel.setOnClickListener(v -> {
            dismiss();
        });
        mBind.tvConfirm.setOnClickListener(v -> {
            if (TextUtils.isEmpty(mBind.wvBottom.getSelectedText())) {
                return;
            }
            dismiss();
            final int selected = mBind.wvBottom.getSelected();
            if (listener != null) {
                listener.onValueSelected(selected);
            }
        });
        super.onCreateView();
    }

    @Override
    public float getDimAmount() {
        return 0.7f;
    }

    public void setDatas(ArrayList<String> datas, int index) {
        this.mDatas = datas;
        this.mIndex = index;
    }

    private OnBottomListener listener;

    public void setListener(OnBottomListener listener) {
        this.listener = listener;
    }

    public interface OnBottomListener {
        void onValueSelected(int value);
    }
}
