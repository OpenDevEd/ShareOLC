package com.android.shareolc.utils;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.provider.Settings;
import android.widget.Button;
import androidx.appcompat.app.AlertDialog;
import com.android.shareolc.R;


public class DialogUtils {

    private Activity mContext;
    private ProgressDialog progressDialog;
    private AlertDialog locationSettingDialog;
    private AlertDialog alertNetwork;
    private AlertDialog alertSession;

    public DialogUtils(Activity context) {
        this.mContext = context;
    }


    public void showProgressDialog(String message) {
        progressDialog = new ProgressDialog(mContext);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        progressDialog.show();
    }


    public void dismissProgressDialog() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }


    public void networkConnectionDialog() {
        if (mContext != null) {
            if (alertNetwork != null && alertNetwork.isShowing()) return;

            String strAlertTitle = mContext.getResources().getString(R.string.title_no_connection);
            String strAlertMessage = mContext.getResources().getString(R.string.msg_message_connection);
            String strCloseApp = mContext.getResources().getString(R.string.msg_ok);

            AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
            builder.setTitle(strAlertTitle);
            builder.setMessage(strAlertMessage);
            builder.setCancelable(false);
            builder.setPositiveButton(strCloseApp, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    if (alertNetwork != null) {
                        alertNetwork.dismiss();
                    }
                }
            });
            alertNetwork = builder.create();
            alertNetwork.show();
            Button pbutton = alertNetwork.getButton(DialogInterface.BUTTON_POSITIVE);
            pbutton.setTextColor(Color.BLACK);
        }
    }

    public void isDismissNetworkAlert() {
        if (alertNetwork != null && alertNetwork.isShowing()) {
            alertNetwork.dismiss();
        }
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
