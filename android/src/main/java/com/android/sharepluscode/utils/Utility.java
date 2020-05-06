package com.android.sharepluscode.utils;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

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
                (seconds == 0 ? "00" : seconds < 10 ? "0" + seconds : String.valueOf(seconds))+ "s";

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

}
