package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.databinding.FragmentMainBinding
import hr.fer.tel.gibalica.utils.DetectionUseCase
import timber.log.Timber

class MainFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private val binding: FragmentMainBinding
        get() = _binding!!
    private var _binding: FragmentMainBinding? = null

    private var speechRecognizerEnabled: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMainBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        defineActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun defineActions() {
        binding.apply {
            ivSettings.setOnClickListener { navigateToSettingsFragment() }
            btnTraining.setOnClickListener { navigateToTrainingSelectionFragment() }
            btnCompetition.setOnClickListener { navigateToSettingsSelectionFragment(DetectionUseCase.COMPETITION) }
            btnDayNight.setOnClickListener { navigateToSettingsSelectionFragment(DetectionUseCase.DAY_NIGHT) }
        }
    }

    private fun navigateToSettingsFragment() {
        Timber.d("Navigating to SettingsFragment.")
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToSettingsFragment()
        )
    }

    private fun navigateToTrainingSelectionFragment() {
        Timber.d("Navigating to TrainingSelectionFragment")
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToTrainingSelectionFragment()
        )
    }

    private fun navigateToSettingsSelectionFragment(detectionUseCase: DetectionUseCase) {
        Timber.d("Navigating to CompetitionSelectionFragment")
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToSettingsSelectionFragment(detectionUseCase)
        )
    }
}
