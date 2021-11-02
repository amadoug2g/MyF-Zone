package com.myfzone_sport.myf_zone.app.ui.fragment

import android.content.DialogInterface
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventParticipantsOwnerViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.EventParticipantsOwnerViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.CardEventParticipantPendingBinding
import com.myfzone_sport.myf_zone.databinding.CardEventParticipantRefusedBinding
import com.myfzone_sport.myf_zone.databinding.CardEventParticipantValidBinding
import com.myfzone_sport.myf_zone.databinding.FragmentEventParticipantsOwnerBinding
import com.myfzone_sport.myf_zone.domain.event.EventParticipant
import com.myfzone_sport.myf_zone.glide.GlideApp
import com.myfzone_sport.myf_zone.usecases.detailevent.AcceptParticipantUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetAllParticipantsFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.GetEventFromIdUseCase
import com.myfzone_sport.myf_zone.usecases.detailevent.RefuseParticipantUseCase
import com.myfzone_sport.myf_zone.usecases.user.GetImageReferenceUseCase
import com.myfzone_sport.myf_zone.util.Tracking

private const val ARG_PARAM1 = "eventId"

class EventParticipantsOwnerFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentEventParticipantsOwnerBinding
        private lateinit var viewModel: EventParticipantsOwnerViewModel
        private lateinit var viewModelFactory: EventParticipantsOwnerViewModelFactory
        private var eventId: String? = null
        private var validAdapter: FirestoreRecyclerAdapter<EventParticipant, ValidParticipantHolder>? =
            null
        private var pendingAdapter: FirestoreRecyclerAdapter<EventParticipant, PendingParticipantHolder>? =
            null
        private var refusedAdapter: FirestoreRecyclerAdapter<EventParticipant, RefusedParticipantHolder>? =
            null
    }
    //endregion

    //region Firestore RecyclerView
    class ValidParticipantHolder(val binding: CardEventParticipantValidBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant

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

    class PendingParticipantHolder(val binding: CardEventParticipantPendingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant

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

                binding.eventParticipantActionButton01.setOnClickListener { }
            }
        }

        companion object {
            fun from(parent: ViewGroup): PendingParticipantHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CardEventParticipantPendingBinding.inflate(layoutInflater, parent, false)
                return PendingParticipantHolder(binding)
            }
        }
    }

    class RefusedParticipantHolder(val binding: CardEventParticipantRefusedBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(participant: EventParticipant) {
            with(binding) {
                binding.participant = participant

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
            fun from(parent: ViewGroup): RefusedParticipantHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding =
                    CardEventParticipantRefusedBinding.inflate(layoutInflater, parent, false)
                return RefusedParticipantHolder(binding)
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

        val recyclerPendingOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(viewModel.getQuery(eventId!!).whereEqualTo("status", "pending"), EventParticipant::class.java)
            .setLifecycleOwner(this)
            .build()

        val recyclerRefusedOptions = FirestoreRecyclerOptions.Builder<EventParticipant>()
            .setQuery(viewModel.getQuery(eventId!!).whereEqualTo("status", "refused"), EventParticipant::class.java)
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

                holder.binding.eventParticipantActionButton.setOnClickListener { ownerRefuse(model) }
            }

            override fun onDataChanged() {
                val params = binding.validCoachRecyclerView.layoutParams
                params.height = 120 * itemCount
                binding.validCoachRecyclerView.layoutParams = params
            }
        }

        pendingAdapter = object :
            FirestoreRecyclerAdapter<EventParticipant, PendingParticipantHolder>(recyclerPendingOptions) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): PendingParticipantHolder {
                return PendingParticipantHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: PendingParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
                holder.bind(model)

                holder.binding.eventParticipantActionButton01.setOnClickListener { ownerAccept(model) }
                holder.binding.eventParticipantActionButton02.setOnClickListener { ownerRefuse(model) }
            }

            override fun onDataChanged() {
                val params = binding.pendingCoachRecyclerView.layoutParams
                params.height = 120 * itemCount
                binding.pendingCoachRecyclerView.layoutParams = params
            }
        }

        refusedAdapter = object :
            FirestoreRecyclerAdapter<EventParticipant, RefusedParticipantHolder>(recyclerRefusedOptions) {
            override fun onCreateViewHolder(
                parent: ViewGroup,
                viewType: Int
            ): RefusedParticipantHolder {
                return RefusedParticipantHolder.from(parent)
            }

            override fun onBindViewHolder(
                holder: RefusedParticipantHolder,
                position: Int,
                model: EventParticipant
            ) {
                holder.bind(model)

                holder.binding.eventParticipantActionButton.setOnClickListener { ownerAccept(model) }
            }

            override fun onDataChanged() {
                val params = binding.refusedCoachRecyclerView.layoutParams
                params.height = 120 * itemCount
                binding.refusedCoachRecyclerView.layoutParams = params
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_event_participants_owner,
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
        val acceptParticipantUseCase = AcceptParticipantUseCase(repository)
        val refuseParticipantUseCase = RefuseParticipantUseCase(repository)

        viewModelFactory = EventParticipantsOwnerViewModelFactory(
            getEventFromIdUseCase,
            getAllParticipantsFromEventUseCase,
            getImageReferenceUseCase,
            acceptParticipantUseCase,
            refuseParticipantUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(EventParticipantsOwnerViewModel::class.java)
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
        binding.backArrow.background = null

        binding.backArrow.setOnClickListener {
            requireActivity().onBackPressed()
        }

        setupValidRecyclerParameters()
        setupPendingRecyclerParameters()
        setupRefusedRecyclerParameters()


        viewModel.event.observe(viewLifecycleOwner, { event ->
            binding.event = event
        })

        viewModel.eventParticipantsValid.observe(viewLifecycleOwner, {
            binding.validCount = it.size
        })

        viewModel.eventParticipantsPending.observe(viewLifecycleOwner, {
            binding.pendingCount = it.size
        })

        viewModel.eventParticipantsRefused.observe(viewLifecycleOwner, {
            binding.refusedCount = it.size
        })
    }
    //endregion

    //region Event Actions
    fun ownerAccept(participant: EventParticipant) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.participation_title))
            .setMessage(getString(R.string.accept_coach_text))
            .setNeutralButton(getString(R.string.participation_neutral)) { _: DialogInterface, _: Int -> }
            .setPositiveButton(getString(R.string.participation_accept)) { _: DialogInterface, _: Int ->
                TRACKING.logEvent(Tracking.EVENT_DETAILS_OWNER_ACCEPT_PARTICIPATION, null)
                viewModel.acceptParticipant(eventId!!, participant)
            }
            .show()
    }

    fun ownerRefuse(participant: EventParticipant) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.participation_title))
            .setMessage(getString(R.string.refuse_coach_text))
            .setNeutralButton(getString(R.string.participation_neutral)) { _: DialogInterface, _: Int -> }
            .setNegativeButton(getString(R.string.participation_refuse)) { _: DialogInterface, _: Int ->
                TRACKING.logEvent(Tracking.EVENT_DETAILS_OWNER_REFUSE_PARTICIPATION, null)
                viewModel.refuseParticipant(eventId!!, participant)
            }
            .show()
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

    private fun setupPendingRecyclerParameters() {
        binding.pendingCoachRecyclerView.setHasFixedSize(false)
        binding.pendingCoachRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.pendingCoachRecyclerView.adapter = pendingAdapter
        binding.pendingCoachRecyclerView.isNestedScrollingEnabled = false
    }

    private fun setupRefusedRecyclerParameters() {
        binding.refusedCoachRecyclerView.setHasFixedSize(false)
        binding.refusedCoachRecyclerView.layoutManager =
            LinearLayoutManager(requireContext())
        binding.refusedCoachRecyclerView.adapter = refusedAdapter
        binding.refusedCoachRecyclerView.isNestedScrollingEnabled = false
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