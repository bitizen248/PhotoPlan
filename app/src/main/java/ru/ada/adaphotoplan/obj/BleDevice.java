package ru.ada.adaphotoplan.obj;

import android.bluetooth.BluetoothDevice;

/**
 * Created by Bitizen on 01.07.17.
 */

public class BleDevice {

    private String deviceName;

    public BleDevice(BluetoothDevice device) {
        this.deviceName = device.getName();
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }
}
