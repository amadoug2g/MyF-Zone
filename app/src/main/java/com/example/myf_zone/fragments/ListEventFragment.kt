package com.example.myf_zone.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.myf_zone.ListRecyclerAdapter
import com.example.myf_zone.R
import com.example.myf_zone.model.event.EventSection
import kotlinx.android.synthetic.main.fragment_list_event.*


class ListEventFragment : Fragment() {
    private val TAG = ListEventFragment::class.java.simpleName

    var eventList = mutableListOf<EventSection>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initData()

        eventRecyclerView?.adapter = ListRecyclerAdapter(eventList)
//        val decor = DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
//        eventRecyclerView.addItemDecoration(decor)

//        (activity as AppCompatActivity).supportActionBar?.apply {
//            show()
//            setTitle(R.string.calendar)
//        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_list_event, container, false)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                activity?.onBackPressed()
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun initData() {
        val sectionOneName = "Action"
        val sectionOneList = mutableListOf("Captain 'Murica", "Iron Mandem")

        val sectionTwoName = "Adventure"
        val sectionTwoList = mutableListOf("Fellas of the Caribbean", "King King", "Life of Lie")

        val sectionThreeName = "Epic"
        val sectionThreeList = mutableListOf("Titanic", "Malcom X", "Xena")

        val sectionFourName = "War"
        val sectionFourList =
            mutableListOf("Saving Private Ryan", "1997", "Heroes of the Storm", "The Hurt Locker")

        eventList.add(EventSection(sectionOneName, sectionOneList))
        eventList.add(EventSection(sectionTwoName, sectionTwoList))
        eventList.add(EventSection(sectionThreeName, sectionThreeList))
        eventList.add(EventSection(sectionFourName, sectionFourList))
    }
}