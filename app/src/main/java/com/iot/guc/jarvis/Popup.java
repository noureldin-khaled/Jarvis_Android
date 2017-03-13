package com.iot.guc.jarvis;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.View;

public class Popup {

    public AlertDialog create(Activity activity, View view, String positive_msg, final PopupResponse popupResponse) {
        final AlertDialog dialog = new AlertDialog.Builder(activity).setView(view).setPositiveButton(positive_msg, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        }).setCancelable(false).create();

        dialog.show();
        dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupResponse.onPositive(dialog);
            }
        });
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupResponse.onNegative(dialog);
            }
        });
        return dialog;
    }
}
