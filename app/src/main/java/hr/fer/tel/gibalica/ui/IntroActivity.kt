package hr.fer.tel.gibalica.ui

import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivityIntroBinding

class IntroActivity : BaseActivity() {

    private lateinit var binding: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
        setupFragment()
    }

    private fun setupFragment() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_intro) as NavHostFragment
        val navController = navHostFragment.navController
    }

    private fun inflateLayout() {
        binding = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
