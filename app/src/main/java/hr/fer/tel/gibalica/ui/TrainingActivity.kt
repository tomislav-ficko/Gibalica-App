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

const val EXTRA_TRAINING_TYPE = "EXTRA_TRAINING_TYPE"

@AndroidEntryPoint
class TrainingActivity : BaseDetectionActivity() {

    private lateinit var binding: ActivityTrainingBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
        initializeAndStartCamera(binding.txvViewFinder, viewModel)
        defineObserver()
        setupOverlayViews()

        val trainingType = intent.getSerializableExtra(EXTRA_TRAINING_TYPE) as TrainingType
        viewModel.initializeTraining(trainingType)
        viewModel.startTraining()
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

    private fun defineObserver() {
        viewModel.detectionResultLiveData.observe(
            this,
            {
                it?.let { event ->
                    when (event) {
                        PoseDetectionEvent.NOT_DETECTED -> {
                            hideMessage()
                            showResponse(R.string.response_negative)
                        }
                        PoseDetectionEvent.INITIAL_POSE_DETECTED -> {
                            showMessage(R.string.message_start)
                        }
                        PoseDetectionEvent.STARTING_POSE_DETECTED -> {
                            viewModel.poseToBeDetectedMessage?.let { resId ->
                                showMessage(resId)
                            }
                        }
                        PoseDetectionEvent.WANTED_POSE_DETECTED -> {
                            hideMessage()
                            showResponse(R.string.response_positive)
                        }
                        PoseDetectionEvent.UPDATE_MESSAGE -> {
                            viewModel.poseToBeDetectedMessage?.let { resId ->
                                showMessage(resId)
                            }
                            hideResponse()
                        }
                        PoseDetectionEvent.FINISH_DETECTION -> {
                            Timber.d("Finishing training.")
                        }
                        PoseDetectionEvent.HIDE_RESPONSE -> {
                            hideResponse()
                        }
                    }
                }
            }
        )
    }

    private fun setupOverlayViews() {
        showMessage(R.string.message_initial)
        hideResponse()
        binding.btnClose.setOnClickListener {
            startActivity(
                Intent(this@TrainingActivity, MainActivity::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            )
        }
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
