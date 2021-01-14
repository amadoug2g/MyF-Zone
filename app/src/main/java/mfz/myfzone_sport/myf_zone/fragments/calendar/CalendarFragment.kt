package mfz.myfzone_sport.myf_zone.fragments.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.coroutines.flow.collect
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentCalendarBinding
import mfz.myfzone_sport.myf_zone.model.State
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.model.event.calendar.EventSection
import mfz.myfzone_sport.myf_zone.model.event.calendar.ListRecyclerAdapter
import org.jetbrains.anko.support.v4.toast


class CalendarFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    companion object {
        private val TAG = this::class.java.simpleName
        var eventList = mutableListOf<EventSection>()

        private lateinit var binding: FragmentCalendarBinding
        private lateinit var viewModel: CalendarViewModel
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(CalendarViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_calendar,
            container,
            false
        )

        binding.apply {
            lifecycleOwner = this@CalendarFragment
            executePendingBindings()
        }

        setupRecyclerParameters()
        setupSwipeRefresh()

        try {
            viewModel.addEventListener(this::refreshRecycler)
        } catch (e: Exception) {
            Log.e(TAG, "Error in eventListener: $e")
            toast("Error in eventListener: $e")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!viewModel.isListInitialized.value!!) {
            searchNewEvents()
            viewModel.listInit()
        } else {
            refreshRecycler(viewModel.eventList.value!!)
        }
    }

    override fun onRefresh() {
        searchNewEvents()
    }
    //endregion

    //region Event Update
    private fun searchNewEvents() {
        try {
            assignEventList()
        } catch (e: Exception) {
            toast("Error in [onResume]: $e")
        }
    }

    private fun assignEventList() {
        refreshRecycler(viewModel.eventList.value!!)
        lifecycleScope.launchWhenResumed {
            viewModel.getEvents().collect { state ->
                when (state) {
                    is State.Loading -> {
                        startSwipeRefresh()
                    }
                    is State.Success -> {
                        val list = state.data
                        viewModel.assignEventList(list)
                        viewModel.isListDifferent.observe(viewLifecycleOwner) { isListDifferent ->
                            if (isListDifferent) {
                                refreshRecycler(list)
                            }
                        }

                        stopSwipeRefresh()
                    }
                    is State.Failed -> {
                        stopSwipeRefresh()
                        val message = "Error while fetching events: ${state.message}"
                        message.showToast()
                    }
                }
            }
        }
    }
    //endregion

    //region View Methods

    private fun String.showToast() {
        toast(this)
    }
    //endregion

    //region RecyclerView
    private fun setupRecyclerParameters() {
        binding.parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.parentRecyclerView.setHasFixedSize(true)
    }

    private fun refreshRecycler(list: MutableList<Event>) {
        eventList = viewModel.eventToCalendar(list)
        val listAdapter = ListRecyclerAdapter(eventList)

        binding.parentRecyclerView.adapter = listAdapter
    }
    //endregion

    //region Loading
    private fun setupSwipeRefresh() {
        binding.swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        binding.swipeRefreshLayout.setOnRefreshListener(this)
    }

    private fun startSwipeRefresh() {
        binding.swipeRefreshLayout.isRefreshing = true
    }

    private fun stopSwipeRefresh() {
        binding.swipeRefreshLayout.isRefreshing = false
    }
    //endregion
}