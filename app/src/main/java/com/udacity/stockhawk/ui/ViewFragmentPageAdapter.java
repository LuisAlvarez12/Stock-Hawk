package com.udacity.stockhawk.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;


/**
 * Created by luisalvarez on 1/20/17.
 */

public class ViewFragmentPageAdapter extends FragmentStatePagerAdapter {

    private CharSequence[] mtabs;

    public ViewFragmentPageAdapter(FragmentManager fm, CharSequence[] x) {
        super(fm);
        mtabs=x;
    }

    @Override
    public Fragment getItem(int i) {


        if (i == 0) {
            Bundle bundle = new Bundle();
            bundle.putString("sortOrder", "popular");

        }
return null;
    }

    @Override
    public int getCount() {
        return mtabs.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mtabs[position];
    }
}
