package com.example.myf_zone.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.fragment_maps.view.*

class MapsFragment : Fragment() {

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
        val sydney = LatLng(48.0, 2.0)
        val pierrefitte = LatLng(48.9679, 2.3641)
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker near Paris"))
        val pfmark = googleMap.addMarker(
            MarkerOptions()
                .position(pierrefitte)
                .title("Pierrefitte")
        )
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val fragmentInflater = inflater.inflate(R.layout.fragment_maps, container, false)
        (activity as AppCompatActivity).supportActionBar?.hide()

        fragmentInflater.account_button.setOnClickListener {
            Log.d("SignUpFragment", "Clicked")
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

    private fun getMarkerDetail(marker: Marker, name: TextView) {
        name.text = marker.title
    }
}