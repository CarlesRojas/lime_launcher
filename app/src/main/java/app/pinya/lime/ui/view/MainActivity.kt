package app.pinya.lime.ui.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import app.pinya.lime.R
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.databinding.ActivityMainBinding
import app.pinya.lime.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        AppProvider.initialize(this.application)

        appViewModel.onCreate()

        appViewModel.appModel.observe(this, Observer { appList ->
            println("DONETE")
        })
    }
}