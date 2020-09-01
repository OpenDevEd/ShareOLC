package com.psp.jslocale

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import java.util.*

open class JSLocaleAppCompatActivity : AppCompatActivity() {
    private  val easyLocaleActivityDelegate=EasyLocaleActivityDelegate()
    override fun attachBaseContext(newBase: Context?) {
        super.attachBaseContext(easyLocaleActivityDelegate.attachBaseContext(newBase!!))
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        easyLocaleActivityDelegate.initialise(this,this)
    }
    fun setLocale(locale: Locale) {
        easyLocaleActivityDelegate.setLocale(this,locale)
    }
}