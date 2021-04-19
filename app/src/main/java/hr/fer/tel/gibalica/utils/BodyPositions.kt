package hr.fer.tel.gibalica.utils

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import timber.log.Timber
import java.lang.StringBuilder

enum class BodyPositions {
    LEFT_HAND_RAISED,
    RIGHT_HAND_RAISED,
    BOTH_HANDS_RAISED,
    T_POSE,
    SQUAT,
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
            logLandmarks(pose)
            return when {
                squatPerformed(pose) -> SQUAT
                tPosePerformed(pose) -> T_POSE
                handRaised(BOTH_HANDS, pose) -> BOTH_HANDS_RAISED
                handRaised(LEFT_HAND, pose) -> LEFT_HAND_RAISED
                handRaised(RIGHT_HAND, pose) -> RIGHT_HAND_RAISED
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
            val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                ?: throw IllegalArgumentException("Left shoulder landmark not present")
            val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                ?: throw IllegalArgumentException("Right shoulder landmark not present")
            val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                ?: throw IllegalArgumentException("Left elbow landmark not present")
            val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
                ?: throw IllegalArgumentException("Right elbow landmark not present")
            val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                ?: throw IllegalArgumentException("Left wrist landmark not present")
            val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
                ?: throw IllegalArgumentException("Right wrist landmark not present")

            if (!leftShoulder.heightEqualTo(rightShoulder))
                Timber.d("Person is not standing upright")

            return when (handDescriptor) {
                LEFT_HAND -> isArmRaised(leftElbow, leftWrist)
                        && isArmLowered(rightElbow, rightWrist)
                RIGHT_HAND -> isArmRaised(rightElbow, rightWrist)
                        && isArmLowered(leftElbow, leftWrist)
                BOTH_HANDS -> isArmRaised(leftElbow, leftWrist)
                        && isArmRaised(rightElbow, rightWrist)
                else -> throw java.lang.IllegalArgumentException("Wrong handDescriptor argument sent")
            }
        }

        private fun isArmLowered(elbow: PoseLandmark, wrist: PoseLandmark): Boolean =
            elbow.isHigherThan(wrist) && elbow.sidePositionEqualTo(wrist)

        private fun isArmRaised(elbow: PoseLandmark, wrist: PoseLandmark): Boolean =
            wrist.isHigherThan(elbow) && elbow.sidePositionEqualTo(wrist)

        private fun logLandmarks(pose: Pose) {
            val builder = StringBuilder()
            builder.append("\nDetected landmarks:\n")
            pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
                ?.let { builder.appendLandmark("LEFT_WRIST", it) }
            pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
                ?.let { builder.appendLandmark("RIGHT_WRIST", it) }
            pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
                ?.let { builder.appendLandmark("LEFT_ELBOW", it) }
            pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
                ?.let { builder.appendLandmark("RIGHT_ELBOW", it) }
            pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
                ?.let { builder.appendLandmark("LEFT_SHOULDER", it) }
            pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
                ?.let { builder.appendLandmark("RIGHT_SHOULDER", it) }
            Timber.d(builder.toString())
        }

        private fun StringBuilder.appendLandmark(position: String, landmark: PoseLandmark) {
            append("$position = (${landmark.position.x}, ${landmark.position.y}) [inFrame: ${landmark.inFrameLikelihood}]\n")
        }
    }
}
