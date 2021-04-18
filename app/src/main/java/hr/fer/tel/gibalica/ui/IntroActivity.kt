package hr.fer.tel.gibalica.ui

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import hr.fer.tel.gibalica.databinding.ActivityIntroBinding
import timber.log.Timber

class IntroActivity : FragmentActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
    }

    private fun inflateLayout() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
