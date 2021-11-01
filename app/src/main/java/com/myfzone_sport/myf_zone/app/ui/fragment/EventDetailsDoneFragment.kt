package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsDoneBinding
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsGuest2Binding

private const val ARG_PARAM1 = "eventId"

class EventDetailsDoneFragment : Fragment() {

    companion object {
        private lateinit var binding: FragmentEventDetailsDoneBinding
        private var eventId: String? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
        inflater,
        R.layout.fragment_event_details_done,
        container,
        false
    )

        return binding.root
    }
}