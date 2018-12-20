package ru.ada.adaphotoplan.adapter;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import ru.ada.adaphotoplan.R;
import ru.ada.adaphotoplan.fragment.CreateProjectFragment;
import ru.ada.adaphotoplan.fragment.DevicesFragment;
import ru.ada.adaphotoplan.fragment.ProjectsFragment;

/**
 * Created by Bitizen on 28.03.17.
 */

public class MainScreenAdapter extends FragmentPagerAdapter {

    private Context context;
    private String[] titles;
    public MainScreenAdapter(Context context, FragmentManager fm) {
        super(fm);
        this.context = context;
        this.titles = context.getResources().getStringArray(R.array.fragments_titles);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new CreateProjectFragment();
            case 1:
                return new ProjectsFragment();
            case 2:
                return new DevicesFragment();
        }
        return null;
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
