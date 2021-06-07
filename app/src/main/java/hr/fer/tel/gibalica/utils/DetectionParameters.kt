package hr.fer.tel.gibalica.utils

class DetectionParameters(
    var detectionUseCase: DetectionUseCase? = null,
    var trainingType: TrainingType? = null,
    var difficulty: Difficulty? = null,
    var detectionLengthSeconds: Long? = null
)