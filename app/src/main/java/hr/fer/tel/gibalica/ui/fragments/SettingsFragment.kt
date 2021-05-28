package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import hr.fer.tel.gibalica.databinding.FragmentSettingsBinding
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.utils.Setting
import hr.fer.tel.gibalica.utils.Setting.*
import hr.fer.tel.gibalica.utils.tryCast
import timber.log.Timber

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineActions()
        loadSettings()
        binding.swAccessibility.showText = true
    }

    override fun onPause() {
        super.onPause()
        saveSettings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun defineActions() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToMainFragment()
            )
        }
    }

    private fun saveSettings() {
        val selectedLanguage = getSelectedLanguage(binding)
        with(binding) {
            PreferenceManager
                .getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(SOUND.name, swSound.isChecked)
                .putBoolean(VOICE_CONTROL.name, swVoice.isChecked)
                .putBoolean(ACCESSIBILITY.name, swAccessibility.isChecked)
                .putInt(Language.LANGUAGE_BUTTON_ID, selectedLanguage.first)
                .putString(LANGUAGE.name, selectedLanguage.second.name)
                .apply()
        }
    }

    private fun loadSettings() {
        val map = mutableMapOf<Setting, Any>()
        with(PreferenceManager.getDefaultSharedPreferences(context)) {
            val languageString = getString(LANGUAGE.name, Language.EN.name)
            val languageButtonId = getInt(Language.LANGUAGE_BUTTON_ID, -1)
            map[LANGUAGE] = Pair(languageButtonId, languageString) as Any
            map[SOUND] = getBoolean(SOUND.name, false) as Any
            map[VOICE_CONTROL] = getBoolean(VOICE_CONTROL.name, false) as Any
            map[ACCESSIBILITY] = getBoolean(ACCESSIBILITY.name, false) as Any
        }
        setData(map)
    }

    private fun setData(map: Map<Setting, Any>) {
        binding.apply {
            map.entries.forEach { entry ->
                when (entry.key) {
                    LANGUAGE -> {
                        entry.value.tryCast<Pair<Int, String>> {
                            val buttonId =
                                if (first != -1) first
                                else rbEnglish.id
                            rgLanguage.check(buttonId)
                        }
                    }
                    SOUND -> swSound.isChecked = entry.value as Boolean
                    VOICE_CONTROL -> swVoice.isChecked = entry.value as Boolean
                    ACCESSIBILITY -> swAccessibility.isChecked = entry.value as Boolean
                }
            }
        }
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
