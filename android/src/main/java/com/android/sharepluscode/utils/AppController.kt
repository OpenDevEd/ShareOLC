package com.android.sharepluscode.utils

import android.app.Application
import android.content.res.Configuration
import com.android.sharepluscode.localeHelper.LocaleHelper


class AppController : Application() {

    override fun onCreate() {
        super.onCreate()
        LocaleHelper.onAttach(applicationContext)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(applicationContext)
    }
}