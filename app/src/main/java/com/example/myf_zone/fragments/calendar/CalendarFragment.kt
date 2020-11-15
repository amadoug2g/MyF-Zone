package com.example.myf_zone.fragments.calendar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.myf_zone.R
import com.example.myf_zone.model.event.calendar.EventSection
import com.example.myf_zone.model.event.calendar.ListRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_calendar.*
import kotlinx.android.synthetic.main.fragment_calendar.view.*
import java.util.*
import kotlin.concurrent.schedule


class CalendarFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private val TAG = CalendarFragment::class.java.simpleName

    var eventList = mutableListOf<EventSection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        initData()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_calendar, container, false)

        val recyclerView = fragmentInflater.parentRecyclerView
        val swipeRefreshLayout = fragmentInflater.swipeRefreshLayout

        swipeRefreshLayout.setColorSchemeResources(R.color.colorAccent)
        swipeRefreshLayout.setOnRefreshListener(this)

        val decor = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        recyclerView.addItemDecoration(decor)

        recyclerView.adapter =
            ListRecyclerAdapter(
                eventList
            )
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)

        return fragmentInflater
    }

    override fun onRefresh() {
        val delay: Long = 350

        Timer().schedule(delay) {
            swipeRefreshLayout.isRefreshing = false
        }
    }

    private fun initData() {
        val sectionOneName = "21 Novembre"
        val sectionOneList = mutableListOf(
            "Organisateur - ...",
            "Organisateur - ...",
            "Organisateur - ...",
            "Organisateur - ..."
        )

        val sectionTwoName = "29 Novembre"
        val sectionTwoList = mutableListOf(
            "Organisateur - ...",
            "Organisateur - ...",
            "Organisateur - ...",
            "Organisateur - ...",
            "Organisateur - ...",
            "Organisateur - ..."
        )

        val sectionThreeName = "6 Décembre"
        val sectionThreeList =
            mutableListOf("Organisateur - ...", "Organisateur - ...", "Organisateur - ...")

        val sectionFourName = "12 Décembre"
        val sectionFourList =
            mutableListOf("Organisateur - ...", "Organisateur - ...", "Organisateur - ...")

        val sectionFiveName = "9 Janvier 2021"
        val sectionFiveList =
            mutableListOf("Organisateur - ...", "Organisateur - ...", "Organisateur - ...")

        val sectionSixName = "17 Janvier 2020"
        val sectionSixList =
            mutableListOf("Organisateur - ...", "Organisateur - ...", "Organisateur - ...")

        eventList.add(
            EventSection(
                sectionOneName,
                sectionOneList
            )
        )
        eventList.add(
            EventSection(
                sectionTwoName,
                sectionTwoList
            )
        )
        eventList.add(
            EventSection(
                sectionThreeName,
                sectionThreeList
            )
        )
        eventList.add(
            EventSection(
                sectionFourName,
                sectionFourList
            )
        )
        eventList.add(
            EventSection(
                sectionFiveName,
                sectionFiveList
            )
        )
        eventList.add(
            EventSection(
                sectionSixName,
                sectionSixList
            )
        )

//        Log.d(TAG, "List is: ${eventList.toString()}")
    }

}