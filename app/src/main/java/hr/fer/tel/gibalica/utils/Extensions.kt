package hr.fer.tel.gibalica.utils

import com.google.mlkit.vision.pose.PoseLandmark
import timber.log.Timber

private const val X_THRESHOLD = 10f
private const val Y_THRESHOLD = 20f

fun PoseLandmark.sidePositionEqualTo(other: PoseLandmark): Boolean {
    val min = other.position.x - X_THRESHOLD
    val max = other.position.x + X_THRESHOLD
    Timber.d("Checking side position: Is ${position.x} in [$min..$max]?")
    return position.x in min..max
}

fun PoseLandmark.isHigherThan(other: PoseLandmark): Boolean {
    val max = other.position.y + Y_THRESHOLD
    Timber.d("Checking height: Is ${position.y} > $max?")
    return position.y > max
}

fun PoseLandmark.heightEqualTo(other: PoseLandmark): Boolean {
    val min = other.position.y - Y_THRESHOLD
    val max = other.position.y + Y_THRESHOLD
    Timber.d("Checking height: Is ${position.y} in [$min..$max]?")
    return position.y in min..max
}
