package mfz.myfzone_sport.myf_zone.fragments.maps

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_profile.*
import kotlinx.android.synthetic.main.fragment_profile.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.event.ProfileEventAdapter
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.globalCoachEventList
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentClub
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.pathToReference
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.updateCurrentUser
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
                        clubCategory = "$clubCategory - ${it.subCategoryName}"
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        CoroutineScope(Main).launch {
            showProgressBar(profileProgressBar)
            profileEventRecycler.layoutManager = LinearLayoutManager(requireContext())
            profileEventRecycler.setHasFixedSize(true)


            if (!globalCoachEventList.isNullOrEmpty()) {
                profileEventRecycler.adapter = ProfileEventAdapter(globalCoachEventList!!)
            }
//            else {
//                toast("List is empty")
//            }

            hideProgressBar(profileProgressBar)
        }
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