package com.udacity.stockhawk.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import com.udacity.stockhawk.R;
import com.udacity.stockhawk.fragments.DetailFragment;

import static android.R.attr.fragment;


public class DetailActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.add(R.id.fragment_detail_holder, new DetailFragment());
        fragmentTransaction.commit();
    }
}


