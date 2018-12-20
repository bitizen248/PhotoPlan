package ru.ada.adaphotoplan.service;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import ru.ada.adaphotoplan.interfaces.OnConnectionStateChanged;
import ru.ada.adaphotoplan.obj.BleDevice;
import ru.ada.adaphotoplan.obj.MeterEvent;

/**
 * Created by Bitizen on 01.07.17.
 */

public class BluetoothService extends Service {

    private static final String METER_UUID_SERVICE = "0000cbbb-0000-1000-8000-00805f9b34fb";
    private static final String METER_UUID_CRACTER = "0000cbb1-0000-1000-8000-00805f9b34fb";

    private final IBinder binder = new BleBinder();

    private BluetoothAdapter bluetoothAdapter;

    private BluetoothManager bluetoothManager;

    private OnConnectionStateChanged connectionStateChanged;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        bluetoothManager =
                (BluetoothManager) getSystemService(Context.BLUETOOTH_SERVICE);
        bluetoothAdapter = bluetoothManager.getAdapter();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public class BleBinder extends Binder {
        public BluetoothService getService() {
            return BluetoothService.this;
        }
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    public void enableBluetooth() {
        bluetoothAdapter.enable();
    }

    private Map<BleDevice, BluetoothDevice> devices = new HashMap<>();
    private BluetoothGatt connectedDevice = null;

    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                connectedDevice = gatt;
                gatt.discoverServices();
                if (connectionStateChanged != null)
                    Observable
                        .create(e ->
                                connectionStateChanged.onDeviceConnected(connectedDevice.getDevice().getName()))
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTING || newState == BluetoothProfile.STATE_DISCONNECTED) {
                connectedDevice = null;
                if (connectionStateChanged != null)
                    Observable
                            .create(e ->
                                    connectionStateChanged.onDeviceDisconnected())
                            .subscribeOn(AndroidSchedulers.mainThread())
                            .subscribe();
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            gatt.setCharacteristicNotification(
                    gatt
                            .getService(UUID.fromString(METER_UUID_SERVICE))
                            .getCharacteristic(UUID.fromString(METER_UUID_CRACTER)), true);
        }

        @Override
        public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            super.onCharacteristicChanged(gatt, characteristic);
            String val = characteristic.getStringValue(0);
            if (val.startsWith("D"))
            EventBus
                    .getDefault()
                    .post(new MeterEvent(val.substring(1)));
        }
    };

    public void connectToDevice(BleDevice device) {
        if (devices.containsKey(device))
           devices.get(device).connectGatt(this, false, callback);
    }

    public void disconnect() {
        if (connectedDevice != null) {
            connectedDevice.close();
            connectedDevice.disconnect();
            connectionStateChanged.onDeviceDisconnected();
            connectedDevice = null;
        }
    }

    public Observable getDeviceList() {
        devices.clear();
        if (bluetoothAdapter.isDiscovering())
            bluetoothAdapter.cancelDiscovery();
        BluetoothLeScanner scanner = bluetoothAdapter.getBluetoothLeScanner();
        final ScanCallback[] scanCallback = {null};
        return Observable
                .create((e) -> {
                    scanCallback[0] = new ScanCallback() {
                        @Override
                        public void onScanResult(int callbackType, ScanResult result) {
                            if (callbackType == ScanSettings.CALLBACK_TYPE_ALL_MATCHES) {
                                BleDevice device = new BleDevice(result.getDevice());
                                if (!devices.containsValue(result.getDevice())) {
                                    devices.put(device, result.getDevice());
                                    e.onNext(device);
                                }
                            }
                        }
                    };
                    scanner.startScan(scanCallback[0]);
                })
                .mergeWith(Observable.timer(5, TimeUnit.SECONDS))
                .flatMap((Object obj) -> Observable.create(e -> {
                    if (obj instanceof Long && (Long) obj == 0L) {
                        scanner.stopScan(scanCallback[0]);
                        e.onComplete();
                    } else e.onNext(obj);
                }));

    }

    public void setConnectionStateChanged(OnConnectionStateChanged connectionStateChanged) {
        this.connectionStateChanged = connectionStateChanged;
    }

    public boolean isConnected() {
        return connectedDevice != null;
    }

    public String getDeviecName() {
        if (connectedDevice == null)
            return null;
        return connectedDevice.getDevice().getName();
    }
}
