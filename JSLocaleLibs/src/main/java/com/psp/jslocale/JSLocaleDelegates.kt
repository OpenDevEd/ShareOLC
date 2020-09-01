package com.psp.jslocale

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.text.layoutDirection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import java.util.*


class EasyLocaleActivityDelegate : LifecycleObserver {
    private var locale: Locale = Locale.getDefault()
    private var localeChangeListener: EasyLocaleChangeListener? = null
    private lateinit var activity: Activity
    fun setLocale(activity: Activity, mLocale: Locale) {
        if (mLocale == Locale.getDefault()) {
            return
        }
        if (localeChangeListener != null) {
            localeChangeListener?.let { it.onLocaleChanged(locale, mLocale) }
        }
        JSLocaleHelper.setEasylocale(activity, mLocale)
        locale = mLocale
        //activity.recreate()
        restartActivity()
    }

    fun initialise(lifecycleOwner: LifecycleOwner, activity: Activity) {
        this.activity = activity
        lifecycleOwner.lifecycle.addObserver(this)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    fun onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            activity.window.decorView.layoutDirection = Locale.getDefault().layoutDirection
        }
    }

    fun attachBaseContext(ctx: Context): Context {
        return JSLocaleHelper.onAttachBaseContext(ctx)
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    fun onPause() {
        locale = Locale.getDefault()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    fun onResume() {
        if (locale == Locale.getDefault()) {
            return
        }
        else {
            //activity.recreate()
            //restartActivity()
        }
    }

    fun setLocaleChangeListener(listener: EasyLocaleChangeListener) {
        localeChangeListener = listener
    }


    private fun restartActivity() {
        activity.finish()
        val intent = activity.intent
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
        activity.startActivity(intent)
        activity.overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
    }
}


class EasyLocaleApplicationDelegates {
    fun attachBaseContext(ctx: Context): Context {
        return JSLocaleHelper.onAttachBaseContext(ctx)
    }

    fun onConfigurationChanged(ctx: Context) {
        JSLocaleHelper.onAttachBaseContext(ctx)
    }
}

interface EasyLocaleChangeListener {
    fun onLocaleChanged(mOldLocale: Locale, mNewLocale: Locale)
}
