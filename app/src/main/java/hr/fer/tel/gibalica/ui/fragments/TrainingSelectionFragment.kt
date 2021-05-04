package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.databinding.FragmentTrainingSelectionBinding
import hr.fer.tel.gibalica.utils.TrainingType
import timber.log.Timber

class TrainingSelectionFragment : Fragment() {

    private var _binding: FragmentTrainingSelectionBinding? = null
    private val binding: FragmentTrainingSelectionBinding
        get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingSelectionBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            ivBack.setOnClickListener {
                findNavController().navigate(R.id.action_trainingSelectionFragment_to_mainFragment)
            }
            btnLeftHand.setOnClickListener { navigateToTrainingFragment(TrainingType.LEFT_HAND) }
            btnRightHand.setOnClickListener { navigateToTrainingFragment(TrainingType.RIGHT_HAND) }
            btnBothHands.setOnClickListener { navigateToTrainingFragment(TrainingType.BOTH_HANDS) }
            btnTPose.setOnClickListener { navigateToTrainingFragment(TrainingType.T_POSE) }
            btnSquat.setOnClickListener { navigateToTrainingFragment(TrainingType.SQUAT) }
            btnRandom.setOnClickListener { navigateToTrainingFragment(TrainingType.RANDOM) }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToTrainingFragment(trainingType: TrainingType) {
        findNavController().navigate(
            TrainingSelectionFragmentDirections
                .actionTrainingSelectionFragmentToTrainingFragment(trainingType)
        )
    }
}
