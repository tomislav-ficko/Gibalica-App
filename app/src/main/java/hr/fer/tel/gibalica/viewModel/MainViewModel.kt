package hr.fer.tel.gibalica.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.fer.tel.gibalica.utils.CounterCause
import hr.fer.tel.gibalica.utils.EventType
import hr.fer.tel.gibalica.utils.NotificationEvent
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.schedulers.Schedulers
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {

    val notificationLiveData = MutableLiveData<NotificationEvent>()
    val speechRecognizer: LiveData<Boolean>
        get() = mutableSpeechRecognizer
    private val mutableSpeechRecognizer = MutableLiveData<Boolean>()

    fun startCounter(cause: CounterCause, valueSeconds: Long) {
        Completable.timer(valueSeconds, TimeUnit.SECONDS, Schedulers.computation())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe {
                notificationLiveData.value = NotificationEvent(cause, EventType.COUNTER_FINISHED)
            }
    }

    fun enableSpeechRecognizer() {
        Timber.d("Turning speech recognizer on.")
        mutableSpeechRecognizer.value = true
    }

    fun disableSpeechRecognizer() {
        Timber.d("Turning speech recognizer off.")
        mutableSpeechRecognizer.value = false
    }
}
