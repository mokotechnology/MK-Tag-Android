package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.moko.bxp.tag.databinding.FragmentDeviceBinding;

public class DeviceFragment extends Fragment {
    private FragmentDeviceBinding mBind;


    public DeviceFragment() {
    }

    public static DeviceFragment newInstance() {
        DeviceFragment fragment = new DeviceFragment();
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        mBind = FragmentDeviceBinding.inflate(inflater, container, false);
        return mBind.getRoot();
    }

    public void setMac(String mac) {
        mBind.tvMacAddress.setText(mac);
    }

    public void setBattery(int battery) {
        mBind.tvSoc.setText(String.format("%dmV", battery));
    }

    public void setModelNumber(byte[] value) {
        mBind.tvDeviceModel.setText(new String(value).trim());
    }

    public void setSoftwareRevision(byte[] value) {
        mBind.tvSoftwareVersion.setText(new String(value).trim());
    }

    public void setFirmwareRevision(byte[] value) {
        mBind.tvFirmwareVersion.setText(new String(value).trim());
    }

    public void setHardwareRevision(byte[] value) {
        mBind.tvHardwareVersion.setText(new String(value).trim());
    }

    public void setSerialNumber(byte[] value) {
        mBind.tvProductDate.setText(new String(value).trim());
    }

    public void setManufacturer(byte[] value) {
        mBind.tvManufacturer.setText(new String(value).trim());
    }

}
