package com.example.myf_zone.fragments

import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import com.example.myf_zone.util.MapsUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_maps.view.*
import org.jetbrains.anko.support.v4.toast

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

        //Markers
        val markerOptionsPFFC = MarkerOptions()
            .position(LatLng(48.9679, 2.3641))
            .title("Pierrefiette")
            .icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(requireContext().resources, R.mipmap.ic_pffc_logo)
                )
            )

        val markerOptionsFC93 = MarkerOptions()
            .position(LatLng(48.9096, 2.4397))
            .title("Bobigny")
            .icon(
                BitmapDescriptorFactory.fromBitmap(
                    BitmapFactory.decodeResource(requireContext().resources, R.mipmap.ic_fc93_logo)
                )
            )

        MapsUtil.addItem(
            markerList,
            MapsUtil.placeMarkerOnMap(googleMap, markerOptionsPFFC),
            MapsUtil.placeMarkerOnMap(googleMap, markerOptionsFC93)
        )

        MapsUtil.initializeMap(
            googleMap,
            this,
            this,
            markerList, cardView_detail
        )

        toast("Zoom level: " + googleMap.cameraPosition.zoom)
    }

    override fun onMarkerClick(marker: Marker): Boolean {

        if (marker.title == "Pierrefiette")
            MapsUtil.getMarkerDetails(
                marker,
                cardView_detail,
                cardView_clubName,
                cardView_clubImage,
                requireContext(),
                R.drawable.pffc
            )
        else MapsUtil.getMarkerDetails(
            marker,
            cardView_detail,
            cardView_clubName,
            cardView_clubImage,
            requireContext(),
            R.drawable.fc93
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
}