package com.moko.bxp.tag.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.moko.bxp.tag.R;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DeviceFragment extends Fragment {

    @BindView(R.id.tv_soc)
    TextView tvSoc;
    @BindView(R.id.tv_mac_address)
    TextView tvMacAddress;
    @BindView(R.id.tv_device_model)
    TextView tvDeviceModel;
    @BindView(R.id.tv_software_version)
    TextView tvSoftwareVersion;
    @BindView(R.id.tv_firmware_version)
    TextView tvFirmwareVersion;
    @BindView(R.id.tv_hardware_version)
    TextView tvHardwareVersion;
    @BindView(R.id.tv_product_date)
    TextView tvProductDate;
    @BindView(R.id.tv_manufacturer)
    TextView tvManufacturer;

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
        return view;
    }

    public void setMac(String mac) {
        tvMacAddress.setText(mac);
    }

    public void setBattery(int battery) {
        tvSoc.setText(String.format("%dmV", battery));
    }

    public void setModelNumber(byte[] value) {
        tvDeviceModel.setText(new String(value).trim());
    }

    public void setSoftwareRevision(byte[] value) {
        tvSoftwareVersion.setText(new String(value).trim());
    }

    public void setFirmwareRevision(byte[] value) {
        tvFirmwareVersion.setText(new String(value).trim());
    }

    public void setHardwareRevision(byte[] value) {
        tvHardwareVersion.setText(new String(value).trim());
    }

    public void setSerialNumber(byte[] value) {
        tvProductDate.setText(new String(value).trim());
    }

    public void setManufacturer(byte[] value) {
        tvManufacturer.setText(new String(value).trim());
    }

}
