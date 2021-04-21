package hr.fer.tel.gibalica.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.mlkit.vision.pose.PoseLandmark
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.fer.tel.gibalica.utils.EventType.COUNTER_FINISHED
import hr.fer.tel.gibalica.utils.GibalicaPose
import hr.fer.tel.gibalica.utils.NotificationEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val notificationLiveData = MutableLiveData<NotificationEvent>()
    val poseDetectionLiveData = MutableLiveData<GibalicaPose>()

    var startingPoseLandmarks = mapOf<Int, PoseLandmark>()

    fun startCounter(value: Long) {
        Completable.timer(value, TimeUnit.SECONDS, Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                notificationLiveData.postValue(NotificationEvent(COUNTER_FINISHED))
            }
    }
}
