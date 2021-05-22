package hr.fer.tel.gibalica.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.databinding.ActivitySplashBinding
import hr.fer.tel.gibalica.utils.CounterCause
import hr.fer.tel.gibalica.viewModel.MainViewModel
import timber.log.Timber

private const val TIMER_VALUE_IN_SECONDS = 3L

@AndroidEntryPoint
class SplashActivity : ComponentActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
        setupObservable()

        viewModel.startCounter(CounterCause.SPLASH_SCREEN, TIMER_VALUE_IN_SECONDS)
    }

    private fun inflateLayout() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }

    private fun setupObservable() {
        viewModel.notificationLiveData.observe(this) { startIntro() }
    }

    private fun startIntro() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }
}
