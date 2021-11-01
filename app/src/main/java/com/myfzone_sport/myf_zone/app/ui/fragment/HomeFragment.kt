package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnectedLive
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.CloseToClubEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.HomeViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.HomeViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.user.*
import org.jetbrains.anko.support.v4.toast
import java.util.*

class HomeFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentHomeBinding
        private lateinit var adapterCloseToClub: CloseToClubEventAdapter
        private lateinit var adapterCategory: CategoryEventAdapter
        private lateinit var adapterUserEvents: UserEventAdapter
        private lateinit var viewModel: HomeViewModel
        private lateinit var viewModelFactory: HomeViewModelFactory
        private lateinit var greeting: String
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        setupViews()
        setupObservers()

        return binding.root
    }

    override fun onResume() {
        super.onResume()

        viewModel.initializeHome()
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getAllEventsUseCase = GetAllEventsUseCase(repository)
        val getUserUseCase = GetUserUseCase(repository)
        val getUserEventListUseCase = GetUserEventListUseCase(repository)
        val getUserClubUseCase = GetUserClubUseCase(repository)
        val getUserAffiliationUseCase = GetUserClubAffiliationUseCase(repository)

        viewModelFactory = HomeViewModelFactory(
            getAllEventsUseCase,
            getUserUseCase,
            getUserEventListUseCase,
            getUserClubUseCase,
            getUserAffiliationUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(HomeViewModel::class.java)
    }

    private fun setupViews() {
        greeting = when (Calendar.HOUR_OF_DAY) {
            in 5..17 -> {
                "Bonjour"
            }
            else -> {
                "Bonsoir"
            }
        }

        binding.apply {
            lifecycleOwner = this@HomeFragment
            executePendingBindings()
        }

        setUpRecyclerViews()
        binding.homeLogo.setImageResource(R.mipmap.logo_updated_white)
        binding.homeChatBtn.setImageResource(R.mipmap.ic_nounchat_noir_2x)

        binding.userEventLayout.showAll.setOnClickListener {
            val bundle = bundleOf("listType" to "userEvent")
            navigate(R.id.homeFragmentToCategoryListFragment, bundle)
        }

        binding.categoryLayout.showAll.setOnClickListener {
            val bundle = bundleOf("listType" to "categoryEvent")
            navigate(R.id.homeFragmentToCategoryListFragment, bundle)
        }

        binding.homeProfileBtn.setOnClickListener {
            checkStatusDestination("profile")
        }

        binding.homeChatBtn.setOnClickListener {
            checkStatusDestination("chat")
        }

        binding.homeCreateEventBtn.setOnClickListener {
            checkStatusDestination("new event")
        }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })

        viewModel.coach.observe(viewLifecycleOwner, {
            if (it != null) {
                binding.closeToClubLayout.layout.visibility = View.VISIBLE
                binding.userEventLayout.layout.visibility = View.VISIBLE

                binding.homeWelcomeText.text = "Bonjour ${it.firstName} !"
            } else {
                binding.closeToClubLayout.layout.visibility = View.GONE
                binding.userEventLayout.layout.visibility = View.GONE

                binding.homeWelcomeText.text = "Bonjour Coach !"
            }
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

        viewModel.userEventsList.observe(viewLifecycleOwner, {
            adapterUserEvents.setData(it)
            binding.userEventLayout.title.text = "Vos évènements (${it.size})"
        })
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

    private fun checkStatusDestination(destination: String) {
        isConnectedLive.observe(viewLifecycleOwner, {
            when (destination) {
                "profile" -> {
                    if (it) {
                        navigate(R.id.homeFragmentToProfilFragment)
                    } else {
                        navigate(R.id.homeFragmentToRegistrationFragment)
                    }
                }
                "chat" -> {
                    if (it) {
                        navigate(R.id.homeFragmentToMessageFragment)
                    } else {
                        navigate(R.id.homeFragmentToRegistrationFragment)
                    }
                }
                "new event" -> {
                    if (it) {
                        navigate(R.id.homeFragmentToNewEventFragment)
                    } else {
                        navigate(R.id.homeFragmentToRegistrationFragment)
                    }
                }
            }
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

    private fun showError(message: String? = "") {
        Snackbar.make(
            binding.homeChatBtn,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}