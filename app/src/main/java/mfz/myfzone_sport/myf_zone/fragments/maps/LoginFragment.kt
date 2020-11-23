package mfz.myfzone_sport.myf_zone.fragments.maps

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.transition.TransitionInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.*
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import org.jetbrains.anko.support.v4.toast


class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentInflater = inflater.inflate(R.layout.fragment_login, container, false)

        fragmentInflater.loginRegister.setOnClickListener {

            val extras = FragmentNavigatorExtras(
                login_email_layout to "email_transition_field",
                login_password_layout to "password_transition_field",
                login_button to "button_transition"
            )

            navigate(R.id.loginToSignUp, extras)
            resetFields()

            sharedElementEnterTransition = TransitionInflater.from(requireContext())
                .inflateTransition(R.transition.change_image_transform)
        }

        return fragmentInflater
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FragmentNavigator.Extras.Builder()
            .addSharedElement(signup_email_layout, "email_transition_field")
            .addSharedElement(signup_password_layout, "password_transition_field")
            .addSharedElement(signup_button, "button_transition")
            .build()

        login_button.setOnClickListener {
            signInUser()
        }
    }

    private fun signInUser() {
        val email = login_email_input.text.toString()
        val password = login_password_input.text.toString()

        showProgressBar(loginProgressBar)

        when (validateForm()) {
            true -> {
                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            toast(getString(R.string.login_message))
                            findNavController().navigate(R.id.globalToMaps)
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
        hideProgressBar(loginProgressBar)
    }

    private fun resetFields() {
        login_email_layout.error = null
        login_password_layout.error = null
    }

    private fun validateForm(): Boolean {
        var valid = true

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

    private fun navigate(destination: Int, extra: FragmentNavigator.Extras? = null) {
        findNavController().navigate(destination, null, null, extra)
    }
}