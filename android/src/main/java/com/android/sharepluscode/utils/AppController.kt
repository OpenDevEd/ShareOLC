package com.android.sharepluscode.utils

import com.google.firebase.FirebaseApp
import com.psp.jslocale.JSLocaleApplication

class AppController : JSLocaleApplication() {

    override fun onCreate() {
        super.onCreate()
        FirebaseApp.initializeApp(this)
    }
}