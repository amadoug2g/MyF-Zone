package com.example.myf_zone.fragments

import android.content.DialogInterface
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
import com.example.myf_zone.model.event.EventParticipation
import com.example.myf_zone.screens.MapAccountScreen
import com.example.myf_zone.util.FirebaseUtil
import com.example.myf_zone.util.FirebaseUtil.auth
import com.example.myf_zone.util.FirebaseUtil.updateCurrentUser
import com.example.myf_zone.util.MapsUtil
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.fragment_maps.*
import kotlinx.android.synthetic.main.fragment_maps.view.*
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
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

        //Event Markers Data
        val eventOwner = EventParticipation(
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
        val participantList = mutableListOf(eventParticipant)

        MapsUtil.placeFirestoreEvent(
            requireContext(),
            googleMap,
            eventOwner,
            participantList,
            markerList
        )

        MapsUtil.initializeMap(
            googleMap,
            this,
            this,
            markerList, cardView_detail
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()
    }

    override fun onMarkerClick(marker: Marker): Boolean {
//        MapsUtil.zoomOnMarker(this.googleMap, marker.position
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
        val currentUser = auth.currentUser

        fragmentInflater.account_button.setOnClickListener {

            if (currentUser == null) {
                Navigation
                    .findNavController(fragmentInflater)
                    .navigate(R.id.mapsToLogin)
            } else {

                FirebaseUtil.getCurrentUser { user ->
                    toast("Hi, ${user.firstName} ${user.lastName}")

                    if (currentUser.displayName == "") {
                        updateCurrentUser("", user.firstName, user.lastName)
                    }
                }
            }
        }

        fragmentInflater.list_button.setOnClickListener {
            if (currentUser == null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_message_error)
                    .setPositiveButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                    }
                    .show()
            } else {
                toast(currentUser.uid)
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_message)
                    .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                        auth.signOut()
                        toast(R.string.logout_success)
                        startActivity(intentFor<MapAccountScreen>().newTask().clearTask())
                    }
                    .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                    }
                    .show()
            }
        }
        return fragmentInflater
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    override fun onMapClick(p0: LatLng?) {
        cardView_detail.apply {
            visibility = View.GONE
//            startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom))
        }
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

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}