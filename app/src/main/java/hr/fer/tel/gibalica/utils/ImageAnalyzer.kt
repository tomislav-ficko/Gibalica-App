package hr.fer.tel.gibalica.utils

import android.annotation.SuppressLint
import android.content.Context
import android.widget.Toast
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import timber.log.Timber

class ImageAnalyzer(val context: Context) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val poseDetector = preparePoseDetector()
            poseDetector.process(image)
                .addOnSuccessListener { it?.let { detectPose(it) } }
                .addOnFailureListener { Timber.d("Detection failed: $it") }
                .addOnCompleteListener {
                    mediaImage.close()
                    imageProxy.close()
                }
        }
    }

    private fun detectPose(pose: Pose) {
        Timber.d("Detecting pose...")
        val message = when (BodyPositions.getPose(pose)) {
            BodyPositions.NONE -> "Pose could not be detected"
            BodyPositions.SQUAT -> "Squat detected"
            BodyPositions.T_POSE -> "T pose detected"
            BodyPositions.LEFT_HAND_RAISED -> "Left hand raised detected"
            BodyPositions.RIGHT_HAND_RAISED -> "Right hand raised detected"
            BodyPositions.BOTH_HANDS_RAISED -> "Both hands raised detected"
        }
        if (message != "Pose could not be detected") {
            Timber.d(message)
            displayToast(message)
        }
    }

    private fun preparePoseDetector(): PoseDetector {
        val detectionOptions = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        return PoseDetection.getClient(detectionOptions)
    }

    private fun displayToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}