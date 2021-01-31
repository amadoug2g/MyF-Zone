package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_success

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationSuccessBinding
import com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_success.AffiliationSuccessService.getImageReference
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.coroutines.launch
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class AffiliationSuccessFragment : Fragment() {
    companion object {
        private val TAG = AffiliationSuccessFragment::class.java.simpleName

        private var clubId: String? = null
        private lateinit var binding: FragmentAffiliationSuccessBinding
        private lateinit var viewModel: AffiliationSuccessViewModel
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TRACKING.logEvent(Tracking.AFFILIATION_TO_CLUB_SUCCESS, null)

        arguments?.let {
            clubId = it.getString("clubId")
            Log.i(TAG, "clubId in argument = $clubId")
        }

        viewModel = ViewModelProvider(this).get(AffiliationSuccessViewModel::class.java)

        lifecycleScope.launch {
            viewModel.assignCodeClub(clubId!!)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_success,
            container,
            false
        )

        viewModel.club.observe(viewLifecycleOwner) { club ->
            showProgressBar()
            binding.club = club

            try {
                GlideApp.with(requireContext()).apply {
                    load(getImageReference(club.logo))
                        .placeholder(R.drawable.ic_account)
                        .into(binding.affiliationClubImage)
                }
            } catch (e: Exception) {
                val bundleTracking =
                    bundleOf("Affiliation Success ${getString(R.string.error_msg)}" to e.localizedMessage)
                TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                ("Club Image could not load: $e").toast()
            }
            hideProgressBar()
        }

        binding.apply {
            lifecycleOwner = this@AffiliationSuccessFragment
            executePendingBindings()
        }

//        binding.affiliationActivateNotifications.isEnabled = false

        binding.affiliationActivateNotifications.setOnClickListener {
            TRACKING.logEvent(Tracking.AFFILIATION_TO_CLUB_ACTIVATE_NOTIFICATION, null)
            restartApp()
        }

        binding.affiliationLaterNotifications.setOnClickListener {
            TRACKING.logEvent(Tracking.AFFILIATION_TO_CLUB_SUCCESS_MAYBE_LATER, null)
            restartApp()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.affiliationSuccessShimmerLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.affiliationSuccessShimmerLayout.stopShimmer()
    }

    private fun restartApp() {
        startActivity(intentFor<MainScreen>().newTask().clearTask())
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.affiliationSuccessShimmerLayout.startShimmer()
        binding.affiliationSuccessShimmerLayout.visibility = View.VISIBLE
        binding.affiliationSuccessLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.affiliationSuccessShimmerLayout.stopShimmer()
        binding.affiliationSuccessShimmerLayout.visibility = View.GONE
        binding.affiliationSuccessLayout.visibility = View.VISIBLE
    }
    //endregion

    //region View Methods
    private fun String.toast() {
        toast(this)
    }
    //endregion
}