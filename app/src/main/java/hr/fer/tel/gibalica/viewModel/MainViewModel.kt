package hr.fer.tel.gibalica.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.fer.tel.gibalica.utils.EventType
import hr.fer.tel.gibalica.utils.NotificationEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val notificationLiveData = MutableLiveData<NotificationEvent>()

    fun startCounter(valueSeconds: Long) {
        Completable.timer(valueSeconds, TimeUnit.SECONDS, Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                notificationLiveData.value = NotificationEvent(EventType.COUNTER_FINISHED)
            }
    }
}
