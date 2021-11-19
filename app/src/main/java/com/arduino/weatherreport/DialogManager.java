package com.arduino.weatherreport;

import android.app.ProgressDialog;
import android.content.Context;

public class DialogManager {

    public ProgressDialog progressbar(Context context){
        ProgressDialog dialog = new ProgressDialog(context);
        dialog.create();

        return dialog;
    }
}
