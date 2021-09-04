package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.CloseToClubEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import kotlinx.android.synthetic.main.home_close_to_club_layout.view.*

class HomeFragment : Fragment() {

    private val viewModel by activityViewModels<FragmentViewModel>()

    companion object {
        private lateinit var binding: FragmentHomeBinding
        private lateinit var adapterCloseToClub: CloseToClubEventAdapter
        private lateinit var adapterCategory: CategoryEventAdapter
        private lateinit var adapterUserEvents: UserEventAdapter
        lateinit var viewModel: FragmentViewModel
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        viewModel.getCloseEvents()
        setupObservers()
        setUpRecyclerViews()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }
    //endregion

    //region RecyclerView
    private fun setUpRecyclerViews() {
        setUpCloseToClubRecycler()
        setUpCategoryRecycler()
        setUpUserEventsRecycler()
    }

    private fun setUpCloseToClubRecycler() {
        adapterCloseToClub = CloseToClubEventAdapter()
        binding.closeToClubLayout.recyclerView.adapter = adapterCloseToClub
        binding.closeToClubLayout.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        viewModel.closeEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterCloseToClub.setData(it)
        })
    }

    private fun setUpCategoryRecycler() {
        adapterCategory = CategoryEventAdapter()
        binding.categoryLayout.recyclerView.adapter = adapterCategory
        binding.categoryLayout.recyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        val categoryList = mutableListOf("Matches Amicaux", "Tournois", "Plateaux")

        adapterCategory.setData(categoryList)
    }

    private fun setUpUserEventsRecycler() {
        adapterUserEvents = UserEventAdapter()
        binding.userEventLayout.recyclerView.adapter = adapterUserEvents
        binding.userEventLayout.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
//        binding.userEventLayout.recyclerView.setHasFixedSize(true)
//        binding.userEventLayout.recyclerView.isNestedScrollingEnabled = true

        viewModel.closeEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }
    //endregion

    //region View Methods
    private fun loadingStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
    }
    //endregion
}