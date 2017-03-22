package com.udacity.stockhawk.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.fragments.DetailFragment;

public class DetailActivity extends AppCompatActivity {
    private DetailFragment fileDetailsFragment;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        if (savedInstanceState == null) {
            android.support.v4.app.FragmentManager fragmentManager = getSupportFragmentManager();
            android.support.v4.app.FragmentTransaction fragmentTransaction = fragmentManager
                    .beginTransaction();
            fileDetailsFragment = new DetailFragment();
            fragmentTransaction.add(R.id.fragment_detail_holder, fileDetailsFragment);
            fragmentTransaction.commit();
        } else {
            fileDetailsFragment = (DetailFragment) getSupportFragmentManager().findFragmentById(R.id.fragment_detail_holder);
        }
    }
}


