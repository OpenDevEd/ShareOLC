package com.android.sharepluscode.timers

import android.os.Handler
import android.util.Log


class StartSecondsTimer {
    val secondsTimeHandler: Handler = Handler()
    var isSecondsRunning = false
    var startStopHandler = false

    private var mSeconds = 0
    private var mSecondsTimerTickListener: SecondTimerTickListener? = null
    private var totalSecondsDelayed = 5 //5 seconds delayed start timers

    val secondsTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!startStopHandler) {
                try {
                    isSecondsRunning = true
                    if (mSecondsTimerTickListener != null) {
                        mSecondsTimerTickListener?.onTickListener(totalSecondsDelayed - mSeconds)
                    }
                    mSeconds += 1
                    secondsTimeHandler.postDelayed(this, UPDATE_INTERVAL)
                } catch (e: Exception) {
                    e.printStackTrace()
                    isSecondsRunning = false
                }
            }
        }
    }

    fun removeSecondsTimerCallbacks() {
        secondsTimeHandler.removeCallbacksAndMessages(null)
        isSecondsRunning = false
        startStopHandler = true
        Log.e("secondsCallbacks : ", "===> " + "cleared")
    }

    interface SecondTimerTickListener {
        fun onTickListener(seconds: Int)
    }

    fun setOnTimeListener(timerTickListener: SecondTimerTickListener?) {
        mSecondsTimerTickListener = timerTickListener
    }

    companion object {
        private const val UPDATE_INTERVAL = 1000L
    }

}