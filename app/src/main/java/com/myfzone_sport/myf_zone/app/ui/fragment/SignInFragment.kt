package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.ui.activity.MainActivity
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.RegistrationViewModel
import com.myfzone_sport.myf_zone.databinding.FragmentSignInBinding
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.util.Tracking
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class SignInFragment : Fragment() {

    //region Variables
    private val viewModel by activityViewModels<RegistrationViewModel>()

    companion object {
        private lateinit var binding: FragmentSignInBinding
    }
    //endregion

    //region Override Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_sign_in, container, false
        )

        setupViews()
        setupObservers()

        binding.signInBtn.setOnClickListener {
            signIn()
        }

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViews() {
        binding.viewModel = viewModel
    }

    private fun setupObservers() {
        viewModel.errorSignInMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.errorEmailSignUp.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignIn Email Error" to getString(R.string.hint_required))
//                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signInEmailLayout.error = getString(R.string.hint_required)
            } else {
                binding.signInEmailLayout.error = null
            }
        })

        viewModel.errorPasswordSignUp.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignIn Password ${getString(R.string.error_msg)}" to getString(R.string.hint_required))
//                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signInPasswordLayout.error = getString(R.string.hint_required)
            } else {
                binding.signInPasswordLayout.error = null
            }
        })

        viewModel.isSignUpLoading.observe(viewLifecycleOwner, { state ->
            if (state) loadingStart() else loadingStop()
        })

        viewModel.successfulSignIn.observe(viewLifecycleOwner, { state ->
            if (state) {
                toast(getString(R.string.login_message))

                TRACKING.logEvent(Tracking.SIGN_IN_DONE, null)
                startActivity(intentFor<MainActivity>().newTask().clearTask())

//                navigate(R.id.signInFragment2ToAffiliationRequestFragment)
            }
        })
    }
    //endregion

    //region Functions
    private fun signIn() {
        viewModel.signIn()
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun loadingStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String? = "") {

        Snackbar.make(
            binding.signInBtn,
            if (!message.isNullOrEmpty()) message else viewModel.errorSignInMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}