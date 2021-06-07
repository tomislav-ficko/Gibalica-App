package hr.fer.tel.gibalica.base

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.utils.SharedPrefsUtils
import timber.log.Timber
import java.util.*

open class BaseActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        changeLocaleBasedOnApplicationLanguage()
    }

    /**
     * Used to change from which strings.xml file will the resources be retrieved.
     */
    private fun changeLocaleBasedOnApplicationLanguage() {
        val locale = when (SharedPrefsUtils.getApplicationLanguage(applicationContext)) {
            Language.EN -> Locale("en")
            Language.HR -> Locale("hr")
        }
        Timber.d("Chosen locale is $locale.")
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }
}