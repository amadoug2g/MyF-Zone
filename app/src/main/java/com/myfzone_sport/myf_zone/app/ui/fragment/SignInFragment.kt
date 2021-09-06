package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.RegistrationViewModel
import com.myfzone_sport.myf_zone.databinding.FragmentSignInBinding

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

        setupObservers()

        binding.signInBtn.setOnClickListener {
            signIn()
        }

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

//        viewModel.isLoading.observe(viewLifecycleOwner, { state ->
//            if (state) loadingStart() else loadingStop()
//        })
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