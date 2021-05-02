package hr.fer.tel.gibalica.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.TextureView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivityTrainingBinding
import hr.fer.tel.gibalica.utils.*
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 42

@AndroidEntryPoint
class TrainingActivity : BaseActivity(), TextureView.SurfaceTextureListener {

    private lateinit var cameraExecutor: ExecutorService
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

        initializeAndStartCamera()
        defineObserver()
        setupOverlayViews()
        Timber.d("Sending initial pose to analyzer.")
        viewModel.poseDetectionLiveData.value = currentPose
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (permissionsGranted())
                binding.txvViewFinder.post { startCamera() }
            else {
                showToast("Permissions not granted by the user.")
                finish()
            }
        }
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
        if (permissionsGranted()) {
            binding.txvViewFinder.post { startCamera() }
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
        }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    private fun initializeAndStartCamera() {
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (permissionsGranted())
            binding.txvViewFinder.post { startCamera() }
        else
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
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

    private fun hideMessage() {
        binding.tvMessage.invisible()
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .also { it.setSurfaceProvider(binding.txvViewFinder.surfaceProvider) }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, ImageAnalyzer(viewModel))
                    }
                cameraProvider.unbindAll()

                try {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                } catch (e: Exception) {
                    Timber.d("CameraProvider binding failed: $e")
                }
            },
            ContextCompat.getMainExecutor(this)
        )
    }

    private fun inflateLayout() {
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }

    private fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun permissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
}
