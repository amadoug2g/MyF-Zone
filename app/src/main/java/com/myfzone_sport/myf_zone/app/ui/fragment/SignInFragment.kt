package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.checkUserStatus
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.activity.MainActivity
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentSignInBinding
import com.myfzone_sport.myf_zone.usecases.registration.SignInUserUseCase
import com.myfzone_sport.myf_zone.usecases.user.*
import com.myfzone_sport.myf_zone.util.Tracking
import com.myfzone_sport.myf_zone.util.Tracking.ALERT_ERROR
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class SignInFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentSignInBinding
        private lateinit var viewModel: SignInViewModel
        private lateinit var viewModelFactory: SignInViewModelFactory
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

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

        binding.signUpLink.setOnClickListener {
            requireActivity().onBackPressed()
        }

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
                val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val signInUserUseCase = SignInUserUseCase(repository)

        viewModelFactory = SignInViewModelFactory(
            signInUserUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory).get(
            SignInViewModel::class.java)
    }

    private fun setupViews() {
        binding.viewModel = viewModel

        binding.apply {
            lifecycleOwner = this@SignInFragment
            executePendingBindings()
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.errorMessageEmail.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignIn Email Error" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signInEmailLayout.error = getString(R.string.hint_required)
            } else {
                binding.signInEmailLayout.error = null
            }
        })

        viewModel.errorMessagePassword.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignIn Password Error" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signInPasswordLayout.error = getString(R.string.hint_required)
            } else {
                binding.signInPasswordLayout.error = null
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })

        viewModel.successfulSignIn.observe(viewLifecycleOwner, { state ->
            if (state) {
                toast(getString(R.string.login_message))

//                val bundle = bundleOf("page" to R.id.signInFragment)
                TRACKING.logEvent(Tracking.SIGN_IN_DONE, null)
                checkUserStatus()
//                navigate(R.id.signInFragment2ToAffiliationFragment, bundle)
                startActivity(intentFor<MainActivity>().newTask().clearTask())
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
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}