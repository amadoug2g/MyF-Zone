package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryListEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentCategoryListBinding
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase

private const val ARG_PARAM1 = "listType"

class CategoryListFragment : Fragment() {

    //region Variables
    companion object {
        private val TAG = this::class.java.simpleName
        private var listType: String? = null
        private lateinit var binding: FragmentCategoryListBinding
        private lateinit var viewModel: CategoryListViewModel
        private lateinit var viewModelFactory: CategoryListViewModelFactory
        private lateinit var adapterUserEvents: UserEventAdapter
        private lateinit var adapterCategoryEvents: CategoryListEventAdapter
    }
    //endregion

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
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_category_list, container, false
        )

        setupViewModel()
        setupViews()
        setupObservers()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        binding.apply {
            lifecycleOwner = this@CategoryListFragment
            executePendingBindings()
        }

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getAllEventsUseCase = GetAllEventsUseCase(repository)

        viewModelFactory = CategoryListViewModelFactory(
            getAllEventsUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(CategoryListViewModel::class.java)
    }

    private fun setupViews() {
        binding.apply {
            lifecycleOwner = this@CategoryListFragment
            executePendingBindings()
        }

        binding.backArrow.background = null

        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        when (listType) {
            "friendly" -> {
                setFriendlyEventsRecycler()
                binding.pageTitle.text = "Match Amicaux"
            }
            "tourney" -> {
                setTourneyEventsRecycler()
                binding.pageTitle.text = "Tournois"
            }
            "plateau" -> {
                setPlateauEventsRecycler()
                binding.pageTitle.text = "Plateaux"
            }
            "userEvent" -> {
                viewModel.allEventsList.observe(viewLifecycleOwner, { list ->
                    viewModel.getUserEvents(list)
                })
                binding.pageTitle.text = "Vos évènements"
                setUserEventsRecycler()
            }
            "categoryEvent" -> {
                setCategoryEventsRecycler()
                binding.pageTitle.text = "Évènements"
            }
            "userParticipation" -> {
                binding.pageTitle.text = "Vos participations"
            }
            "coachEvent" -> {
                binding.pageTitle.text = "Ses évènements"
            }
            "coachParticipation" -> {
                binding.pageTitle.text = "Ses participations"
            }
        }
    }

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
        binding.eventRecyclerView.adapter = adapterUserEvents
        binding.eventRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.eventRecyclerView.setHasFixedSize(true)
        binding.eventRecyclerView.isNestedScrollingEnabled = true
    }

    private fun setUpCategoryRecyclerView() {
        adapterCategoryEvents = CategoryListEventAdapter()
        binding.eventRecyclerView.adapter = adapterCategoryEvents
        binding.eventRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.eventRecyclerView.setHasFixedSize(true)
        binding.eventRecyclerView.isNestedScrollingEnabled = true
    }

    private fun setFriendlyEventsRecycler() {
        setUpUserRecyclerView()

//        viewModel.friendlyEventsList.observe(viewLifecycleOwner, {
        viewModel.friendlyEventsNotOwnedList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }

    private fun setPlateauEventsRecycler() {
        setUpUserRecyclerView()

//        viewModel.plateauEventsList.observe(viewLifecycleOwner, {
        viewModel.plateauEventsNotOwnedList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }

    private fun setTourneyEventsRecycler() {
        setUpUserRecyclerView()

//        viewModel.tourneyEventsList.observe(viewLifecycleOwner, {
        viewModel.tourneyEventsNotOwnedList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
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

        viewModel.allEventsNotOwnedList.observe(viewLifecycleOwner, {
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
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
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