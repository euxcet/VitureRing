package com.euxcet.viturering.pages.home

import android.content.pm.ActivityInfo
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager2.widget.ViewPager2
import com.euxcet.viturering.R
import com.euxcet.viturering.RingManager
import com.euxcet.viturering.utils.LanguageUtils
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class HomeActivity : AppCompatActivity() {

    @Inject
    lateinit var ringManager: RingManager

    private var controlView: MainControlView? = null
    private val density by lazy {
        resources.displayMetrics.density
    }
    private lateinit var homeViewModel: HomeViewModel

    private val homePageAdapter: HomePageAdapter by lazy {
        HomePageAdapter(this, 2)
    }

    private var viewPageWidth = 0
    private var viewPageHeight = 0

    private lateinit var viewPager: ViewPager2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN or WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN)
        homeViewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        controlView = findViewById<MainControlView>(R.id.control_view).apply {

        }
        homeViewModel.initCardInfoList()
        viewPager = findViewById<ViewPager2>(R.id.view_pager).apply {
            adapter = homePageAdapter
            viewTreeObserver.addOnGlobalLayoutListener {
                if (viewPageWidth == width && viewPageHeight == height) {
                    return@addOnGlobalLayoutListener
                }
                viewPageWidth = width
                viewPageHeight = height
                homePageAdapter.setPageSize(Size(width, height))
            }
        }
    }

    override fun onResume() {
        super.onResume()
        connectRing()
    }

    private fun performFragmentClick(x: Int, y: Int) {
        val fragment = homePageAdapter.getFragment(viewPager.currentItem)
        if (fragment is CardPageFragment) {
            fragment.performClick(x, y)
        }
    }

    private fun performFragmentCursorMove(x: Int, y: Int) {
        val fragment = homePageAdapter.getFragment(viewPager.currentItem)
        if (fragment is CardPageFragment) {
            fragment.onCursorMove(x, y)
        }
    }

    private fun nextPage() {
        if (viewPager.currentItem < homePageAdapter.itemCount - 1) {
            viewPager.setCurrentItem(viewPager.currentItem + 1, true)
        }
    }

    private fun previousPage() {
        if (viewPager.currentItem > 0) {
            viewPager.setCurrentItem(viewPager.currentItem - 1, true)
        }
    }

    private fun connectRing() {
        ringManager.registerListener {
            onGestureCallback { // Gesture
                runOnUiThread {
                    Log.e("Nuix", "Gesture: $it")
                    val gestureText = "手势: ${LanguageUtils.gestureChinese(it)}"
                    when (it) {
                        "pinch" -> {
                            val cursorPoint = controlView?.getCursorPoint()
                            cursorPoint?.let { p ->
                                performFragmentClick(p.x.toInt(), p.y.toInt())
                            }
                        }
                        "wave_left" -> {
                            previousPage()
                        }
                        "wave_right" -> {
                            nextPage()
                        }
                        "circle_clockwise" -> {
                            nextPage()
                        }
                        "circle_counterclockwise" -> {
                            previousPage()
                        }
                        "snap" -> {
                            moveTaskToBack(true)
                        }
                    }
                }
            }
            onMoveCallback { // Move
                runOnUiThread {
                    controlView?.let { control ->
                        control.move(it.first * (density.coerceAtLeast(2f)) / 2, it.second * (density.coerceAtLeast(2f)) / 2)
                        val cursorPoint = control.getCursorPoint()
                        performFragmentCursorMove(cursorPoint.x.toInt(), cursorPoint.y.toInt())
                    }
                }
            }
            ringManager.connect()
        }
    }
}