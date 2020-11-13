package com.example.myf_zone.fragments.secondary

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import com.example.myf_zone.util.StorageUtil.getClubFromCode
import kotlinx.android.synthetic.main.fragment_affiliation_success.*
import kotlinx.android.synthetic.main.fragment_affiliation_success.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AffiliationSuccessFragment : Fragment() {
    private val TAG = AffiliationSuccessFragment::class.java.simpleName

    private var clubId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setTitle(R.string.affiliation_text)
            setHomeButtonEnabled(false)
            setDisplayHomeAsUpEnabled(false)
        }

        arguments?.let {
            clubId = it.getString("clubId")
            Log.d(TAG, "onCREATE: $clubId")
        }
    }

    override fun onDetach() {
        super.onDetach()
        (activity as AppCompatActivity).supportActionBar?.setTitle(R.string.affiliationRequestTitle)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, clubId.toString())

        try {

            CoroutineScope(Dispatchers.IO).launch {
                val club = getClubFromCode(clubId!!)

                withContext(Main) {
//                affiliationClubImage.setImageResource(0) //club.logo
                    affiliationClubName.text = club.acronym
                }

                Log.d(TAG, club.toString())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
        }

    }
}