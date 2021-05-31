package hr.fer.tel.gibalica.ui.fragments

import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import hr.fer.tel.gibalica.databinding.FragmentMainBinding
import hr.fer.tel.gibalica.utils.*
import hr.fer.tel.gibalica.viewModel.MainViewModel
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
        defineObserver()
        getStoredValues()
    }

    override fun onResume() {
        super.onResume()
        if (speechRecognizerEnabled) {
            binding.btnVoice.visible()
        } else {
            binding.btnVoice.invisible()
        }
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
            btnVoice.setOnClickListener { startVoiceRecognizer() }
        }
    }

    private fun defineObserver() {
        viewModel.speechRecognizer.observe(requireActivity()) { recognizerEnabledValue ->
            Timber.d("LiveData value changed!")
            speechRecognizerEnabled = recognizerEnabledValue
        }
    }

    private fun getStoredValues() {
        speechRecognizerEnabled = SharedPrefsUtils.isVoiceRecognitionEnabled(requireContext())
    }

    private fun startVoiceRecognizer() {
        enableScreen(isEnabled = false)
        val recognizer = SpeechRecognizer.createSpeechRecognizer(requireContext())
        recognizer.setRecognitionListener(getRecognitionListener())
        recognizer.startListening(
            Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            }
        )
    }

    private fun getRecognitionListener(): RecognitionListener {
        return object : BaseRecognitionListener() {
            override fun onResults(results: Bundle?) {
                enableScreen(isEnabled = true)
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val scores = results?.getFloatArray(SpeechRecognizer.CONFIDENCE_SCORES)
                Timber.d("Recognized content: ${matches.toString()} ${matches?.size}")
                Timber.d("Confidence score: ${scores?.get(0)}")
                matches?.let {
                    val recognizedSentence = it[0].toLowerCase()
                    if (recognizedSentence.containsAllNecessaryParameters()) {
                        navigateToDetectionBasedOnRecognizerOutput(recognizedSentence)
                    } else {
                        showNegativeMessage()
                    }
                }
            }
        }
    }

    // Sentence should be "start ___ with difficulty ___ and length ___ minutes"
    //    or "start training for ___ pose"
    private fun navigateToDetectionBasedOnRecognizerOutput(recognizedSentence: String) {
        val detectionUseCase = when {
            recognizedSentence.contains("training") -> DetectionUseCase.TRAINING
            recognizedSentence.contains("competition") -> DetectionUseCase.COMPETITION
            recognizedSentence.contains("day night") -> DetectionUseCase.DAY_NIGHT
            else -> {
                Timber.d("Detection use case not recognized.")
                null
            }
        }
        val trainingType =
            if (detectionUseCase == DetectionUseCase.TRAINING) {
                when {
                    recognizedSentence.contains("left") -> TrainingType.LEFT_HAND
                    recognizedSentence.contains("right") -> TrainingType.RIGHT_HAND
                    recognizedSentence.contains("both") -> TrainingType.BOTH_HANDS
                    recognizedSentence.contains("squat") -> TrainingType.SQUAT
                    recognizedSentence.contains("pose") -> TrainingType.T_POSE
                    else -> TrainingType.RANDOM
                }
            } else null
        val difficulty =
            if (detectionUseCase == DetectionUseCase.COMPETITION || detectionUseCase == DetectionUseCase.DAY_NIGHT) {
                when {
                    recognizedSentence.contains("easy") -> Difficulty.EASY
                    recognizedSentence.contains("medium") -> Difficulty.MEDIUM
                    else -> Difficulty.HARD
                }
            } else null
        val detectionLengthSeconds =
            if (recognizedSentence.contains("length")) {
                recognizedSentence.filter { it.isDigit() }.toLong()
            } else null

        // Start detection
        when {
            trainingType != null && trainingType == TrainingType.RANDOM ->
                navigateToDetectionFragment(detectionUseCase!!, trainingType, Difficulty.NONE, 0)
            trainingType != null -> navigateToIllustrationFragment(trainingType)
            detectionUseCase == null -> Timber.d("Detection use case not recognized.")
            difficulty == null -> Timber.d("Difficulty not recognized.")
            detectionLengthSeconds == null -> Timber.d("Detection length not recognized.")
            else ->
                navigateToDetectionFragment(detectionUseCase, TrainingType.RANDOM, difficulty, detectionLengthSeconds)
        }
    }

    private fun enableScreen(isEnabled: Boolean) {
        binding.apply {
            if (isEnabled) recognitionBackground.invisible() else recognitionBackground.visible()
            btnVoice.isEnabled = isEnabled
            btnTraining.isEnabled = isEnabled
            btnCompetition.isEnabled = isEnabled
            btnDayNight.isEnabled = isEnabled
            ivSettings.isEnabled = isEnabled
        }
    }

    private fun showNegativeMessage() {
        Toast.makeText(
            context,
            "All necessary parameters were not recognized, please try again",
            Toast.LENGTH_LONG
        ).show()
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

    private fun navigateToIllustrationFragment(trainingType: TrainingType) {
        Timber.d("Navigating to IllustrationFragment")
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToIllustrationFragment(trainingType)
        )
    }

    private fun navigateToDetectionFragment(
        detectionUseCase: DetectionUseCase,
        trainingType: TrainingType,
        difficulty: Difficulty,
        detectionLengthSeconds: Long
    ) {
        Timber.d("Navigating to DetectionFragment")
        findNavController().navigate(
            MainFragmentDirections.actionMainFragmentToDetectionFragment(
                detectionUseCase,
                trainingType,
                difficulty,
                detectionLengthSeconds
            )
        )
    }

    private fun String.containsAllNecessaryParameters(): Boolean {
        return when {
            contains("training") &&
                    (contains("left") or
                            contains("right") or
                            contains("both") or
                            contains("squat") or
                            contains("pose") or
                            contains("random")) -> true
            !contains("competition") or !contains("day night") -> false
            !contains("easy") or !contains("medium") or !contains("hard") -> false
            !contains("length") -> false
            else -> true
        }
    }
}
