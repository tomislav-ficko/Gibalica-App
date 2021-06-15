package hr.fer.tel.gibalica.ui.fragments

import android.graphics.SurfaceTexture
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseDetectionFragment
import hr.fer.tel.gibalica.base.REQUEST_CODE_PERMISSIONS
import hr.fer.tel.gibalica.databinding.FragmentDetectionBinding
import hr.fer.tel.gibalica.utils.*
import hr.fer.tel.gibalica.utils.Constants.DETECTION_INTERVAL_COMPETITION_MILLIS_EASY
import hr.fer.tel.gibalica.utils.Constants.DETECTION_INTERVAL_COMPETITION_MILLIS_HARD
import hr.fer.tel.gibalica.utils.Constants.DETECTION_INTERVAL_COMPETITION_MILLIS_MEDIUM
import hr.fer.tel.gibalica.utils.Constants.DETECTION_INTERVAL_DAY_NIGHT_MILLIS_EASY
import hr.fer.tel.gibalica.utils.Constants.DETECTION_INTERVAL_DAY_NIGHT_MILLIS_HARD
import hr.fer.tel.gibalica.utils.Constants.DETECTION_INTERVAL_DAY_NIGHT_MILLIS_MEDIUM
import hr.fer.tel.gibalica.utils.Constants.DETECTION_TIMEOUT_MILLIS_DEFAULT
import hr.fer.tel.gibalica.utils.Constants.DETECTION_TIMEOUT_MILLIS_EASY
import hr.fer.tel.gibalica.utils.Constants.DETECTION_TIMEOUT_MILLIS_HARD
import hr.fer.tel.gibalica.utils.Constants.DETECTION_TIMEOUT_MILLIS_MEDIUM
import hr.fer.tel.gibalica.viewModel.MainViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DetectionFragment : BaseDetectionFragment(), ImageAnalyzer.AnalyzerListener {

    var _binding: FragmentDetectionBinding? = null
    private val binding: FragmentDetectionBinding
        get() = _binding!!

    private val args: DetectionFragmentArgs by navArgs()
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var analyzer: ImageAnalyzer
    private var textToSpeech: TextToSpeech? = null
    private var currentPose = GibalicaPose.ALL_JOINTS_VISIBLE
    private var detectionInProgress = false

    // -- Variables for competition and Day-Night --
    // Once the interval runs out, detector moves to the next pose
    private var detectionIntervalMillis: Long? = null
    private var intervalTimerDisposable: Disposable? = null

    // When the competition is done, these will be used to calculate statistics and assign a score
    private var correctPoses = 0
    private var totalPoses = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetectionBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnClose.setOnClickListener { returnToMainFragment() }
        when (args.detectionUseCase) {
            DetectionUseCase.TRAINING -> initializeDataForTraining()
            else -> initializeDataForCompetitionAndDayNight()
        }
        defineCounterLogic()
        initializeAndStartCamera(binding.txvViewFinder, analyzer)
        updatePoseInAnalyzer()
        setupTextToSpeech()
        viewModel.startCounter(CounterCause.WAIT_FOR_COMPONENTS_TO_INITIALIZE, 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        textToSpeech?.stop()
        textToSpeech?.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted())
                initializeAndStartCamera(binding.txvViewFinder, analyzer)
            else {
                showErrorToast()
                returnToMainFragment()
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        initializeAndStartCamera(binding.txvViewFinder, analyzer)
    }

    override fun onPoseDetected(detectedPose: GibalicaPose) {
        if (detectionInProgress) {
            detectionInProgress = false
            Timber.d("${currentPose.name} detected.")

            when (currentPose) {
                GibalicaPose.ALL_JOINTS_VISIBLE -> runLogicWhenInitialPoseDetected()
                GibalicaPose.STARTING_POSE -> runLogicWhenStartingPoseDetected()
                else -> runLogicWhenOtherPosesDetected()
            }
        }
    }

    override fun onPoseNotDetected(detectedPose: GibalicaPose) {
        if (detectionInProgress) {
            when {
                isCompetition() or isDayNight() ->
                    Timber.d("Not showing negative message since interval for pose is still in progress.")
                isDetectingActualPose() ->
                    disableDetectionAndShowPoseNotDetected()
            }
        }
    }

    private fun initializeDataForTraining() {
        val trainingType = args.trainingType
        Timber.d("Starting training for type ${trainingType.name}.")
        setupImageAnalyzer(DETECTION_TIMEOUT_MILLIS_DEFAULT)
    }

    private fun initializeDataForCompetitionAndDayNight() {
        Timber.d("Starting ${args.detectionUseCase.name}.")

        when (args.difficulty) {
            Difficulty.EASY -> {
                setupImageAnalyzer(DETECTION_TIMEOUT_MILLIS_EASY)
                detectionIntervalMillis =
                    if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                        DETECTION_INTERVAL_COMPETITION_MILLIS_EASY
                    else
                        DETECTION_INTERVAL_DAY_NIGHT_MILLIS_EASY
            }
            Difficulty.MEDIUM -> {
                setupImageAnalyzer(DETECTION_TIMEOUT_MILLIS_MEDIUM)
                detectionIntervalMillis =
                    if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                        DETECTION_INTERVAL_COMPETITION_MILLIS_MEDIUM
                    else
                        DETECTION_INTERVAL_DAY_NIGHT_MILLIS_MEDIUM
            }
            Difficulty.HARD -> {
                setupImageAnalyzer(DETECTION_TIMEOUT_MILLIS_HARD)
                detectionIntervalMillis =
                    if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                        DETECTION_INTERVAL_COMPETITION_MILLIS_HARD
                    else
                        DETECTION_INTERVAL_DAY_NIGHT_MILLIS_HARD
            }
            Difficulty.NONE ->
                Timber.e("Detection was started without difficulty value.")
        }
    }

    private fun runLogicWhenInitialPoseDetected() {
        currentPose = GibalicaPose.STARTING_POSE
        showMessageForCurrentPose()
        speakCurrentPoseMessage()
        updatePoseInAnalyzer()
        viewModel.startCounter(CounterCause.WAIT_BEFORE_DETECTING_STARTING_POSE, 1)
    }

    private fun runLogicWhenStartingPoseDetected() {
        currentPose = getInitialPose()
        showMessageForCurrentPose()
        speakCurrentPoseMessage()
        updatePoseInAnalyzer()
        startTimersIfCompetitionOrDayNightUseCase()
        detectionInProgress = true
        Timber.d("Starting pose detected, moving to detection of actual poses.")
    }

    private fun runLogicWhenOtherPosesDetected() {
        hideMessage()
        showPoseDetectedResponse()
        if (isRandomDetection()) {
            intervalTimerDisposable?.dispose()
            updatePoseCompletionData(poseDetected = true)
            getNewRandomPose()
            updatePoseInAnalyzer()
            viewModel.startCounter(CounterCause.SWITCHING_TO_NEW_POSE, 1)
        } else {
            viewModel.startCounter(CounterCause.FINISH_DETECTION, 1)
        }
    }

    private fun disableDetectionAndShowPoseNotDetected() {
        detectionInProgress = false
        Timber.d("${currentPose.name} not detected.")

        showPoseNotDetectedResponse()
        viewModel.startCounter(CounterCause.HIDE_NEGATIVE_RESULT, 2)
    }

    private fun setupImageAnalyzer(detectionTimeoutMillis: Long) {
        analyzer = ImageAnalyzer(detectionTimeoutMillis)
        analyzer.setListener(this)
    }

    private fun setupTextToSpeech() {
        if (isSoundEnabled()) {
            val enginePackageName = when (getApplicationLanguage()) {
                Language.EN -> "com.google.android.tts"
                Language.HR -> "alfanum.co.rs.alfanumtts.cro"
            }
            Timber.d("TTS engine to be initialized: $enginePackageName.")
            textToSpeech = TextToSpeech(
                requireContext(),
                { status ->
                    if (status == TextToSpeech.SUCCESS) {
                        textToSpeech?.let { tts ->
                            Timber.i("Available TTS engines:\n${tts.engines}.")
                            Timber.d("Chosen TTS language: ${tts.voice?.locale}.")
                        }
                    } else Timber.d("TTS engine could not be initialized, status code is $status.")
                },
                enginePackageName
            )
        } else {
            Timber.d("TTS engine not initialized, sound setting disabled.")
        }
    }

    private fun getInitialPose(): GibalicaPose {
        return when {
            isRandomDetection() -> getRandomPose()
            args.trainingType == TrainingType.LEFT_HAND -> GibalicaPose.LEFT_HAND_RAISED
            args.trainingType == TrainingType.RIGHT_HAND -> GibalicaPose.RIGHT_HAND_RAISED
            args.trainingType == TrainingType.BOTH_HANDS -> GibalicaPose.BOTH_HANDS_RAISED
            args.trainingType == TrainingType.T_POSE -> GibalicaPose.T_POSE
            else -> GibalicaPose.SQUAT
        }
    }

    private fun defineCounterLogic() {
        viewModel.notificationLiveData.observe(viewLifecycleOwner) { event ->

            when (event?.eventType) {
                EventType.COUNTER_FINISHED -> {
                    when (event.cause) {
                        CounterCause.WAIT_FOR_COMPONENTS_TO_INITIALIZE -> {
                            Timber.d("Components initialized, starting detection.")
                            updateUIAndContinueDetection()
                            speakCurrentPoseMessage()
                        }
                        CounterCause.WAIT_BEFORE_DETECTING_STARTING_POSE -> {
                            Timber.d("Counter finished, continuing detection.")
                            detectionInProgress = true
                        }
                        CounterCause.HIDE_NEGATIVE_RESULT -> {
                            Timber.d("Negative result hidden, continuing detection.")
                            updateUIAndContinueDetection()
                        }
                        CounterCause.SWITCHING_TO_NEW_POSE -> {
                            Timber.d("Switched to new pose, continuing detection.")
                            updateUIAndContinueDetection()
                            speakCurrentPoseMessage()
                            restartTimerIfCompetitionOrDayNightUseCase()
                        }
                        CounterCause.FINISH_DETECTION -> endDetection()
                        else -> Timber.d("Timer was trigger for ${event.cause}.")
                    }
                }
            }
        }
    }

    private fun updateUIAndContinueDetection() {
        hideResponse()
        showMessageForCurrentPose()
        detectionInProgress = true
    }

    private fun startTimersIfCompetitionOrDayNightUseCase() {
        if (isCompetition() or isDayNight()) {
            startDetectionTimer(args.detectionLengthMinutes)
            startIntervalTimer()
        }
    }

    private fun restartTimerIfCompetitionOrDayNightUseCase() {
        if (isCompetition() or isDayNight())
            startIntervalTimer()
    }

    private fun updatePoseCompletionData(poseDetected: Boolean) {
        totalPoses++
        if (poseDetected) correctPoses++
    }

    private fun poseNotDetectedMoveToNext() {
        disableDetectionAndShowPoseNotDetected()
        updatePoseCompletionData(poseDetected = false)
        getNewRandomPose()
        updatePoseInAnalyzer()
        viewModel.startCounter(CounterCause.SWITCHING_TO_NEW_POSE, 2)
    }

    private fun getNewRandomPose() {
        currentPose = getRandomPose()
    }

    private fun updatePoseInAnalyzer() {
        analyzer.updatePose(currentPose)
    }

    private fun speakCurrentPoseMessage() {
        val message = getMessageForCurrentPose()
        Timber.d("TTS message: $message")
        textToSpeech?.speak(message, TextToSpeech.QUEUE_FLUSH, null, System.currentTimeMillis().toString())
    }

    private fun getRandomPose(): GibalicaPose {
        return when (args.detectionUseCase) {
            DetectionUseCase.TRAINING, DetectionUseCase.COMPETITION -> {
                when (Random.nextInt(5)) {
                    0 -> GibalicaPose.LEFT_HAND_RAISED
                    1 -> GibalicaPose.RIGHT_HAND_RAISED
                    2 -> GibalicaPose.BOTH_HANDS_RAISED
                    3 -> GibalicaPose.SQUAT
                    else -> GibalicaPose.T_POSE
                }
            }
            DetectionUseCase.DAY_NIGHT -> {
                when (Random.nextInt(2)) {
                    0 -> GibalicaPose.SQUAT
                    else -> GibalicaPose.UPRIGHT
                }
            }
        }
    }

    private fun startDetectionTimer(valueMinutes: Long) {
        Flowable.interval(1, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Timber.d("Starting detection timer for $valueMinutes minutes.")
                binding.tvTimer.visible()
            }
            .subscribe(
                { tick ->
                    val valueSeconds = valueMinutes * 60
                    if (tick == valueSeconds) {
                        navigateToFinishFragment()
                    } else {
                        val remainingSeconds = valueSeconds - tick
                        val minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds)
                        val seconds = remainingSeconds - TimeUnit.MINUTES.toSeconds(minutes)
                        if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                            binding.tvTimer.text = String.format("%02d:%02d", minutes, seconds)
                    }
                },
                {}
            )
    }

    private fun startIntervalTimer() {
        intervalTimerDisposable =
            Flowable.interval(0, 100, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .doOnSubscribe {
                    Timber.d("Starting interval timer for $detectionIntervalMillis milliseconds.")
                }
                .subscribe(
                    { tick ->
                        val timerValueMillis = tick * 100
                        if (timerValueMillis >= detectionIntervalMillis!!) {
                            Timber.d("Stopping interval timer.")
                            poseNotDetectedMoveToNext()
                            intervalTimerDisposable?.dispose()
                        }
                    },
                    {}
                )
    }

    private fun isRandomDetection() = isCompetition() || isDayNight() || args.trainingType == TrainingType.RANDOM

    private fun isDetectingActualPose() =
        currentPose != GibalicaPose.ALL_JOINTS_VISIBLE && currentPose != GibalicaPose.STARTING_POSE

    private fun isCompetition() = args.detectionUseCase == DetectionUseCase.COMPETITION

    private fun isDayNight() = args.detectionUseCase == DetectionUseCase.DAY_NIGHT

    private fun endDetection() = navigateToFinishFragment()

    private fun returnToMainFragment() {
        findNavController().navigate(
            DetectionFragmentDirections.actionDetectionFragmentToMainFragment()
        )
    }

    private fun navigateToFinishFragment() {
        Timber.d("Finishing training.")
        findNavController().navigate(
            DetectionFragmentDirections.actionDetectionFragmentToFinishFragment(
                detectionUseCase = args.detectionUseCase,
                totalPoses = totalPoses,
                correctPoses = correctPoses
            )
        )
    }

    private fun showPoseDetectedResponse() = showResponse(R.string.response_positive)

    private fun showPoseNotDetectedResponse() = showResponse(R.string.response_negative)

    private fun showResponse(resId: Int) = binding.apply {
        tvResponse.setText(resId)
        cvResponse.visible()
    }

    private fun hideResponse() = binding.apply { cvResponse.invisible() }

    private fun hideMessage() = binding.apply { tvMessage.invisible() }

    private fun showMessageForCurrentPose() = binding.apply {
        val message = getMessageForCurrentPose()
        tvMessage.text = message
        tvMessage.visible()
    }

    private fun getMessageForCurrentPose(): String {
        val resId = when {
            args.detectionUseCase == DetectionUseCase.DAY_NIGHT
                    && currentPose == GibalicaPose.UPRIGHT -> R.string.day_message
            args.detectionUseCase == DetectionUseCase.DAY_NIGHT
                    && currentPose == GibalicaPose.SQUAT -> R.string.night_message
            else -> currentPose.getPoseMessage()
        }

        return if (resId != null) {
            getString(resId)
        } else {
            Timber.d("Received message resId is null, returning empty string.")
            ""
        }
    }
}
