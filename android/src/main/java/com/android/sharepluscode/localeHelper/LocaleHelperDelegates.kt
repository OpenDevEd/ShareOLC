package com.android.sharepluscode.localeHelper

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.Intent.getIntent
import android.os.Build
import android.view.View
import androidx.core.content.ContextCompat.startActivity
import java.util.*


interface LocaleHelperActivityDelegate {
    fun setLocale(activity: Activity, newLocale: Locale)
    fun attachBaseContext(newBase: Context): Context
    //fun onResumed(activity: Activity)
    fun onCreate(activity: Activity)
}

class LocaleHelperActivityDelegateImpl : LocaleHelperActivityDelegate {

    override fun onCreate(activity: Activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.window.decorView.layoutDirection =
                    if (LocaleHelper.isRTL(Locale.getDefault())) View.LAYOUT_DIRECTION_RTL else View.LAYOUT_DIRECTION_LTR
        }
        locale = Locale.getDefault()
    }


    private var locale: Locale = Locale.getDefault()
    override fun setLocale(activity: Activity, newLocale: Locale) {
        LocaleHelper.setLocale(activity, newLocale)
        locale = newLocale
        restartActivity(activity)
        //activity.recreate()
    }

    private fun restartActivity(activity: Activity) {
        val intent = activity.intent
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        activity.finish()
        activity.startActivity(intent)
    }

    override fun attachBaseContext(newBase: Context): Context {
        return LocaleHelper.onAttach(newBase)
    }

   /* override fun onPaused() {
        //locale = Locale.getDefault()
    }*/

    /*override fun onResumed(activity: Activity) {
        if (locale == Locale.getDefault()) return
        activity.recreate()
    }*/
}

class LocaleHelperApplicationDelegate {
    fun attachBaseContext(base: Context): Context {
        return LocaleHelper.onAttach(base)
    }

    fun onConfigurationChanged(context: Context) {
        LocaleHelper.onAttach(context)
    }
}