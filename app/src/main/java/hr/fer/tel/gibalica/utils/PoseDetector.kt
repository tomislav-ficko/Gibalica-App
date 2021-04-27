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
                return get(LEFT_KNEE)!!.isHorizontalPositionHigherThan(get(LEFT_HIP)!!) &&
                        get(RIGHT_HIP)!!.isHorizontalPositionHigherThan(get(RIGHT_KNEE)!!) &&
                        get(LEFT_HIP)!!.isVerticalPositionHigherThan(get(LEFT_KNEE)!!) &&
                        get(RIGHT_HIP)!!.isVerticalPositionHigherThan(get(RIGHT_KNEE)!!)
            }
        }

        fun Pose.isTPosePerformed(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return armsSpreadOut(this) &&
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
            with(landmarks) {
                val shouldersInLine =
                    get(LEFT_SHOULDER)!!.isVerticalPositionIdenticalTo(get(RIGHT_SHOULDER)!!)
                val leftLegInLine =
                    get(LEFT_HIP)!!.isHorizontalPositionIdenticalTo(get(LEFT_KNEE)!!)
                val rightLegInLine =
                    get(RIGHT_HIP)!!.isHorizontalPositionIdenticalTo(get(RIGHT_KNEE)!!)
                val torsoInLine =
                    get(LEFT_SHOULDER)!!.isHorizontalPositionEqualTo(get(LEFT_HIP)!!) &&
                            get(RIGHT_SHOULDER)!!.isHorizontalPositionEqualTo(get(RIGHT_HIP)!!)
                Timber.d("Checking if standing upright.. $shouldersInLine && $leftLegInLine && $rightLegInLine && $torsoInLine")
                return shouldersInLine && leftLegInLine && rightLegInLine && torsoInLine
            }
        }

        private fun armsSpreadOut(landmarks: Map<Int, PoseLandmark>): Boolean {
            with(landmarks) {
                val result = get(LEFT_WRIST)!!.isVerticalPositionEqualTo(get(LEFT_ELBOW)!!) &&
                        get(LEFT_ELBOW)!!.isVerticalPositionEqualTo(get(LEFT_SHOULDER)!!) &&
                        get(LEFT_SHOULDER)!!.isVerticalPositionEqualTo(get(RIGHT_SHOULDER)!!) &&
                        get(RIGHT_SHOULDER)!!.isVerticalPositionEqualTo(get(RIGHT_ELBOW)!!) &&
                        get(RIGHT_ELBOW)!!.isVerticalPositionEqualTo(get(RIGHT_WRIST)!!)
                Timber.d("Checking if arms are spread out.. $result")
                return result
            }
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
