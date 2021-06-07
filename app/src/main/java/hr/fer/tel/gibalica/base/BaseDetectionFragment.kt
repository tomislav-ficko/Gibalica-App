package hr.fer.tel.gibalica.base

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.SurfaceTexture
import android.view.TextureView
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.utils.ImageAnalyzer
import timber.log.Timber
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

const val REQUEST_CODE_PERMISSIONS = 42

open class BaseDetectionFragment : BaseFragment(), TextureView.SurfaceTextureListener {

    private lateinit var cameraExecutor: ExecutorService

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}

    override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = true

    override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}

    protected fun initializeAndStartCamera(previewView: PreviewView, analyzer: ImageAnalyzer) {
        cameraExecutor = Executors.newSingleThreadExecutor()

        if (permissionsGranted())
            previewView.post { startCamera(previewView, analyzer) }
        else
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.CAMERA),
                REQUEST_CODE_PERMISSIONS
            )
    }

    protected fun permissionsGranted(): Boolean =
        ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED

    protected fun showErrorToast() =
        Toast.makeText(
            requireContext(),
            getString(R.string.message_permissions_not_granted),
            Toast.LENGTH_SHORT
        ).show()

    private fun startCamera(previewView: PreviewView, analyzer: ImageAnalyzer) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        cameraProviderFuture.addListener(
            {
                val cameraProvider = cameraProviderFuture.get()

                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()
                    .also { it.setSurfaceProvider(previewView.surfaceProvider) }
                val imageAnalyzer = ImageAnalysis.Builder()
                    .build()
                    .also {
                        it.setAnalyzer(cameraExecutor, analyzer)
                    }
                cameraProvider.unbindAll()

                try {
                    cameraProvider.bindToLifecycle(this, cameraSelector, preview, imageAnalyzer)
                } catch (e: Exception) {
                    Timber.d("CameraProvider binding failed: $e")
                }
            },
            ContextCompat.getMainExecutor(requireContext())
        )
    }
}
