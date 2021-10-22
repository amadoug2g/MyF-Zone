package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentNewEventAddressBinding

class NewEventAddressFragment : Fragment() {

    companion object {
        private lateinit var binding: FragmentNewEventAddressBinding
//        private lateinit var viewModel: NewEventAddressViewModel
//        private lateinit var viewModelFactory: NewEventViewModelAddressFactory
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_new_event_address,
            container,
            false
        )

        setupViews()

        return binding.root
    }

    private fun setupViews() {
        binding.exitNewEventAddress.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }
}