package com.example.myf_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import androidx.navigation.fragment.FragmentNavigator
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.transition.TransitionInflater
import com.example.myf_zone.R
import kotlinx.android.synthetic.main.fragment_login.*
import kotlinx.android.synthetic.main.fragment_login.view.*
import kotlinx.android.synthetic.main.fragment_sign_up.*


class LoginFragment : Fragment() {

    private val TAG = LoginFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setTitle(R.string.login_coach)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentInflater = inflater.inflate(R.layout.fragment_login, container, false)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.login_coach)

        fragmentInflater.loginRegister.setOnClickListener {

            val extras = FragmentNavigatorExtras(
                login_email_layout to "email_transition_field",
                login_password_layout to "password_transition_field",
                login_button to "button_transition"
            )

            Navigation
                .findNavController(fragmentInflater)
                .navigate(R.id.loginToSignUp, null, null, extras)

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

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }
}