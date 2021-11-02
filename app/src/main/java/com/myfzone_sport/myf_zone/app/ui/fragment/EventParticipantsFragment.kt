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
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventParticipantsViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventParticipantsViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.CardEventParticipantValidBinding
import com.myfzone_sport.myf_zone.databinding.FragmentEventParticipantsListBinding
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase

private const val ARG_PARAM1 = "eventId"

class EventParticipantsFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentEventParticipantsListBinding
        private lateinit var viewModel: EventParticipantsViewModel
        private lateinit var viewModelFactory: EventParticipantsViewModelFactory
        private var eventId: String? = null
        private var validAdapter: FirestoreRecyclerAdapter<EventParticipant, ValidParticipantHolder>? =
            null
    }
    //endregion

    //region Firestore RecyclerView
    class ValidParticipantHolder(val binding: CardEventParticipantValidBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant
                binding.eventParticipantActionButton.visibility = View.GONE

                try {
                    GlideApp.with(itemView).apply {
                        load(viewModel.getImageReference(participant.clubLogo))
                            .placeholder(R.drawable.ic_account)
                            .centerCrop()
                            .into(binding.eventParticipantImage)
                    }
                } catch (e: Exception) {
                    Log.e("List ParticipantHolder", "Image could not load: $e")
                }
            }
        }

        companion object {
            fun from(parent: ViewGroup): ValidParticipantHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CardEventParticipantValidBinding.inflate(layoutInflater, parent, false)
                return ValidParticipantHolder(binding)
            }
        }
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }

        setupViewModel()

        viewModel.assignEventId(eventId!!)

        val recyclerValidOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(viewModel.getQuery(eventId!!).whereEqualTo("status", "validate"), EventParticipant::class.java)
            .setLifecycleOwner(this)
            .build()

        validAdapter = object :
            FirestoreRecyclerAdapter<EventParticipant, ValidParticipantHolder>(recyclerValidOptions) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): ValidParticipantHolder {
                return ValidParticipantHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: ValidParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
                holder.bind(model)
            }

            override fun onDataChanged() {
                val params = binding.validCoachRecyclerView.layoutParams
                params.height = 120 * itemCount
                binding.validCoachRecyclerView.layoutParams = params
            }
        }
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
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getEventFromIdUseCase = GetEventFromIdUseCase(repository)
        val getAllParticipantsFromEventUseCase = GetAllParticipantsFromEventUseCase(repository)
        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

        viewModelFactory = EventParticipantsViewModelFactory(
            getEventFromIdUseCase,
            getAllParticipantsFromEventUseCase,
            getImageReferenceUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EventParticipantsViewModel::class.java)
    }

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
        binding.backArrow.background = null

        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupValidRecyclerParameters()

        viewModel.event.observe(viewLifecycleOwner, { event ->
            binding.event = event
        })

        viewModel.eventParticipantsValid.observe(viewLifecycleOwner, {
            binding.validCount = it.size
        })
    }
    //endregion

    //region RecyclerViews
    private fun setupValidRecyclerParameters() {
        binding.validCoachRecyclerView.setHasFixedSize(false)
        binding.validCoachRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.validCoachRecyclerView.adapter = validAdapter
        binding.validCoachRecyclerView.isNestedScrollingEnabled = false
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