package hr.fer.tel.gibalica.base

import androidx.fragment.app.Fragment
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.utils.SharedPrefsUtils
import timber.log.Timber
import java.util.*

open class BaseFragment : Fragment() {

    /**
     * Used to change from which strings.xml file will the resources be retrieved.
     */
    protected fun changeLocaleBasedOnApplicationLanguage() {
        val locale = when (getApplicationLanguage()) {
            Language.EN -> Locale("en")
            Language.HR -> Locale("hr")
        }
        Timber.d("Chosen locale is $locale.")
        val configuration = resources.configuration
        configuration.setLocale(locale)
        resources.updateConfiguration(configuration, resources.displayMetrics)
    }

    protected fun getApplicationLanguage() = SharedPrefsUtils.getApplicationLanguage(requireContext())

    protected fun isSoundEnabled() = SharedPrefsUtils.isSoundEnabled(requireContext())
}