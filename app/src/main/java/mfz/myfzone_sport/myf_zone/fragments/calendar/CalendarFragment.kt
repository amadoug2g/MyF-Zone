package mfz.myfzone_sport.myf_zone.fragments.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
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
import java.util.*
import kotlin.concurrent.schedule


class CalendarFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    companion object {
        private val TAG = CalendarFragment::class.java.simpleName
        var eventList = mutableListOf<EventSection>()
        private var adapter: ListRecyclerAdapter? = null
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_calendar, container, false)

        val swipeRefreshLayout = fragmentInflater.swipeRefreshLayout

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener(this)

        return fragmentInflater
    }

    override fun onResume() {
        super.onResume()
        swipeRefreshLayout.isRefreshing = true

        CoroutineScope(Main).launch {
            swipeRefreshLayout.isRefreshing = true
//            val list = getEventsByDate()!!

            if (!globalEventList.isNullOrEmpty()) {
                swipeRefreshLayout.isRefreshing = true

                eventList = eventToCalendar(globalEventList!!)

                if (parentRecyclerView != null) {
                    val listAdapter = ListRecyclerAdapter(eventList)
                    listAdapter.notifyDataSetChanged()
                    swipeRefreshLayout.isRefreshing = true
                    parentRecyclerView.adapter = listAdapter

                    parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    parentRecyclerView.setHasFixedSize(true)
                    swipeRefreshLayout.isRefreshing = false
                }
            } else {
                swipeRefreshLayout.isRefreshing = true

                val list = getEventsByDate()
                eventList = eventToCalendar(list!!)

                if (parentRecyclerView != null) {
                    swipeRefreshLayout.isRefreshing = true
                    parentRecyclerView.adapter =
                        ListRecyclerAdapter(
                            eventList
                        )

                    parentRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                    parentRecyclerView.setHasFixedSize(true)
                    swipeRefreshLayout.isRefreshing = false
                }
            }
        }

        swipeRefreshLayout.isRefreshing = false

    }

    override fun onRefresh() {
        val delay: Long = 350
        CoroutineScope(Main).launch {
            swipeRefreshLayout.isRefreshing = true

            val listAdapter = ListRecyclerAdapter(eventList)
            listAdapter.notifyDataSetChanged()
            val list = getEventsByDate()
            eventList = eventToCalendar(list!!)
            parentRecyclerView.adapter = listAdapter
//            Log.d(TAG, "$eventList")
//            eventList.forEach { Log.d(TAG, "$it") }

            Timer().schedule(delay) {
                swipeRefreshLayout.isRefreshing = false
            }
        }
    }
}