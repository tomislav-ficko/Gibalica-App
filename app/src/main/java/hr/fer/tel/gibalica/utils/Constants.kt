package hr.fer.tel.gibalica.utils

object Constants {

    // Amount of time in Competition mode between moving on to next pose
    const val DETECTION_INTERVAL_COMPETITION_MILLIS_EASY = 8000L
    const val DETECTION_INTERVAL_COMPETITION_MILLIS_MEDIUM = 5000L
    const val DETECTION_INTERVAL_COMPETITION_MILLIS_HARD = 3000L

    // Amount of time between analyzing pose data
    const val DETECTION_TIMEOUT_MILLIS_DEFAULT = 1000L

    // Amount of time in Day-Night mode between moving on to next pose
    const val DETECTION_INTERVAL_DAY_NIGHT_MILLIS_EASY = 5000L
    const val DETECTION_INTERVAL_DAY_NIGHT_MILLIS_MEDIUM = 4000L
    const val DETECTION_INTERVAL_DAY_NIGHT_MILLIS_HARD = 3000L
}
