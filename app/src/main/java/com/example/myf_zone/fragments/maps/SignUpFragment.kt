package com.example.myf_zone.fragments.maps

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.transition.ChangeBounds
import com.example.myf_zone.R
import com.example.myf_zone.model.coach.Coach
import com.example.myf_zone.util.user.UserAccount
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.ktx.userProfileChangeRequest
import kotlinx.android.synthetic.main.fragment_sign_up.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

class SignUpFragment : Fragment() {

    private val TAG = SignUpFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sharedElementEnterTransition = ChangeBounds().apply {
            duration = 300
        }
        sharedElementReturnTransition = ChangeBounds().apply {
            duration = 300
        }

        return inflater.inflate(R.layout.fragment_sign_up, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        signup_button.setOnClickListener {
            createAccount()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
                resetFields()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun createAccount() {
        val email = signup_email_input.text.toString()
        val password = signup_password_input.text.toString()
        val firstName = signup_firstName_input.text.toString()
        val lastName = signup_lastName_input.text.toString()
        val time: Date = Calendar.getInstance().time

        showProgressBar(signUpProgressBar)

        when (validateForm()) {
            true -> {
                UserAccount.auth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val user = UserAccount.auth.currentUser
                            val id = user!!.uid
                            val coach = Coach(id, email, firstName, lastName, time)
                            UserAccount.addUserToDB(coach, id)

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
                            findNavController().navigate(R.id.globalToAffiliationRequest)
                        } else {
                            Log.d(TAG, "signInUserWithEmail:failed: " + task.exception)
                            MaterialAlertDialogBuilder(requireContext())
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

    private fun resetFields() {
        signup_email_layout.error = null
        signup_password_layout.error = null
        signup_firstName_layout.error = null
        signup_lastName_layout.error = null
    }

    private fun validateForm(): Boolean {
        var valid = true

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

        return valid
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
}