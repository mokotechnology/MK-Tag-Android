package com.moko.support.tag;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.Context;

import com.elvishew.xlog.XLog;
import com.moko.ble.lib.MokoBleManager;
import com.moko.ble.lib.callback.MokoResponseCallback;
import com.moko.ble.lib.utils.MokoUtils;
import com.moko.support.tag.entity.OrderCHAR;
import com.moko.support.tag.entity.OrderServices;

import androidx.annotation.NonNull;

final class MokoBleConfig extends MokoBleManager {

    private MokoResponseCallback mMokoResponseCallback;
    private BluetoothGattCharacteristic paramsCharacteristic;
    private BluetoothGattCharacteristic disconnectCharacteristic;
    private BluetoothGattCharacteristic accCharacteristic;
    private BluetoothGattCharacteristic hallCharacteristic;
    private BluetoothGattCharacteristic passwordCharacteristic;
    private BluetoothGatt gatt;

    public MokoBleConfig(@NonNull Context context, MokoResponseCallback callback) {
        super(context);
        mMokoResponseCallback = callback;
    }

    @Override
    public boolean checkServiceCharacteristicSupported(BluetoothGatt gatt) {
        final BluetoothGattService service = gatt.getService(OrderServices.SERVICE_CUSTOM.getUuid());
        if (service != null) {
            this.gatt = gatt;
            paramsCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PARAMS.getUuid());
            disconnectCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_DISCONNECT.getUuid());
            accCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_ACC.getUuid());
            hallCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_HALL.getUuid());
            passwordCharacteristic = service.getCharacteristic(OrderCHAR.CHAR_PASSWORD.getUuid());
            return disconnectCharacteristic != null
                    && paramsCharacteristic != null
                    && passwordCharacteristic != null;
        }
        return false;
    }

    @Override
    public void init() {
        requestMtu(247).with(((device, mtu) -> {
        })).then((device -> {
            enableDisconnectNotify();
            enablePasswordNotify();
            enableParamsNotify();
        })).enqueue();
    }

    @Override
    public void write(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicWrite(characteristic, value);
    }

    @Override
    public void read(BluetoothGattCharacteristic characteristic, byte[] value) {
        mMokoResponseCallback.onCharacteristicRead(characteristic, value);
    }

    @Override
    public void onDeviceConnecting(@NonNull BluetoothDevice device) {
    }


    @Override
    public void onDeviceConnected(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceFailedToConnect(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }

    @Override
    public void onDeviceReady(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnecting(@NonNull BluetoothDevice device) {

    }

    @Override
    public void onDeviceDisconnected(@NonNull BluetoothDevice device, int reason) {
        mMokoResponseCallback.onDeviceDisconnected(device, reason);
    }

    public void enableParamsNotify() {
        setNotificationCallback(paramsCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(paramsCharacteristic, value);
        });
        enableNotifications(paramsCharacteristic).done(device -> mMokoResponseCallback.onServicesDiscovered(gatt)).enqueue();
    }

    public void disableParamsNotify() {
        disableNotifications(paramsCharacteristic).enqueue();
    }

    public void enableDisconnectNotify() {
        setNotificationCallback(disconnectCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(disconnectCharacteristic, value);
        });
        enableNotifications(disconnectCharacteristic).enqueue();
    }

    public void disableDisconnectNotify() {
        disableNotifications(disconnectCharacteristic).enqueue();
    }

    public void enablePasswordNotify() {
        setNotificationCallback(passwordCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(passwordCharacteristic, value);
        });
        enableNotifications(passwordCharacteristic).enqueue();
    }

    public void disablePasswordNotify() {
        disableNotifications(passwordCharacteristic).enqueue();
    }

    public void enableHallStatusNotify() {
        setNotificationCallback(hallCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(hallCharacteristic, value);
        });
        enableNotifications(hallCharacteristic).enqueue();
    }

    public void disableHallStatusNotify() {
        disableNotifications(hallCharacteristic).enqueue();
    }

    public void enableAccNotify() {
        setNotificationCallback(accCharacteristic).with((device, data) -> {
            final byte[] value = data.getValue();
            XLog.e("onDataReceived");
            XLog.e("device to app : " + MokoUtils.bytesToHexString(value));
            mMokoResponseCallback.onCharacteristicChanged(accCharacteristic, value);
        });
        enableNotifications(accCharacteristic).enqueue();
    }

    public void disableAccNotify() {
        disableNotifications(accCharacteristic).enqueue();
    }
}