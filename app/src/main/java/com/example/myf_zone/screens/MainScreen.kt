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
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import com.example.myf_zone.R
import com.example.myf_zone.model.coach.Coach
import com.example.myf_zone.util.FirebaseUtil
import com.example.myf_zone.util.FirebaseUtil.auth
import com.google.android.material.bottomappbar.BottomAppBar
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.android.synthetic.main.activity_main_screen.*
import kotlinx.android.synthetic.main.fragment_affiliation_request.*
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.toast
import java.util.*


class MainScreen : AppCompatActivity(),
    BottomNavigationView.OnNavigationItemSelectedListener,
    BottomNavigationView.OnNavigationItemReselectedListener,
    NavController.OnDestinationChangedListener {

    private val TAG = MainScreen::class.java.simpleName

    lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)
        Log.d(TAG, "onCREATE")

        auth = FirebaseAuth.getInstance()

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentNavHost) as NavHostFragment
        navController = navHostFragment.navController
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        bottomNavBar.apply {
            background = null
            setOnNavigationItemSelectedListener(this@MainScreen)
            getMenu().getItem(3).isEnabled = false
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onResume() {
        super.onResume()
        navController.addOnDestinationChangedListener(this)
    }

    override fun onPause() {
        navController.removeOnDestinationChangedListener(this)
        super.onPause()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.mapsFragment -> {
                if (navController.currentDestination!!.id != item.itemId)
                    navigateToFragment(R.id.globalToMaps)
            }

            R.id.listEventFragment -> {
                navigateToFragment(R.id.globalToList)
            }

            R.id.messageFragment -> {
                navigateToFragment(R.id.globalToMessage)
            }
        }
        return true
    }

    override fun onNavigationItemReselected(item: MenuItem) {
        when (item.itemId) {
            R.id.mapsFragment, R.id.listEventFragment, R.id.messageFragment -> {
                toast("reselected")
            }
        }
    }

    override fun onBackPressed() {
        if (!navController.popBackStack()) {
            MaterialAlertDialogBuilder(this)
                .setTitle(getString(R.string.close_app))
                .setMessage(getString(R.string.exit_application))
                .setPositiveButton(getString(R.string.exit_text)) { _: DialogInterface, _: Int ->
                    super.onBackPressed()
                }
                .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                }
                .show()
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
                navBar.visibility = View.VISIBLE
                fabButton.visibility = View.VISIBLE

                fabMain.setImageResource(R.drawable.ic_filter)
                fabMain.backgroundTintList = ColorStateList.valueOf(
                    ContextCompat.getColor(
                        applicationContext,
                        R.color.colorAccent
                    )
                )

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
            }
            R.id.listEventFragment -> {
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
            }
            else -> {
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
                            FirebaseUtil.addUserToDB(coach, id)

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
                            navController.navigate(R.id.globalToMaps)
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

    private fun resetFields() {

        login_email_layout.error = null
        login_password_layout.error = null

        signup_email_layout.error = null
        signup_password_layout.error = null
        signup_firstName_layout.error = null
        signup_lastName_layout.error = null

        affiliationCodeLayout.error = null
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

            "affiliation" -> {

                val affiliationCode = affiliationCodeInput.text.toString()
                val affiliationSport = sportSpinner.selectedItem.toString()
                val affiliationCategory = categorySpinner.selectedItem.toString()
                val affiliationSubCategory = subCategorySpinner.selectedItem.toString()

                if (TextUtils.isEmpty(affiliationCode)) {
                    login_email_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    login_email_layout.error = null
                }

                if (affiliationSport == R.string.sportChoice.toString()) {
                    toast(getString(R.string.sport_select_prompt))
                    valid = false
                }

                if (affiliationCategory == R.string.sportChoice.toString()) {
                    toast(getString(R.string.category_select_prompt))
                    valid = false
                }

                if (affiliationSubCategory == R.string.sportChoice.toString()) {
                    toast(getString(R.string.subCategory_select_prompt))
                    valid = false
                }
            }
        }

        return valid
    }

    private fun navigateToFragment(destination: Int) {
        navController.navigate(destination)
    }
}