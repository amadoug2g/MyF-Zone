package com.example.myf_zone.screens

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myf_zone.R
import com.example.myf_zone.setupWithNavController
import com.example.myf_zone.util.user.UserAccount.auth
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main_screen.*
import org.jetbrains.anko.toast
import java.util.*
import kotlin.concurrent.schedule


class MainScreen : AppCompatActivity(), NavController.OnDestinationChangedListener {

    private val TAG = MainScreen::class.java.simpleName
    private var doubleBackToExitPressedOnce = false

    lateinit var navController: NavController
    private var currentNavController: LiveData<NavController>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        Log.d(TAG, "onCREATE")

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        auth = FirebaseAuth.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        bottomNavBar.apply {
            background = null
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        if (doubleBackToExitPressedOnce || supportFragmentManager.backStackEntryCount != 0) {
            super.onBackPressed()
            return
        }
        this.doubleBackToExitPressedOnce = true
        toast(getString(R.string.exit_message))

        val delay: Long = 2000
        Timer().schedule(delay) {
            doubleBackToExitPressedOnce = false
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val navBar: BottomAppBar = this.findViewById(R.id.bottomBar)
        val fabButton: FloatingActionButton = this.findViewById(R.id.fabMain)
        when (destination.id) {
            R.id.mapsFragment -> {
                window.enterTransition = null

                navBar.visibility = View.VISIBLE
                fabButton.visibility = View.VISIBLE

                fabMain.setImageResource(R.drawable.ic_filter)
                fabMain.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorAccent
                    )
                )

                supportActionBar!!.apply {
                    hide()
                }

                bottomBar.hideOnScroll = false
            }
            R.id.messageFragment -> {
                navBar.visibility = View.VISIBLE
                fabButton.visibility = View.VISIBLE

                fabMain.setImageResource(R.drawable.ic_add)
                fabMain.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorCoral
                    )
                )

                bottomBar.hideOnScroll = false

                supportActionBar!!.apply {
                    hide()
                }
            }
            R.id.calendarFragment -> {
                navBar.visibility = View.VISIBLE
                fabButton.visibility = View.VISIBLE

                fabMain.setImageResource(R.drawable.ic_add)
                fabMain.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorAccent
                    )
                )

                bottomBar.hideOnScroll = true

                supportActionBar!!.apply {
                    hide()
                }
            }
            else -> {

                supportActionBar!!.apply {
                    show()
                }

                navBar.visibility = View.GONE
                fabButton.visibility = View.GONE
            }
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        if (ev!!.action == MotionEvent.ACTION_DOWN) {
            val v: View? = currentFocus
            if (v is TextInputEditText) {
                val outRect = Rect()
                v.getGlobalVisibleRect(outRect)
                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
                    if (currentFocus != null) {
                        val imm =
                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
                    }
                    hideKeyboard()
                    v.clearFocus()
                }
            }
        }
        return super.dispatchTouchEvent(ev)
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }

    private fun setupBottomNavigationBar() {
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavBar)

        val navGraphIds = listOf(R.navigation.calendar, R.navigation.map, R.navigation.message)

        // Setup the bottom navigation view with a list of navigation graphs
        val controller = bottomNavigationView.setupWithNavController(
            navGraphIds = navGraphIds,
            fragmentManager = supportFragmentManager,
            containerId = R.id.fragmentNavHost,
            intent = intent
        )

        // Whenever the selected controller changes, setup the action bar.
        controller.observe(this, Observer { navController ->
            setupActionBarWithNavController(navController)
            navController.addOnDestinationChangedListener(this)
        })
        currentNavController = controller
    }

    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }

}