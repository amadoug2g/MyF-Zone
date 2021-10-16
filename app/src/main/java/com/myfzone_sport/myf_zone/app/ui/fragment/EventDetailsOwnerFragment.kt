package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventDetailsParticipantViewModelFactory
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsBinding

private const val ARG_PARAM1 = "param1"

class EventDetailsOwnerFragment : Fragment() {
    companion object {
        private lateinit var binding: FragmentEventDetailsBinding
        private lateinit var viewModel: EventDetailsParticipantViewModel
        private lateinit var viewModelFactory: EventDetailsParticipantViewModelFactory
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
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_event_details_owner2, container, false)
    }
}