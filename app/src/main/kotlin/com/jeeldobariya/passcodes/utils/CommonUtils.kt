package com.jeeldobariya.passcodes.utils

import android.content.Context
import android.content.Context.MODE_PRIVATE
import com.jeeldobariya.passcodes.R

class CommonUtils {
    companion object {
        fun getCurrTheme(context: Context): Int {
            val sharedPrefs = context.getSharedPreferences(Constant.APP_PREFS_NAME, MODE_PRIVATE)
            val savedThemeStyle =
                sharedPrefs.getInt(Constant.THEME_KEY, R.style.PasscodesTheme_Default)
            return savedThemeStyle
        }

        fun updateCurrTheme(context: Context) {
            context.setTheme(getCurrTheme(context.applicationContext))
        }
    }
}
