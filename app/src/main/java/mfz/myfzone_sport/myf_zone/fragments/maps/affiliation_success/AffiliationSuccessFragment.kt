package mfz.myfzone_sport.myf_zone.fragments.maps.affiliation_success

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_affiliation_success.*
import kotlinx.android.synthetic.main.fragment_affiliation_success.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.util.user.AffiliationForm.queryClubFromCode

class AffiliationSuccessFragment : Fragment() {
    companion object {
        private val TAG = AffiliationSuccessFragment::class.java.simpleName

        private var clubId: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            clubId = it.getString("clubId")
            Log.d(TAG, "onCREATE: $clubId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater =
            inflater.inflate(R.layout.fragment_affiliation_success, container, false)

        fragmentInflater.affiliationActivateNotifications.isEnabled = false

        fragmentInflater.affiliationLaterNotifications.setOnClickListener {
            findNavController().navigate(R.id.globalToMaps)
        }

        return fragmentInflater
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        Log.d(TAG, clubId.toString())

        try {
            CoroutineScope(Main).launch {
                val club = queryClubFromCode(clubId!!)!!

//                affiliationClubImage.setImageResource(0) //club.logo
                affiliationClubName.text = club.acronym

                Log.d(TAG, club.toString())
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error: $e")
        }

    }
}