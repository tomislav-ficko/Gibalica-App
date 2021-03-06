package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.base.BaseFragment
import hr.fer.tel.gibalica.databinding.FragmentTrainingSelectionBinding
import hr.fer.tel.gibalica.utils.DetectionUseCase
import hr.fer.tel.gibalica.utils.Difficulty
import hr.fer.tel.gibalica.utils.TrainingType
import timber.log.Timber

class TrainingSelectionFragment : BaseFragment() {

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

        requestPermission()
        binding.apply {
            ivBack.setOnClickListener { returnToMainFragment() }
            btnLeftHand.setOnClickListener { navigateToIllustrationFragment(TrainingType.LEFT_HAND) }
            btnRightHand.setOnClickListener { navigateToIllustrationFragment(TrainingType.RIGHT_HAND) }
            btnBothHands.setOnClickListener { navigateToIllustrationFragment(TrainingType.BOTH_HANDS) }
            btnTPose.setOnClickListener { navigateToIllustrationFragment(TrainingType.T_POSE) }
            btnSquat.setOnClickListener { navigateToIllustrationFragment(TrainingType.SQUAT) }
            btnRandom.setOnClickListener { navigateToDetectionFragment() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun returnToMainFragment() {
        findNavController().navigate(
            TrainingSelectionFragmentDirections.actionTrainingSelectionFragmentToMainFragment()
        )
    }

    private fun navigateToIllustrationFragment(trainingType: TrainingType) {
        findNavController().navigate(
            TrainingSelectionFragmentDirections
                .actionTrainingSelectionFragmentToIllustrationFragment(trainingType)
        )
    }

    private fun navigateToDetectionFragment() {
        findNavController().navigate(
            TrainingSelectionFragmentDirections.actionTrainingSelectionFragmentToDetectionFragment(
                detectionUseCase = DetectionUseCase.TRAINING,
                trainingType = TrainingType.RANDOM,
                difficulty = Difficulty.NONE,
                detectionLengthMinutes = 0
            )
        )
    }
}
