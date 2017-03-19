package com.udacity.stockhawk.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;

import com.udacity.stockhawk.R;

import butterknife.BindView;
import butterknife.ButterKnife;


public class AddStockDialog extends DialogFragment {

    //touch input to add abbreviation with dialogInput
    @SuppressWarnings("WeakerAccess")
    @BindView(R.id.dialog_stock)
    EditText stock;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        //create the builder
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        @SuppressLint("InflateParams") View custom = inflater.inflate(R.layout.add_stock_dialog, null);

        ButterKnife.bind(this, custom);

        stock.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                //call to the callback method
                addStock();
                return true;
            }
        });
        //custom view with edittext for stock abbrev input
        builder.setView(custom);
        //title of dialog
        builder.setMessage(getString(R.string.dialog_title));
        //add tag
        builder.setPositiveButton(getString(R.string.dialog_add),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        addStock();
                    }
                });
        //cancel tag
        builder.setNegativeButton(getString(R.string.dialog_cancel), null);
        //dialog creation
        Dialog dialog = builder.create();

        Window window = dialog.getWindow();
        if (window != null) {
            //check the layout of the dialog
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        }

        return dialog;
    }


//add stock callback to mainactivities addstock method
    private void addStock() {
        //get instance of mainactivity
        android.app.Fragment parent = getParentFragment();
        //check if mainactivity is parent
        if (parent instanceof MainFragment) {
            //callback to add stock from contents of the input.
            ((MainFragment) parent).addStock(stock.getText().toString());
        }
        //dismiss the fragment dialog
        dismissAllowingStateLoss();
    }


}
