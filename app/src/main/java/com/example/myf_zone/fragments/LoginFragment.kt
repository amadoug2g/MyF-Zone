package com.example.myf_zone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import kotlinx.android.synthetic.main.fragment_login.view.*


class LoginFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity).supportActionBar?.show()
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.login_coach)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_login, container, false)
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.login_coach)

        fragmentInflater.loginRegister.setOnClickListener {
            Log.d("LogInFragment", "Clicked")
            Navigation
                .findNavController(fragmentInflater)
                .navigate(R.id.loginToSignUp)
        }

        return fragmentInflater
    }
}