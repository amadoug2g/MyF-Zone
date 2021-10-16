package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventParticipantsViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventParticipantsViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentEventParticipantsListBinding
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase

private const val ARG_PARAM1 = "eventId"
private const val ARG_PARAM2 = "coachRole"

class EventParticipantsFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentEventParticipantsListBinding
        private lateinit var viewModel: EventParticipantsViewModel
        private lateinit var viewModelFactory: EventParticipantsViewModelFactory
        private var eventId: String? = null
        private var coachRole: String? = null
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
            coachRole = it.getString(ARG_PARAM2)
        }

        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getAllParticipantsFromEventUseCase = GetAllParticipantsFromEventUseCase(repository)

        viewModelFactory = EventParticipantsViewModelFactory(getAllParticipantsFromEventUseCase)

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EventParticipantsViewModel::class.java)

        viewModel.assignEventId(eventId!!)
//        viewModel.assignCoachRole(coachRole!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_participants_list,
            container,
            false
        )

        setupViews()
        setupObservers()

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

    private fun setupViews() {
//        setVisibleRecyclers()

        viewModel.eventParticipantsValid.observe(viewLifecycleOwner, {
            binding.validCoachTitle.text = "${it.size} validés"
        })

        viewModel.eventParticipantsPending.observe(viewLifecycleOwner, {
            binding.pendingCoachTitle.text = "${it.size} en attente"
        })

        viewModel.eventParticipantsRefused.observe(viewLifecycleOwner, {
            binding.refusedCoachTitle.text = "${it.size} refusés"
        })
    }
    //endregion

    //region View Methods
    private fun setVisibleRecyclers() {
        viewModel.coachRole.observe(viewLifecycleOwner, { role ->
            when(role) {
                "participant", "guest" -> {
                    binding.pendingCoachLayout.visibility = View.GONE
                    binding.refusedCoachLayout.visibility = View.GONE
                }
                else -> {
                    binding.validCoachLayout.visibility = View.VISIBLE
                    binding.pendingCoachLayout.visibility = View.VISIBLE
                    binding.refusedCoachLayout.visibility = View.VISIBLE
                }
            }
        })
    }

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