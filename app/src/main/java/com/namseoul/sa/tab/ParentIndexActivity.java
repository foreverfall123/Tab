package com.namseoul.sa.tab;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;

public class ParentIndexActivity extends AppCompatActivity {

    private ViewPager mViewPager;
    ActionBar bar;
    private FragmentManager fm;
    private ArrayList<Fragment>fList;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.parent_index);

        mViewPager = (ViewPager)findViewById(R.id.pager);

        fm = getSupportFragmentManager();

        bar = getSupportActionBar();

        bar.setDisplayShowTitleEnabled(true);
        bar.setTitle("타이틀?");

        bar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        ActionBar.Tab tab1 = bar.newTab().setText("Tab1").setTabListener(tabListener);
        ActionBar.Tab tab2 = bar.newTab().setText("Tab2").setTabListener(tabListener);
        ActionBar.Tab tab3 = bar.newTab().setText("Tab3").setTabListener(tabListener);

        bar.addTab(tab1);
        bar.addTab(tab2);
        bar.addTab(tab3);

        fList = new ArrayList<Fragment>();
        fList.add(ParentFirstFragment.newInstance());
        fList.add(PArentSecondFragment.newInstance());
        fList.add(ParentThirdFragment.newInstance());

        mViewPager.setOnPageChangeListener(viewPagerListener);

        CustomFragmentPagerAdapter adapter = new CustomFragmentPagerAdapter(fm, fList);
        mViewPager.setAdapter(adapter);
    }

    ViewPager.SimpleOnPageChangeListener viewPagerListener = new ViewPager.SimpleOnPageChangeListener(){
        @Override
        public void onPageSelected(int position) {
            super.onPageSelected(position);
            bar.setSelectedNavigationItem(position);
        }
    };

    ActionBar.TabListener tabListener = new ActionBar.TabListener(){

        @Override
        public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //탭에서 벗어낫을 경우 처리
        }

        @Override
        public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
            mViewPager.setCurrentItem(tab.getPosition());
        }

        @Override
        public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
            //다시 선택되었을 경우 처리
        }
    };
}
