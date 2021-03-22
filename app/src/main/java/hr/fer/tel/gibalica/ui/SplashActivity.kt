package hr.fer.tel.gibalica.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivitySplashBinding
import hr.fer.tel.gibalica.viewModel.MainViewModel

private const val TIMER_VALUE_IN_SECONDS = 3L

@AndroidEntryPoint
class SplashActivity : BaseActivity() {

    private lateinit var binding: ActivitySplashBinding
    private val viewModel by viewModels<MainViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
        setupObservable()

        viewModel.startCounter(TIMER_VALUE_IN_SECONDS)
    }

    private fun inflateLayout() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun setupObservable() {
        viewModel.notificationLiveData.observe(this, { startIntro() })
    }

    private fun startIntro() {
        startActivity(Intent(this, MainActivity::class.java))
    }
}