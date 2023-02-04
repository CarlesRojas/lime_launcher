package app.pinya.lime.ui.view.activity

import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.WindowInsetsController
import androidx.activity.viewModels
import androidx.core.content.ContextCompat
import androidx.viewpager.widget.ViewPager
import androidx.viewpager.widget.ViewPager.OnPageChangeListener
import app.pinya.lime.R
import app.pinya.lime.data.memory.AppProvider
import app.pinya.lime.databinding.ActivityMainBinding
import app.pinya.lime.ui.view.adapter.MainPagerAdapter
import app.pinya.lime.ui.viewmodel.AppViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val appViewModel: AppViewModel by viewModels()

    private lateinit var viewPager: ViewPager
    private lateinit var customPageAdapter: MainPagerAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(R.layout.activity_main)

        AppProvider.initialize(this.application)

        appViewModel.onCreate()

        linkAdapter()
        dimBackground()

        appViewModel.drawerList.observe(this) { newAppList ->
            customPageAdapter.drawer?.updateAppList(newAppList)
        }
    }

    override fun onResume() {
        super.onResume()

        // TODO update wallpaper daily
        // TODO refetch installed apps
        // TODO Hide any active menu
        // TODO Show home Page
        // TODO Dim background
        // TODO Hide any active menu
    }

    private fun linkAdapter() {
        viewPager = findViewById(R.id.viewPager)

        customPageAdapter = MainPagerAdapter(this, appViewModel).also { adapter ->
            viewPager.adapter = adapter

            viewPager.addOnPageChangeListener(object : OnPageChangeListener {
                override fun onPageScrolled(
                    position: Int,
                    positionOffset: Float,
                    positionOffsetPixels: Int
                ) {
                }

                override fun onPageSelected(position: Int) {
                    if (position == 0) adapter.onHomePageSelected() else adapter.onDrawerPageSelected()
                }

                override fun onPageScrollStateChanged(state: Int) {}
            })
        }
    }

    private fun dimBackground() {
        val isTextBlack = false // TODO get from settings
        val shouldDimBackground = true

        viewPager.setBackgroundColor(
            ContextCompat.getColor(
                this,
                if (shouldDimBackground) (if (isTextBlack) R.color.white_extra_low else R.color.black_extra_low) else R.color.transparent
            )
        )
    }
}