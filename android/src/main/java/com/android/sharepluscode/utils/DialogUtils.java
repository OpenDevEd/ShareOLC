package com.android.sharepluscode.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.android.sharepluscode.R;


public class DialogUtils {

    private Activity mContext;
    private AlertDialog locationSettingDialog;

    public DialogUtils(Activity context) {
        this.mContext = context;
    }

    public void showGPSSettingsAlert() {
        if (locationSettingDialog != null && locationSettingDialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.enable_gps));
        builder.setMessage(mContext.getResources().getString(R.string.enable_gps_msg));
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.action_settings, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (locationSettingDialog != null) {
                    locationSettingDialog.dismiss();
                }
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                mContext.startActivity(intent);
            }
        });

        builder.setNegativeButton(R.string.no_close_app, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (locationSettingDialog != null) {
                    locationSettingDialog.dismiss();
                }
                //mContext.finish();
            }
        });
        locationSettingDialog = builder.create();
        locationSettingDialog.show();
    }


    public void isDismissGPSAlert() {
        if (locationSettingDialog != null && locationSettingDialog.isShowing()) {
            locationSettingDialog.dismiss();
        }
    }
}
