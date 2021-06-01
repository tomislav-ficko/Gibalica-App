package hr.fer.tel.gibalica.utils

import android.content.Context
import androidx.preference.PreferenceManager

class SharedPrefsUtils {
    companion object {
        fun isVoiceRecognitionEnabled(context: Context): Boolean {
            val sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context)
            return sharedPrefs.getBoolean(Setting.VOICE_CONTROL.name, false)
        }
    }
}
