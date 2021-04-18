package hr.fer.tel.gibalica.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Matrix
import android.graphics.SurfaceTexture
import android.os.Bundle
import android.view.Surface
import android.view.TextureView
import android.widget.Toast
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivityTrainingBinding
import hr.fer.tel.gibalica.utils.BodyPositions
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

private const val REQUEST_CODE_PERMISSIONS = 42

@AndroidEntryPoint
class TrainingActivity : BaseActivity(), TextureView.SurfaceTextureListener {

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var binding: ActivityTrainingBinding
    private var displayRotation = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()

        initializeCamera()
        initializeViewfinder()
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
//        binding.txvViewFinder.addOnLayoutChangeListener { updateTransform() }
    }

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    private fun initializeCamera() {
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .also {
                        it.setSurfaceProvider(binding.txvViewFinder.surfaceProvider)
                    }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, BodyAnalyzer())
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

    /**
     * Used for automatically rotating content around the center of the screen, based on the orientation of the phone
     */
    private fun updateTransform() {
        val matrix = Matrix()

        with(binding.txvViewFinder) {
            val centerX = width / 2f
            val centerY = height / 2f

            this@TrainingActivity.displayRotation =
                when (display.rotation) {
                    Surface.ROTATION_0 -> 0
                    Surface.ROTATION_90 -> 90
                    Surface.ROTATION_180 -> 180
                    Surface.ROTATION_270 -> 270
                    else -> throw UnsupportedOperationException("Unsupported display rotation")
                }

            matrix.postRotate(-displayRotation.toFloat(), centerX, centerY)
//            setTransform(matrix)
        }
    }

    private fun inflateLayout() {
        binding = ActivityTrainingBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }

    private fun initializeViewfinder() {
        binding.txvViewFinder.apply {
//            addOnLayoutChangeListener { updateTransform() }
//            surfaceTextureListener = this@TrainingActivity
        }
    }

    private fun showToast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    private fun permissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    inner class BodyAnalyzer() : ImageAnalysis.Analyzer {

        @SuppressLint("UnsafeExperimentalUsageError")
        override fun analyze(imageProxy: ImageProxy) {
            val mediaImage = imageProxy.image
            if (mediaImage != null) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val poseDetector = preparePoseDetector()
                poseDetector.process(image)
                    .addOnSuccessListener { it?.let { calculateDetectedPose(it) } }
                    .addOnFailureListener { Timber.d("Detection failed: $it") }
            }
        }

        private fun calculateDetectedPose(pose: Pose) {
            when (BodyPositions.getPose(pose)) {
                BodyPositions.NONE -> Timber.d("Pose could not be detected")
                BodyPositions.SQUAT -> Timber.d("Squat detected")
                BodyPositions.T_POSE -> Timber.d("T pose detected")
                BodyPositions.LEFT_HAND_RAISED -> Timber.d("Left hand raised detected")
                BodyPositions.RIGHT_HAND_RAISED -> Timber.d("Right hand raised detected")
                BodyPositions.BOTH_HANDS_RAISED -> Timber.d("Both hands raised detected")
            }
        }

        private fun preparePoseDetector(): PoseDetector {
            val detectionOptions = PoseDetectorOptions.Builder()
                .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
                .build()
            return PoseDetection.getClient(detectionOptions)
        }
    }
}
