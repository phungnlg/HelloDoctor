package com.phungnlg.hellodoctor;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Phil on 07/05/2017.
 */

public class TabHomeActivity extends AppCompatActivity {
    private TabLayout mTabLayout;

    private FloatingActionButton fab;

    private int[] mTabsIcons = {
            R.drawable.ic_home_white_24dp,
            R.drawable.ic_perm_identity_white_24dp,
            R.drawable.ic_notifications_white_24dp,
            R.drawable.ic_reorder_white_24dp};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        hideFloatingActionButton();

        View decorView = getWindow().getDecorView();
        //int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_FULLSCREEN;
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        decorView.setSystemUiVisibility(uiOptions);

        // Setup the viewPager
        ViewPager viewPager = (ViewPager) findViewById(R.id.view_pager);
        viewPager.setOffscreenPageLimit(4);
        MyPagerAdapter pagerAdapter = new MyPagerAdapter(getSupportFragmentManager());
        if (viewPager != null) {
            viewPager.setAdapter(pagerAdapter);
        }

        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);
        if (mTabLayout != null) {
            mTabLayout.setupWithViewPager(viewPager);

            for (int i = 0; i < mTabLayout.getTabCount(); i++) {
                TabLayout.Tab tab = mTabLayout.getTabAt(i);
                if (tab != null) {
                    tab.setCustomView(pagerAdapter.getTabView(i));
                }
            }

            mTabLayout.getTabAt(0).getCustomView().setSelected(true);
        }
    }

    private class MyPagerAdapter extends FragmentPagerAdapter {

        private final int pageCount = 4;

        private final String[] mTabsTitle = {"Home", "Add User", "Notifications", "More"};

        MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        public View getTabView(int position) {
            View view = LayoutInflater.from(TabHomeActivity.this).inflate(R.layout.activity_custom, null);
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            icon.setImageResource(mTabsIcons[position]);
            return view;
        }

        @Override
        public Fragment getItem(int pos) {
            switch (pos) {

                case 0:
                    return NewsFeedFragment.newInstance(1);

                case 1:
                    return ProfileFragment_.newInstance(2);
                case 2:
                    return NotificationFragment_.newInstance(3);
                case 3:
                    return MoreFragment.newInstance(4);
            }
            return null;
        }

        @Override
        public int getCount() {
            return pageCount;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mTabsTitle[position];
        }

    }

    public void showFloatingActionButton() {
        fab.show();
    }

    public void hideFloatingActionButton() {
        fab.hide();
    }
}
