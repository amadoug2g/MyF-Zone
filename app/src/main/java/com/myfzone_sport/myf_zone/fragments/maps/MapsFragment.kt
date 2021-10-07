package com.myfzone_sport.myf_zone.fragments.maps

import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.maps.android.clustering.ClusterManager
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentMapsBinding
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.activeCoach
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isAffiliated
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth.isConnected
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.domain.event.EventData
import com.myfzone_sport.myf_zone.domain.maps.ClusterRenderer
import com.myfzone_sport.myf_zone.domain.maps.MyClusterItem
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.card_event_item.*
import kotlinx.coroutines.flow.collect
//import org.jetbrains.anko.support.v4.toast
import java.util.*


@Suppress("SameParameterValue")
class MapsFragment : Fragment(),
    GoogleMap.OnMapClickListener {

    private lateinit var customClusterRenderer: ClusterRenderer

    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private var shortAnimationDuration: Int = 300

        private lateinit var binding: FragmentMapsBinding
        private lateinit var viewModel: MapsViewModel

        //        private lateinit var clusterManager: ClusterManager<MapClusterItem>
        private lateinit var customClusterManager: ClusterManager<MyClusterItem>
    }

    //region Map Callback
    private val callback = OnMapReadyCallback { googleMap ->
        /**
         * Manipulates the map once available.
         * This callback is triggered when the map is ready to be used.
         * This is where we can add markers or lines, add listeners or move the camera.
         * In this case, we just add a marker near Sydney, Australia.
         * If Google Play services is not installed on the device, the user will be prompted to
         * install it inside the SupportMapFragment. This method will only be triggered once the
         * user has installed Google Play services and returned to the app.
         */

        viewModel.assignMap(googleMap)
        if (!viewModel.isMapInitialized.value!!) {
            viewModel.map.observe(viewLifecycleOwner) { map ->
                viewModel.initializeMap(map)
            }

            refreshEventList()

            viewModel.mapInit()
        }

        googleMap.setOnMarkerClickListener { marker ->
            viewModel.assignMarker(marker)
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 14f))
            marker.markerClick()

            false
        }

        googleMap.setOnMapClickListener(this)

        viewModel.assignMap(googleMap)
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel = ViewModelProvider(this).get(MapsViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_maps,
            container,
            false
        )

        viewModel.assignContext(requireContext())
        binding.viewModel = viewModel
        binding.mapFilter.viewModel = viewModel

        binding.filter.setOnClickListener {
            calendarClick()
//            filterList()
        }

        binding.mapFilterLayout.setOnClickListener {
//            binding.mapFilterLayout.visibility = View.GONE
        }

        binding.mapFilter.filterDate.setOnClickListener {
            calendarClick()
        }

        binding.mapFilter.eventFilterButton.setOnClickListener {
            binding.mapFilterLayout.visibility = View.GONE

            viewModel.assignIsTourney(binding.mapFilter.checkBoxTournament.isChecked)
            viewModel.assignIsPlateau(binding.mapFilter.checkBoxPlateau.isChecked)
            viewModel.assignIsFriendly(binding.mapFilter.checkBoxFriendly.isChecked)

            try {
                refreshEventList()
            } catch (e: Exception) {
//                toast("$e")
            }
        }

        binding.cardEventDetail.cardViewDetail.setOnClickListener {
            when (binding.cardEventDetail.cardViewTag.text) {
                null -> {

                }
                else -> {
                    TRACKING.logEvent(Tracking.MAP_OPEN_EVENT, null)
                    val bundle = bundleOf("eventId" to cardView_tag.text)

                    try {
                        if (isConnected) {
                            if (isAffiliated) {
                                if (viewModel.owner.value?.coachId == activeCoach?.id) {
                                    navigate(R.id.mapsToEventDetailsOwner, bundle)
                                } else {
                                    navigate(R.id.mapsToEventDetailsParticipant, bundle)
                                }
                            } else {
                                navigate(R.id.mapsToEventDetailsGuest, bundle)
                            }
                        } else {
                            navigate(R.id.mapsToEventDetailsGuest, bundle)
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "ERROR: ${e.localizedMessage}")
                    }

//                    navigate(R.id.mapsToEventDetails, bundle)
                }
            }
        }

        try {
            viewModel.addEventListener {
                refreshEventList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error in eventListener: $e")
//            toast("Error in eventListener: $e")
        }

        ManagerAuth.checkUserStatus()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onStop() {
        super.onStop()
        viewModel.marker.observe(viewLifecycleOwner) { marker ->
            marker.hideInfoWindow()
        }
    }

    override fun onMapClick(p0: LatLng?) {
        binding.cardEventDetail
        if (binding.cardEventDetail.cardViewDetail.isVisible) {
            crossFadeEnd()
            binding.cardEventDetail.cardViewDetail.visibility = View.INVISIBLE
        } else {
            binding.cardEventDetail.cardViewDetail.visibility = View.INVISIBLE
        }
    }
    //endregion

    //region Marker Placement
    private fun refreshEventList() {
        lifecycleScope.launchWhenResumed {
            viewModel.getEvents().collect { state ->
                when (state) {
                    is State.Loading -> {
                        loadingMsgStart()
                    }
                    is State.Success -> {
                        loadingMsgEnd()
                        val list = state.data
                        viewModel.assignEventList(list)
//                        checkEventList(list)
                        binding.viewModel = viewModel
//                        viewModel.placeEvents(viewModel.map.value!!, requireContext(), list)
//                        viewModel.placeEventsCluster(viewModel.map.value!!, requireContext(), list)
                        setUpClusters(viewModel.map.value!!)
                        placeUserClub()
//                        Log.i(TAG, "Club ID: 01")
                    }
                    is State.Failed -> {
                        loadingMsgEnd()
                        val message = "Erreur de chargement: ${state.message}"
                        showToast(message)
                    }
                }
            }
        }
    }

    private fun placeUserClub() {
        if (isConnected) {
            if (isAffiliated) {
                viewModel.placeUserClub(
                    ManagerAuth.activeCoachClub!!,
                    viewModel.map.value!!,
                    requireContext()
                )
            }
        }
    }

    private fun Marker.markerClick() {
        //this.hideInfoWindow()
        when (this.tag) {
            null -> {
//                Log.i(TAG, "tag = $tag")
                binding.cardEventDetail.cardViewTag.text = null
                binding.cardEventDetail.cardViewDetail.visibility = View.INVISIBLE
                crossFadeEnd()
            }
            else -> {
//                Log.i(TAG, "tag = $tag")
                TRACKING.logEvent(Tracking.MAP_OPEN_SMALL_EVENT_DETAILS, null)
                binding.cardEventDetail.cardViewTag.text = this.tag as String
                viewModel.assignEventId(this)
                viewModel.eventId.observe(viewLifecycleOwner) { eventId ->
                    viewModel.assignEvent(eventId)
                    viewModel.getOwnerFromEvent()
//                    Log.i(TAG, "bEvent izs $eventId")
                    viewModel.event.observe(viewLifecycleOwner) { event ->
                        binding.cardEventDetail.event = event
                        if (!binding.cardEventDetail.cardViewDetail.isVisible) {
                            crossFadeStart()
                            binding.cardEventDetail.cardViewDetail.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

    private fun newEventCheck() {
        if (EventData.newEvent != null) {
            if (viewModel.eventInList(EventData.newEvent!!.id)) {
                viewModel.map.value!!.animateCamera(
                    CameraUpdateFactory.newLatLngZoom(
                        EventData.newEvent!!.getPosition(),
                        14f
                    )
                )
                EventData.newEvent = null
            }
        }
    }
    //endregion

    //region Cluster
    private fun setUpClusters(map: GoogleMap) {
        map.clear()
        customClusterManager = ClusterManager(requireContext(), map)
        customClusterRenderer = ClusterRenderer(requireContext(), map, customClusterManager)
        customClusterManager.renderer = customClusterRenderer
//        customClusterRenderer

        map.setOnCameraMoveStartedListener {
            customClusterManager.markerCollection.markers.forEach { it.alpha = 0.6f }
            customClusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 0.6f }

            customClusterManager.onCameraIdle()
            customClusterManager.cluster()
        }

        map.setOnCameraIdleListener {
            customClusterManager.markerCollection.markers.forEach { it.alpha = 1.0f }
            customClusterManager.clusterMarkerCollection.markers.forEach { it.alpha = 1.0f }

            customClusterManager.onCameraIdle()
            customClusterManager.cluster()
        }

        customClusterManager.setOnClusterItemClickListener { item ->
            viewModel.assignItem(item)
//            map.animateCamera(CameraUpdateFactory.newLatLngZoom(item.position, 14f))
            item.markerClick()

            false
        }

        customClusterManager.setOnClusterClickListener { cluster ->
            val camPosition = map.cameraPosition.zoom + 1.5f
//            map.animateCamera(CameraUpdateFactory.zoomBy(camPosition))
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(cluster.position, camPosition))

//            if (map.cameraPosition.zoom == 16f) {
//                cluster.items.forEach { item ->
//                    Log.i(TAG, "matched : ${item.event}")
//                }
//            }

            true
        }

        addClusters()
        map.animateCamera(
            CameraUpdateFactory.newLatLngZoom(
                map.cameraPosition.target,
                map.cameraPosition.zoom
            )
        )
    }

    private fun addClusters() {
        viewModel.eventList.observe(viewLifecycleOwner) { list ->
            if (list.isEmpty()) {
//                Log.i(TAG, "list is empty")
            } else {
//                (viewModel.map.value!!).clear()
//                Log.i(TAG, "list is not empty: $list")
                list.forEach { event ->
                    addItems(event)
                }
//                customClusterManager.cluster()
            }
        }
    }

    private fun addItems(event: Event) {
        val offsetItem =
            MyClusterItem(event.title, event.description, event.id, event)
        customClusterManager.addItem(offsetItem)
    }

    private fun MyClusterItem.markerClick() {
        //this.hideInfoWindow()
        when (this.tag.isEmpty()) {
            true -> {
                binding.cardEventDetail.cardViewTag.text = null
                binding.cardEventDetail.cardViewDetail.visibility = View.INVISIBLE
                crossFadeEnd()
            }
            else -> {
                TRACKING.logEvent(Tracking.MAP_OPEN_SMALL_EVENT_DETAILS, null)
                binding.cardEventDetail.cardViewTag.text = this.tag
                viewModel.assignEventId(this)
                viewModel.eventId.observe(viewLifecycleOwner) { eventId ->
                    viewModel.assignEvent(eventId)
                    viewModel.getOwnerFromEvent()
//                    Log.i(TAG, "aEvent izs $eventId")
                    viewModel.event.observe(viewLifecycleOwner) { event ->
                        binding.cardEventDetail.event = event
                        if (!binding.cardEventDetail.cardViewDetail.isVisible) {
                            crossFadeStart()
                            binding.cardEventDetail.cardViewDetail.visibility = View.VISIBLE
                        }
                    }
                }
            }
        }
    }

//    private fun Cluster.click() {}
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun crossFadeStart() {
        binding.cardEventDetail.cardViewDetail.apply {
            alpha = 0f

            animate()
                .translationY(0f)
                .alpha(1f)
                .setDuration(shortAnimationDuration.toLong())
                .start()

        }
    }

    private fun crossFadeEnd() {
        binding.cardEventDetail.cardViewDetail.apply {
            animate()
                .translationY(100f)
                .setDuration(shortAnimationDuration.toLong())
                .start()
        }
    }

    private fun loadingMsgStart() {
        binding.loadLayout.visibility = View.VISIBLE
    }

    private fun loadingMsgEnd() {
        binding.loadLayout.visibility = View.INVISIBLE
    }

    private fun showToast(string: String) {
//        toast(string)
    }

    private fun calendarClick() {
        TRACKING.logEvent(Tracking.MAP_FILTERS, null)
        setCalendar()
    }

    private fun filterList() {
        binding.mapFilterLayout.visibility = View.VISIBLE
    }

    private fun setCalendar() {
        val builder = MaterialDatePicker.Builder.dateRangePicker()
        val now = Calendar.getInstance().time
        val constraints = CalendarConstraints.Builder().apply {
            setStart(now.time)
            setOpenAt(viewModel.startDate.value!!)
        }

        builder.apply {
            setTitleText(getString(R.string.select_filter_period))
            setSelection(
                androidx.core.util.Pair(
                    viewModel.startDate.value!!,
                    viewModel.endDate.value!!
                )
            )
            setCalendarConstraints(constraints.build())
//            setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
        }

        val filter = builder.build()

        filter.show(parentFragmentManager, "Event Range Picker")

        filter.addOnNegativeButtonClickListener {

        }

        filter.addOnPositiveButtonClickListener {
            viewModel.assignStartDate(it.first!!)
            viewModel.assignEndDate(it.second!!)

            try {
                refreshEventList()
            } catch (e: Exception) {
//                toast("$e")
            }
        }
    }

    private fun checkEventList(list: MutableList<Event>) {
        if (list.isEmpty()) {
            binding.filterButton.visibility = View.VISIBLE
//            binding.filterButtonBall.visibility = View.GONE
//            binding.filterButtonStadium.visibility = View.GONE
//            binding.filterButtonCup.visibility = View.GONE

            viewModel.assignFilterCount(0)
        } else assignFilters(list)
    }

    private fun assignFilters(list: MutableList<Event>) {
        var nbTourneyEvent = 0
        var nbFriendlyEvent = 0
        var nbPlateauEvent = 0

        binding.filterButton.visibility = View.GONE
//        binding.filterButtonBall.visibility = View.GONE
//        binding.filterButtonStadium.visibility = View.GONE
//        binding.filterButtonCup.visibility = View.GONE

        list.forEach {
            when (it.type) {
                "plateau" -> {
                    nbPlateauEvent++
                }
                "friendly" -> {
                    nbFriendlyEvent++
                }
                "tournament" -> {
                    nbTourneyEvent++
                }
            }
        }

        if (nbFriendlyEvent != 0) {
//            binding.filterButtonBall.visibility = View.VISIBLE
            viewModel.assignFilterFriendlyCount(nbFriendlyEvent)
        }

        if (nbPlateauEvent != 0) {
//            binding.filterButtonStadium.visibility = View.VISIBLE
            viewModel.assignFilterPlateauCount(nbPlateauEvent)
        }

        if (nbTourneyEvent != 0) {
//            binding.filterButtonCup.visibility = View.VISIBLE
            viewModel.assignFilterTourneyCount(nbTourneyEvent)
        }

        newEventCheck()
    }
    //endregion

    //region Location Request
    private fun setUpMapRequest() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }
    }
    //endregion
}