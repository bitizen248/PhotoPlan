package ru.ada.adaphotoplan.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.interfaces.OnDeviceChoose;
import ru.ada.adaphotoplan.obj.BleDevice;

/**
 * Created by Bitizen on 01.07.17.
 */

public class DevicesAdapter extends RecyclerView.Adapter {
    private static final String TAG = "DevicesAdapter";

    private static final int TYPE_ITEM = 0;
    private static final int TYPE_SPACE = 1;

    private List<BleDevice> projects = new ArrayList<>();
    private OnDeviceChoose deviceChoose;

    public DevicesAdapter(List<BleDevice> projects, OnDeviceChoose deviceChoose) {
        this.projects = projects;
        this.deviceChoose = deviceChoose;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater lf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ITEM)
            return new DeviceViewHolder(lf.inflate(R.layout.item_device, parent, false));
        else if (viewType == TYPE_SPACE)
            return new SimpleViewHolder(lf.inflate(R.layout.item_space, parent, false));
        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        if (position < projects.size()) {
            BleDevice device = projects.get(position);
            ((DeviceViewHolder) holder).name.setText(device.getDeviceName());
            ((DeviceViewHolder) holder).wrapper.setOnClickListener(v -> {
                deviceChoose.onChoose(device);
            });
        }
    }

    @Override
    public int getItemCount() {
        return projects.size() + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position == getItemCount() - 1)
            return TYPE_SPACE;
        else
            return TYPE_ITEM;
    }

    class DeviceViewHolder extends RecyclerView.ViewHolder {

        ViewGroup wrapper;
        TextView name;

        public DeviceViewHolder(View itemView) {
            super(itemView);

            wrapper = itemView.findViewById(R.id.wrapper);
            name = itemView.findViewById(R.id.name);
        }
    }

    class SimpleViewHolder extends RecyclerView.ViewHolder {

        public SimpleViewHolder(View itemView) {
            super(itemView);
        }
    }


}