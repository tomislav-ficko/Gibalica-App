package hr.fer.tel.gibalica.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import hr.fer.tel.gibalica.databinding.ActivityIntroBinding

class IntroActivity : AppCompatActivity() {

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
