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
import hr.fer.tel.gibalica.databinding.FragmentTrainingBinding
import hr.fer.tel.gibalica.utils.*
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber
import kotlin.random.Random

class TrainingFragment : BaseDetectionFragment(), ImageAnalyzer.AnalyzerListener {

    companion object {
        private const val DEFAULT_DETECTION_TIMEOUT_SECONDS = 2L
    }

    var _binding: FragmentTrainingBinding? = null
    private val binding: FragmentTrainingBinding
        get() = _binding!!

    private val args: TrainingFragmentArgs by navArgs()
    private val viewModel by viewModels<MainViewModel>()

    private lateinit var analyzer: ImageAnalyzer
    private lateinit var poseToBeDetected: GibalicaPose
    private var poseToBeDetectedMessage: Int? = null
    private var currentPose = GibalicaPose.ALL_JOINTS_VISIBLE
    private var detectionInProgress = true
    private var randomTraining = false
    private var currentCounterCause = CounterCause.NO_EVENT

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTrainingBinding.inflate(inflater, container, false)
        Timber.d("Inflated!")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupImageAnalyzer(DEFAULT_DETECTION_TIMEOUT_SECONDS)
        initializeAndStartCamera(binding.txvViewFinder, analyzer)
        setupOverlayViews()

        val trainingType = args.trainingType
        Timber.d("Starting training for type ${trainingType.name}")
        initializeTraining(trainingType)
        startTraining()
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
            Timber.d("${currentPose.name} detected")
            detectionInProgress = false

            when (currentPose) {
                GibalicaPose.ALL_JOINTS_VISIBLE -> {
                    continueToStartingPose()
                    currentPose = GibalicaPose.STARTING_POSE
                    updateDetectionOfPose()
                    currentCounterCause = CounterCause.DO_NOT_DETECT
                    viewModel.startCounter(1)
                }
                GibalicaPose.STARTING_POSE -> {
                    continueToActualPoseDetection()
                    currentPose = poseToBeDetected
                    updateDetectionOfPose()
                    currentCounterCause = CounterCause.DO_NOT_DETECT
                    viewModel.startCounter(3)
                }
                else -> {
                    if (!randomTraining) {
                        showPoseDetected()
                        currentCounterCause = CounterCause.FINISH_DETECTION
                        viewModel.startCounter(2)
                    } else {
                        showPoseDetected()
                        currentPose = getRandomPose()
                        poseToBeDetectedMessage = currentPose.getPoseMessage()
                        updateDetectionOfPose()
                        currentCounterCause = CounterCause.SWITCHING_TO_NEW_POSE
                        viewModel.startCounter(3)
                    }
                }
            }
        }
    }

    override fun onPoseNotDetected(detectedPose: GibalicaPose) {
        if (detectionInProgress &&
            currentPose != GibalicaPose.ALL_JOINTS_VISIBLE &&
            currentPose != GibalicaPose.STARTING_POSE
        ) {
            Timber.d("${poseToBeDetected.name} not detected")
            detectionInProgress = false

            showPoseNotDetected()
            currentCounterCause = CounterCause.HIDE_NEGATIVE_RESULT
            viewModel.startCounter(2)
        }
    }

    private fun setupImageAnalyzer(analysisTimeout: Long) {
        analyzer = ImageAnalyzer(analysisTimeout)
        analyzer.setListener(this)
    }

    private fun setupOverlayViews() {
        showMessage(R.string.message_initial)
        hideResponse()
        binding.btnClose.setOnClickListener { returnToMainFragment() }
    }

    private fun initializeTraining(trainingType: TrainingType) {
        when (trainingType) {
            TrainingType.LEFT_HAND -> poseToBeDetected = GibalicaPose.LEFT_HAND_RAISED
            TrainingType.RIGHT_HAND -> poseToBeDetected = GibalicaPose.RIGHT_HAND_RAISED
            TrainingType.BOTH_HANDS -> poseToBeDetected = GibalicaPose.BOTH_HANDS_RAISED
            TrainingType.T_POSE -> poseToBeDetected = GibalicaPose.T_POSE
            TrainingType.SQUAT -> poseToBeDetected = GibalicaPose.SQUAT
            TrainingType.RANDOM -> {
                randomTraining = true
                poseToBeDetected = getRandomPose()
            }
        }
        poseToBeDetectedMessage = poseToBeDetected.getPoseMessage()
        setupDetectionLogic()
    }

    private fun startTraining() {
        Timber.d("Sending initial pose to analyzer.")
        updateDetectionOfPose()
    }

    private fun setupDetectionLogic() {
        viewModel.notificationLiveData.observe(viewLifecycleOwner) { event ->

            when (event?.eventType) {
                EventType.COUNTER_FINISHED -> {
                    when (currentCounterCause) {
                        CounterCause.FINISH_DETECTION -> endDetection()
                        CounterCause.SWITCHING_TO_NEW_POSE -> {
                            updateMessage()
                            detectionInProgress = true
                        }
                        CounterCause.HIDE_NEGATIVE_RESULT -> {
                            updateMessage()
                            detectionInProgress = true
                        }
                        CounterCause.DO_NOT_DETECT -> detectionInProgress = true
                        CounterCause.NO_EVENT -> {
                        }
                    }
                }
            }
        }
    }

    private fun updateDetectionOfPose() {
        analyzer.updatePose(currentPose)
    }

    private fun getRandomPose(): GibalicaPose {
        val numberOfPoses = TrainingType.values().size
        return when (Random.nextInt(numberOfPoses)) {
            0 -> GibalicaPose.LEFT_HAND_RAISED
            1 -> GibalicaPose.RIGHT_HAND_RAISED
            2 -> GibalicaPose.BOTH_HANDS_RAISED
            3 -> GibalicaPose.SQUAT
            else -> GibalicaPose.T_POSE
        }
    }

    private fun endDetection() = navigateToFinishFragment()

    private fun updateMessage() {
        poseToBeDetectedMessage?.let { resId ->
            showMessage(resId)
        }
        hideResponse()
    }

    private fun showPoseDetected() {
        hideMessage()
        showResponse(R.string.response_positive)
    }

    private fun showPoseNotDetected() = showResponse(R.string.response_negative)

    private fun continueToStartingPose() = showMessage(R.string.message_start)

    private fun continueToActualPoseDetection() {
        poseToBeDetectedMessage?.let { resId ->
            showMessage(resId)
        }
    }

    private fun returnToMainFragment() {
        findNavController().navigate(
            TrainingFragmentDirections.actionTrainingFragmentToMainFragment()
        )
    }

    private fun navigateToFinishFragment() {
        Timber.d("Finishing training.")
        findNavController().navigate(
            TrainingFragmentDirections.actionTrainingFragmentToFinishFragment()
        )
    }

    private fun showResponse(resId: Int) = binding.apply {
        tvResponse.setText(resId)
        cvResponse.visible()
    }

    private fun hideResponse() = binding.apply {
        cvResponse.invisible()
    }

    private fun showMessage(resId: Int) = binding.apply {
        tvMessage.setText(resId)
        tvMessage.visible()
    }

    private fun hideMessage() = binding.apply {
        tvMessage.invisible()
    }
}
