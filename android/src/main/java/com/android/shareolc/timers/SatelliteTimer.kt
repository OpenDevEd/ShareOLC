package com.android.shareolc.timers

import android.os.Handler
import android.util.Log


class SatelliteTimer {
    val satelliteTimeHandler: Handler = Handler()
    var isSatelliteRunning = false
    var stopHandler = false

    private var mSeconds = 0
    private var mSatelliteTimerTickListener: SatelliteTimerTickListener? = null
    val satelliteTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!stopHandler) {
                try {
                    isSatelliteRunning = true
                    if (mSatelliteTimerTickListener != null) {
                        mSatelliteTimerTickListener?.onTickListener(3 - mSeconds)
                    }
                    mSeconds += 1
                    satelliteTimeHandler.postDelayed(this, UPDATE_INTERVAL * 60L)
                } catch (e: Exception) {
                    e.printStackTrace()
                    isSatelliteRunning = false
                }
            }
        }
    }

    fun removeSatelliteTimerCallbacks() {
        satelliteTimeHandler.removeCallbacksAndMessages(null)
        isSatelliteRunning = false
        stopHandler = true
        Log.e("SatellitesCallbacks : ", "===> " + "cleared")
    }

    interface SatelliteTimerTickListener {
        fun onTickListener(minutes: Int)
    }

    fun setOnTimeListener(timerTickListener: SatelliteTimerTickListener?) {
        mSatelliteTimerTickListener = timerTickListener
    }

    companion object {
        private const val UPDATE_INTERVAL = 1000L
    }

}