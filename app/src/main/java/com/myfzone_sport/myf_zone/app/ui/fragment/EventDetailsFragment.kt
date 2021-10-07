package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.databinding.FragmentEventDetailsBinding
import org.jetbrains.anko.support.v4.toast

private const val ARG_PARAM1 = "eventId"

class EventDetailsFragment : Fragment() {

    private val viewModel by activityViewModels<EventViewModel>()

    companion object {
        private lateinit var binding: FragmentEventDetailsBinding
        private var eventId: String? = null
    }

    //region Override Methods
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
            inflater, R.layout.fragment_event_details, container, false
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            binding.eventDetailDescription.justificationMode = JUSTIFICATION_MODE_INTER_WORD
        }

        setupViews()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViews() {
        setupEvent()
    }

    private fun setupEvent() {
        eventId.let {
            viewModel.assignEventId(it)
        }

        viewModel.currentEventId.observe(viewLifecycleOwner, {
            viewModel.getEvent(it)
            viewModel.getOwner(it)
        })

        viewModel.currentEvent.observe(viewLifecycleOwner, {
            binding.event = it
        })

        viewModel.currentEventOwner.observe(viewLifecycleOwner, {
            binding.owner = it
        })
    }
    //endregion
}