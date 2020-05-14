package com.android.sharepluscode.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.android.sharepluscode.BuildConfig;
import com.android.sharepluscode.model.DeviceModel;

public class Utility {

    public static String millisToTimeFormat(long mSeconds) {
        long passMillis = mSeconds * 1000;
        //long seconds = (passMillis / 1000) % 60;
        long minutes = (passMillis / (1000 * 60)) % 60;
        long hours = passMillis / (1000 * 60 * 60);
        return (hours == 0 ? "00" : hours < 10 ? "0" + hours : String.valueOf(hours)) +
                "h" +
                (minutes == 0 ? "00" : minutes < 10 ? "0" + minutes : String.valueOf(minutes)) +
                "m" /*+
                (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : String.valueOf(seconds))*/;

    }


    public static String millisToTimeSecondsFormat(long mSeconds) {
        long passMillis = mSeconds * 1000;
        long seconds = (passMillis / 1000) % 60;
        long minutes = (passMillis / (1000 * 60)) % 60;
        long hours = passMillis / (1000 * 60 * 60);
        return (hours == 0 ? "00" : hours < 10 ? "0" + hours : String.valueOf(hours)) +
                "h" +
                (minutes == 0 ? "00" : minutes < 10 ? "0" + minutes : String.valueOf(minutes)) +
                "m" +
                (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : String.valueOf(seconds)) + "s";

    }

    public static void toastLong(Activity activity, String message) {
        Toast.makeText(activity, "" + message, Toast.LENGTH_LONG).show();
    }

    public static void toastLong(Context activity, String message) {
        Toast.makeText(activity, "" + message, Toast.LENGTH_LONG).show();
    }

    public static void toastShort(Activity activity, String message) {
        Toast.makeText(activity, "" + message, Toast.LENGTH_SHORT).show();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }


    //get device information...
    public static DeviceModel getDeviceModel() {
        DeviceModel deviceModel = new DeviceModel();
        String manufacturer = Build.MANUFACTURER;
        String model = Build.MODEL;
        String deviceOsVersion = Build.VERSION.RELEASE;
        String versionName = BuildConfig.VERSION_NAME;

        String deviceName = "";
        if (model.startsWith(manufacturer)) {
            deviceName = capitalize(model);
        } else {
            deviceName = capitalize(manufacturer) + " " + model;
        }


        deviceModel.setDeviceName(deviceName);
        deviceModel.setAppVersion(versionName);
        deviceModel.setDeviceOsVersion(deviceOsVersion);
        return deviceModel;
    }


    private static String capitalize(String s) {
        if (s == null || s.length() == 0) {
            return "";
        }
        char first = s.charAt(0);
        if (Character.isUpperCase(first)) {
            return s;
        } else {
            return Character.toUpperCase(first) + s.substring(1);
        }
    }


}
