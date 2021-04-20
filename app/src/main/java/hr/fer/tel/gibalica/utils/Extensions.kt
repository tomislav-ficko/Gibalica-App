package hr.fer.tel.gibalica.utils

import android.view.View
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

private const val X_THRESHOLD = 10f
private const val Y_THRESHOLD = 20f

fun PoseLandmark.sidePositionEqualTo(other: PoseLandmark): Boolean {
    val min = other.position.x - X_THRESHOLD
    val max = other.position.x + X_THRESHOLD
    Timber.d("Checking side position: Is ${position.x} (${getLandmarkName()}) in [$min..$max]?")
    return position.x in min..max
}

fun PoseLandmark.isHigherThan(other: PoseLandmark): Boolean {
    val max = other.position.y + Y_THRESHOLD
    Timber.d("Checking height: Is ${position.y} (${getLandmarkName()}) > $max?")
    return position.y > max
}

fun PoseLandmark.heightEqualTo(other: PoseLandmark): Boolean {
    val min = other.position.y - Y_THRESHOLD
    val max = other.position.y + Y_THRESHOLD
    Timber.d("Checking height: Is ${position.y} (${getLandmarkName()}) in [$min..$max]?")
    return position.y in min..max
}

fun PoseLandmark.getLandmarkName(): String {
    return when (landmarkType) {
        PoseLandmark.LEFT_THUMB -> "LEFT_THUMB"
        PoseLandmark.LEFT_INDEX -> "LEFT_INDEX"
        PoseLandmark.LEFT_PINKY -> "LEFT_PINKY"
        PoseLandmark.LEFT_WRIST -> "LEFT_WRIST"
        PoseLandmark.LEFT_ELBOW -> "LEFT_ELBOW"
        PoseLandmark.LEFT_SHOULDER -> "LEFT_SHOULDER"
        PoseLandmark.LEFT_EAR -> "LEFT_EAR"
        PoseLandmark.LEFT_EYE -> "LEFT_EYE"
        PoseLandmark.LEFT_EYE_INNER -> "LEFT_EYE_INNER"
        PoseLandmark.LEFT_EYE_OUTER -> "LEFT_EYE_OUTER"
        PoseLandmark.LEFT_MOUTH -> "LEFT_MOUTH"
        PoseLandmark.LEFT_HIP -> "LEFT_HIP"
        PoseLandmark.LEFT_KNEE -> "LEFT_KNEE"
        PoseLandmark.LEFT_ANKLE -> "LEFT_ANKLE"
        PoseLandmark.LEFT_HEEL -> "LEFT_HEEL"
        PoseLandmark.LEFT_FOOT_INDEX -> "LEFT_FOOT_INDEX"
        PoseLandmark.NOSE -> "NOSE"
        PoseLandmark.RIGHT_THUMB -> "RIGHT_THUMB"
        PoseLandmark.RIGHT_INDEX -> "RIGHT_INDEX"
        PoseLandmark.RIGHT_PINKY -> "RIGHT_PINKY"
        PoseLandmark.RIGHT_WRIST -> "RIGHT_WRIST"
        PoseLandmark.RIGHT_ELBOW -> "RIGHT_ELBOW"
        PoseLandmark.RIGHT_SHOULDER -> "RIGHT_SHOULDER"
        PoseLandmark.RIGHT_EAR -> "RIGHT_EAR"
        PoseLandmark.RIGHT_EYE -> "RIGHT_EYE"
        PoseLandmark.RIGHT_EYE_INNER -> "RIGHT_EYE_INNER"
        PoseLandmark.RIGHT_EYE_OUTER -> "RIGHT_EYE_OUTER"
        PoseLandmark.RIGHT_MOUTH -> "RIGHT_MOUTH"
        PoseLandmark.RIGHT_HIP -> "RIGHT_HIP"
        PoseLandmark.RIGHT_KNEE -> "RIGHT_KNEE"
        PoseLandmark.RIGHT_ANKLE -> "RIGHT_ANKLE"
        PoseLandmark.RIGHT_HEEL -> "RIGHT_HEEL"
        PoseLandmark.RIGHT_FOOT_INDEX -> "RIGHT_FOOT_INDEX"
        else -> ""
    }
}

fun View.visible() {
    visibility = View.VISIBLE
}

fun View.invisible() {
    visibility = View.INVISIBLE
}

fun StringBuilder.appendLandmark(landmark: PoseLandmark) {
    val positionString = String.format("%15s", landmark.getLandmarkName())
    val x = String.format("%.2f", landmark.position.x)
    val y = String.format("%.2f", landmark.position.y)
    val inFrame = String.format("%.3f", landmark.inFrameLikelihood)
    append("$positionString = ($x, $y) [inFrame: $inFrame]\n")
}

fun Pose.getRequiredLandmarksFor(position: BodyPositions): Map<Int, PoseLandmark> {
    val leftShoulder = getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
        ?: throw IllegalArgumentException("Left shoulder landmark not present")
    val rightShoulder = getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
        ?: throw IllegalArgumentException("Right shoulder landmark not present")
    val leftElbow = getPoseLandmark(PoseLandmark.LEFT_ELBOW)
        ?: throw IllegalArgumentException("Left elbow landmark not present")
    val rightElbow = getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
        ?: throw IllegalArgumentException("Right elbow landmark not present")
    val leftWrist = getPoseLandmark(PoseLandmark.LEFT_WRIST)
        ?: throw IllegalArgumentException("Left wrist landmark not present")
    val rightWrist = getPoseLandmark(PoseLandmark.RIGHT_WRIST)
        ?: throw IllegalArgumentException("Right wrist landmark not present")

    return when (position) {
        BodyPositions.LEFT_HAND_RAISED, BodyPositions.RIGHT_HAND_RAISED, BodyPositions.BOTH_HANDS_RAISED ->
            mapOf(
                Pair(PoseLandmark.LEFT_SHOULDER, leftShoulder),
                Pair(PoseLandmark.RIGHT_SHOULDER, rightShoulder),
                Pair(PoseLandmark.LEFT_ELBOW, leftElbow),
                Pair(PoseLandmark.RIGHT_ELBOW, rightElbow),
                Pair(PoseLandmark.LEFT_WRIST, leftWrist),
                Pair(PoseLandmark.RIGHT_WRIST, rightWrist)
            )
        BodyPositions.SQUAT -> mapOf()
        BodyPositions.T_POSE -> mapOf()
        BodyPositions.ALL_JOINTS_VISIBLE -> mapOf()
        BodyPositions.STARTING_POSE -> mapOf()
        BodyPositions.NONE -> mapOf()
    }
}

fun Pose.logLandmarkDetails() {
    val builder = StringBuilder()
    val dateFormat = SimpleDateFormat.getTimeInstance()
    val time = dateFormat.format(Date(System.currentTimeMillis()))
    builder.append("Detected landmarks ($time):\n")
    with(this) {
        getPoseLandmark(PoseLandmark.LEFT_WRIST)?.let { builder.appendLandmark(it) }
        getPoseLandmark(PoseLandmark.RIGHT_WRIST)?.let { builder.appendLandmark(it) }
        getPoseLandmark(PoseLandmark.LEFT_ELBOW)?.let { builder.appendLandmark(it) }
        getPoseLandmark(PoseLandmark.RIGHT_ELBOW)?.let { builder.appendLandmark(it) }
        getPoseLandmark(PoseLandmark.LEFT_SHOULDER)?.let { builder.appendLandmark(it) }
        getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)?.let { builder.appendLandmark(it) }
    }
    Timber.d(builder.toString())
}
