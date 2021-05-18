package hr.fer.tel.gibalica.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.pose.PoseLandmark
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.utils.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import kotlin.random.Random

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    companion object {
        private const val DEFAULT_DETECTION_THRESHOLD_IN_SECONDS = 2L
    }

    val notificationLiveData = MutableLiveData<NotificationEvent>()
    val updatePoseLiveData = MutableLiveData<GibalicaPose>()
    val detectionResultLiveData = MutableLiveData<PoseDetectionEvent>()

    var detectionThresholdInSeconds: Long = DEFAULT_DETECTION_THRESHOLD_IN_SECONDS
    var startingPoseLandmarks = mapOf<Int, PoseLandmark>()
    var poseToBeDetectedMessage: Int? = null
    private lateinit var poseToBeDetected: GibalicaPose
    private var currentPose = GibalicaPose.ALL_JOINTS_VISIBLE
    private var detectionInProgress = true
    private var randomTraining: Boolean = false
    private var currentCounterCause = CounterCause.NO_EVENT

    fun startCounter(valueSeconds: Long) {
        Completable.timer(valueSeconds, TimeUnit.SECONDS, Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                notificationLiveData.value = NotificationEvent(EventType.COUNTER_FINISHED)
            }
    }

    fun initializeTraining(trainingType: TrainingType) {
        when (trainingType) {
            TrainingType.LEFT_HAND -> poseToBeDetected = GibalicaPose.LEFT_HAND_RAISED
            TrainingType.RIGHT_HAND -> poseToBeDetected = GibalicaPose.RIGHT_HAND_RAISED
            TrainingType.BOTH_HANDS -> poseToBeDetected = GibalicaPose.BOTH_HANDS_RAISED
            TrainingType.T_POSE -> poseToBeDetected = GibalicaPose.T_POSE
            TrainingType.SQUAT -> poseToBeDetected = GibalicaPose.SQUAT
            TrainingType.RANDOM -> {
                randomTraining = true
                poseToBeDetected = getRandomPose()
            }
        }
        poseToBeDetectedMessage = getPoseMessage(poseToBeDetected)
        setupDetectionLogic()
    }

    fun startTraining() {
        Timber.d("Sending initial pose to analyzer.")
        updateDetectionOfPose()
    }

    private fun setupDetectionLogic() {
        notificationLiveData.observeForever { event ->

            when (event?.eventType) {
                EventType.POSE_DETECTED -> {
                    if (detectionInProgress) {
                        Timber.d("${currentPose.name} detected")
                        detectionInProgress = false

                        when (currentPose) {
                            GibalicaPose.ALL_JOINTS_VISIBLE -> {
                                notifyViewAboutEvent(PoseDetectionEvent.INITIAL_POSE_DETECTED)
                                currentPose = GibalicaPose.STARTING_POSE
                                updateDetectionOfPose()
                                currentCounterCause = CounterCause.DO_NOT_DETECT
                                startCounter(1)
                            }
                            GibalicaPose.STARTING_POSE -> {
                                notifyViewAboutEvent(PoseDetectionEvent.STARTING_POSE_DETECTED)
                                currentPose = poseToBeDetected
                                updateDetectionOfPose()
                                currentCounterCause = CounterCause.DO_NOT_DETECT
                                startCounter(3)
                            }
                            else -> {
                                if (!randomTraining) {
                                    notifyViewAboutEvent(PoseDetectionEvent.WANTED_POSE_DETECTED)
                                    currentCounterCause = CounterCause.FINISH_DETECTION
                                    startCounter(2)
                                } else {
                                    notifyViewAboutEvent(PoseDetectionEvent.WANTED_POSE_DETECTED)
                                    currentPose = getRandomPose()
                                    poseToBeDetectedMessage = getPoseMessage(currentPose)
                                    updateDetectionOfPose()
                                    currentCounterCause = CounterCause.SWITCHING_TO_NEW_POSE
                                    startCounter(3)
                                }
                            }
                        }
                    }
                }
                EventType.POSE_NOT_DETECTED -> {
                    if (detectionInProgress &&
                        currentPose != GibalicaPose.ALL_JOINTS_VISIBLE &&
                        currentPose != GibalicaPose.STARTING_POSE
                    ) {
                        Timber.d("${poseToBeDetected.name} not detected")
                        detectionInProgress = false

                        notifyViewAboutEvent(PoseDetectionEvent.NOT_DETECTED)
                        currentCounterCause = CounterCause.HIDE_NEGATIVE_RESULT
                        startCounter(2)
                    }
                }
                EventType.COUNTER_FINISHED -> {
                    when (currentCounterCause) {
                        CounterCause.FINISH_DETECTION ->
                            notifyViewAboutEvent(PoseDetectionEvent.FINISH_DETECTION)
                        CounterCause.SWITCHING_TO_NEW_POSE -> {
                            notifyViewAboutEvent(PoseDetectionEvent.UPDATE_MESSAGE)
                            detectionInProgress = true
                        }
                        CounterCause.HIDE_NEGATIVE_RESULT -> {
                            notifyViewAboutEvent(PoseDetectionEvent.HIDE_RESPONSE)
                            detectionInProgress = true
                        }
                        CounterCause.DO_NOT_DETECT -> detectionInProgress = true
                        CounterCause.NO_EVENT -> {
                        }
                    }
                }
            }
        }
    }

    private fun updateDetectionOfPose() {
        updatePoseLiveData.value = currentPose
    }

    private fun notifyViewAboutEvent(detectionEvent: PoseDetectionEvent) {
        detectionResultLiveData.value = detectionEvent
    }

    private fun getRandomPose(): GibalicaPose {
        val numberOfPoses = TrainingType.values().size
        return when (Random.nextInt(numberOfPoses)) {
            0 -> GibalicaPose.LEFT_HAND_RAISED
            1 -> GibalicaPose.RIGHT_HAND_RAISED
            2 -> GibalicaPose.BOTH_HANDS_RAISED
            3 -> GibalicaPose.SQUAT
            else -> GibalicaPose.T_POSE
        }
    }

    private fun getPoseMessage(pose: GibalicaPose): Int? {
        return when (pose) {
            GibalicaPose.LEFT_HAND_RAISED -> R.string.message_left_hand
            GibalicaPose.RIGHT_HAND_RAISED -> R.string.message_right_hand
            GibalicaPose.BOTH_HANDS_RAISED -> R.string.message_both_hands
            GibalicaPose.T_POSE -> R.string.message_t_pose
            GibalicaPose.SQUAT -> R.string.message_squat
            GibalicaPose.STARTING_POSE -> R.string.message_start
            GibalicaPose.ALL_JOINTS_VISIBLE -> R.string.message_initial
            GibalicaPose.NONE -> null
        }
    }
}
