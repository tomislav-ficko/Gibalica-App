package hr.fer.tel.gibalica.utils

import timber.log.Timber

class SpeechRecognizerUtils {

    companion object {

        fun stringContainsAllNecessaryDataInEnglish(recognizedSentence: String): Boolean {
            with(recognizedSentence) {
                return when {
                    contains("training") &&
                            (contains("left") or
                                    contains("right") or
                                    contains("both") or
                                    contains("squat") or
                                    contains("pose") or
                                    contains("random")) -> true
                    !contains("competition") && !contains("day night") -> false
                    !contains("easy") && !contains("medium") && !contains("hard") -> false
                    !contains("length") -> false
                    else -> true
                }
            }
        }

        // Sentence should be "start ___ with difficulty ___ and length ___ minutes"
        //    or "start training for ___ pose"
        fun getDetectionParametersFromEnglish(recognizedSentence: String): DetectionParameters {
            val detectionUseCase = when {
                recognizedSentence.contains("training") -> DetectionUseCase.TRAINING
                recognizedSentence.contains("competition") -> DetectionUseCase.COMPETITION
                recognizedSentence.contains("day night") -> DetectionUseCase.DAY_NIGHT
                else -> {
                    Timber.d("Detection use case not recognized.")
                    null
                }
            }
            val trainingType =
                if (detectionUseCase == DetectionUseCase.TRAINING) {
                    when {
                        recognizedSentence.contains("left") -> TrainingType.LEFT_HAND
                        recognizedSentence.contains("right") -> TrainingType.RIGHT_HAND
                        recognizedSentence.contains("both") -> TrainingType.BOTH_HANDS
                        recognizedSentence.contains("squat") -> TrainingType.SQUAT
                        recognizedSentence.contains("pose") -> TrainingType.T_POSE
                        else -> TrainingType.RANDOM
                    }
                } else null
            val difficulty =
                if (detectionUseCase == DetectionUseCase.COMPETITION || detectionUseCase == DetectionUseCase.DAY_NIGHT) {
                    when {
                        recognizedSentence.contains("easy") -> Difficulty.EASY
                        recognizedSentence.contains("medium") -> Difficulty.MEDIUM
                        else -> Difficulty.HARD
                    }
                } else null
            val detectionLengthSeconds =
                if (recognizedSentence.contains("length")) {
                    recognizedSentence.filter { it.isDigit() }.toLong()
                } else null
            return DetectionParameters(detectionUseCase, trainingType, difficulty, detectionLengthSeconds)
        }

        fun stringContainsAllNecessaryDataInCroatian(recognizedSentence: String): Boolean {
            with(recognizedSentence) {
                return when {
                    contains("trening") &&
                            (contains("lijev") or
                                    contains("desn") or
                                    contains("obje") or
                                    contains("čučanj") or
                                    contains("poz") or
                                    contains("nasumičn")) -> true
                    !contains("natjecanje") && !contains("dan noć") -> false
                    !contains("lagano") && !contains("srednje") && !contains("teško") -> false
                    !contains("duljina") -> false
                    else -> true
                }
            }
        }

        fun getDetectionParametersFromCroatian(recognizedSentence: String): DetectionParameters {
            val detectionUseCase = when {
                recognizedSentence.contains("trening") -> DetectionUseCase.TRAINING
                recognizedSentence.contains("natjecanje") -> DetectionUseCase.COMPETITION
                recognizedSentence.contains("dan noć") -> DetectionUseCase.DAY_NIGHT
                else -> {
                    Timber.d("Detection use case not recognized.")
                    null
                }
            }
            val trainingType =
                if (detectionUseCase == DetectionUseCase.TRAINING) {
                    when {
                        recognizedSentence.contains("lijev") -> TrainingType.LEFT_HAND
                        recognizedSentence.contains("desn") -> TrainingType.RIGHT_HAND
                        recognizedSentence.contains("obje") -> TrainingType.BOTH_HANDS
                        recognizedSentence.contains("čučanj") -> TrainingType.SQUAT
                        recognizedSentence.contains("poz") -> TrainingType.T_POSE
                        else -> TrainingType.RANDOM
                    }
                } else null
            val difficulty =
                if (detectionUseCase == DetectionUseCase.COMPETITION || detectionUseCase == DetectionUseCase.DAY_NIGHT) {
                    when {
                        recognizedSentence.contains("lagano") -> Difficulty.EASY
                        recognizedSentence.contains("srednje") -> Difficulty.MEDIUM
                        else -> Difficulty.HARD
                    }
                } else null
            val detectionLengthSeconds =
                if (recognizedSentence.contains("duljina")) {
                    recognizedSentence.filter { it.isDigit() }.toLong()
                } else null
            return DetectionParameters(detectionUseCase, trainingType, difficulty, detectionLengthSeconds)
        }
    }
}