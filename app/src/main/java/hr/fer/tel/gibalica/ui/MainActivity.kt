package hr.fer.tel.gibalica.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.databinding.ActivityMainBinding
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : FragmentActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
    }

    private fun inflateLayout() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }
}
