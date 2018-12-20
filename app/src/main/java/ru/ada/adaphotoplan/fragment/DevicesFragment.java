package ru.ada.adaphotoplan.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.ada.adaphotoplan.AdaPPAplication;
import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.adapter.DevicesAdapter;
import ru.ada.adaphotoplan.interfaces.OnConnectionStateChanged;
import ru.ada.adaphotoplan.obj.BleDevice;
import ru.ada.adaphotoplan.service.BluetoothService;

/**
 * Created by Bitizen on 28.03.17.
 */

public class DevicesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_devices, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ViewGroup bleDisabled = view.findViewById(R.id.ble_disabled);
        Button bleEnable = view.findViewById(R.id.enable_ble);
        ViewGroup bleListView = view.findViewById(R.id.ble_list);
        RecyclerView bleDeviceList = view.findViewById(R.id.device_list);
        FloatingActionButton reset = view.findViewById(R.id.reset);
        ViewGroup bleConnected = view.findViewById(R.id.ble_connected);
        TextView connectDevice = view.findViewById(R.id.connected_to);
        Button disconnect = view.findViewById(R.id.disconnect);


        BluetoothService bleService =
                ((AdaPPAplication) getActivity().getApplication()).getBluetoothService();
        assert bleService != null;
        List<BleDevice> devices = new ArrayList<>();
        DevicesAdapter adapter = new DevicesAdapter(devices, bleService::connectToDevice);
        bleDeviceList.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        bleDeviceList.setAdapter(adapter);

        bleService.setConnectionStateChanged(new OnConnectionStateChanged() {
            @Override
            public void onDeviceConnected(String name) {
                bleDisabled.setVisibility(View.GONE);
                bleListView.setVisibility(View.GONE);
                bleConnected.setVisibility(View.VISIBLE);
                connectDevice.setText(getString(R.string.connected_to, name));
            }

            @Override
            public void onDeviceDisconnected() {
                bleConnected.setVisibility(View.GONE);
                if (!bleService.isBluetoothEnabled()) {
                    bleDisabled.setVisibility(View.VISIBLE);
                    bleListView.setVisibility(View.GONE);
                } else {
                    bleDisabled.setVisibility(View.GONE);
                    bleListView.setVisibility(View.VISIBLE);
                    devices.clear();
                    adapter.notifyDataSetChanged();
                    bleService
                            .getDeviceList()
                            .doOnNext(dev -> {
                                devices.add((BleDevice) dev);
                                adapter.notifyItemInserted(devices.size());
                            })
                            .subscribe();
                }
            }
        });

        bleEnable.setOnClickListener(v -> {
            bleService.enableBluetooth();
            bleDisabled.setVisibility(View.GONE);
            bleListView.setVisibility(View.VISIBLE);
            bleConnected.setVisibility(View.GONE);
        });

        disconnect.setOnClickListener(v -> bleService.disconnect());

        reset.setOnClickListener(v -> {
            devices.clear();
            adapter.notifyDataSetChanged();
            bleService
                    .getDeviceList()
                    .doOnNext(dev -> {
                        devices.add((BleDevice) dev);
                        adapter.notifyItemInserted(devices.size());
                    })
                    .subscribe();
        });

        if (!bleService.isBluetoothEnabled()) {
            bleDisabled.setVisibility(View.VISIBLE);
            bleListView.setVisibility(View.GONE);
            bleConnected.setVisibility(View.GONE);
        } else if (bleService.isConnected()){
            bleDisabled.setVisibility(View.GONE);
            bleListView.setVisibility(View.GONE);
            bleConnected.setVisibility(View.VISIBLE);
            connectDevice.setText(getString(R.string.connected_to, bleService.getDeviecName()));
        } else {
            bleDisabled.setVisibility(View.GONE);
            bleListView.setVisibility(View.VISIBLE);
            bleConnected.setVisibility(View.GONE);
            devices.clear();
            adapter.notifyDataSetChanged();
            bleService
                    .getDeviceList()
                    .doOnNext(dev -> {
                        devices.add((BleDevice) dev);
                        adapter.notifyItemInserted(devices.size());
                    })
                    .subscribe();
        }
    }
}
