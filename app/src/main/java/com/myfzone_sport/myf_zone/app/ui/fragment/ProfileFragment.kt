package com.myfzone_sport.myf_zone.app.ui.fragment

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.util.Log
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
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoach
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.activeCoachClub
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.activity.MainActivity
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryListEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.*
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentProfile2Binding
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import com.myfzone_sport.myf_zone.usecases.user.*

private const val ARG_PARAM1 = "coachId"

class ProfileFragment : Fragment() {

    //region Variables
    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
        private var coachId: String? = null
        private lateinit var binding: FragmentProfile2Binding
        private lateinit var viewModel: ProfileViewModel
        private lateinit var viewModelFactory: ProfileViewModelFactory
        private lateinit var adapterUserEvents: CategoryListEventAdapter
        private lateinit var adapterUserParticipations: UserEventAdapter
    }
    //endregion

    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach: fun frag")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate: fun frag")

        arguments?.let {
            coachId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_profile2, container, false
        )
        Log.d(TAG, "onCreateView: fun frag")

        setupViewModel()
        setupViews()
        setupObservers()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated: fun frag")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart: fun frag")
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume: fun frag")
    }

    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause: fun frag")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop: fun frag")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView: fun frag")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy: fun frag")
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: fun frag")
    }

    //endregion

    //region Setups
    private fun setupViewModel() {
        binding.apply {
            lifecycleOwner = this@ProfileFragment
            executePendingBindings()
        }

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getAllEventsUseCase = GetAllEventsUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)
        val getUserUseCase = GetUserUseCase(repository)
        val getUserClubUseCase = GetUserClubUseCase(repository)
        val getUserAffiliationUseCase = GetUserClubAffiliationUseCase(repository)
        val getUserEventListUseCase = GetUserEventListUseCase(repository)
        val getAllParticipantsFromEventUseCase = GetAllParticipantsFromEventUseCase(repository)

        viewModelFactory = ProfileViewModelFactory(
            getAllEventsUseCase,
            getImageReferenceUseCase,
            getUserUseCase,
            getUserClubUseCase,
            getUserAffiliationUseCase,
            getUserEventListUseCase,
            getAllParticipantsFromEventUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)
    }

    private fun setupViews() {
        setupProfile()
        setUpUserEventsRecycler()
        setUpParticipationEventsRecycler()

        viewModel.getImageReference(activeCoachClub!!.logo)

        binding.userEventLayout.showAll.setOnClickListener {
            val bundle = bundleOf("listType" to "userEvent")
            navigate(R.id.profileFragmentToCategoryListFragment, bundle)
        }

        binding.participationShowAll.setOnClickListener {
            val bundle = bundleOf("listType" to "userParticipation")
            navigate(R.id.profileFragmentToCategoryListFragment, bundle)
        }

        binding.exitProfile.setOnClickListener { requireActivity().onBackPressed() }

        binding.profileSettings.setOnClickListener { navigate(R.id.profileFragmentToSettingsFragment) }

        binding.profileNotifications.setOnClickListener { navigate(R.id.profileFragmentToNotificationsFragment) }
    }

    private fun setupObservers() {
        viewModel.errorMessage.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) showError()
        })

        viewModel.isLoading.observe(viewLifecycleOwner, { contentIsLoading ->
            if (contentIsLoading) loadingStart() else loadingStop()
        })

        viewModel.coach.observe(viewLifecycleOwner, {
            binding.coach = it
        })

        viewModel.coachClub.observe(viewLifecycleOwner, {
            binding.club = it
            Log.i("TAG","it! $it")
        })
    }

    private fun setupProfile() {
        binding.userEventLayout.layout.visibility = View.VISIBLE

        viewModel.userImagePath.observe(viewLifecycleOwner, {
            displayUserImage()
        })

        try {
            binding.coachInfo.text = activeCoach!!.getName()
        } catch (e: Exception) {
            Log.i("TAG", "Error: $e")
        }
    }

    private fun displayUserImage() {
        GlideApp.with(this).apply {
            load(viewModel.userImagePath.value)
                .centerCrop()
                .into(binding.profileClubImage)
        }
    }
    //endregion

    //region RecyclerView
    private fun setUpUserEventsRecycler() {
        adapterUserEvents = CategoryListEventAdapter()
        binding.userEventLayout.recyclerView.adapter = adapterUserEvents
        binding.userEventLayout.recyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.userEventLayout.recyclerView.setHasFixedSize(false)
        binding.userEventLayout.recyclerView.isNestedScrollingEnabled = false

        viewModel.userEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapterUserEvents.setData(it)
            }
            binding.userEventLayout.title.text = "Mes évènements (${it.size})"
        })
    }

    private fun setUpParticipationEventsRecycler() {
        adapterUserParticipations = UserEventAdapter()
        binding.participationRecyclerView.adapter = adapterUserParticipations
        binding.participationRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.userEventLayout.recyclerView.setHasFixedSize(false)
        binding.userEventLayout.recyclerView.isNestedScrollingEnabled = false

        viewModel.participationList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapterUserParticipations.setData(it)
            }
            binding.participationTitle.text = "Mes participations (${it.size})"
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
            binding.progressBar,
            if (!message.isNullOrEmpty()) message else viewModel.errorMessage.value.toString(),
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}