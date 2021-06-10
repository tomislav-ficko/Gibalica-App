package hr.fer.tel.gibalica.utils

import hr.fer.tel.gibalica.utils.SpeechRecognitionDataSetItem.*
import timber.log.Timber

class SpeechRecognizerUtils {

    companion object {

        private val map = createSpeechRecognitionDataSetMap()

        /**
         * Sentence must contain one of the following combination of keywords:
         *
         * 1. 'training' + 'left/right/both/squat/pose/random'
         * 2. 'competition' (medium difficulty, length 4 minutes)
         * 3. 'competition' + 'length' + number + 'easy/medium/hard'
         * 4. 'day night' (medium difficulty, length 4 minutes)
         * 5. 'day night' + 'length' + number + 'easy/medium/hard'
         */
        fun stringContainsAllNecessaryDataInEnglish(recognizedSentence: String) =
            stringContainsAllNecessaryData(recognizedSentence, Language.EN)

        /**
         * Rečenica mora sadržavati neku od sljedećih kombinacija riječi:
         *
         * 1. 'trening' + 'lijeva/desna/obje/čučanj/poza/nasumično'
         * 2. 'natjecanje' (srednja težina, duljina 4 minute)
         * 3. 'natjecanje' + 'duljina' + broj + 'lagano/srednje/teško'
         * 4. 'dan noć' (srednja težina, duljina 4 minute)
         * 5. 'dan noć' + 'duljina' + broj + 'lagano/srednje/teško'
         */
        fun stringContainsAllNecessaryDataInCroatian(recognizedSentence: String) =
            stringContainsAllNecessaryData(recognizedSentence, Language.HR)

        fun getDetectionParametersFromEnglish(recognizedSentence: String) =
            getDetectionParameters(recognizedSentence, Language.EN)

        fun getDetectionParametersFromCroatian(recognizedSentence: String) =
            getDetectionParameters(recognizedSentence, Language.HR)

        private fun stringContainsAllNecessaryData(
            recognizedSentence: String,
            language: Language
        ): Boolean {
            val training = getString(language, TRAINING)
            val competition = getString(language, COMPETITION)
            val dayNight = getString(language, DAY_NIGHT)
            val left = getString(language, LEFT)
            val right = getString(language, RIGHT)
            val both = getString(language, BOTH)
            val squat = getString(language, SQUAT)
            val pose = getString(language, POSE)
            val random = getString(language, RANDOM)

            with(recognizedSentence) {
                return when {
                    contains(training) &&
                            (contains(left) or
                                    contains(right) or
                                    contains(both) or
                                    contains(squat) or
                                    contains(pose) or
                                    contains(random)) -> true
                    !contains(competition) && !contains(dayNight) -> false
                    else -> true
                }
            }
        }

        private fun getDetectionParameters(recognizedSentence: String, language: Language): DetectionParameters {
            val training = getString(language, TRAINING)
            val competition = getString(language, COMPETITION)
            val dayNight = getString(language, DAY_NIGHT)
            val left = getString(language, LEFT)
            val right = getString(language, RIGHT)
            val both = getString(language, BOTH)
            val squat = getString(language, SQUAT)
            val pose = getString(language, POSE)
            val easy = getString(language, EASY)
            val medium = getString(language, MEDIUM)
            val length = getString(language, LENGTH)

            val useCase = when {
                recognizedSentence.contains(training) -> DetectionUseCase.TRAINING
                recognizedSentence.contains(competition) -> DetectionUseCase.COMPETITION
                recognizedSentence.contains(dayNight) -> DetectionUseCase.DAY_NIGHT
                else -> {
                    Timber.d("Detection use case not recognized.")
                    null
                }
            }
            if (useCase == DetectionUseCase.TRAINING) {
                val type = when {
                    recognizedSentence.contains(left) -> TrainingType.LEFT_HAND
                    recognizedSentence.contains(right) -> TrainingType.RIGHT_HAND
                    recognizedSentence.contains(both) -> TrainingType.BOTH_HANDS
                    recognizedSentence.contains(squat) -> TrainingType.SQUAT
                    recognizedSentence.contains(pose) -> TrainingType.T_POSE
                    else -> TrainingType.RANDOM
                }
                return DetectionParameters(detectionUseCase = useCase, trainingType = type)
            }
            val difficulty =
                if (useCase == DetectionUseCase.COMPETITION || useCase == DetectionUseCase.DAY_NIGHT) {
                    when {
                        recognizedSentence.contains(easy) -> Difficulty.EASY
                        recognizedSentence.contains(medium) -> Difficulty.MEDIUM
                        else -> Difficulty.HARD
                    }
                } else null
            val lengthSeconds =
                if (recognizedSentence.contains(length)) {
                    recognizedSentence.filter { it.isDigit() }.let {
                        if (it.isNotBlank()) it.toLong() else null
                    }
                } else null

            return when {
                difficulty != null && lengthSeconds != null ->
                    DetectionParameters(useCase, null, difficulty, lengthSeconds)
                else -> DetectionParameters(useCase, null, Difficulty.MEDIUM, 4)
            }
        }

        private fun createSpeechRecognitionDataSetMap(): Map<Language, Map<SpeechRecognitionDataSetItem, String>> {
            val croatianDataSet = mapOf(
                Pair(TRAINING, "trening"),
                Pair(COMPETITION, "natjecanje"),
                Pair(DAY_NIGHT, "dan noć"),
                Pair(LEFT, "lijev"),
                Pair(RIGHT, "desn"),
                Pair(BOTH, "obje"),
                Pair(SQUAT, "čučanj"),
                Pair(POSE, "poz"),
                Pair(RANDOM, "nasumičn"),
                Pair(EASY, "lagano"),
                Pair(MEDIUM, "srednje"),
                Pair(HARD, "teško"),
                Pair(LENGTH, "duljina")
            )
            val englishDataSet = mapOf(
                Pair(TRAINING, "training"),
                Pair(COMPETITION, "competition"),
                Pair(DAY_NIGHT, "day night"),
                Pair(LEFT, "left"),
                Pair(RIGHT, "right"),
                Pair(BOTH, "both"),
                Pair(SQUAT, "squat"),
                Pair(POSE, "pose"),
                Pair(RANDOM, "random"),
                Pair(EASY, "easy"),
                Pair(MEDIUM, "medium"),
                Pair(HARD, "hard"),
                Pair(LENGTH, "length")
            )
            return mapOf(
                Pair(Language.EN, englishDataSet),
                Pair(Language.HR, croatianDataSet)
            )
        }

        private fun getString(language: Language, item: SpeechRecognitionDataSetItem): String {
            return map[language]?.get(item)!!
        }
    }
}