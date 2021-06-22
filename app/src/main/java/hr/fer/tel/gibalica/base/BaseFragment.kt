package hr.fer.tel.gibalica.base

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.utils.Language
import hr.fer.tel.gibalica.utils.SharedPrefsUtils
import timber.log.Timber
import java.util.*

abstract class BaseFragment : Fragment() {

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

    protected fun requestPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.CAMERA),
            REQUEST_CODE_PERMISSIONS
        )
    }

    protected fun permissionsGranted(): Boolean = ContextCompat.checkSelfPermission(
        requireContext(),
        Manifest.permission.CAMERA
    ) == PackageManager.PERMISSION_GRANTED

    protected fun showPermissionErrorToast() = Toast.makeText(
        requireContext(),
        getString(R.string.message_permissions_not_granted),
        Toast.LENGTH_SHORT
    ).show()
}