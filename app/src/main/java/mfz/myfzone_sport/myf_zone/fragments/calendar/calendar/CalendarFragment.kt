package mfz.myfzone_sport.myf_zone.fragments.calendar.calendar

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
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
import org.jetbrains.anko.support.v4.toast
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

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater = inflater.inflate(R.layout.fragment_calendar, container, false)

        val swipeRefreshLayout = fragmentInflater.swipeRefreshLayout

        fragmentInflater.account_button.background = null
        fragmentInflater.account_button.setOnClickListener {
            accountButton()
        }

//        isOnline(requireContext())
//        checkCo()

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
                        navigate(R.id.calendarToAffiliationRequest)
                    }
                }
            }
        }
    }

    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }

    private fun checkCo() {
        if (getConnectionType(requireContext()))
            toast("Connected")
        else
            toast("No Connection")
    }

    private fun getConnectionType(context: Context): Boolean {
        val result = false // Returns connection type. 0: none; 1: mobile data; 2: wifi
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            cm?.run {
                val all = cm.allNetworks
                val all2 = cm.isDefaultNetworkActive
//                val all3 = cm.
                Log.i("Internet", "list? $all")
                Log.i("Internet", "list2? $all2")
                all.forEach { Log.i("Internet", it.socketFactory.toString()) }
                cm.getNetworkCapabilities(cm.activeNetwork)?.run {
                    return when {
                        hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            true
                        }
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            true
                        }
                        hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            }
        } else {
            cm?.run {
                val all = cm.allNetworks
                val all2 = cm.isDefaultNetworkActive
                Log.i("Internet", "list? ${all.size}")

                cm.activeNetworkInfo?.run {
                    return when (type) {
                        ConnectivityManager.TYPE_WIFI -> {
                            true
                        }
                        ConnectivityManager.TYPE_MOBILE -> {
                            true
                        }
                        ConnectivityManager.TYPE_VPN -> {
                            true
                        }
                        else -> {
                            false
                        }
                    }
                }
            }
        }
        return result
    }

    private fun isInternetAvailable(context: Context): Boolean {
        var result = false
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val networkCapabilities = connectivityManager.activeNetwork ?: return false
            val actNw =
                connectivityManager.getNetworkCapabilities(networkCapabilities) ?: return false
            result = when {
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
                actNw.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
                else -> false
            }
        } else {
            connectivityManager.run {
                connectivityManager.activeNetworkInfo?.run {
                    result = when (type) {
                        ConnectivityManager.TYPE_WIFI -> true
                        ConnectivityManager.TYPE_MOBILE -> true
                        ConnectivityManager.TYPE_ETHERNET -> true
                        else -> false
                    }

                }
            }
        }

        return result
    }
}