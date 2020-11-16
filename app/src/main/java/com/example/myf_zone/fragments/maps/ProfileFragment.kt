package com.example.myf_zone.fragments.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.myf_zone.R
import com.example.myf_zone.util.user.UserAccount.auth
import com.example.myf_zone.util.user.UserAccount.getCurrentClub
import com.example.myf_zone.util.user.UserAccount.getCurrentUser
import com.example.myf_zone.util.user.UserAccount.updateCurrentUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*

class ProfileFragment : Fragment() {
    private val TAG = ProfileFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        auth = FirebaseAuth.getInstance()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_profile, container, false)

        val currentUser = auth.currentUser

        getCurrentUser { user ->
            fragmentInflater.profile_firstName.text = user.firstName
            fragmentInflater.profile_lastName.text = user.lastName
            fragmentInflater.profileEmail.text = user.mail

            getCurrentClub {
                Glide.with(requireActivity()).apply {
                    load(it.clubLogo)
                        .placeholder(R.drawable.ic_account)
                        .centerCrop()
                        .into(profileClubImage)
                }

//                toast(it.clubLogo)
                profileClubName.text = it.clubAcronym
                val clubPosition = profilePosition.text.toString() + " - ${it.sportName}"
                profilePosition.text = clubPosition
//                var clubCategory = it.categoryName
//                if (it.subCategoryName.isNotEmpty())
//                    clubCategory += " - ${it.subCategoryName}"
//                profileCategory_subCategory.text = clubCategory
            }


            if (currentUser!!.displayName == "") {
                updateCurrentUser("", user.firstName, user.lastName)
            }
        }

        return fragmentInflater
    }
}