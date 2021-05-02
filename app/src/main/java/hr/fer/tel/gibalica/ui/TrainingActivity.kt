package hr.fer.tel.gibalica.ui

import android.content.Intent
import android.graphics.SurfaceTexture
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseDetectionActivity
import hr.fer.tel.gibalica.base.REQUEST_CODE_PERMISSIONS
import hr.fer.tel.gibalica.databinding.ActivityTrainingBinding
import hr.fer.tel.gibalica.utils.*
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber


@AndroidEntryPoint
class TrainingActivity : BaseDetectionActivity() {

    private lateinit var binding: ActivityTrainingBinding
    private var poseToBeDetected: GibalicaPose
    private var poseToBeDetectedMessage: Int?
    private val viewModel by viewModels<MainViewModel>()
    private var currentPose = GibalicaPose.ALL_JOINTS_VISIBLE
    private var poseProcessed = false

    init {
        poseToBeDetected = GibalicaPose.LEFT_HAND_RAISED
        poseToBeDetectedMessage =
            when (poseToBeDetected) {
                GibalicaPose.LEFT_HAND_RAISED -> R.string.message_left_hand
                GibalicaPose.RIGHT_HAND_RAISED -> R.string.message_right_hand
                GibalicaPose.BOTH_HANDS_RAISED -> R.string.message_both_hands
                GibalicaPose.SQUAT -> R.string.message_squat
                GibalicaPose.T_POSE -> R.string.message_t_pose
                else -> null
            }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
        initializeAndStartCamera(binding.txvViewFinder, viewModel)
        defineObserver()
        setupOverlayViews()
        Timber.d("Sending initial pose to analyzer.")
        viewModel.poseDetectionLiveData.value = currentPose
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted())
                initializeAndStartCamera(binding.txvViewFinder, viewModel)
            else {
                showErrorToast()
                finish()
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        initializeAndStartCamera(binding.txvViewFinder, viewModel)
    }

    private fun inflateLayout() {
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }

    private fun setupOverlayViews() {
        binding.tvMessage.setText(R.string.message_initial)
        showMessage()
        hideResponse()
        binding.btnClose.setOnClickListener {
            startActivity(
                Intent(this@TrainingActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
    }

    private fun defineObserver() {
        viewModel.notificationLiveData.observe(
            this,
            {
                when (it?.eventType) {
                    EventType.POSE_DETECTED -> {
                        if (!poseProcessed) {
                            Timber.d("${currentPose.name} detected")
                            poseProcessed = true

                            when (currentPose) {
                                GibalicaPose.ALL_JOINTS_VISIBLE -> {
                                    binding.tvMessage.setText(R.string.message_start)
                                    startDetectingNewPose(GibalicaPose.STARTING_POSE)
                                    viewModel.startCounter(1)
                                }
                                GibalicaPose.STARTING_POSE -> {
                                    poseToBeDetectedMessage?.let { binding.tvMessage.setText(it) }
                                    startDetectingNewPose(poseToBeDetected)
                                    viewModel.startCounter(3)
                                }
                                else -> {
                                    startDetectingNewPose(poseToBeDetected)
                                    showResponse(R.string.response_positive, 3)
                                }
                            }
                        }
                    }
                    EventType.POSE_NOT_DETECTED -> {
                        if (!poseProcessed &&
                            currentPose != GibalicaPose.ALL_JOINTS_VISIBLE &&
                            currentPose != GibalicaPose.STARTING_POSE
                        ) {
                            Timber.d("${poseToBeDetected.name} not detected")
                            poseProcessed = true

                            hideMessage()
                            showResponse(R.string.response_negative, 2)
                        }
                    }
                    EventType.COUNTER_FINISHED -> {
                        showMessage()
                        hideResponse()
                        poseProcessed = false
                    }
                }
            }
        )
    }

    private fun showResponse(resId: Int, timeSeconds: Long) {
        binding.apply {
            tvResponse.setText(resId)
            cvResponse.visible()
            viewModel.startCounter(timeSeconds)
        }
    }

    private fun startDetectingNewPose(pose: GibalicaPose) {
        currentPose = pose
        viewModel.poseDetectionLiveData.value = currentPose
    }

    private fun hideResponse() {
        binding.cvResponse.invisible()
    }

    private fun showMessage() {
        binding.tvMessage.visible()
    }

    private fun hideMessage() = binding.apply {
        tvMessage.invisible()
    }
}
