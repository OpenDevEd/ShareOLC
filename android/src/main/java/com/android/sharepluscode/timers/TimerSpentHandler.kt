package com.android.sharepluscode.timers

import android.os.Handler
import android.util.Log
import com.android.sharepluscode.utils.JSConstant
import java.lang.StringBuilder

class TimerSpentHandler {

    val spentHandler: Handler = Handler()
    var isSpentRunning = false
    var spentStopHandler = false

    private var mSeconds = 0
    private var mStage1Seconds = 0
    private var mStage2Seconds = 0
    private var mStage3Seconds = 0
    private var mStage4Seconds = 0
    private var mStage5Seconds = 0

    var stage1Data = ""
    var stage2Data = ""
    var stage3Data = ""
    var stage4Data = ""
    var stage5Data = ""

    var mCurrentStatus = 0

    val spentRunnable: Runnable = object : Runnable {
        override fun run() {
            if (!spentStopHandler) {
                try {
                    isSpentRunning = true
                    mSeconds += 1
                    if (mCurrentStatus == JSConstant.JSEVENT_STAGE1) {
                        mStage1Seconds += 1
                        stage1Data = "1,$mStage1Seconds;"
                    } else if (mCurrentStatus == JSConstant.JSEVENT_STAGE2) {
                        mStage2Seconds += 1
                        stage2Data = "2,$mStage2Seconds;"
                    } else if (mCurrentStatus == JSConstant.JSEVENT_STAGE3) {
                        mStage3Seconds += 1
                        stage3Data = "3,$mStage3Seconds;"
                    } else if (mCurrentStatus == JSConstant.JSEVENT_STAGE4) {
                        mStage4Seconds += 1
                        stage4Data = "4,$mStage4Seconds;"
                    } else if (mCurrentStatus == JSConstant.JSEVENT_STAGE5) {
                        mStage5Seconds += 1
                        stage5Data = "5,$mStage5Seconds;"
                    } else if (mCurrentStatus == JSConstant.JSEVENT_STAGE_END) {
                        removeSpentTimerCallbacks()
                    }
                    sendDataHome()
                    spentHandler.postDelayed(this, UPDATE_INTERVAL)
                } catch (e: Exception) {
                    e.printStackTrace()
                    isSpentRunning = false
                }
            }
        }
    }


    fun sendDataHome(): String {
        val builder = StringBuilder()
        if (stage1Data.isNotEmpty()) {
            builder.append(stage1Data).append(" ")
        }
        if (stage2Data.isNotEmpty()) {
            builder.append(stage2Data).append(" ")
        }
        if (stage3Data.isNotEmpty()) {
            builder.append(stage3Data).append(" ")
        }
        if (stage4Data.isNotEmpty()) {
            builder.append(stage4Data).append(" ")
        }
        if (stage5Data.isNotEmpty()) {
            builder.append(stage5Data)
        }
        //Log.e("sendDataHome: ", "===> $builder")
        return builder.toString()
    }

    fun resetData() {
        mSeconds = 0
        mStage1Seconds = 0
        mStage2Seconds = 0
        mStage3Seconds = 0
        mStage4Seconds = 0
        mStage5Seconds = 0

        stage1Data = ""
        stage2Data = ""
        stage3Data = ""
        stage4Data = ""
        stage5Data = ""
        mCurrentStatus = 0
    }

    fun updateData(mStatus: Int) {
        this.mCurrentStatus = mStatus
    }

    fun removeSpentTimerCallbacks() {
        spentHandler.removeCallbacksAndMessages(null)
        isSpentRunning = false
        spentStopHandler = true
        Log.e("removeSpentCallbacks : ", "===> " + "cleared")
    }

    companion object {
        private const val UPDATE_INTERVAL = 1000L
    }
}