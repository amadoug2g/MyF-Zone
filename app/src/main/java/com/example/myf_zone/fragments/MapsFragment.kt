package com.example.myf_zone.fragments

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import com.example.myf_zone.model.event.Event
import com.example.myf_zone.model.event.EventParticipation
import com.example.myf_zone.util.MapsUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_maps.view.*
import org.jetbrains.anko.sdk27.coroutines.onClick
import org.jetbrains.anko.support.v4.toast
import java.util.*

class MapsFragment : Fragment(),
    GoogleMap.OnMarkerClickListener,
    GoogleMap.OnMapClickListener {
    private val TAG = MapsFragment::class.java.simpleName

    private val markerList: MutableList<Marker> = mutableListOf()

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

        //Event Markers Data
        val eventOwner01 = EventParticipation(
            "",
            "PSG",
            "coachID",
            "Full Name",
            "sportID",
            "Football",
            "categoryID",
            "U21",
            "subCategoryID",
            "Regional"
        )

        val eventOwner02 = EventParticipation(
            "",
            "Pierrefiette FC",
            "coachID",
            "Full Name",
            "sportID",
            "Football",
            "categoryID",
            "U21",
            "subCategoryID",
            "Regional"
        )

        val eventOwner03 = EventParticipation(
            "",
            "Bobigny FC",
            "coachID",
            "Full Name",
            "sportID",
            "Football",
            "categoryID",
            "U21",
            "subCategoryID",
            "Regional"
        )

        val eventParticipant = EventParticipation()
        val participantList = mutableListOf<EventParticipation>(eventParticipant)

        val event01 = Event(
            "Match Amical - U21", eventOwner01.clubAcronym, "Friendly", 8, Date(0),
            "address", 48.8414, 2.2530, Date(0), eventOwner01, participantList
        )

        val event02 = Event(
            "Plateau - U9", eventOwner02.clubAcronym, "Plateau", 6, Date(0),
            "address", 48.9679, 2.3641, Date(0), eventOwner02, participantList
        )

        val event03 = Event(
            "Tournoi - U16", eventOwner03.clubAcronym, "Tournament", 8, Date(0),
            "address", 48.9096, 2.4397, Date(0), eventOwner03, participantList
        )

        val eventList = mutableListOf<Event>()

        eventList.add(event01)
        eventList.add(event02)
        eventList.add(event03)

        MapsUtil.addItem(
            markerList,
            MapsUtil.placeEventOnMap(googleMap, event01, requireContext()),
            MapsUtil.placeEventOnMap(googleMap, event02, requireContext(), R.mipmap.ic_pffc_logo),
            MapsUtil.placeEventOnMap(googleMap, event03, requireContext(), R.mipmap.ic_fc93_logo)
        )

        MapsUtil.addItemEvent(event01, event02, event03)

        MapsUtil.initializeMap(
            googleMap,
            this,
            this,
            markerList, cardView_detail
        )

        list_button.onClick {
            toast("Zoom: " + googleMap.cameraPosition.zoom)
        }

//        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
//        setUpMapRequest()
    }

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

    override fun onMarkerClick(marker: Marker): Boolean {
//        MapsUtil.zoomOnMarker(this.googleMap, marker.position)
        MapsUtil.getMarkerDetails(
            marker,
            cardView_detail,
            cardView_clubName,
            cardView_clubDesc,
            cardView_clubImage,
            requireContext()
        )

        return true
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentInflater = inflater.inflate(R.layout.fragment_maps, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        fragmentInflater.account_button.setOnClickListener {
            Navigation
                .findNavController(fragmentInflater)
                .navigate(R.id.mapsToLogin)
        }
        return fragmentInflater
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onMapClick(p0: LatLng?) {
        cardView_detail.visibility = View.GONE
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}