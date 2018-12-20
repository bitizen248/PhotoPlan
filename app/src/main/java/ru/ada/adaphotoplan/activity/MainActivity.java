package ru.ada.adaphotoplan.activity;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.adapter.MainScreenAdapter;
import ru.ada.adaphotoplan.obj.OpenPageEvent;


/**
 * Created by Bitizen on 28.03.17.
 */

public class MainActivity extends ProtoActivity {

    private TabLayout tabs;
    private ViewPager pager;

    @Override
    protected void onStart() {
        super.onStart();
        EventBus.getDefault().register(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        EventBus.getDefault().unregister(this);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        tabs = findViewById(R.id.tab);
        pager = findViewById(R.id.pager);

        pager.setAdapter(new MainScreenAdapter(this, getSupportFragmentManager()));
        tabs.setupWithViewPager(pager);

        int[] permissionCheck = {
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE),
                ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA),
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION),
                ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        };

        if (permissionCheck[0] != PackageManager.PERMISSION_GRANTED
                || permissionCheck[1] != PackageManager.PERMISSION_GRANTED
                || permissionCheck[2] != PackageManager.PERMISSION_GRANTED
                || permissionCheck[3] != PackageManager.PERMISSION_GRANTED) {
            String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.CAMERA,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            };
            ActivityCompat.requestPermissions(this, permissions, 0);
        }
    }

    @Subscribe
    public void openPage(OpenPageEvent event) {
        if (pager != null)
            pager.setCurrentItem(event.getPage(), true);
    }
}
