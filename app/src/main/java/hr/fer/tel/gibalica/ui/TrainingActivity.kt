package hr.fer.tel.gibalica.ui

import android.Manifest
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
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivityTrainingBinding
import hr.fer.tel.gibalica.utils.EventType
import hr.fer.tel.gibalica.utils.GibalicaPose
import hr.fer.tel.gibalica.utils.ImageAnalyzer
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 42

@AndroidEntryPoint
class TrainingActivity : BaseActivity(), TextureView.SurfaceTextureListener {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: ActivityTrainingBinding
    private val viewModel by viewModels<MainViewModel>()
    private var poseToBeDetected = GibalicaPose.ALL_JOINTS_VISIBLE

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()

        initializeAndStartCamera()
        defineObserver()
        Timber.d("Sending first pose to analyzer.")
        viewModel.poseDetectionLiveData.value = poseToBeDetected
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

    private fun defineObserver() {
        viewModel.notificationLiveData.observe(
            this,
            {
                when (it?.eventType) {
                    EventType.POSE_DETECTED -> {
                        val message = "${poseToBeDetected.name} detected"
                        Timber.d(message)
                        showToast(message)
                        poseToBeDetected = when (poseToBeDetected) {
                            GibalicaPose.ALL_JOINTS_VISIBLE -> GibalicaPose.STARTING_POSE
                            GibalicaPose.STARTING_POSE -> GibalicaPose.LEFT_HAND_RAISED
                            GibalicaPose.LEFT_HAND_RAISED -> GibalicaPose.RIGHT_HAND_RAISED
                            GibalicaPose.RIGHT_HAND_RAISED -> GibalicaPose.BOTH_HANDS_RAISED
                            else -> GibalicaPose.ALL_JOINTS_VISIBLE
                        }
                        viewModel.poseDetectionLiveData.value = poseToBeDetected
                    }
                    EventType.POSE_NOT_DETECTED -> {
                        val message = "${poseToBeDetected.name} not detected"
                        Timber.d(message)
                        showToast(message)
                    }
                    else -> {
                    }
                }
            }
        )
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
