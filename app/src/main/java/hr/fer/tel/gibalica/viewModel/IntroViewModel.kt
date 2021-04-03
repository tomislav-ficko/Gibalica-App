package hr.fer.tel.gibalica.viewModel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class IntroViewModel @Inject constructor() : ViewModel() {

    val nextButtonLiveData = MutableLiveData<Boolean>()

    fun showNextButton() {
        nextButtonLiveData.postValue(true)
    }
}
