package ru.ada.adaphotoplan.activity;

import android.os.Bundle;
import android.support.annotation.Nullable;

import ru.ada.adaphotoplan.R;

/**
 * Created by Bitizen on 30.08.17.
 */

public class BluetoothActivity extends ProtoActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bluetooth);

        title.setText(R.string.bluetooth);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return super.onSupportNavigateUp();
    }
}
