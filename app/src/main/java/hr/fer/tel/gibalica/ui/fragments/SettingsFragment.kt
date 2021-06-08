package hr.fer.tel.gibalica.ui.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import hr.fer.tel.gibalica.base.BaseFragment
import hr.fer.tel.gibalica.databinding.FragmentSettingsBinding
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.utils.Setting
import hr.fer.tel.gibalica.utils.Setting.*
import hr.fer.tel.gibalica.utils.tryCast
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber

class SettingsFragment : BaseFragment() {

    companion object {
        private const val REQUEST_CODE_RECORD_AUDIO = 11
    }

    private val viewModel: MainViewModel by activityViewModels()
    private val binding: FragmentSettingsBinding
        get() = _binding!!
    private var _binding: FragmentSettingsBinding? = null

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
        loadSettings()
        defineActions()
        binding.swAccessibility.showText = true
    }

    override fun onResume() {
        super.onResume()
        when {
            recordAudioPermissionNotGranted() -> {
                Timber.d("Audio recording permission denied, turning the feature off.")
                binding.swVoice.isChecked = false
            }
            binding.swVoice.isChecked -> {
                Timber.d("Audio recording permission granted, enabling recognizer.")
                enableRecognizer()
            }
            else -> Timber.d("Audio recording permission granted, but option not enabled.")
        }
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
        binding.apply {
            ivBack.setOnClickListener { navigateToMainFragment() }
            btnGuide.setOnClickListener { navigateToGuideFragment() }
            swVoice.setOnCheckedChangeListener { _, isChecked ->
                when {
                    !isChecked -> viewModel.disableSpeechRecognizer()
                    recordAudioPermissionNotGranted() -> requestRecordAudioPermission()
                    else -> enableRecognizer()
                }
            }
        }
    }

    private fun enableRecognizer() {
        if (SpeechRecognizer.isRecognitionAvailable(requireContext())) {
            viewModel.enableSpeechRecognizer()
        } else {
            Timber.d("SpeechRecognizer not available on device, turning feature off.")
            Toast.makeText(requireContext(), "Not available on device.", Toast.LENGTH_LONG).show()
            binding.swVoice.isChecked = false
        }
    }

    private fun requestRecordAudioPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.RECORD_AUDIO),
            REQUEST_CODE_RECORD_AUDIO
        )
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

    private fun navigateToMainFragment() {
        Timber.d("Navigating to MainFragment.")
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToMainFragment())
    }

    private fun navigateToGuideFragment() {
        Timber.d("Navigating to GuideFragment.")
        findNavController().navigate(SettingsFragmentDirections.actionSettingsFragmentToGuideFragment2())
    }

    private fun recordAudioPermissionNotGranted() = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.RECORD_AUDIO
    ) != PackageManager.PERMISSION_GRANTED
}
