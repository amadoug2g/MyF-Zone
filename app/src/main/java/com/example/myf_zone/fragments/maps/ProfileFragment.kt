package com.example.myf_zone.fragments.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myf_zone.R
import com.example.myf_zone.glide.GlideApp
import com.example.myf_zone.util.user.UserAccount.auth
import com.example.myf_zone.util.user.UserAccount.getCurrentClub
import com.example.myf_zone.util.user.UserAccount.getCurrentUser
import com.example.myf_zone.util.user.UserAccount.pathToReference
import com.example.myf_zone.util.user.UserAccount.updateCurrentUser
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import org.jetbrains.anko.support.v4.toast

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

//        if (globalUser == null) {
//            toast("Not connected")
//        }

        getCurrentUser { user ->
            fragmentInflater.profile_firstName.text = user.firstName
            fragmentInflater.profile_lastName.text = user.lastName
            fragmentInflater.profileEmail.text = user.mail

            getCurrentClub {
                try {
                    GlideApp.with(this).apply {
                        load(pathToReference(it.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(profileClubImage)
                    }
                } catch (e: Exception) {
                    toast("Image could not load: $e")
                }
//                toast("${pathToReference(it.clubLogo)}")
                profileClubName.text = it.clubAcronym
                val clubPosition = profilePosition.text.toString() + " - ${it.sportName}"
                profilePosition.text = clubPosition
                var clubCategory = it.categoryName
                if (it.categoryName!!.isNotEmpty()) {
                    if (it.subCategoryName!!.isNotEmpty()) {
                        clubCategory += " - ${it.subCategoryName}"
                        profileCategory_subCategory.text = clubCategory
                    } else {
                        profileCategory_subCategory.text = clubCategory
                    }
                } else {
                    profileCategory_subCategory.text = ""
                }
            }


            if (currentUser!!.displayName == "") {
                updateCurrentUser("", user.firstName, user.lastName)
            }
        }

        return fragmentInflater
    }
}