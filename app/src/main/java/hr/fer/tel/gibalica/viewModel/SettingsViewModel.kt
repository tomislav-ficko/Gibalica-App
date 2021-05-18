package hr.fer.tel.gibalica.viewModel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.preference.PreferenceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.fer.tel.gibalica.databinding.FragmentSettingsBinding
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.viewModel.Setting.*
import timber.log.Timber
import javax.inject.Inject
import kotlin.collections.set

@HiltViewModel
class SettingsViewModel @Inject constructor(application: Application) :
    AndroidViewModel(application) {

    val savedSettingsLiveData = MutableLiveData<Map<Setting, Any>>()

    fun saveSettings(binding: FragmentSettingsBinding) {
        val selectedLanguage = getSelectedLanguage(binding)
        with(binding) {
            PreferenceManager
                .getDefaultSharedPreferences(getApplication<Application>().applicationContext)
                .edit()
                .putBoolean(SOUND.name, swSound.isChecked)
                .putBoolean(VOICE_CONTROL.name, swVoice.isChecked)
                .putBoolean(ACCESSIBILITY.name, swAccessibility.isChecked)
                .putInt(Language.LANGUAGE_BUTTON_ID, selectedLanguage.first)
                .putString(LANGUAGE.name, selectedLanguage.second.name)
                .apply()
        }
    }

    fun loadSettings() {
        val map = mutableMapOf<Setting, Any>()
        with(
            PreferenceManager.getDefaultSharedPreferences(
                getApplication<Application>().applicationContext
            )
        ) {
            val languageString = getString(LANGUAGE.name, Language.EN.name)
            val languageButtonId = getInt(Language.LANGUAGE_BUTTON_ID, -1)
            map[LANGUAGE] = Pair(languageButtonId, languageString) as Any
            map[SOUND] = getBoolean(SOUND.name, false) as Any
            map[VOICE_CONTROL] = getBoolean(VOICE_CONTROL.name, false) as Any
            map[ACCESSIBILITY] = getBoolean(ACCESSIBILITY.name, false) as Any
        }
        savedSettingsLiveData.postValue(map)
    }

    private fun getSelectedLanguage(binding: FragmentSettingsBinding): Pair<Int, Language> {
        with(binding) {
            return when (rgLanguage.checkedRadioButtonId) {
                rbCroatian.id -> Pair(rbCroatian.id, Language.HR)
                rbEnglish.id -> Pair(rbEnglish.id, Language.EN)
                else -> {
                    Timber.d("Unknown language selected, setting English by default.")
                    Pair(rbEnglish.id, Language.EN)
                }
            }
        }
    }
}

enum class Setting {
    LANGUAGE,
    SOUND,
    VOICE_CONTROL,
    ACCESSIBILITY
}
