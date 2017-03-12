package com.iot.guc.jarvis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Confirmation {
    public AlertDialog create(Activity activity, String message, String title, DialogInterface.OnClickListener positive, DialogInterface.OnClickListener negative) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setTitle(title);
        builder.setPositiveButton("Yes", positive);
        builder.setNegativeButton("Cancel", negative);
        AlertDialog dialog = builder.create();
        return dialog;
    }
}
