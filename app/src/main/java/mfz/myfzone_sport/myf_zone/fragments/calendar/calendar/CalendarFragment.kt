package mfz.myfzone_sport.myf_zone.fragments.calendar.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.model.event.calendar.EventSection
import mfz.myfzone_sport.myf_zone.model.event.calendar.ListRecyclerAdapter
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.getEventsByDate
import mfz.myfzone_sport.myf_zone.util.event.EventUtil.globalEventList
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil.eventToCalendar
import mfz.myfzone_sport.myf_zone.util.user.UserAccount
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation
import java.util.*
import kotlin.concurrent.schedule


class CalendarFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    companion object {
        private val TAG = CalendarFragment::class.java.simpleName
        var eventList = mutableListOf<EventSection>()
        private var adapter: ListRecyclerAdapter? = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_calendar, container, false)

        val swipeRefreshLayout = fragmentInflater.swipeRefreshLayout

        fragmentInflater.account_button.setOnClickListener {
            accountButton()
        }

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener(this)

        return fragmentInflater
    }

    override fun onResume() {
        super.onResume()
        if (swipeRefreshLayout != null)
            swipeRefreshLayout.isRefreshing = true

        CoroutineScope(Main).launch {
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.isRefreshing = true
//            val list = getEventsByDate()!!

            if (!globalEventList.isNullOrEmpty()) {
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.isRefreshing = true

                eventList = eventToCalendar(globalEventList!!)

                if (parentRecyclerView != null) {
                    val listAdapter = ListRecyclerAdapter(eventList)
                    listAdapter.notifyDataSetChanged()
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.isRefreshing = true
                    parentRecyclerView.adapter = listAdapter

                    parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    parentRecyclerView.setHasFixedSize(true)
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.isRefreshing = false
                }
            } else {
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.isRefreshing = true

                val list = getEventsByDate()
                eventList = eventToCalendar(list!!)

                if (parentRecyclerView != null && swipeRefreshLayout != null) {
                    swipeRefreshLayout.isRefreshing = true
                    parentRecyclerView.adapter =
                        ListRecyclerAdapter(
                            eventList
                        )

                    parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    parentRecyclerView.setHasFixedSize(true)
                    if (swipeRefreshLayout != null)
                        swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        if (swipeRefreshLayout != null)
            swipeRefreshLayout.isRefreshing = false

    }

    override fun onRefresh() {
        val delay: Long = 350
        CoroutineScope(Main).launch {
            if (swipeRefreshLayout != null)
                swipeRefreshLayout.isRefreshing = true

            val listAdapter = ListRecyclerAdapter(eventList)
            listAdapter.notifyDataSetChanged()
            val list = getEventsByDate()
            eventList = eventToCalendar(list!!)
            if (parentRecyclerView != null)
                parentRecyclerView.adapter = listAdapter
//            Log.d(TAG, "$eventList")
//            eventList.forEach { Log.d(TAG, "$it") }

            Timer().schedule(delay) {
                if (swipeRefreshLayout != null)
                    swipeRefreshLayout.isRefreshing = false
            }
        }
    }

    private fun accountButton() {
        val currentUser = auth.currentUser
        account_button.setOnClickListener {
            if (currentUser == null) {
                navigate(R.id.calendarToLogin)
            } else {
                UserAccount.getCurrentUser { user ->
                    if (currentUser.displayName == "") {
                        UserAccount.updateCurrentUser("", user.firstName, user.lastName)
                    }
                }

                UserAffiliation.userAffiliationStatus {
                    when (it) {
                        true -> {
                            navigate(R.id.calendarToProfile)
                        }
                        false -> {
                            (activity as AppCompatActivity).supportActionBar?.apply {
                                show()
                                setTitle(R.string.affiliation_text)
                                setHomeButtonEnabled(true)
                                setDisplayHomeAsUpEnabled(true)
                            }
                            navigate(R.id.calendarToAffiliationRequest)
                        }
                    }
                }
            }
        }
    }

    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }
}