package com.android.sharepluscode.timers

import android.os.Handler
import android.util.Log

class HandlerTimer {

    val timeHandler: Handler = Handler()
    var isRunning = false
    var stopHandler = false

    private var mSeconds = 1000L
    private var mTimerTickListener: TimerTickListener? = null
    private var totalMinutesDelayed = 1260000L // 20 minutes delayed timer...

    val timeRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!stopHandler) {
                try {
                    isRunning = true
                    if (mTimerTickListener != null) {
                        mTimerTickListener?.onTickListener(totalMinutesDelayed - mSeconds)
                    }
                    mSeconds += 1000L

                    timeHandler.postDelayed(this, UPDATE_INTERVAL)
                } catch (e: Exception) {
                    e.printStackTrace()
                    isRunning = false
                }
            }
        }
    }

    fun removeTimerCallbacks() {
        timeHandler.removeCallbacksAndMessages(null)
        isRunning = false
        stopHandler = true
        Log.e("removeTimerCallbacks : ", "===> cleared $stopHandler")
    }

    interface TimerTickListener {
        fun onTickListener(milliSeconds: Long)
    }

    fun setOnTimeListener(timerTickListener: TimerTickListener) {
        mTimerTickListener = timerTickListener
    }

    companion object {
        private const val UPDATE_INTERVAL = 1000L
    }
}