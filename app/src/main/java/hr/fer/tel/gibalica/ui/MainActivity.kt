package hr.fer.tel.gibalica.ui

import android.content.Intent
import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.base.BaseActivity
import hr.fer.tel.gibalica.databinding.ActivityMainBinding
import hr.fer.tel.gibalica.utils.TrainingType
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        inflateLayout()
        defineActions()
    }

    private fun inflateLayout() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        Timber.d("Inflated!")
    }

    private fun defineActions() {
        binding.apply {
            btnTraining.setOnClickListener {
                startActivity(
                    Intent(this@MainActivity, TrainingActivity::class.java)
                        .putExtra(EXTRA_TRAINING_TYPE, TrainingType.LEFT_HAND)
                )
            }
            btnCompetition.setOnClickListener {} // Mode not yet implemented
            btnDayNight.setOnClickListener {} // Mode not yet implemented
        }
    }
}
