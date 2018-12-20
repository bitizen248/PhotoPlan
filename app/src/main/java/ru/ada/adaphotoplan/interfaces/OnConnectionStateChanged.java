package ru.ada.adaphotoplan.interfaces;

/**
 * Created by Bitizen on 01.07.17.
 */

public interface OnConnectionStateChanged {

    void onDeviceConnected(String name);

    void onDeviceDisconnected();
}
