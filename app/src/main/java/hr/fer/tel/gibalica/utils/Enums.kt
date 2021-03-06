package hr.fer.tel.gibalica.utils

import java.io.Serializable
import kotlin.random.Random

enum class GibalicaPose {
    LEFT_HAND_RAISED,
    RIGHT_HAND_RAISED,
    BOTH_HANDS_RAISED,
    T_POSE,
    SQUAT,
    UPRIGHT,
    STARTING_POSE,
    ALL_JOINTS_VISIBLE,
    NONE;

    companion object {
        fun getRandomDayNightPose(): GibalicaPose {
            return when (Random.nextInt(2)) {
                0 -> SQUAT
                else -> UPRIGHT
            }
        }

        fun getRandomGeneralPose(): GibalicaPose {
            return when (Random.nextInt(5)) {
                0 -> LEFT_HAND_RAISED
                1 -> RIGHT_HAND_RAISED
                2 -> BOTH_HANDS_RAISED
                3 -> SQUAT
                else -> T_POSE
            }
        }
    }
}

enum class TrainingType : Serializable {
    LEFT_HAND,
    RIGHT_HAND,
    BOTH_HANDS,
    T_POSE,
    SQUAT,
    RANDOM
}

enum class Difficulty : Serializable {
    EASY,
    MEDIUM,
    HARD,
    NONE
}

enum class DetectionUseCase : Serializable {
    TRAINING,
    COMPETITION,
    DAY_NIGHT
}

enum class CounterCause {
    SPLASH_SCREEN,
    WAIT_FOR_COMPONENTS_TO_INITIALIZE,
    WAIT_BEFORE_DETECTING_STARTING_POSE,
    HIDE_NEGATIVE_RESULT,
    SWITCHING_TO_NEW_POSE,
    WAIT_BEFORE_NEW_POSE,
    FINISH_DETECTION
}

enum class Language {
    HR,
    EN;

    companion object {
        const val LANGUAGE_BUTTON_ID = "language_button_id"
    }
}

enum class Setting {
    LANGUAGE,
    SOUND,
    VOICE_CONTROL,
    FIRST_START
}

enum class SpeechRecognitionDataSetItem {
    TRAINING,
    COMPETITION,
    DAY_NIGHT,
    LEFT,
    RIGHT,
    BOTH,
    SQUAT,
    POSE,
    RANDOM,
    LENGTH,
    EASY,
    MEDIUM,
    HARD
}
