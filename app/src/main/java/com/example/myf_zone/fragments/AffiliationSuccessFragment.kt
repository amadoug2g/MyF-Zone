package com.example.myf_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import kotlinx.android.synthetic.main.fragment_affiliation_success.view.*

class AffiliationSuccessFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setTitle(R.string.affiliation_request_success_text)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater =
            inflater.inflate(R.layout.fragment_affiliation_success, container, false)

        fragmentInflater.affiliationLaterNotifications.setOnClickListener {

            Navigation
                .findNavController(fragmentInflater)
                .navigate(R.id.affiliationSuccessToMaps)
        }

        return fragmentInflater
    }
}