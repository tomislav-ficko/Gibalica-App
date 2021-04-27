package hr.fer.tel.gibalica.utils

import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.google.mlkit.vision.pose.PoseLandmark.*
import timber.log.Timber

class PoseDetector {
    companion object {

        fun Pose.isSquatPerformed(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return get(LEFT_HIP)!!.isVerticalPositionEqualTo(get(LEFT_KNEE)!!) &&
                        get(RIGHT_HIP)!!.isVerticalPositionEqualTo(get(RIGHT_KNEE)!!)
            }
        }

        fun Pose.isTPosePerformed(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return get(LEFT_WRIST)!!.isVerticalPositionEqualTo(get(RIGHT_WRIST)!!) &&
                        get(LEFT_ELBOW)!!.isVerticalPositionEqualTo(get(RIGHT_ELBOW)!!) &&
                        isStandingUpright(this)
            }
        }

        fun Pose.isLeftHandRaised(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return isArmRaised(
                    get(LEFT_SHOULDER)!!,
                    get(LEFT_ELBOW)!!,
                    get(LEFT_WRIST)!!
                ) && isArmLowered(
                    get(RIGHT_SHOULDER)!!,
                    get(RIGHT_ELBOW)!!,
                    get(RIGHT_WRIST)!!
                )
            }
        }

        fun Pose.isRightHandRaised(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return isArmRaised(
                    get(RIGHT_SHOULDER)!!,
                    get(RIGHT_ELBOW)!!,
                    get(RIGHT_WRIST)!!
                ) && isArmLowered(
                    get(LEFT_SHOULDER)!!,
                    get(LEFT_ELBOW)!!,
                    get(LEFT_WRIST)!!
                )
            }
        }

        fun Pose.areBothHandsRaised(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return isArmRaised(
                    get(LEFT_SHOULDER)!!,
                    get(LEFT_ELBOW)!!,
                    get(LEFT_WRIST)!!
                ) && isArmRaised(
                    get(RIGHT_SHOULDER)!!,
                    get(RIGHT_ELBOW)!!,
                    get(RIGHT_WRIST)!!
                )
            }
        }

        fun Pose.areAllJointsVisible(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return get(LEFT_SHOULDER)!!.isVisible() &&
                        get(RIGHT_SHOULDER)!!.isVisible() &&
                        get(LEFT_WRIST)!!.isVisible() &&
                        get(RIGHT_WRIST)!!.isVisible() &&
                        get(LEFT_ELBOW)!!.isVisible() &&
                        get(RIGHT_ELBOW)!!.isVisible() &&
                        get(LEFT_HIP)!!.isVisible() &&
                        get(RIGHT_HIP)!!.isVisible() &&
                        get(LEFT_KNEE)!!.isVisible() &&
                        get(RIGHT_KNEE)!!.isVisible()
            }
        }

        fun Pose.isStartingPoseDetected(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return isArmLowered(
                    get(LEFT_SHOULDER)!!,
                    get(LEFT_ELBOW)!!,
                    get(LEFT_WRIST)!!
                ) && isArmLowered(
                    get(RIGHT_SHOULDER)!!,
                    get(RIGHT_ELBOW)!!,
                    get(RIGHT_WRIST)!!
                ) && isStandingUpright(this)
            }
        }

        private fun isArmLowered(
            shoulder: PoseLandmark,
            elbow: PoseLandmark,
            wrist: PoseLandmark
        ): Boolean {
            val elbowValueLower = wrist.isVerticalPositionHigherThan(elbow)
            val shoulderValueLower = elbow.isVerticalPositionHigherThan(shoulder)
            val wristInLine = elbow.isHorizontalPositionEqualTo(wrist)
            val elbowInLine = shoulder.isHorizontalPositionEqualTo(elbow)
            Timber.d("Checking if arm lowered.. $elbowValueLower && $shoulderValueLower && $wristInLine && $elbowInLine")
            return elbowValueLower && shoulderValueLower && wristInLine && elbowInLine
        }

        private fun isArmRaised(
            shoulder: PoseLandmark,
            elbow: PoseLandmark,
            wrist: PoseLandmark
        ): Boolean {
            val elbowValueHigher = elbow.isVerticalPositionHigherThan(wrist)
            val shoulderValueHigher = shoulder.isVerticalPositionHigherThan(elbow)
            val wristInLine = elbow.isHorizontalPositionEqualTo(wrist)
            val elbowInLine = shoulder.isHorizontalPositionEqualTo(elbow)
            Timber.d("Checking if arm raised.. $elbowValueHigher && $shoulderValueHigher && $wristInLine && $elbowInLine")
            return elbowValueHigher && shoulderValueHigher && wristInLine && elbowInLine
        }

        private fun isStandingUpright(landmarks: Map<Int, PoseLandmark>): Boolean {
            val shouldersInLine =
                landmarks[LEFT_SHOULDER]!!.isVerticalPositionEqualTo(landmarks[RIGHT_SHOULDER]!!)
            val leftLegInLine =
                landmarks[LEFT_HIP]!!.isHorizontalPositionEqualTo(landmarks[LEFT_KNEE]!!)
            val rightLegInLine =
                landmarks[RIGHT_HIP]!!.isHorizontalPositionEqualTo(landmarks[RIGHT_KNEE]!!)
            Timber.d("Checking if standing upright.. $shouldersInLine && $leftLegInLine && $rightLegInLine")
            return shouldersInLine && leftLegInLine && rightLegInLine
        }

        private fun Pose.landmarksNotPresent(): Boolean {
            val landmarks = this.allPoseLandmarks
            if (landmarks.isEmpty()) {
                Timber.d("No person detected")
                return true
            }
            return false
        }
    }
}
