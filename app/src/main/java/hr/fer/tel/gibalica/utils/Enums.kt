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

enum class PoseDetectionEvent {
    INITIAL_POSE_DETECTED,
    STARTING_POSE_DETECTED,
    WANTED_POSE_DETECTED,
    NOT_DETECTED,
    UPDATE_MESSAGE,
    FINISH_DETECTION,
    HIDE_RESPONSE
}

enum class CounterCause {
    FINISH_DETECTION,
    SWITCHING_TO_NEW_POSE,
    HIDE_NEGATIVE_RESULT,
    DO_NOT_DETECT,
    NO_EVENT
}
