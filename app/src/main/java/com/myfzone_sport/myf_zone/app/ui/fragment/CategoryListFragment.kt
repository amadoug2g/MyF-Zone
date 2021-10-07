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
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryListEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.databinding.FragmentCategoryListBinding

private const val ARG_PARAM1 = "listType"

class CategoryListFragment : Fragment() {
    private val viewModel by activityViewModels<FragmentViewModel>()

    companion object {
        private val TAG = this::class.java.simpleName
        private var listType: String? = null
        private lateinit var binding: FragmentCategoryListBinding
        private lateinit var adapterUserEvents: UserEventAdapter
        private lateinit var adapterCategoryEvents: CategoryListEventAdapter
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
    private fun setUpUserRecyclerView() {
        adapterUserEvents = UserEventAdapter()
        binding.userRecyclerView.adapter = adapterUserEvents
        binding.userRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.userRecyclerView.setHasFixedSize(true)
        binding.userRecyclerView.isNestedScrollingEnabled = true

        binding.userRecyclerView.visibility = View.VISIBLE
        binding.categoryRecyclerView.visibility = View.GONE
    }

    private fun setUpCategoryRecyclerView() {
        adapterCategoryEvents = CategoryListEventAdapter()
        binding.categoryRecyclerView.adapter = adapterCategoryEvents
        binding.categoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.categoryRecyclerView.setHasFixedSize(true)
        binding.categoryRecyclerView.isNestedScrollingEnabled = true

        binding.categoryRecyclerView.visibility = View.VISIBLE
        binding.userRecyclerView.visibility = View.GONE
    }

    private fun setFriendlyEventsRecycler() {
        setUpUserRecyclerView()

        viewModel.friendlyEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)// else toast("empty")
        })
    }

    private fun setPlateauEventsRecycler() {
        setUpUserRecyclerView()

        viewModel.plateauEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)// else toast("empty")
        })
    }

    private fun setTourneyEventsRecycler() {
        setUpUserRecyclerView()

        viewModel.tourneyEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)// else toast("empty")
        })
    }

    private fun setUserEventsRecycler() {
        setUpUserRecyclerView()

        viewModel.userEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }

    private fun setCategoryEventsRecycler() {
        setUpCategoryRecyclerView()

        viewModel.allEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterCategoryEvents.setData(it)
        })
    }
    //endregion

    //region Navigation
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
            binding.layout,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}