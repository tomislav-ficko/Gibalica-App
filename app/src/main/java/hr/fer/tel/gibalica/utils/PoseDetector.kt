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
                val leftShoulderHipDistance = get(LEFT_HIP)!!.position.y - get(LEFT_SHOULDER)!!.position.y
                val rightShoulderHipDistance = get(RIGHT_HIP)!!.position.y - get(RIGHT_SHOULDER)!!.position.y
                val averageShoulderHipDistance = listOf(leftShoulderHipDistance, rightShoulderHipDistance).average()
                val halfShoulderHipDistance = averageShoulderHipDistance / 2

                val leftHipKneeDistance = get(LEFT_KNEE)!!.position.y - get(LEFT_HIP)!!.position.y
                val rightHipKneeDistance = get(RIGHT_KNEE)!!.position.y - get(RIGHT_HIP)!!.position.y
                val averageHipKneeDistance = listOf(leftHipKneeDistance, rightHipKneeDistance).average()

                val hipsLowered = averageHipKneeDistance <= halfShoulderHipDistance
                Timber.d("Hips lowered: $hipsLowered ($averageHipKneeDistance <= $halfShoulderHipDistance?).")
                val kneesOutsideHips = get(LEFT_KNEE)!!.isHorizontalPositionHigherThan(get(LEFT_HIP)!!) &&
                        get(RIGHT_HIP)!!.isHorizontalPositionHigherThan(get(RIGHT_KNEE)!!)

                return hipsLowered && kneesOutsideHips
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

        fun Pose.isStandingUpright(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()

            with(getLandmarks()) {
                return isStandingUpright(this)
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
            Timber.d("Checking if arm lowered.. $elbowValueLower && $shoulderValueLower && $wristInLine")
            return elbowValueLower && shoulderValueLower && wristInLine
        }

        private fun isArmRaised(
            shoulder: PoseLandmark,
            elbow: PoseLandmark,
            wrist: PoseLandmark
        ): Boolean {
            val elbowValueHigher = elbow.isVerticalPositionHigherThan(wrist)
            val shoulderValueHigher = shoulder.isVerticalPositionHigherThan(elbow)
            val wristInLine = elbow.isHorizontalPositionEqualTo(wrist)
            Timber.d("Checking if arm raised.. $elbowValueHigher && $shoulderValueHigher && $wristInLine")
            return elbowValueHigher && shoulderValueHigher && wristInLine
        }

        private fun isStandingUpright(landmarks: Map<Int, PoseLandmark>): Boolean {
            with(landmarks) {
                val shouldersInLine =
                    get(LEFT_SHOULDER)!!.isVerticalPositionIdenticalTo(get(RIGHT_SHOULDER)!!)
                val leftLegInLine =
                    get(LEFT_HIP)!!.isHorizontalPositionIdenticalTo(get(LEFT_KNEE)!!)
                val rightLegInLine =
                    get(RIGHT_HIP)!!.isHorizontalPositionIdenticalTo(get(RIGHT_KNEE)!!)

                Timber.d("Checking if standing upright.. $shouldersInLine && $leftLegInLine && $rightLegInLine")
                return shouldersInLine && leftLegInLine && rightLegInLine
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
