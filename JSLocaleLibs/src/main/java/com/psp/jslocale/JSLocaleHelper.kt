package com.psp.jslocale

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import androidx.core.content.edit
import java.util.*

internal object JSLocaleHelper {

    internal fun onAttachBaseContext(ctx: Context) = setEasylocale(ctx, getCurrentLocale(ctx))

    private fun getCurrentLocale(ctx: Context): Locale {
        EasyLocalePrefrences.getLocaleSharedPreference(ctx)?.let {
            return Locale(
                it.getString(EasyLocalePrefrences.PREF_LANGUAGE, Locale.getDefault().language)!!,
                it.getString(
                    EasyLocalePrefrences.PREF_COUNTRY, Locale.getDefault().country
                )!!
            )
        }
        return Locale.getDefault()
    }


    internal fun setEasylocale(ctx: Context, locale: Locale): Context {
        EasyLocalePrefrences.setLocalePrefrences(ctx, locale)
        Locale.setDefault(locale)
        ctx.resources.configuration.apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                setLocale(locale)
                setLayoutDirection(locale)
                return ctx.createConfigurationContext(this)
            } else {
                this.locale = locale
                ctx.resources.updateConfiguration(
                    this,
                    ctx.resources.displayMetrics
                )
                return ctx
            }
        }
    }

    private object EasyLocalePrefrences {
        internal const val PREF_LANGUAGE = "easylocale.language"
        internal const val PREF_COUNTRY = "easylocale.country"
        private const val PREF_NAME = "easylocalPref"

        internal fun getLocaleSharedPreference(ctx: Context): SharedPreferences? {
            return ctx.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        }

        internal fun setLocalePrefrences(ctx: Context, locale: Locale) {
            getLocaleSharedPreference(ctx)?.edit {
                putString(PREF_LANGUAGE, locale.language)
                putString(PREF_COUNTRY, locale.country)
            }
        }
    }
}