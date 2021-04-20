package hr.fer.tel.gibalica.utils

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.PoseLandmark.*
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

enum class BodyPositions {
    LEFT_HAND_RAISED,
    RIGHT_HAND_RAISED,
    BOTH_HANDS_RAISED,
    T_POSE,
    SQUAT,
    STARTING_POSE,
    ALL_JOINTS_VISIBLE,
    NONE;

    companion object {
        private const val LEFT_HAND = 0
        private const val RIGHT_HAND = 1
        private const val BOTH_HANDS = 2

        fun getPose(pose: Pose): BodyPositions {
            val landmarks = pose.allPoseLandmarks
            if (landmarks.isEmpty()) {
                Timber.d("No person detected")
                return NONE
            }
            logLandmarkDetails(pose)

            return when {
                squatPerformed(pose) -> SQUAT
                tPosePerformed(pose) -> T_POSE
                handRaised(BOTH_HANDS, pose) -> BOTH_HANDS_RAISED
                handRaised(LEFT_HAND, pose) -> LEFT_HAND_RAISED
                handRaised(RIGHT_HAND, pose) -> RIGHT_HAND_RAISED
                startingPose(pose) -> STARTING_POSE
                allJointsVisible(pose) -> ALL_JOINTS_VISIBLE
                else -> {
                    Timber.d("No known pose detected")
                    NONE
                }
            }
        }

        private fun squatPerformed(pose: Pose): Boolean {
            return false
        }

        private fun tPosePerformed(pose: Pose): Boolean {
            return false
        }

        private fun handRaised(handDescriptor: Int, pose: Pose): Boolean {
            return when (handDescriptor) {
                LEFT_HAND -> with(pose.getRequiredLandmarksFor(BOTH_HANDS_RAISED)) {
                    isArmRaised(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
                            && isArmLowered(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
                }
                RIGHT_HAND -> with(pose.getRequiredLandmarksFor(BOTH_HANDS_RAISED)) {
                    isArmRaised(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
                            && isArmLowered(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
                }
                BOTH_HANDS -> with(pose.getRequiredLandmarksFor(BOTH_HANDS_RAISED)) {
                    isArmRaised(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
                            && isArmRaised(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
                }
                else -> throw IllegalArgumentException("Wrong handDescriptor argument sent")
            }
        }

        private fun allJointsVisible(pose: Pose): Boolean {
            return false
        }

        private fun startingPose(pose: Pose): Boolean {
            return false
        }

        private fun isArmLowered(elbow: PoseLandmark, wrist: PoseLandmark): Boolean =
            elbow.isHigherThan(wrist) && elbow.sidePositionEqualTo(wrist)

        private fun isArmRaised(elbow: PoseLandmark, wrist: PoseLandmark): Boolean =
            wrist.isHigherThan(elbow) && elbow.sidePositionEqualTo(wrist)

        private fun logLandmarkDetails(pose: Pose) {
            val builder = StringBuilder()
            val dateFormat = SimpleDateFormat.getTimeInstance()
            val time = dateFormat.format(Date(System.currentTimeMillis()))
            builder.append("Detected landmarks ($time):\n")
            with(pose) {
                getPoseLandmark(LEFT_WRIST)?.let { builder.appendLandmark(it) }
                getPoseLandmark(RIGHT_WRIST)?.let { builder.appendLandmark(it) }
                getPoseLandmark(LEFT_ELBOW)?.let { builder.appendLandmark(it) }
                getPoseLandmark(RIGHT_ELBOW)?.let { builder.appendLandmark(it) }
                getPoseLandmark(LEFT_SHOULDER)?.let { builder.appendLandmark(it) }
                getPoseLandmark(RIGHT_SHOULDER)?.let { builder.appendLandmark(it) }
            }
            Timber.d(builder.toString())
        }
    }
}
