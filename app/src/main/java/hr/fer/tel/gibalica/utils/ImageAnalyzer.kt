package hr.fer.tel.gibalica.utils

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.areAllJointsVisible
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.areBothHandsRaised
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.isLeftHandRaised
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.isRightHandRaised
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.isSquatPerformed
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.isStandingUpright
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.isStartingPoseDetected
import hr.fer.tel.gibalica.utils.PoseDetector.Companion.isTPosePerformed
import timber.log.Timber

class ImageAnalyzer() : ImageAnalysis.Analyzer {

    interface AnalyzerListener {
        fun onPoseDetected(detectedPose: GibalicaPose)
        fun onPoseNotDetected(detectedPose: GibalicaPose)
    }

    private var lastAnalyzedTimestamp = 0L
    private var listener: AnalyzerListener? = null
    private var detectionEnded = false
    private lateinit var poseToBeDetected: GibalicaPose

    @SuppressLint("UnsafeExperimentalUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        if (imageProxy.image != null)
            analyzeImageUsingPoseDetector(imageProxy)
    }

    fun setListener(listener: AnalyzerListener) {
        this.listener = listener
    }

    fun updatePose(newPose: GibalicaPose) {
        poseToBeDetected = newPose
    }

    fun stopDetection() {
        detectionEnded = true
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun analyzeImageUsingPoseDetector(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image!!
        val currentTimestamp = System.currentTimeMillis()
        if (detectionEnded) {
            Timber.d("Detection ended, doing nothing.")
        } else {
            if (enoughTimePassed(currentTimestamp)) {
                val image =
                    InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                val poseDetector = preparePoseDetector()
                poseDetector.process(image)
                    .addOnSuccessListener { it?.let { detectPose(it) } }
                    .addOnFailureListener { Timber.d("Detection failed: $it") }
                    .addOnCompleteListener {
                        mediaImage.close()
                        imageProxy.close()
                        lastAnalyzedTimestamp = currentTimestamp
                    }
            } else {
                Timber.v("Not analyzing, not enough time passed ($currentTimestamp).")
                mediaImage.close()
                imageProxy.close()
            }
        }
    }

    private fun enoughTimePassed(currentTimestamp: Long) =
        currentTimestamp - lastAnalyzedTimestamp >= Constants.DETECTION_TIMEOUT_MILLIS_DEFAULT

    private fun detectPose(pose: Pose) {
        Timber.d("${poseToBeDetected.name} detection in progress.")
        val poseDetected =
            when (poseToBeDetected) {
                GibalicaPose.STARTING_POSE -> pose.isStartingPoseDetected()
                GibalicaPose.ALL_JOINTS_VISIBLE -> pose.areAllJointsVisible()
                GibalicaPose.LEFT_HAND_RAISED -> pose.isLeftHandRaised()
                GibalicaPose.RIGHT_HAND_RAISED -> pose.isRightHandRaised()
                GibalicaPose.BOTH_HANDS_RAISED -> pose.areBothHandsRaised()
                GibalicaPose.T_POSE -> pose.isTPosePerformed()
                GibalicaPose.SQUAT -> pose.isSquatPerformed()
                GibalicaPose.UPRIGHT -> pose.isStandingUpright()
                GibalicaPose.NONE -> false
            }

        if (poseDetected) {
            Timber.d("Pose detected.")
            listener?.onPoseDetected(poseToBeDetected)
        } else {
            Timber.d("Pose not detected.")
            listener?.onPoseNotDetected(poseToBeDetected)
        }
    }

    private fun preparePoseDetector(): PoseDetector {
        val detectionOptions = PoseDetectorOptions.Builder()
            .setDetectorMode(PoseDetectorOptions.STREAM_MODE)
            .build()
        return PoseDetection.getClient(detectionOptions)
    }
}
