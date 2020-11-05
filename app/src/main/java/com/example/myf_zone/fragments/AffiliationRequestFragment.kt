package com.example.myf_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import kotlinx.android.synthetic.main.fragment_affiliation_request.view.*

class AffiliationRequestFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setTitle(R.string.affiliationRequestTitle)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater =
            inflater.inflate(R.layout.fragment_affiliation_request, container, false)

        fragmentInflater.affiliationLaterAffiliate.setOnClickListener {

            Navigation
                .findNavController(fragmentInflater)
                .navigate(R.id.affiliationRequestToMaps)
        }

        fragmentInflater.affiliateButton.setOnClickListener {

            Navigation
                .findNavController(fragmentInflater)
                .navigate(R.id.affiliationRequestToAffiliationSuccess)
        }

        return fragmentInflater
    }
}