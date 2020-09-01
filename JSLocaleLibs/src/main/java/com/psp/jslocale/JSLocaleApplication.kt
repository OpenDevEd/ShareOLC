package com.psp.jslocale

import android.app.Application
import android.content.Context
import android.content.res.Configuration

open class JSLocaleApplication :Application() {
    private val localeApplicationDelegates=EasyLocaleApplicationDelegates()
    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(localeApplicationDelegates.attachBaseContext(base!!))
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        localeApplicationDelegates.onConfigurationChanged(this)
    }
}