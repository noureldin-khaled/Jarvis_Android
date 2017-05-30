package com.iot.guc.jarvis.responses;

import android.support.v7.app.AlertDialog;

public interface PopupResponse {
    void onPositive(AlertDialog dialog);
    void onNegative(AlertDialog dialog);
}
