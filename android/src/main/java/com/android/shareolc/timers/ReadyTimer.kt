package com.android.shareolc.timers

import android.os.Handler
import android.util.Log


class ReadyTimer {
    val readyTimeHandler: Handler = Handler()
    var isReadyRunning = false
    //var stopHandler = false

    private var mSeconds = 0
    private var mReadyTimerTickListener: ReadyTimerTickListener? = null

    val readyTimeRunnable: Runnable = object : Runnable {
        override fun run() {
            //if (!stopHandler) {
                try {
                    isReadyRunning = true
                    if (mReadyTimerTickListener != null) {
                        mReadyTimerTickListener?.onTickListener(10 - mSeconds)
                    }
                    mSeconds += 1
                    Log.e("ReadyTimer : ", "===> $mSeconds")
                    readyTimeHandler.postDelayed(this, UPDATE_INTERVAL)
                } catch (e: Exception) {
                    e.printStackTrace()
                    isReadyRunning = false
                }
           // }
        }
    }

    fun removeReadyTimerCallbacks() {
        readyTimeHandler.removeCallbacksAndMessages(null)
        isReadyRunning = false
        mSeconds = 0
       // stopHandler = true
        Log.e("removeReadyCallbacks : ", "===> " + "cleared")
    }

    interface ReadyTimerTickListener {
        fun onTickListener(minutes: Int)
    }

    fun setOnTimeListener(timerTickListener: ReadyTimerTickListener?) {
        this.mReadyTimerTickListener = timerTickListener
    }

    companion object {
        private const val UPDATE_INTERVAL = 1000L
    }

}