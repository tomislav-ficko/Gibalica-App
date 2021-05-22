package hr.fer.tel.gibalica.utils

import java.io.Serializable

enum class GibalicaPose {
    LEFT_HAND_RAISED,
    RIGHT_HAND_RAISED,
    BOTH_HANDS_RAISED,
    T_POSE,
    SQUAT,
    STARTING_POSE,
    ALL_JOINTS_VISIBLE,
    NONE
}

enum class TrainingType : Serializable {
    LEFT_HAND,
    RIGHT_HAND,
    BOTH_HANDS,
    T_POSE,
    SQUAT,
    RANDOM
}

enum class CompetitionDifficulty : Serializable {
    EASY,
    MEDIUM,
    HARD,
    NONE
}

enum class DetectionUseCase : Serializable {
    TRAINING,
    COMPETITION
}

enum class CounterCause {
    WAIT_BEFORE_DETECTING_STARTING_POSE,
    WAIT_BEFORE_ACTUAL_DETECTION,
    HIDE_NEGATIVE_RESULT,
    SWITCHING_TO_NEW_POSE,
    FINISH_DETECTION
}

enum class Language {
    HR,
    EN;

    companion object {
        const val LANGUAGE_BUTTON_ID = "language_button_id"
    }
}
