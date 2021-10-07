package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.databinding.FragmentProfile2Binding

private const val ARG_PARAM1 = "coachId"

class ProfileFragment : Fragment() {

    private val viewModel by activityViewModels<FragmentViewModel>()

    companion object {
        private var coachId: String? = null
        private lateinit var binding: FragmentProfile2Binding
        private lateinit var adapterUserEvents: UserEventAdapter
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            coachId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,R.layout.fragment_profile2, container, false
        )

        setupViews()
        setUpUserEventsRecycler()
//        setUpParticipationEventsRecycler()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViews() {
        setupProfile()

        binding.userEventLayout.showAll.setOnClickListener {
            val bundle = bundleOf("listType" to "userEvent")
            navigate(R.id.profileFragmentToCategoryListFragment, bundle)
        }

        binding.participationShowAll.setOnClickListener {
            val bundle = bundleOf("listType" to "userParticipation")
            navigate(R.id.profileFragmentToCategoryListFragment, bundle)
        }
    }

    private fun setupProfile() {
        binding.userEventLayout.layout.visibility = View.VISIBLE

        try {
            binding.coachInfo.text = activeCoach!!.getName()
        } catch (e: Exception) {
            Log.i("TAG", "Error: $e")
        }
    }
    //endregion

    //region RecyclerView
    private fun setUpUserEventsRecycler() {
        adapterUserEvents = UserEventAdapter()
        binding.userEventLayout.recyclerView.adapter = adapterUserEvents
        binding.userEventLayout.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
//        binding.userEventLayout.recyclerView.setHasFixedSize(true)
//        binding.userEventLayout.recyclerView.isNestedScrollingEnabled = true

        viewModel.userEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapterUserEvents.setData(it)
            }
        })
    }

    private fun setUpParticipationEventsRecycler() {
        adapterUserEvents = UserEventAdapter()
        binding.participationRecyclerView.adapter = adapterUserEvents
        binding.participationRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.userEventLayout.recyclerView.setHasFixedSize(true)
        binding.userEventLayout.recyclerView.isNestedScrollingEnabled = true

        viewModel.userEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapterUserEvents.setData(it)
            }
        })
    }
    //endregion

    //Navigation
    private fun navigateWithView(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion
}