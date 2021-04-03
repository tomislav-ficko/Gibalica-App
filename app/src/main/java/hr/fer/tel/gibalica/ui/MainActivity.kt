package hr.fer.tel.gibalica.ui

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivityMainBinding

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
    }

    private fun inflateLayout() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
