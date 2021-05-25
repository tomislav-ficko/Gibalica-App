package hr.fer.tel.gibalica.utils

object Constants {

    // Amount of time in Competition mode between moving on to next pose
    const val DETECTION_INTERVAL_MILLIS_EASY = 8000L
    const val DETECTION_INTERVAL_MILLIS_MEDIUM = 5000L
    const val DETECTION_INTERVAL_MILLIS_HARD = 2000L

    // Amount of time between analyzing pose data
    const val DETECTION_TIMEOUT_MILLIS_DEFAULT = 2000L
    const val DETECTION_TIMEOUT_MILLIS_EASY = 2000L
    const val DETECTION_TIMEOUT_MILLIS_MEDIUM = 1000L
    const val DETECTION_TIMEOUT_MILLIS_HARD = 500L
}
