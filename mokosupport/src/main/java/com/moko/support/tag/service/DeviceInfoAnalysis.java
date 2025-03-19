package com.moko.support.tag.service;

import com.moko.support.tag.entity.DeviceInfo;

public interface DeviceInfoAnalysis<T> {
    T parseDeviceInfo(DeviceInfo deviceInfo);
}
