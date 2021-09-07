package com.myfzone_sport.myf_zone.app.ui.fragment

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.FirebaseService.TRACKING
import com.myfzone_sport.myf_zone.app.ui.viewmodel.FragmentViewModel
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.CloseToClubEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import com.myfzone_sport.myf_zone.fragments.settings.SettingsFragment
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.util.Constants
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.home_close_to_club_layout.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

class HomeFragment : Fragment() {

    private val viewModel by activityViewModels<FragmentViewModel>()

    companion object {
        private lateinit var binding: FragmentHomeBinding
        private lateinit var adapterCloseToClub: CloseToClubEventAdapter
        private lateinit var adapterCategory: CategoryEventAdapter
        private lateinit var adapterUserEvents: UserEventAdapter
        lateinit var viewModel: FragmentViewModel
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

//        viewModel.getCloseEvents()
        setupObservers()
        setUpRecyclerViews()
        setupViews()


        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViews() {
//        binding.homeCreateEventBtn.buttonEffect()

        binding.userEventLayout.showAll.setOnClickListener {
            val bundle = bundleOf("listType" to "userEvent")
            navigate(R.id.homeFragmentToCategoryListFragment, bundle)
        }

        binding.categoryLayout.showAll.setOnClickListener {
            val bundle = bundleOf("listType" to "categoryEvent")
            navigate(R.id.homeFragmentToCategoryListFragment, bundle)
        }

        binding.homeCreateEventBtn.setOnClickListener {
            toast("clicked new event")
            signOut()
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
//            adapterCloseToClub.submitList(it)
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
//        binding.userEventLayout.recyclerView.setHasFixedSize(true)
//        binding.userEventLayout.recyclerView.isNestedScrollingEnabled = true

        viewModel.closeEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) adapterUserEvents.setData(it)
        })
    }
    //endregion

    //Navigation
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

    @SuppressLint("ClickableViewAccessibility")
    fun View.buttonEffect() {
        this.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
//                    v.background.setColorFilter(-0x1f0b8adf, PorterDuff.Mode.SRC_ATOP)
                    v.background.colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(-0x1f0b8adf,
                        BlendModeCompat.SRC_ATOP)
                    v.invalidate()
                }
                MotionEvent.ACTION_UP -> {
                    v.background.clearColorFilter()
                    v.invalidate()
                }
            }
            false
        }
    }
    //endregion
}