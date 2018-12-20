package ru.ada.adaphotoplan;

import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import ru.ada.adaphotoplan.service.BluetoothService;

/**
 * Created by Bitizen on 14.06.17.
 */

public class AdaPPAplication extends Application {


    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics());
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .migration((realm, oldVersion, newVersion) -> {

                })
                .schemaVersion(1)
                .build();
        Realm.setDefaultConfiguration(configuration);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            startService(new Intent(this, BluetoothService.class));
            bindService(new Intent(this, BluetoothService.class), connection, Context.BIND_AUTO_CREATE);
        }
    }

    @Nullable
    private BluetoothService bluetoothService;

    private ServiceConnection connection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            bluetoothService = ((BluetoothService.BleBinder )iBinder).getService();
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            bluetoothService = null;
        }
    };

    @Nullable
    public BluetoothService getBluetoothService() {
        return bluetoothService;
    }
}
