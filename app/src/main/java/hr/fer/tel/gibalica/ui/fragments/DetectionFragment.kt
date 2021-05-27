package hr.fer.tel.gibalica.ui.fragments

import android.graphics.SurfaceTexture
import android.os.Bundle
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
    private lateinit var poseToBeDetected: GibalicaPose
    private var poseToBeDetectedMessage: Int? = null
    private var currentPose = GibalicaPose.ALL_JOINTS_VISIBLE
    private var detectionInProgress = true
    private var randomDetectionType: DetectionUseCase? = null

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

        when (args.detectionUseCase) {
            DetectionUseCase.TRAINING -> initializeDataForTraining()
            else -> initializeDataForCompetitionAndDayNight()
        }
        defineCounterLogic()
        initializeAndStartCamera(binding.txvViewFinder, analyzer)
        setupOverlayViews()
        startDetection()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
            Timber.d("${currentPose.name} detected.")
            detectionInProgress = false

            when (currentPose) {
                GibalicaPose.ALL_JOINTS_VISIBLE -> {
                    currentPose = GibalicaPose.STARTING_POSE
                    showStartingPoseMessage()
                    updatePoseInAnalyzer()
                    viewModel.startCounter(CounterCause.WAIT_BEFORE_DETECTING_STARTING_POSE, 1)
                }
                GibalicaPose.STARTING_POSE -> {
                    currentPose = poseToBeDetected
                    updateMessage()
                    updatePoseInAnalyzer()
                    startTimersIfCompetitionOrDayNightUseCase()
                    detectionInProgress = true
                    Timber.d("Starting pose detected, moving to detection of actual poses.")
                }
                else -> {
                    showPoseDetected()
                    if (isRandomDetection()) {
                        updatePoseCompletionData(poseDetected = true)
                        getNewRandomPose()
                        updatePoseInAnalyzer()
                        viewModel.startCounter(CounterCause.SWITCHING_TO_NEW_POSE, 1)
                    } else {
                        viewModel.startCounter(CounterCause.FINISH_DETECTION, 1)
                    }
                }
            }
        }
    }

    override fun onPoseNotDetected(detectedPose: GibalicaPose) {
        if (isCompetition() or isDayNight())
            Timber.d("Not showing negative message since interval for pose is still in progress.")
        else if (detectionInProgress && isDetectingActualPose())
            showPoseNotDetected()
    }

    private fun initializeDataForTraining() {
        val trainingType = args.trainingType
        Timber.d("Starting training for type ${trainingType.name}.")
        setupImageAnalyzer(DETECTION_TIMEOUT_MILLIS_DEFAULT)
        initializeDetection(trainingType)
        if (trainingType == TrainingType.RANDOM)
            randomDetectionType = DetectionUseCase.TRAINING
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
        initializeDetection(TrainingType.RANDOM)
        randomDetectionType = args.detectionUseCase
    }

    private fun showPoseNotDetected() {
        Timber.d("${poseToBeDetected.name} not detected.")
        detectionInProgress = false

        showPoseNotDetectedResponse()
        viewModel.startCounter(CounterCause.HIDE_NEGATIVE_RESULT, 2)
    }

    private fun setupImageAnalyzer(detectionTimeoutMillis: Long) {
        analyzer = ImageAnalyzer(detectionTimeoutMillis)
        analyzer.setListener(this)
    }

    private fun setupOverlayViews() {
        showMessage(R.string.message_initial)
        hideResponse()
        binding.btnClose.setOnClickListener { returnToMainFragment() }
    }

    private fun initializeDetection(trainingType: TrainingType) {
        poseToBeDetected = when (trainingType) {
            TrainingType.LEFT_HAND -> GibalicaPose.LEFT_HAND_RAISED
            TrainingType.RIGHT_HAND -> GibalicaPose.RIGHT_HAND_RAISED
            TrainingType.BOTH_HANDS -> GibalicaPose.BOTH_HANDS_RAISED
            TrainingType.T_POSE -> GibalicaPose.T_POSE
            TrainingType.SQUAT -> GibalicaPose.SQUAT
            TrainingType.RANDOM -> getRandomPose()
        }
        poseToBeDetectedMessage = poseToBeDetected.getPoseMessage()
    }

    private fun startDetection() {
        Timber.d("Sending initial pose to analyzer.")
        updatePoseInAnalyzer()
    }

    private fun defineCounterLogic() {
        viewModel.notificationLiveData.observe(viewLifecycleOwner) { event ->

            when (event?.eventType) {
                EventType.COUNTER_FINISHED -> {
                    when (event.cause) {
                        CounterCause.WAIT_BEFORE_DETECTING_STARTING_POSE -> {
                            detectionInProgress = true
                            Timber.d("Counter finished, continuing detection.")
                        }
                        CounterCause.HIDE_NEGATIVE_RESULT -> {
                            hideResponseAndShowMessage()
                            detectionInProgress = true
                            Timber.d("Negative result hidden, continuing detection.")
                        }
                        CounterCause.SWITCHING_TO_NEW_POSE -> {
                            updateMessage()
                            hideResponse()
                            detectionInProgress = true
                            restartTimerIfCompetitionOrDayNightUseCase()
                            Timber.d("Switched to new pose, continuing detection.")
                        }
                        CounterCause.FINISH_DETECTION -> endDetection()
                        else -> Timber.d("Timer was trigger for ${event.cause}.")
                    }
                }
            }
        }
    }

    private fun startTimersIfCompetitionOrDayNightUseCase() {
        if (isCompetition() or isDayNight()) {
            startDetectionTimer(args.detectionLengthSeconds)
            startIntervalTimer()
        }
    }

    private fun restartTimerIfCompetitionOrDayNightUseCase() {
        if (isCompetition() or isDayNight())
            startIntervalTimer()
    }

    private fun updatePoseCompletionData(poseDetected: Boolean) {
        totalPoses++
        if (poseDetected) {
            correctPoses++
            intervalTimerDisposable?.dispose()
        }
    }

    private fun poseNotDetectedMoveToNext() {
        showPoseNotDetected()
        updatePoseCompletionData(poseDetected = false)
        getNewRandomPose()
        updatePoseInAnalyzer()
        viewModel.startCounter(CounterCause.SWITCHING_TO_NEW_POSE, 2)
    }

    private fun getNewRandomPose() {
        currentPose = getRandomPose()
        poseToBeDetectedMessage =
            when {
                randomDetectionType != DetectionUseCase.DAY_NIGHT ->
                    currentPose.getPoseMessage()
                currentPose == GibalicaPose.UPRIGHT ->
                    R.string.day_message
                else ->
                    R.string.night_message
            }
    }

    private fun updatePoseInAnalyzer() {
        analyzer.updatePose(currentPose)
    }

    private fun getRandomPose(): GibalicaPose {
        return when (randomDetectionType) {
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
            null -> {
                Timber.d("Trying to get new random pose while randomDetectionType is null.")
                GibalicaPose.NONE
            }
        }
    }

    private fun startDetectionTimer(valueSeconds: Long) {
        Flowable.interval(1, 1, TimeUnit.SECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                Timber.d("Starting timer for $valueSeconds seconds.")
                binding.tvTimer.visible()
            }
            .subscribe(
                { tick ->
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
                    Timber.d("Starting interval timer for $detectionIntervalMillis seconds.")
                }
                .subscribe(
                    { tick ->
                        if (tick >= detectionIntervalMillis!!)
                            poseNotDetectedMoveToNext()
                    },
                    {}
                )
    }

    private fun isRandomDetection() = randomDetectionType != null

    private fun isDetectingActualPose() =
        currentPose != GibalicaPose.ALL_JOINTS_VISIBLE && currentPose != GibalicaPose.STARTING_POSE

    private fun isCompetition() = randomDetectionType == DetectionUseCase.COMPETITION

    private fun isDayNight() = randomDetectionType == DetectionUseCase.DAY_NIGHT

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

    private fun updateMessage() {
        poseToBeDetectedMessage?.let { resId ->
            showMessage(resId)
        }
    }

    private fun showPoseDetected() {
        hideMessage()
        showResponse(R.string.response_positive)
    }

    private fun showPoseNotDetectedResponse() = showResponse(R.string.response_negative)

    private fun showStartingPoseMessage() = showMessage(R.string.message_start)

    private fun hideResponseAndShowMessage() {
        hideResponse()
        showMessage()
    }

    private fun showResponse(resId: Int) = binding.apply {
        tvResponse.setText(resId)
        cvResponse.visible()
    }

    private fun hideResponse() = binding.apply {
        cvResponse.invisible()
    }

    private fun showMessage() = binding.apply {
        tvMessage.visible()
    }

    private fun showMessage(resId: Int) = binding.apply {
        tvMessage.setText(resId)
        tvMessage.visible()
    }

    private fun hideMessage() = binding.apply {
        tvMessage.invisible()
    }
}
