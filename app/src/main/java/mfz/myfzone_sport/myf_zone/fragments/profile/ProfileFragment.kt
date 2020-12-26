package mfz.myfzone_sport.myf_zone.fragments.profile

import android.content.DialogInterface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentProfileBinding
import mfz.myfzone_sport.myf_zone.fragments.profile.ProfileService.firebaseAuth
import mfz.myfzone_sport.myf_zone.fragments.profile.ProfileService.getImageReference
import mfz.myfzone_sport.myf_zone.glide.GlideApp
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.screens.MainScreen
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class ProfileFragment : Fragment() {
    companion object {
        private val TAG = ProfileFragment::class.java.simpleName
        private lateinit var viewModel: ProfileViewModel
        private lateinit var binding: FragmentProfileBinding
        private lateinit var viewModelFactory: ProfileViewModelFactory
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_profile,
            container,
            false
        )

        viewModelFactory = ProfileViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        binding.apply {
            lifecycleOwner = this@ProfileFragment
            executePendingBindings()
        }

        lifecycleScope.launch {
            loadUser()
            loadUserClub()
            loadUserEvents()
        }

        binding.profileSettings.setOnClickListener { signOut() }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        binding.profileShimmerLayout.startShimmer()
    }

    override fun onStop() {
        super.onStop()
        binding.profileShimmerLayout.stopShimmer()
    }
    //endregion

    //region User Info
    private suspend fun loadUser() {
        viewModel.getCurrentUser().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    val user = state.data
                    binding.user = user
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred [in loadUser]: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun loadUserClub() {
        viewModel.getCurrentUserClub().collect { state ->
            when (state) {
                is State.Loading -> {
//                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    val club = state.data
                    binding.club = club
                    Log.d(TAG, "club: ${club.clubLogo}")
                    binding.profileSubCategory.text =
                        if (club.subCategoryName.isNullOrEmpty()) "" else " - ${club.subCategoryName}"

                    GlideApp.with(this).apply {
                        load(getImageReference(club.clubLogo))
                            .centerCrop()
                            .into(binding.profileClubImage)
                    }
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred [in loadUserClub]: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun loadUserEvents() {
        viewModel.getCurrentUserEvents().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    val eventList = state.data
                    eventList.forEach { event -> loadEventOwner(event.id, eventList) }
                    hideProgressBar()
                    setupEventRecyclerView(eventList)
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred [in loadUserEvents]: ${state.message}"
                    showToast(message)
                }
            }
        }
    }

    private suspend fun loadEventOwner(eventId: String, eventList: MutableList<Event>) {
        viewModel.getEventOwner(eventId).collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
//                    hideProgressBar()
                    val eventOwner = state.data
                    eventList.forEach { event -> event.owner = eventOwner }
                    Log.i(TAG, "event list is:$eventList")
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "An error occurred [in loadEventOwner]: ${state.message}"
                    showToast(message)
                }
            }
        }
    }
    //endregion

    //region RecyclerView
    private fun setupEventRecyclerView(eventList: MutableList<Event>) {
        val adapter = ProfileEventAdapter()
        adapter.submitList(eventList)
        binding.profileEventRecycler.apply {
            this.adapter = adapter
            layoutManager = LinearLayoutManager(activity)
            setHasFixedSize(true)
        }
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.profileShimmerLayout.startShimmer()
        binding.profileShimmerLayout.visibility = View.VISIBLE
        binding.profileLayout.visibility = View.GONE
    }

    private fun hideProgressBar() {
        binding.profileShimmerLayout.stopShimmer()
        binding.profileShimmerLayout.visibility = View.GONE
        binding.profileLayout.visibility = View.VISIBLE
    }
    //endregion

    //region View Methods
    private fun signOut() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.logout)
            .setMessage(R.string.logout_message)
            .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                firebaseAuth.signOut()
                toast(R.string.logout_success)
                startActivity(intentFor<MainScreen>().newTask().clearTask())
            }
            .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
            }
            .show()
    }

    private fun showToast(string: String) {
        toast(string)
    }
    //endregion
}