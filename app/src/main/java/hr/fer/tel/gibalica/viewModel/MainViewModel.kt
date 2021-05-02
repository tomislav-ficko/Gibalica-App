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

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val notificationLiveData = MutableLiveData<NotificationEvent>()
    val updatePoseLiveData = MutableLiveData<GibalicaPose>()
    val detectionResultLiveData = MutableLiveData<PoseDetectionEvent>()

    var startingPoseLandmarks = mapOf<Int, PoseLandmark>()
    var poseToBeDetectedMessage: Int? = null
    private lateinit var poseToBeDetected: GibalicaPose
    private var currentPose = GibalicaPose.ALL_JOINTS_VISIBLE
    private var processingPose = false
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

    private fun setupDetectionLogic() {
        notificationLiveData.observeForever { event ->

            when (event?.eventType) {
                EventType.POSE_DETECTED -> {
                    if (!processingPose) {
                        Timber.d("${currentPose.name} detected")
                        processingPose = true

                        when (currentPose) {
                            GibalicaPose.ALL_JOINTS_VISIBLE -> {
                                notifyActivityAboutEvent(PoseDetectionEvent.INITIAL_POSE_DETECTED)
                                currentPose = GibalicaPose.STARTING_POSE
                                startDetectingCurrentPose()
                                currentCounterCause = CounterCause.DO_NOT_DETECT
                                startCounter(1)
                            }
                            GibalicaPose.STARTING_POSE -> {
                                notifyActivityAboutEvent(PoseDetectionEvent.STARTING_POSE_DETECTED)
                                currentPose = poseToBeDetected
                                startDetectingCurrentPose()
                                currentCounterCause = CounterCause.DO_NOT_DETECT
                                startCounter(3)
                            }
                            else -> {
                                if (!randomTraining) {
                                    notifyActivityAboutEvent(PoseDetectionEvent.WANTED_POSE_DETECTED)
                                    currentCounterCause = CounterCause.FINISH_DETECTION
                                    startCounter(3)
                                } else {
                                    notifyActivityAboutEvent(PoseDetectionEvent.WANTED_POSE_DETECTED)
                                    currentPose = getRandomPose()
                                    currentCounterCause = CounterCause.SWITCHING_TO_NEW_POSE
                                    startCounter(3)
                                    startDetectingCurrentPose()
                                }
                            }
                        }
                    }
                }
                EventType.POSE_NOT_DETECTED -> {
                    if (!processingPose &&
                        currentPose != GibalicaPose.ALL_JOINTS_VISIBLE &&
                        currentPose != GibalicaPose.STARTING_POSE
                    ) {
                        Timber.d("${poseToBeDetected.name} not detected")
                        processingPose = true

                        notifyActivityAboutEvent(PoseDetectionEvent.NOT_DETECTED)
                        currentCounterCause = CounterCause.HIDE_NEGATIVE_RESULT
                        startCounter(2)
                    }
                }
                EventType.COUNTER_FINISHED -> {
                    when (currentCounterCause) {
                        CounterCause.FINISH_DETECTION ->
                            notifyActivityAboutEvent(PoseDetectionEvent.FINISH_DETECTION)
                        CounterCause.SWITCHING_TO_NEW_POSE ->
                            notifyActivityAboutEvent(PoseDetectionEvent.UPDATE_MESSAGE)
                        CounterCause.HIDE_NEGATIVE_RESULT -> {
                            notifyActivityAboutEvent(PoseDetectionEvent.HIDE_RESPONSE)
                            processingPose = false
                            // In order to prevent multiple negative messages from appearing one
                            // after the other, I would have to introduce another semaphore which would
                            // be set to true here, but the original one would be set to true because
                            // we want to allow the right pose to be detected
                            // The problem is that this EventType LiveData cannot be used for this
                            // second semaphore
                        }
                        CounterCause.DO_NOT_DETECT -> processingPose = false
                        CounterCause.NO_EVENT -> {
                        }
                    }
                }
            }
        }
    }

    fun startTraining() {
        Timber.d("Sending initial pose to analyzer.")
        startDetectingCurrentPose()
    }

    private fun startDetectingCurrentPose() {
        updatePoseLiveData.value = currentPose
    }

    private fun notifyActivityAboutEvent(detectionEvent: PoseDetectionEvent) {
        detectionResultLiveData.value = detectionEvent
    }

    private fun getRandomPose(): GibalicaPose {
        TODO("Not yet implemented")
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
