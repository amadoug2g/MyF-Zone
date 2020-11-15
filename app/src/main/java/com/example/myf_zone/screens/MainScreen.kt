package com.example.myf_zone.screens

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.res.ColorStateList
import android.graphics.Rect
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.myf_zone.R
import com.example.myf_zone.model.coach.Coach
import com.example.myf_zone.setupWithNavController
import com.example.myf_zone.util.user.UserAccount.addUserToDB
import com.example.myf_zone.util.user.UserAccount.auth
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
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

    fun createAccount(view: View) {
        val email = signup_email_input.text.toString()
        val password = signup_password_input.text.toString()
        val firstName = signup_firstName_input.text.toString()
        val lastName = signup_lastName_input.text.toString()
        val time: Date = Calendar.getInstance().time

        showProgressBar(signUpProgressBar)

        when (validateForm("signUp")) {
            true -> {
                auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val id = user!!.uid
                            val coach = Coach(id, email, firstName, lastName, time)
                            addUserToDB(coach, id)

                            val profileUpdates = userProfileChangeRequest {
                                displayName = "$firstName $lastName"
                            }

                            user.updateProfile(profileUpdates)
                                .addOnCompleteListener {
                                    if (it.isSuccessful) {
                                        Log.d(TAG, "User profile updated")
                                    } else {
                                        Log.d(TAG, "An error occurred: ${it.exception.toString()}")
                                    }
                                }
                            toast(getString(R.string.account_creation_msg))
//                            startActivity(intentFor<MainScreen>().newTask().clearTask())
//                            navController.navigate(R.id.signUpToAffiliationRequest)
                            navController.navigate(R.id.globalToAffiliationRequest)
                            supportActionBar!!.apply {
                                show()
                                setTitle(R.string.affiliation_text)
                                setHomeButtonEnabled(true)
                                setDisplayHomeAsUpEnabled(true)
                            }
                        } else {
                            Log.d(TAG, "signInUserWithEmail:failed: " + task.exception)
                            MaterialAlertDialogBuilder(this)
                                .setTitle(R.string.auth_error)
                                .setMessage(task.exception?.localizedMessage.toString())
                                .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                                }
                                .show()
                        }
                    }
            }
            false -> {

            }
        }
        hideProgressBar(signUpProgressBar)
    }

    fun signInUser(view: View) {
        val email = login_email_input.text.toString()
        val password = login_password_input.text.toString()

        showProgressBar(loginProgressBar)

        when (validateForm("login")) {
            true -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser
                            val welcomeBackMsg = R.string.welcome_back_message.toString()
                            toast(welcomeBackMsg + ", ${user!!.displayName}")
//                            startActivity(intentFor<MainScreen>().newTask().clearTask())
//                            navController.navigate(R.id.globalToMaps)
                        } else {
                            Log.d(TAG, "signInUserWithEmail:failed: " + task.exception)
                            MaterialAlertDialogBuilder(this)
                                .setTitle(R.string.auth_error)
                                .setMessage(task.exception?.localizedMessage.toString())
                                .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                                }
                                .show()
                        }
                    }
            }
            false -> {

            }
        }
        hideProgressBar(loginProgressBar)
    }

    private fun showProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.GONE
        }
    }

    private fun validateForm(function: String): Boolean {
        var valid = true

        when (function) {
            "login" -> {

                val email = login_email_input.text.toString()
                val password = login_password_input.text.toString()

                if (TextUtils.isEmpty(email)) {
                    login_email_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    login_email_layout.error = null
                }

                if (TextUtils.isEmpty(password)) {
                    login_password_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    login_password_layout.error = null
                }
            }

            "signUp" -> {

                val email = signup_email_input.text.toString()
                val password = signup_password_input.text.toString()
                val firstName = signup_firstName_input.text.toString()
                val lastName = signup_lastName_input.text.toString()

                if (TextUtils.isEmpty(email)) {
                    signup_email_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_email_layout.error = null
                }

                if (TextUtils.isEmpty(password)) {
                    signup_password_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_password_layout.error = null
                }

                if (TextUtils.isEmpty(firstName)) {
                    signup_firstName_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_firstName_layout.error = null
                }

                if (TextUtils.isEmpty(lastName)) {
                    signup_lastName_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_lastName_layout.error = null
                }
            }
        }

        return valid
    }

    private fun navigateToFragment(destination: Int) {
//        navController.navigate(destination)
    }
}