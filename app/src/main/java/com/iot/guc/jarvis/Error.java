package com.iot.guc.jarvis;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;

public class Error {
    public AlertDialog create(Activity activity, String message, String title) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(message).setTitle(title);

        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

            }
        });

        AlertDialog dialog = builder.create();
        return dialog;
    }
}
