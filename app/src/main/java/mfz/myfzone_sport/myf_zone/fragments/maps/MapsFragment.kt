package mfz.myfzone_sport.myf_zone.fragments.maps

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
import androidx.lifecycle.observe
import androidx.navigation.Navigation
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.android.synthetic.main.card_event_item.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentMapsBinding
import mfz.myfzone_sport.myf_zone.model.State
import org.jetbrains.anko.support.v4.toast
import java.util.*


class MapsFragment : Fragment(),
    GoogleMap.OnMapClickListener {
    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
        private var shortAnimationDuration: Int = 300

        private lateinit var binding: FragmentMapsBinding
        private lateinit var viewModel: MapsViewModel
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

        lifecycleScope.launch {
            try {
                viewModel.assignUser()
                viewModel.assignClubAffiliation()
                viewModel.assignClub()
            } catch (e: Exception) {
                Log.i(TAG, "An error occurred: $e")
            }
            viewModel.checkUserAffiliationStatus()
        }
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
        //placeUserClub()

        binding.filterButton.setOnClickListener { setCalendar() }

        binding.cardEventDetail.cardViewDetail.setOnClickListener {
            when (binding.cardEventDetail.cardViewTag.text) {
                null -> {

                }
                else -> {
                    val bundle = bundleOf("eventId" to cardView_tag.text)
                    navigate(R.id.mapsToEventDetails, bundle)
                }
            }
        }

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
                        val size = list.size
                        binding.filterButton.icon
                        viewModel.assignFilterCount(size)
                        binding.viewModel = viewModel
                        viewModel.placeEvents(viewModel.map.value!!, requireContext(), list)
                        placeUserClub()
                    }
                    is State.Failed -> {
                        loadingMsgEnd()
                        val message = "Error loading markers: ${state.message}"
                        showToast(message)
                    }
                }
            }
        }
    }

    private fun placeUserClub() {
        viewModel.isUserSignedIn.observe(viewLifecycleOwner) { isUserSignedIn ->
            if (isUserSignedIn) {
                viewModel.isUserAffiliated.observe(viewLifecycleOwner) { isUserAffiliated ->
                    if (isUserAffiliated) {
                        viewModel.placeUserClub(
                            viewModel.club.value!!,
                            viewModel.map.value!!,
                            requireContext()
                        )
                    }
                }
            }
        }
    }

    private fun Marker.markerClick() {
        //this.hideInfoWindow()
        when (this.tag) {
            null -> {
                binding.cardEventDetail.cardViewTag.text = null
                binding.cardEventDetail.cardViewDetail.visibility = View.INVISIBLE
                crossFadeEnd()
            }
            else -> {
                binding.cardEventDetail.cardViewTag.text = this.tag as String
                viewModel.assignEventId(this)
                viewModel.eventId.observe(viewLifecycleOwner) { eventId ->
                    viewModel.assignEvent(eventId)
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
        toast(string)
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
            setTheme(R.style.ThemeOverlay_MaterialComponents_MaterialCalendar)
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
                toast("$e")
            }
        }
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