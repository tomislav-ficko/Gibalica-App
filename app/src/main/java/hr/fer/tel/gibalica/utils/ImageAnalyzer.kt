package hr.fer.tel.gibalica.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import timber.log.Timber

class ImageAnalyzer() : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            val image =
                InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
            val poseDetector = preparePoseDetector()
            poseDetector.process(image)
                .addOnSuccessListener {
                    it?.let {
                        Timber.d("Detecting pose...")
                        calculateDetectedPose(it)
                    }
                }
                .addOnFailureListener { Timber.d("Detection failed: $it") }
                .addOnCompleteListener {
                    mediaImage.close()
                    imageProxy.close()
                }
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