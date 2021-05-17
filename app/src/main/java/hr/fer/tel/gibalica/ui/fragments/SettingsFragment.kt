package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.databinding.FragmentSettingsBinding
import hr.fer.tel.gibalica.utils.tryCast
import hr.fer.tel.gibalica.viewModel.Setting
import hr.fer.tel.gibalica.viewModel.SettingsViewModel
import timber.log.Timber

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding: FragmentSettingsBinding
        get() = _binding!!
    private val viewModel by viewModels<SettingsViewModel>()

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
        defineObservers()
        defineActions()
        getSavedValues()
        binding.swAccessibility.showText = true
    }

    override fun onPause() {
        super.onPause()
        viewModel.saveSettings(binding)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun defineObservers() {
        viewModel.savedSettingsLiveData.observe(viewLifecycleOwner, { map ->
            map.entries.forEach { entry ->
                when (entry.key) {
                    Setting.LANGUAGE -> {
                        entry.value.tryCast<Pair<Int, String>> {
                            val buttonId = if (first != -1) first else binding.rbEnglish.id
                            binding.rgLanguage.check(buttonId)
                        }
                    }
                    Setting.SOUND ->
                        binding.swSound.isChecked = entry.value as Boolean
                    Setting.VOICE_CONTROL ->
                        binding.swVoice.isChecked = entry.value as Boolean
                    Setting.ACCESSIBILITY ->
                        binding.swAccessibility.isChecked = entry.value as Boolean
                }
            }
        })
    }

    private fun defineActions() {
        binding.ivBack.setOnClickListener {
            findNavController().navigate(
                SettingsFragmentDirections.actionSettingsFragmentToMainFragment()
            )
        }
    }

    private fun getSavedValues() {
        viewModel.loadSettings()
    }
}
