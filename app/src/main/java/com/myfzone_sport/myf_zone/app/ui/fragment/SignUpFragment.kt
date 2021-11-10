package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentSignUp2Binding
import com.myfzone_sport.myf_zone.usecases.registration.AddUserToDatabaseUseCase
import com.myfzone_sport.myf_zone.usecases.registration.AssignDisplayNameUseCase
import com.myfzone_sport.myf_zone.usecases.registration.AssignProfileImageUseCase
import com.myfzone_sport.myf_zone.usecases.registration.SignUpUserUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import com.myfzone_sport.myf_zone.util.Tracking.ALERT_ERROR
import org.jetbrains.anko.support.v4.toast

class SignUpFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentSignUp2Binding
        private lateinit var viewModel: SignUpViewModel
        private lateinit var viewModelFactory: SignUpViewModelFactory
    }
    //endregion

    //region Override Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_sign_up2, container, false
        )
        setupViewModel()
        setupViews()
        setupObservers()

        binding.signUpBtn.setOnClickListener {
            signUp()
        }

        binding.loginRegister.setOnClickListener {
            navigate(R.id.signUpFragment2ToSignInFragment)
        }

        return binding.root
    }

    override fun onStop() {
        super.onStop()
        resetErrors()
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        binding.apply {
            lifecycleOwner = this@SignUpFragment
            executePendingBindings()
        }

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val addUserToDatabaseUseCase = AddUserToDatabaseUseCase(repository)
        val assignDisplayNameUseCase = AssignDisplayNameUseCase(repository)
        val assignProfileImageUseCase = AssignProfileImageUseCase(repository)
        val signUpUserUseCase = SignUpUserUseCase(repository)

        viewModelFactory = SignUpViewModelFactory(
            addUserToDatabaseUseCase,
            assignDisplayNameUseCase,
            assignProfileImageUseCase,
            signUpUserUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory).get(
            SignUpViewModel::class.java)
    }

    private fun setupViews() {
        binding.viewModel = viewModel
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.errorMessageEmail.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignUp Email Error" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signUpEmailLayout.error = getString(R.string.hint_required)
            } else {
                binding.signUpEmailLayout.error = null
            }
        })

        viewModel.errorMessagePassword.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignUp Password ${getString(R.string.error_msg)}" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signUpPasswordLayout.error = getString(R.string.hint_required)
            } else {
                binding.signUpPasswordLayout.error = null
            }
        })

        viewModel.errorMessageFirstName.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignUp FirstName ${getString(R.string.error_msg)}" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signUpFirstNameLayout.error = getString(R.string.hint_required)
            } else {
                binding.signUpFirstNameLayout.error = null
            }
        })

        viewModel.errorMessageLastName.observe(viewLifecycleOwner, {
            if (it) {
                val bundleTracking = bundleOf("SignUp LastName ${getString(R.string.error_msg)}" to getString(R.string.hint_required))
                TRACKING.logEvent(ALERT_ERROR, bundleTracking)
                binding.signUpLastNameLayout.error = getString(R.string.hint_required)
            } else {
                binding.signUpLastNameLayout.error = null
            }
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { state ->
            if (state) loadingStart() else loadingStop()
        })

        viewModel.successfulSignUp.observe(viewLifecycleOwner, { state ->
            if (state) {
                toast(getString(R.string.account_creation_msg))

                val bundle = bundleOf("page" to R.id.signUpFragment)
                navigate(R.id.signUpFragment2ToAffiliationFragment, bundle)
                TRACKING.logEvent(Tracking.SIGN_UP_DONE, null)
            }
        })
    }
    //endregion

    //region Functions
    private fun signUp() {
        viewModel.signUp()
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
            binding.signUpBtn,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }

    private fun resetErrors() {
        viewModel.resetFields()
    }
    //endregion
}