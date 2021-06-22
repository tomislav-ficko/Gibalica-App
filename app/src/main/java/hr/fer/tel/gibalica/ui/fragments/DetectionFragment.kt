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
import hr.fer.tel.gibalica.viewModel.MainViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.disposables.Disposable
import timber.log.Timber
import java.util.concurrent.TimeUnit
import kotlin.random.Random

class DetectionFragment : BaseDetectionFragment(), ImageAnalyzer.AnalyzerListener {

    private val args: DetectionFragmentArgs by navArgs()
    private val viewModel by viewModels<MainViewModel>()
    private var binding: FragmentDetectionBinding? = null

    private lateinit var analyzer: ImageAnalyzer
    private var textToSpeech: TextToSpeech? = null
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
        binding = FragmentDetectionBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding!!.btnClose.setOnClickListener { returnToMainFragment() }
        initializeData()
        defineCounterLogic()
        initializeAndStartCamera(binding!!.txvViewFinder, analyzer)
        updatePoseInAnalyzer(GibalicaPose.ALL_JOINTS_VISIBLE)
        setupTextToSpeech()
        viewModel.startCounter(CounterCause.WAIT_FOR_COMPONENTS_TO_INITIALIZE, 1)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        binding = null
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
                initializeAndStartCamera(binding!!.txvViewFinder, analyzer)
            else {
                showErrorToast()
                returnToMainFragment()
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        initializeAndStartCamera(binding!!.txvViewFinder, analyzer)
    }

    override fun onPoseDetected(detectedPose: GibalicaPose) {
        if (detectionInProgress) {
            detectionInProgress = false
            intervalTimerDisposable?.dispose()
            Timber.d("${detectedPose.name} detected.")

            when (detectedPose) {
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
                isDetectingActualPose(detectedPose) -> {
                    detectionInProgress = false
                    Timber.d("${detectedPose.name} not detected.")
                    showPoseNotDetectedResponse()
                    viewModel.startCounter(CounterCause.HIDE_NEGATIVE_RESULT, 2)
                }
            }
        }
    }

    private fun initializeData() {
        Timber.d("Starting ${args.detectionUseCase.name}.")
        val trainingType = args.trainingType
        Timber.d("Starting training for type ${trainingType.name}.")
        setupImageAnalyzer()
        when (args.difficulty) {
            Difficulty.EASY -> {
                detectionIntervalMillis =
                    if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                        DETECTION_INTERVAL_COMPETITION_MILLIS_EASY
                    else
                        DETECTION_INTERVAL_DAY_NIGHT_MILLIS_EASY
            }
            Difficulty.MEDIUM -> {
                detectionIntervalMillis =
                    if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                        DETECTION_INTERVAL_COMPETITION_MILLIS_MEDIUM
                    else
                        DETECTION_INTERVAL_DAY_NIGHT_MILLIS_MEDIUM
            }
            Difficulty.HARD -> {
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
        updateAndShowMessageForCurrentPose(GibalicaPose.STARTING_POSE)
        speakPoseMessage(GibalicaPose.STARTING_POSE)
        updatePoseInAnalyzer(GibalicaPose.STARTING_POSE)
        viewModel.startCounter(CounterCause.WAIT_BEFORE_DETECTING_STARTING_POSE, 1)
    }

    private fun runLogicWhenStartingPoseDetected() {
        val newPose = getInitialPose()
        updateAndShowMessageForCurrentPose(newPose)
        speakPoseMessage(newPose)
        updatePoseInAnalyzer(newPose)
        startDetectionTimerIfCompetitionOrDayNightUseCase()
        detectionInProgress = true
        Timber.d("Starting pose detected, moving to detection of actual poses.")
    }

    private fun runLogicWhenOtherPosesDetected() {
        hideMessage()
        showPoseDetectedResponse()
        if (isRandomDetection()) {
            updatePoseCompletionData(poseDetected = true)
            updatePoseInAnalyzer(getRandomPose())
            viewModel.startCounter(CounterCause.SWITCHING_TO_NEW_POSE, 1)
        } else {
            viewModel.startCounter(CounterCause.FINISH_DETECTION, 1)
        }
    }

    private fun setupImageAnalyzer() {
        analyzer = ImageAnalyzer()
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
                            hideResponse()
                            updateAndShowMessageForCurrentPose(GibalicaPose.ALL_JOINTS_VISIBLE)
                            speakPoseMessage(GibalicaPose.ALL_JOINTS_VISIBLE)
                            detectionInProgress = true
                        }
                        CounterCause.WAIT_BEFORE_DETECTING_STARTING_POSE -> {
                            Timber.d("Counter finished, continuing detection.")
                            detectionInProgress = true
                        }
                        CounterCause.HIDE_NEGATIVE_RESULT -> {
                            Timber.d("Negative result hidden, continuing detection.")
                            hideResponse()
                            showMessage()
                            detectionInProgress = true
                        }
                        CounterCause.SWITCHING_TO_NEW_POSE -> {
                            Timber.d("Switched to new pose.")
                            hideResponse()
                            updateAndShowMessageForCurrentPose(analyzer.getCurrentPose())
                            speakPoseMessage(analyzer.getCurrentPose())
                            viewModel.startCounter(CounterCause.WAIT_BEFORE_NEW_POSE, 1)
                        }
                        CounterCause.WAIT_BEFORE_NEW_POSE -> {
                            Timber.d("Counter finished, starting detection of new pose.")
                            startIntervalTimerIfCompetitionOrDayNightUseCase()
                            detectionInProgress = true
                        }
                        CounterCause.FINISH_DETECTION -> navigateToFinishFragment()
                        else -> Timber.d("Timer was trigger for ${event.cause}.")
                    }
                }
            }
        }
    }

    private fun startDetectionTimerIfCompetitionOrDayNightUseCase() {
        if (isCompetition() or isDayNight())
            startDetectionTimer(args.detectionLengthMinutes)
    }

    private fun startIntervalTimerIfCompetitionOrDayNightUseCase() {
        if (isCompetition() or isDayNight())
            startIntervalTimer()
    }

    private fun updatePoseCompletionData(poseDetected: Boolean) {
        totalPoses++
        if (poseDetected) correctPoses++
    }

    private fun poseNotDetectedMoveToNext() {
        detectionInProgress = false
        intervalTimerDisposable?.dispose()
        Timber.d("Pose not detected in given interval.")
        showPoseNotDetectedResponse()
        updatePoseCompletionData(poseDetected = false)
        updatePoseInAnalyzer(getRandomPose())
        viewModel.startCounter(CounterCause.SWITCHING_TO_NEW_POSE, 2)
    }

    private fun updatePoseInAnalyzer(newPose: GibalicaPose) {
        analyzer.updatePose(newPose)
    }

    private fun speakPoseMessage(pose: GibalicaPose) {
        val message = getMessageForPose(pose)
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
                binding?.tvTimer?.visible()
            }
            .subscribe(
                { tick ->
                    val valueSeconds = valueMinutes * 60
                    if (tick == valueSeconds) {
                        intervalTimerDisposable?.dispose()
                        navigateToFinishFragment()
                    } else {
                        val remainingSeconds = valueSeconds - tick
                        val minutes = TimeUnit.SECONDS.toMinutes(remainingSeconds)
                        val seconds = remainingSeconds - TimeUnit.MINUTES.toSeconds(minutes)
                        if (args.detectionUseCase == DetectionUseCase.COMPETITION)
                            binding?.tvTimer?.text = String.format("%02d:%02d", minutes, seconds)
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
                        }
                    },
                    {}
                )
    }

    private fun isRandomDetection() = isCompetition() || isDayNight() || args.trainingType == TrainingType.RANDOM

    private fun isDetectingActualPose(detectedPose: GibalicaPose) =
        detectedPose != GibalicaPose.ALL_JOINTS_VISIBLE && detectedPose != GibalicaPose.STARTING_POSE

    private fun isCompetition() = args.detectionUseCase == DetectionUseCase.COMPETITION

    private fun isDayNight() = args.detectionUseCase == DetectionUseCase.DAY_NIGHT

    private fun returnToMainFragment() {
        findNavController().navigate(
            DetectionFragmentDirections.actionDetectionFragmentToMainFragment()
        )
    }

    private fun navigateToFinishFragment() {
        Timber.d("Finishing detection.")
        analyzer.stopDetection()
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

    private fun showResponse(resId: Int) = binding?.apply {
        tvResponse.setText(resId)
        cvResponse.visible()
    }

    private fun hideResponse() = binding?.apply { cvResponse.invisible() }

    private fun hideMessage() = binding?.apply { tvMessage.invisible() }

    private fun showMessage() = binding?.apply { tvMessage.visible() }

    private fun updateAndShowMessageForCurrentPose(currentPose: GibalicaPose) = binding?.apply {
        val message = getMessageForPose(currentPose)
        tvMessage.text = message
        tvMessage.visible()
    }

    private fun getMessageForPose(pose: GibalicaPose): String {
        val resId = when {
            args.detectionUseCase == DetectionUseCase.DAY_NIGHT
                    && pose == GibalicaPose.UPRIGHT -> R.string.day_message
            args.detectionUseCase == DetectionUseCase.DAY_NIGHT
                    && pose == GibalicaPose.SQUAT -> R.string.night_message
            else -> pose.getPoseMessage()
        }

        return if (resId != null) {
            getString(resId)
        } else {
            Timber.d("Received message resId is null, returning empty string.")
            ""
        }
    }
}
