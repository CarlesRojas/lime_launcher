package app.pinya.lime.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.Observer
import app.pinya.lime.R
import app.pinya.lime.databinding.ActivityMainBinding
import app.pinya.lime.viewmodel.AppViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val appViewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        appViewModel.appModel.observe(this, Observer { appList ->
            println("DONETE")
        })
    }
}