package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentLogin2Binding

class LoginFragment : Fragment() {
    companion object {
        private lateinit var binding: FragmentLogin2Binding
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_login2,
            container,
            false
        )

        binding.loginConnectBtn.setOnClickListener {
            navigate(R.id.loginFragmentToSignUpFragment2)
        }

        binding.loginSkipBtn.setOnClickListener {
            navigate(R.id.loginFragmentToHomeFragment)
        }

        return binding.root
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion
}