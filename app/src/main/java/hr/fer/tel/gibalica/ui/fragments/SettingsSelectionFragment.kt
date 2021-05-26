package hr.fer.tel.gibalica.ui.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.databinding.FragmentSettingsSelectionBinding
import hr.fer.tel.gibalica.utils.DetectionUseCase
import hr.fer.tel.gibalica.utils.Difficulty
import hr.fer.tel.gibalica.utils.TrainingType
import timber.log.Timber

class SettingsSelectionFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentSettingsSelectionBinding? = null
    private val binding: FragmentSettingsSelectionBinding
        get() = _binding!!

    private val args: SettingsSelectionFragmentArgs by navArgs()
    private var competitionLengthSeconds = 60L

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsSelectionBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setData()
        defineActions()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onClick(clickedView: View?) {
        val difficulty = when (clickedView) {
            binding.btnEasy -> Difficulty.EASY
            binding.btnMedium -> Difficulty.MEDIUM
            binding.btnHard -> Difficulty.HARD
            else -> return
        }
        Timber.d("Navigating to DetectionFragment")
        findNavController().navigate(
            SettingsSelectionFragmentDirections.actionSettingsSelectionFragmentToTrainingFragment(
                detectionUseCase = args.detectionUseCase,
                difficulty = difficulty,
                competitionLengthSeconds = competitionLengthSeconds,
                trainingType = TrainingType.RANDOM
            )
        )
    }

    private fun setData() {
        binding.apply {
            when (args.detectionUseCase) {
                DetectionUseCase.COMPETITION -> {
                    btnEasy.setButtonText(R.string.comp_btn_easy, R.string.comp_btn_easy_value)
                    btnMedium.setButtonText(R.string.comp_btn_medium, R.string.comp_btn_medium_value)
                    btnHard.setButtonText(R.string.comp_btn_hard, R.string.comp_btn_hard_value)
                    lengthSlider.apply {
                        valueFrom = 1F
                        valueTo = 13F
                        stepSize = 3F
                        value = 1F
                    }
                }
                DetectionUseCase.DAY_NIGHT -> {
                    btnEasy.setButtonText(R.string.comp_btn_easy, R.string.day_night_btn_easy_value)
                    btnMedium.setButtonText(R.string.comp_btn_medium, R.string.day_night_btn_medium_value)
                    btnHard.setButtonText(R.string.comp_btn_hard, R.string.day_night_btn_hard_value)
                    lengthSlider.apply {
                        valueFrom = 3F
                        valueTo = 7F
                        stepSize = 2F
                        value = 3F
                    }
                }
                else -> {
                }
            }
        }
    }

    private fun defineActions() {
        binding.apply {
            ivBack.setOnClickListener {
                Timber.d("Navigating to MainFragment")
                navigateToMainFragment()
            }
            btnEasy.setOnClickListener(this@SettingsSelectionFragment)
            btnEasy.setOnClickListener(this@SettingsSelectionFragment)
            btnHard.setOnClickListener(this@SettingsSelectionFragment)
            lengthSlider.addOnChangeListener { _, value, _ ->
                Timber.d("$value minutes selected in slider.")
                competitionLengthSeconds = (value * 60).toLong()
                Timber.d("$competitionLengthSeconds")
            }
        }
    }

    private fun navigateToMainFragment() {
        findNavController().navigate(
            SettingsSelectionFragmentDirections.actionSettingsSelectionFragmentToMainFragment()
        )
    }

    private fun Button.setButtonText(@StringRes difficultyString: Int, @StringRes timeString: Int) {
        text = getString(
            R.string.comp_btn_format_specifier,
            getString(difficultyString),
            getString(timeString)
        )
    }
}
