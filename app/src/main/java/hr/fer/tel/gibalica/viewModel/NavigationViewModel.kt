package hr.fer.tel.gibalica.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.navigation.NavDirections
import dagger.hilt.android.lifecycle.HiltViewModel
import hr.fer.tel.gibalica.utils.Event
import hr.fer.tel.gibalica.utils.NavCommand
import javax.inject.Inject

@HiltViewModel
class NavigationViewModel @Inject constructor() : ViewModel() {

    private val _navCommand = MutableLiveData<Event<NavCommand>>()
    val navCommand: LiveData<Event<NavCommand>> = _navCommand

    fun navigate(directions: NavDirections) {
        _navCommand.postValue(Event(NavCommand.To(directions)))
    }

    fun navigateUp() {
        _navCommand.postValue(Event(NavCommand.Up))
    }
}
