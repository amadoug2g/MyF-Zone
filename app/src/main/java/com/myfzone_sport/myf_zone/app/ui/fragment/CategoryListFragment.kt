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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.CloseToClubEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.databinding.FragmentCategoryListBinding
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import org.jetbrains.anko.support.v4.toast

private const val ARG_PARAM1 = "listType"

class CategoryListFragment : Fragment() {
    private val viewModel by activityViewModels<FragmentViewModel>()

    companion object {
        private val TAG = this::class.java.simpleName
        private var listType: String? = null
        private lateinit var binding: FragmentCategoryListBinding
        private lateinit var adapterUserEvents: UserEventAdapter
//        lateinit var viewModel: FragmentViewModel
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            listType = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_category_list, container, false
        )
        setupObservers()
        setUpRecyclerView()
        when (listType) {
            "friendly" -> {
                setFriendlyEventsRecycler()
            }
            "tourney" -> {
                setTourneyEventsRecycler()
            }
            "plateau" -> {
                setPlateauEventsRecycler()
            }
            "userEvent" -> {
                setUserEventsRecycler()
            }
            "categoryEvent" -> {
                setCategoryEventsRecycler()
            }
            "userParticipation" -> {
            }
            "coachEvent" -> {
            }
            "coachParticipation" -> {
            }
        }

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })
    }
    //endregion

    //region RecyclerView
    private fun setUpRecyclerView() {
        adapterUserEvents = UserEventAdapter()
        binding.recyclerView.adapter = adapterUserEvents
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.setHasFixedSize(true)
        binding.recyclerView.isNestedScrollingEnabled = true
    }

    private fun setFriendlyEventsRecycler() {
        viewModel.friendlyEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)// else toast("empty")
        })
    }

    private fun setPlateauEventsRecycler() {
        viewModel.plateauEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)// else toast("empty")
        })
    }

    private fun setTourneyEventsRecycler() {
        viewModel.tourneyEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)// else toast("empty")
        })
    }

    private fun setUserEventsRecycler() {
        viewModel.userEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }

    private fun setCategoryEventsRecycler() {
        viewModel.allEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }
    //endregion

    //Navigation
    private fun navigate(destination: Int, extra: Bundle? = null, view: View) {
        Navigation
            .findNavController(view)
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun loadingStart() {
//        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
//        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String? = "") {

        Snackbar.make(
            binding.recyclerView,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}