package hr.fer.tel.gibalica.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber

class ImageAnalyzer(val viewModel: MainViewModel) : ImageAnalysis.Analyzer {

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null)
            analyzeImageUsingPoseDetector(imageProxy)
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun analyzeImageUsingPoseDetector(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image!!
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

    private fun detectPose(pose: Pose) {
        Timber.d("Detecting pose...")
        with(viewModel.notificationLiveData) {
            when (BodyPositions.getPose(pose)) {
                BodyPositions.SQUAT ->
                    postValue(NotificationEvent(EventType.DETECTED_SQUAT))
                BodyPositions.T_POSE ->
                    postValue(NotificationEvent(EventType.DETECTED_T_POSE))
                BodyPositions.LEFT_HAND_RAISED ->
                    postValue(NotificationEvent(EventType.DETECTED_LEFT_HAND))
                BodyPositions.RIGHT_HAND_RAISED ->
                    postValue(NotificationEvent(EventType.DETECTED_RIGHT_HAND))
                BodyPositions.BOTH_HANDS_RAISED ->
                    postValue(NotificationEvent(EventType.DETECTED_BOTH_HANDS))
                BodyPositions.STARTING_POSE ->
                    postValue(NotificationEvent(EventType.DETECTED_STARTING_POSE))
                BodyPositions.ALL_JOINTS_VISIBLE ->
                    postValue(NotificationEvent(EventType.DETECTED_ALL_JOINTS_VISIBLE))
                BodyPositions.NONE -> {
                    //No pose detected
                }
            }
        }
    }

    private fun preparePoseDetector(): PoseDetector {
        val detectionOptions = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        return PoseDetection.getClient(detectionOptions)
    }
}