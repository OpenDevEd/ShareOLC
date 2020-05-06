package com.android.sharepluscode.utils;

public class JSConstant {

    public static boolean IS_READY_SHARE= false;
    public static long endTimerMillisecondsDelayed = 10000L; //10 seconds end timers

    //15 minutes delayed timer change in file => HandlerTimer => totalMinutesDelayed
    //Satellites 3 minutes delayed timer change in file => SatelliteTimer => totalMinutesDelayed
    //Start timer 5 seconds delayed timer change in file => StartSecondsTimer => totalSecondsDelayed

    public static double minSatellite = 5;
    public static double accuracyNo = 0.0;
    public static double accuracyHighStart = 1.0;
    public static double accuracyHighEnd = 5.0;
    public static double accuracyMediumStart = 6.0;
    public static double accuracyMediumEnd = 100.0;
}
