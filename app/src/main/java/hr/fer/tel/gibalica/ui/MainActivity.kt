package hr.fer.tel.gibalica.ui

import android.os.Bundle
import dagger.hilt.android.AndroidEntryPoint
import hr.fer.tel.gibalica.R
import hr.fer.tel.gibalica.base.BaseActivity

@AndroidEntryPoint
class MainActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }
}
