package com.example.myf_zone.screens

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.example.myf_zone.R
import com.example.myf_zone.model.coach.Coach
import com.example.myf_zone.util.FirebaseUtil
import com.example.myf_zone.util.FirebaseUtil.auth
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.intentFor
import org.jetbrains.anko.newTask
import org.jetbrains.anko.toast
import java.util.*

class MainScreen : AppCompatActivity(), BottomNavigationView.OnNavigationItemSelectedListener {

    private val TAG = MainScreen::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main_screen)

        auth = FirebaseAuth.getInstance()
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
                            val welcomeMsg = R.string.welcome_message
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
                            toast("$welcomeMsg, ${user.displayName}")
                            startActivity(intentFor<MainScreen>().newTask().clearTask())
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
                            startActivity(intentFor<MainScreen>().newTask().clearTask())
                        }
                    }
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

    private fun fieldToCoach(coach: Coach): HashMap<String, Any?> {
        return hashMapOf(
            "mail" to coach.mail,
            "firstName" to coach.firstName,
            "lastName" to coach.lastName,
            "id" to coach.id,
            "createdDate" to coach.createdDate
        )
    }

    private fun addUserToDB(coach: Coach, id: String) {
        // Access a Cloud Firestore instance from your Activity

        val user = fieldToCoach(coach)

        FirebaseUtil.db.collection(FirebaseUtil.coachPath)
            .document(id)
            .set(user)
            .addOnSuccessListener {
                Log.d(TAG, "Document added successfully")
            }
            .addOnFailureListener {
                Log.d(TAG, "Document added failed")
            }
            .addOnCompleteListener {
                Log.d(TAG, "Document added completed")
            }
    }

    private fun validateForm(function: String): Boolean {
        var valid = true

        when (function) {
            "login" -> {
                val email = login_email_input.text.toString()
                if (TextUtils.isEmpty(email)) {
                    login_email_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    login_email_layout.error = null
                }

                val password = login_password_input.text.toString()
                if (TextUtils.isEmpty(password)) {
                    login_password_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    login_password_layout.error = null
                }
            }

            "signUp" -> {
                val email = signup_email_input.text.toString()
                if (TextUtils.isEmpty(email)) {
                    signup_email_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_email_layout.error = null
                }

                val password = signup_password_input.text.toString()
                if (TextUtils.isEmpty(password)) {
                    signup_password_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_password_layout.error = null
                }

                val firstName = signup_firstName_input.text.toString()
                if (TextUtils.isEmpty(firstName)) {
                    signup_firstName_layout.error = getString(R.string.hint_required)
                    valid = false
                } else {
                    signup_firstName_layout.error = null
                }

                val lastName = signup_lastName_input.text.toString()
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

    override fun onNavigationItemSelected(item: MenuItem): Boolean {

        return false
    }

    private fun openFragment(destination: Int) {
        val frag = Fragment(R.layout.activity_main_screen)

        val fragmentTransaction: FragmentTransaction =
            supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(destination, frag)
        fragmentTransaction.addToBackStack(null)
        fragmentTransaction.commit()
    }

}