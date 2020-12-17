package mfz.myfzone_sport.myf_zone.screens

import android.app.Activity
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.ActivityMainScreenBinding
import mfz.myfzone_sport.myf_zone.setupWithNavController
import org.jetbrains.anko.toast
import java.util.*
import kotlin.concurrent.schedule


class MainScreen : AppCompatActivity(), NavController.OnDestinationChangedListener {

    companion object {
        private val TAG = MainScreen::class.java.simpleName
        lateinit var navController: NavController

        private var currentNavController: LiveData<NavController>? = null
        private var doubleBackToExitPressedOnce = false

        private lateinit var binding: ActivityMainScreenBinding
        private lateinit var viewModel: MainViewModel
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setTheme(R.style.AppTheme)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_screen)

        Log.d(TAG, "onCREATE")

        binding.apply {
            lifecycleOwner = this@MainScreen
            executePendingBindings()
        }

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        lifecycleScope.launch {
            viewModel.checkUserAffiliationStatus()
        }

        binding.accountButton.background = null

        if (savedInstanceState == null) {
            setupBottomNavigationBar()
        }

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        navController = navHostFragment.navController

        //bottomNavBar.selectedItemId = R.id.map
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        setupBottomNavigationBar()
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        binding.bottomNavBar.apply {
            background = null
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onBackPressed() {
        try {
            if (navController.currentDestination!!.id == R.id.calendarFragment) {
                if (doubleBackToExitPressedOnce || supportFragmentManager.backStackEntryCount != 0) {
                    super.onBackPressed()
                    return
                }

                doubleBackToExitPressedOnce = true
                toast(getString(R.string.exit_message))

                val delay: Long = 2000
                Timer().schedule(delay) {
                    doubleBackToExitPressedOnce = false
                }
            } else {
                super.onBackPressed()
            }
        } catch (e: Exception) {
            toast("Error: $e")
            Log.e(TAG, "Error in onBackPressed")
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        when (destination.id) {
            R.id.mapsFragment -> {
                mainNavBarAppearance()
                binding.bottomBar.hideOnScroll = false

                binding.fabMain.setOnClickListener {
                    fabButton(R.id.mapsFragment)
                }

                binding.accountButton.setOnClickListener {
                    profileButton(R.id.mapsFragment)
                }
            }
            R.id.messageFragment -> {
                mainNavBarAppearance()
                binding.bottomBar.hideOnScroll = true

                binding.fabMain.setOnClickListener {
                    fabButton(R.id.messageFragment)
                }

                binding.accountButton.setOnClickListener {
                    profileButton(R.id.messageFragment)
                }
            }
            R.id.calendarFragment -> {
                mainNavBarAppearance()
                binding.bottomBar.hideOnScroll = true

                binding.fabMain.setOnClickListener {
                    fabButton(R.id.calendarFragment)
                }

                binding.accountButton.setOnClickListener {
                    profileButton(R.id.calendarFragment)
                }
            }
            else -> {
                supportActionBar!!.apply {
                    show()
                }

                binding.bottomBar.visibility = View.GONE
                binding.fabMain.visibility = View.GONE
                binding.accountButton.visibility = View.GONE
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        return currentNavController?.value?.navigateUp() ?: false
    }
    //endregion

    //region Bottom Navigation
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

    private fun mainNavBarAppearance() {
        binding.bottomBar.visibility = View.VISIBLE
        binding.fabMain.visibility = View.VISIBLE
        binding.accountButton.visibility = View.VISIBLE

        supportActionBar!!.apply {
            hide()
        }

        binding.fabMain.setImageResource(R.drawable.ic_add)
        binding.fabMain.backgroundTintList = ColorStateList.valueOf(
            ContextCompat.getColor(
                applicationContext,
                R.color.colorAccent
            )
        )
    }
    //endregion

    //region View Methods

    /*
    //    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
    //        if (ev!!.action == MotionEvent.ACTION_DOWN) {
    //            val v: View? = currentFocus
    //            if (v is TextInputEditText) {
    //                val outRect = Rect()
    //                v.getGlobalVisibleRect(outRect)
    //                if (!outRect.contains(ev.rawX.toInt(), ev.rawY.toInt())) {
    //                    if (currentFocus != null) {
    //                        val imm =
    //                            getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
    //                        imm.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
    //                    }
    //                    hideKeyboard()
    //                    v.clearFocus()
    //                }
    //            }
    //        }
    //        return super.dispatchTouchEvent(ev)
    //    }

     */
    private fun Activity.hideKeyboard() {
        hideKeyboard(currentFocus ?: View(this))
    }

    private fun Context.hideKeyboard(view: View) {
        val inputMethodManager =
            getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int) {
        findNavController(binding.fragmentNavHost.id).navigate(destination)
    }

    private fun fabButton(destinationId: Int) {
        when (destinationId) {
            R.id.mapsFragment -> {
                viewModel.isUserSignedIn.observe(this) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(this) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                navigate(R.id.mapsToEventCreation)
                            } else {
                                toast(getString(R.string.user_not_affiliated))
                                navigate(R.id.mapsToAffiliationRequest)
                            }
                        }
                    } else {
                        navigate(R.id.mapsToLogin)
                    }
                }
            }
            R.id.messageFragment -> {
                viewModel.isUserSignedIn.observe(this) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(this) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                navigate(R.id.messageToEventCreation)
                            } else {
                                toast(getString(R.string.user_not_affiliated))
                                navigate(R.id.messageToAffiliationRequest)
                            }
                        }
                    } else {
                        navigate(R.id.messageToLogin)
                    }
                }
            }
            R.id.calendarFragment -> {
                viewModel.isUserSignedIn.observe(this) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(this) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                navigate(R.id.calendarToEventCreation)
                            } else {
                                toast(getString(R.string.user_not_affiliated))
                                navigate(R.id.calendarToAffiliationRequest)
                            }
                        }
                    } else {
                        navigate(R.id.calendarToLogin)
                    }
                }
            }
        }
    }

    private fun profileButton(destinationId: Int) {
        when (destinationId) {
            R.id.mapsFragment -> {
                viewModel.isUserSignedIn.observe(this) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(this) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                navigate(R.id.mapsToProfile)
                            } else {
                                toast(getString(R.string.user_not_affiliated))
                                navigate(R.id.mapsToAffiliationRequest)
                            }
                        }
                    } else {
                        navigate(R.id.mapsToLogin)
                    }
                }
            }
            R.id.messageFragment -> {
                viewModel.isUserSignedIn.observe(this) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(this) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                navigate(R.id.messageToProfile)
                            } else {
                                toast(getString(R.string.user_not_affiliated))
                                navigate(R.id.messageToAffiliationRequest)
                            }
                        }
                    } else {
                        navigate(R.id.messageToLogin)
                    }
                }
            }
            R.id.calendarFragment -> {
                viewModel.isUserSignedIn.observe(this) { isUserSignedIn ->
                    if (isUserSignedIn) {
                        viewModel.isUserAffiliated.observe(this) { isUserAffiliated ->
                            if (isUserAffiliated) {
                                navigate(R.id.calendarToProfile)
                            } else {
                                toast(getString(R.string.user_not_affiliated))
                                navigate(R.id.calendarToAffiliationRequest)
                            }
                        }
                    } else {
                        navigate(R.id.calendarToLogin)
                    }
                }
            }
        }
    }
    //endregion
}