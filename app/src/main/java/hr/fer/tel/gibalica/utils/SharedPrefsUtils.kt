package hr.fer.tel.gibalica.utils

import android.content.Context
import androidx.preference.PreferenceManager
import timber.log.Timber

class SharedPrefsUtils {
    companion object {
        fun isVoiceRecognitionEnabled(context: Context): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPrefs.getBoolean(Setting.VOICE_CONTROL.name, false)
        }

        fun isSoundEnabled(context: Context): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPrefs.getBoolean(Setting.SOUND.name, false)
        }

        fun isAccessibilityEnabled(context: Context): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPrefs.getBoolean(Setting.ACCESSIBILITY.name, false)
        }

        fun getApplicationLanguage(context: Context): Language {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return when (sharedPrefs.getString(Setting.LANGUAGE.name, null)) {
                Language.EN.name -> Language.EN
                Language.HR.name -> Language.HR
                else -> {
                    Timber.d("Saved language not recognized, returning english by default.")
                    Language.EN
                }
            }
        }

        fun isFirstApplicationStart(context: Context): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPrefs.getBoolean(Setting.FIRST_START.name, true)
        }
    }
}
