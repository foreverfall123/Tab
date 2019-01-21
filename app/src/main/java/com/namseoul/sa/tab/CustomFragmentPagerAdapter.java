package com.namseoul.sa.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

public class CustomFragmentPagerAdapter extends FragmentPagerAdapter {

    private String[] VIEW_MAPNTOP_TITLES = {"TAB1","TAB2","TAB3"};
    private ArrayList<Fragment> fList;

    public CustomFragmentPagerAdapter(FragmentManager fm, ArrayList<Fragment>fList){
        super(fm);
        this.fList= fList;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return VIEW_MAPNTOP_TITLES[position];
    }

    @Override
    public Fragment getItem(int position) {
        return this.fList.get(position);
    }

    @Override
    public int getCount() {
        return fList.size();
    }
}
