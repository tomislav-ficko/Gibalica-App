package hr.fer.tel.gibalica.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.viewModels
import androidx.preference.PreferenceManager
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.databinding.ActivitySplashBinding
import hr.fer.tel.gibalica.utils.CounterCause
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.utils.Setting
import hr.fer.tel.gibalica.utils.SharedPrefsUtils
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
        saveCroatianAsStartupLanguage()
        viewModel.startCounter(CounterCause.SPLASH_SCREEN, TIMER_VALUE_IN_SECONDS)
    }

    private fun inflateLayout() {
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }

    private fun setupObservable() {
        viewModel.notificationLiveData.observe(this) {
            if (SharedPrefsUtils.isFirstApplicationStart(baseContext)) {
                disableIntroOnStartup()
                startIntro()
            } else
                startMainActivity()
        }
    }

    private fun startIntro() {
        startActivity(Intent(this, IntroActivity::class.java))
        finish()
    }

    private fun startMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    private fun saveCroatianAsStartupLanguage() {
        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .edit()
            .putInt(Language.LANGUAGE_BUTTON_ID, -1)
            .putString(Setting.LANGUAGE.name, Language.HR.name)
            .apply()
    }

    private fun disableIntroOnStartup() {
        PreferenceManager.getDefaultSharedPreferences(baseContext)
            .edit()
            .putBoolean(Setting.FIRST_START.name, false)
            .apply()
    }
}
