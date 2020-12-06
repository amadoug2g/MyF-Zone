package mfz.myfzone_sport.myf_zone.fragments.message

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import kotlinx.android.synthetic.main.fragment_message.*
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation

class MessageFragment : Fragment() {
    companion object {
        private val TAG = MessageFragment::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_message, container, false)

        fragmentInflater.account_button.setOnClickListener {
            accountButton()
        }

        return fragmentInflater
    }

    private fun accountButton() {
        val currentUser = auth.currentUser
        account_button.setOnClickListener {
            if (currentUser == null) {
                navigate(R.id.messageToLogin)
            } else {
                UserAccount.getCurrentUser { user ->
                    if (currentUser.displayName == "") {
                        UserAccount.updateCurrentUser("", user.firstName, user.lastName)
                    }
                }

                UserAffiliation.userAffiliationStatus {
                    when (it) {
                        true -> {
                            navigate(R.id.messageToProfile)
                        }
                        false -> {
                            (activity as AppCompatActivity).supportActionBar?.apply {
                                show()
                                setTitle(R.string.affiliation_text)
                                setHomeButtonEnabled(true)
                                setDisplayHomeAsUpEnabled(true)
                            }
                            navigate(R.id.messageToAffiliationRequest)
                        }
                    }
                }
            }
        }
    }

    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }
}