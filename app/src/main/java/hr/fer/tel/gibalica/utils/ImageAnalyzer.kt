package hr.fer.tel.gibalica.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.allJointsVisible
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.bothHandsRaised
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.leftHandRaised
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.rightHandRaised
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.squatPerformed
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.startingPoseDetected
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.tPosePerformed
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber

class ImageAnalyzer(val viewModel: MainViewModel) : ImageAnalysis.Analyzer {

    private lateinit var poseToBeDetected: GibalicaPose

    init {
        viewModel.poseDetectionLiveData.observeForever { it?.let { poseToBeDetected = it } }
    }

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
        Timber.d("${poseToBeDetected.name} detection in progress.")
        with(viewModel.notificationLiveData) {
            val poseDetected =
                when (poseToBeDetected) {
                    GibalicaPose.STARTING_POSE -> pose.startingPoseDetected()
                    GibalicaPose.ALL_JOINTS_VISIBLE -> pose.allJointsVisible()
                    GibalicaPose.LEFT_HAND_RAISED -> pose.leftHandRaised()
                    GibalicaPose.RIGHT_HAND_RAISED -> pose.rightHandRaised()
                    GibalicaPose.BOTH_HANDS_RAISED -> pose.bothHandsRaised()
                    GibalicaPose.T_POSE -> pose.tPosePerformed()
                    GibalicaPose.SQUAT -> pose.squatPerformed()
                    GibalicaPose.NONE -> false
                }
            if (poseDetected) postValue(NotificationEvent(EventType.POSE_DETECTED))
            else postValue(NotificationEvent(EventType.POSE_NOT_DETECTED))
        }
    }

    private fun preparePoseDetector(): PoseDetector {
        val detectionOptions = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        return PoseDetection.getClient(detectionOptions)
    }
}