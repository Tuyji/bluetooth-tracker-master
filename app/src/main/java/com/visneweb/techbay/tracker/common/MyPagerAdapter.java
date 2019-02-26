package com.visneweb.techbay.tracker.common;


import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.visneweb.techbay.tracker.R;
import com.visneweb.techbay.tracker.mydevices.MyDevicesFragment;
import com.visneweb.techbay.tracker.scan.ScannerFragment;

/**
 * Created by riskactive on 16.03.2018.
 */

public abstract class MyPagerAdapter extends FragmentPagerAdapter {
    String[] titles = new String[]{
            getContext().getString(R.string.scanner),
            getContext().getString(R.string.my_devices)
    };

    public MyPagerAdapter(FragmentManager fm) {
        super(fm);
    }


    @Override
    public Fragment getItem(int position) {
        Fragment f;
        switch (position) {
            case 0:
                f = new ScannerFragment();
                break;
            case 1:
                f = new MyDevicesFragment();
                break;
            default:
                f = null;
                break;
        }
        return f;
    }

    public abstract Context getContext();

    @Override
    public int getCount() {
        return 2;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }
}
