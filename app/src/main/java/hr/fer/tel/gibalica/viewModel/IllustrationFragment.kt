package hr.fer.tel.gibalica.viewModel

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.databinding.FragmentIllustrationBinding
import hr.fer.tel.gibalica.utils.Difficulty
import hr.fer.tel.gibalica.utils.DetectionUseCase
import hr.fer.tel.gibalica.utils.TrainingType
import timber.log.Timber

class IllustrationFragment : Fragment() {

    private var _binding: FragmentIllustrationBinding? = null
    private val binding: FragmentIllustrationBinding
        get() = _binding!!
    private val args: IllustrationFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentIllustrationBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val trainingType = args.trainingType
        Timber.d("Showing illustration for training type: ${trainingType.name}")
        val resource = when (trainingType) {
            TrainingType.LEFT_HAND -> R.drawable.illustration_left
            TrainingType.RIGHT_HAND -> R.drawable.illustration_right
            TrainingType.BOTH_HANDS -> R.drawable.illustration_both
            TrainingType.T_POSE -> R.drawable.illustration_t_pose
            TrainingType.SQUAT -> R.drawable.illustration_squat
            TrainingType.RANDOM -> null
        }
        binding.apply {
            resource?.let { tvIllustration.setImageResource(it) }
            btnStart.setOnClickListener { navigateToDetectionFragment() }
            btnClose.setOnClickListener { navigateToTrainingSelectionFragment() }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToTrainingSelectionFragment() {
        findNavController().navigate(
            IllustrationFragmentDirections.actionIllustrationFragmentToTrainingSelectionFragment()
        )
    }

    private fun navigateToDetectionFragment() {
        findNavController().navigate(
            IllustrationFragmentDirections.actionIllustrationFragmentToDetectionFragment(
                detectionUseCase = DetectionUseCase.TRAINING,
                trainingType = args.trainingType,
                difficulty = Difficulty.NONE,
                detectionLengthSeconds = 0
            )
        )
    }
}
