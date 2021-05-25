package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.databinding.FragmentMainBinding
import timber.log.Timber

class MainFragment : Fragment() {

    private var _binding: FragmentMainBinding? = null
    private val binding: FragmentMainBinding
        get() = _binding!!

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
            ivLogo.setOnClickListener { navigateToSettingsFragment() }
            btnTraining.setOnClickListener { navigateToTrainingSelectionFragment() }
            btnCompetition.setOnClickListener { navigateToCompetitionSelectionFragment() }
            btnDayNight.setOnClickListener {} // Mode not yet implemented
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

    private fun navigateToCompetitionSelectionFragment() {
        Timber.d("Navigating to CompetitionSelectionFragment")
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToCompetitionSelectionFragment()
        )
    }
}
