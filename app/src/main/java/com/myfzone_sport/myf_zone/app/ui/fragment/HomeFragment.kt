package com.myfzone_sport.myf_zone.app.ui.fragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.isConnectedLive
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.CloseToClubEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.HomeViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.HomeViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import com.myfzone_sport.myf_zone.domain.coach.Coach
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.usecases.event.*
import com.myfzone_sport.myf_zone.usecases.user.GetUserClubAffiliationUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserClubUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetUserUseCase
import com.myfzone_sport.myf_zone.usecases.user.SignOutUseCase
import com.myfzone_sport.myf_zone.util.Tracking
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() {

    //region Variables
//    private val viewModel by activityViewModels<FragmentViewModel>()

    companion object {
        private lateinit var binding: FragmentHomeBinding
        private lateinit var adapterCloseToClub: CloseToClubEventAdapter
        private lateinit var adapterCategory: CategoryEventAdapter
        private lateinit var adapterUserEvents: UserEventAdapter


        private lateinit var viewModel: HomeViewModel
        private lateinit var viewModelFactory: HomeViewModelFactory
//        lateinit var viewModel: FragmentViewModel
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(/*localDataSource,*/ remoteDataSource)

        //View Model
        val getCloseEventsUseCase = GetCloseEventsUseCase(repository)
        val getFriendlyEventsUseCase = GetFriendlyEventsUseCase(repository)
        val getAllEventsUseCase = GetAllEventsUseCase(repository)
        val getTourneyEventsUseCase = GetTourneyEventsUseCase(repository)
        val getPlateauEventsUseCase = GetPlateauEventsUseCase(repository)
        val getUserEventsUseCase = GetUserEventsUseCase(repository)
        val getUserUseCase = GetUserUseCase(repository)
        val getUserClubUseCase = GetUserClubUseCase(repository)
        val getUserAffiliationUseCase = GetUserClubAffiliationUseCase(repository)
        val signOutUseCase = SignOutUseCase(repository)

        viewModelFactory = HomeViewModelFactory(
            getCloseEventsUseCase,
            getAllEventsUseCase,
            getFriendlyEventsUseCase,
            getTourneyEventsUseCase,
            getPlateauEventsUseCase,
            getUserEventsUseCase,
            getUserUseCase,
            getUserClubUseCase,
            getUserAffiliationUseCase,
            signOutUseCase
        )
        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(HomeViewModel::class.java)

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

        setupViews()
        setupObservers()
//        viewModel.getUserEvents()
//        viewModel.getCloseEvents()

        return binding.root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

    }

    override fun onResume() {
        super.onResume()
    }
    //endregion

    //region Setups
    private fun setupViews() {
        setUpRecyclerViews()

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
//            checkStatusDestination("new event")
            signOut()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })

//        activeCoachLive.observe(viewLifecycleOwner, {
//            if (it != null) {
//                binding.closeToClubLayout.layout.visibility = View.VISIBLE
//                binding.userEventLayout.layout.visibility = View.VISIBLE
//
//                binding.homeWelcomeText.text = "Bonjour ${it.firstName} !"
//            } else {
//                binding.closeToClubLayout.layout.visibility = View.GONE
//                binding.userEventLayout.layout.visibility = View.GONE
//
//                binding.homeWelcomeText.text = "Bonjour Coach !"
//            }
//        })

//        activeCoachClubAffiliationLive.observe(viewLifecycleOwner, MyCoachAffiliationCoach())
//        viewModel.coach.observe(viewLifecycleOwner, MyCoachObserver())
//        viewModel.coachAffiliation.observe(viewLifecycleOwner, MyCoachAffiliationObserver())
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

        viewModel.coachAffiliation.observe(viewLifecycleOwner, {
            if (it != null) {
                viewModel.getUserEvents()
                viewModel.getCloseEvents()
            }
        })

//        activeCoachClubAffiliationLive.observe(viewLifecycleOwner, {
//            if (it != null) {
//                viewModel.getUserEvents()
//                viewModel.getCloseEvents()
//            }
//        })
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
            adapterCloseToClub.setData(it)
        })

//        viewModel.closeEventsList.observe(viewLifecycleOwner, MyCloseEventObserver())
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

//        viewModel.userEventsList.observe(viewLifecycleOwner, MyUserEventObserver())

        viewModel.userEventsList.observe(viewLifecycleOwner, {
            adapterUserEvents.setData(it)
        })
    }
    //endregion

    //region Navigation
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
    private fun signOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                TRACKING.logEvent(Tracking.LOGOUT, null)
                viewModel.signOut()
                toast(R.string.logout_success)
//                ManagerAuth.checkUserStatus()
                startActivity(intentFor<MainScreen>().newTask().clearTask())
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }
            .show()
    }

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

    //region Observer Classes
    class MyCoachObserver : Observer<Coach?> {
        @SuppressLint("SetTextI18n")
        override fun onChanged(it: Coach?) {
            if (it != null) {
                binding.closeToClubLayout.layout.visibility = View.VISIBLE
                binding.userEventLayout.layout.visibility = View.VISIBLE

                binding.homeWelcomeText.text = "Bonjour ${it.firstName} !"
            } else {
                binding.closeToClubLayout.layout.visibility = View.GONE
                binding.userEventLayout.layout.visibility = View.GONE

                binding.homeWelcomeText.text = "Bonjour Coach !"
            }
        }
    }

//    class MyCoachAffiliationObserver : Observer<ClubAffiliation?> {
//        override fun onChanged(it: ClubAffiliation?) {
//            if (it != null) {
//                viewModel.getUserEvents()
//                viewModel.getCloseEvents()
//            }
//        }
//    }

    class MyCloseEventObserver : Observer<MutableList<Event>> {
        override fun onChanged(it: MutableList<Event>) {
            adapterCloseToClub.setData(it)
        }
    }

    class MyUserEventObserver : Observer<MutableList<Event>> {
        override fun onChanged(it: MutableList<Event>) {
            adapterUserEvents.setData(it)
            Log.i("TAG onChanged", "onChanged list: $it.")
        }
    }
    //endregion
}