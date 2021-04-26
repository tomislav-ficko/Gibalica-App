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
                return get(LEFT_HIP)!!.isHeightEqualTo(get(LEFT_KNEE)!!)
                        && get(RIGHT_HIP)!!.isHeightEqualTo(get(RIGHT_KNEE)!!)
            }
        }

        fun Pose.isTPosePerformed(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()
            with(getLandmarks()) {
                return get(LEFT_WRIST)!!.isHeightEqualTo(get(RIGHT_WRIST)!!)
                        && get(LEFT_ELBOW)!!.isHeightEqualTo(get(RIGHT_ELBOW)!!)
                        && isStandingUpright(this)
            }
        }

        fun Pose.isLeftHandRaised(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()
            with(getLandmarks()) {
                return isArmRaised(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
                        && isArmLowered(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
            }
        }

        fun Pose.isRightHandRaised(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()
            with(getLandmarks()) {
                return isArmRaised(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
                        && isArmLowered(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
            }
        }

        fun Pose.areBothHandsRaised(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()
            with(getLandmarks()) {
                return isArmRaised(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
                        && isArmRaised(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
            }
        }

        fun Pose.areAllJointsVisible(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()
            with(getLandmarks()) {
                return get(LEFT_SHOULDER)!!.isVisible()
                        && get(RIGHT_SHOULDER)!!.isVisible()
                        && get(LEFT_WRIST)!!.isVisible()
                        && get(RIGHT_WRIST)!!.isVisible()
                        && get(LEFT_ELBOW)!!.isVisible()
                        && get(RIGHT_ELBOW)!!.isVisible()
                        && get(LEFT_HIP)!!.isVisible()
                        && get(RIGHT_HIP)!!.isVisible()
                        && get(LEFT_KNEE)!!.isVisible()
                        && get(RIGHT_KNEE)!!.isVisible()
            }
        }

        fun Pose.isStartingPoseDetected(): Boolean {
            if (landmarksNotPresent()) return false
            logLandmarkDetails()
            return with(getLandmarks()) {
                isArmLowered(get(LEFT_ELBOW)!!, get(LEFT_WRIST)!!)
                        && isArmLowered(get(RIGHT_ELBOW)!!, get(RIGHT_WRIST)!!)
                        && isStandingUpright(this)
            }
        }

        private fun isArmLowered(elbow: PoseLandmark, wrist: PoseLandmark): Boolean =
            elbow.isHigherThan(wrist) && elbow.sidePositionEqualTo(wrist)

        private fun isArmRaised(elbow: PoseLandmark, wrist: PoseLandmark): Boolean =
            wrist.isHigherThan(elbow) && elbow.sidePositionEqualTo(wrist)

        private fun isStandingUpright(landmarks: Map<Int, PoseLandmark>): Boolean =
            landmarks[LEFT_SHOULDER]!!.isHeightEqualTo(landmarks[RIGHT_SHOULDER]!!)
                    && landmarks[LEFT_HIP]!!.sidePositionEqualTo(landmarks[LEFT_KNEE]!!)
                    && landmarks[RIGHT_HIP]!!.sidePositionEqualTo(landmarks[RIGHT_KNEE]!!)

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
