package mfz.myfzone_sport.myf_zone.fragments.maps.map

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.card_event_item.*
import kotlinx.coroutines.launch
import mfz.myfzone_sport.myf_zone.R
import mfz.myfzone_sport.myf_zone.databinding.FragmentMapsBinding
import mfz.myfzone_sport.myf_zone.model.event.Event
import mfz.myfzone_sport.myf_zone.screens.MainScreen
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil.getCardDetail
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil.initializeMap
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil.placeEventsOnMap
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil.placeUserClub
import mfz.myfzone_sport.myf_zone.util.event.MapsUtil.setMapZoomPreferences
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.auth
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentClub
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.getCurrentUser
import mfz.myfzone_sport.myf_zone.util.user.UserAccount.updateCurrentUser
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation.affiliationStatus
import mfz.myfzone_sport.myf_zone.util.user.UserAffiliation.userAffiliationStatus
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast


class MapsFragment : Fragment(),
    GoogleMap.OnMapClickListener {
    private var mapInitialized = false

    companion object {
        private val TAG = MapsFragment::class.java.simpleName
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1

        private lateinit var binding: FragmentMapsBinding
        private lateinit var viewModel: MapsViewModel
    }

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

        if (!mapInitialized) {
            lifecycleScope.launch {
                initializeMap(
                    googleMap,
                    markerList, cardView_detail
                )
            }
            mapInitialized = true
        }

        googleMap.setOnMarkerClickListener {
            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it.position, 14f))

            getCardDetail(
                it,
                cardView_detail,
                cardView_clubName,
                cardView_clubDesc,
                cardView_clubImage,
                cardView_tag
            )

            false
        }

        googleMap.setOnCameraMoveListener {
            if (googleMap.cameraPosition.zoom > 9) {
//                showMarkerItem(
//                    markerItemList
//                )
            } else {
//                hideMarkerItem(
//                    markerItemList
//                )
                cardView_detail.visibility = View.GONE
            }
        }

        googleMap.setOnMapClickListener(this)

        setMapZoomPreferences(googleMap)

        lifecycleScope.launch {
//            if (loadLayout != null)
            binding.loadLayout.visibility = View.VISIBLE
            placeEventsOnMap(googleMap, requireContext(), markerList)

            try {
                when (affiliationStatus()!!) {
                    true -> {
                        getCurrentClub { clubAffiliation ->
                            placeUserClub(clubAffiliation, googleMap, requireContext())
                        }
                    }
                    false -> {

                    }
                }
            } catch (e: Exception) {
                Log.d(TAG, "Error: $e")
            }

//            if (loadLayout != null)
            binding.loadLayout.visibility = View.INVISIBLE
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        auth = FirebaseAuth.getInstance()
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

        val fragmentInflater = inflater.inflate(R.layout.fragment_maps, container, false)

        binding.accountButton.background = null
        binding.listButton.background = null

        val currentUser = auth.currentUser

        binding.accountButton.setOnClickListener {
            if (currentUser == null) {
                navigate(R.id.mapsToLogin)
            } else {
                getCurrentUser { user ->
                    if (currentUser.displayName == "") {
                        updateCurrentUser("", user.firstName, user.lastName)
                    }
                }

                userAffiliationStatus {
                    when (it) {
                        true -> {
//                            toast("Vous êtes affilié")

                            navigate(R.id.mapsToProfile)
                        }
                        false -> {
                            toast("Vous n'êtes pas affilié")

                            (activity as AppCompatActivity).supportActionBar?.apply {
                                show()
                                setTitle(R.string.affiliation_text)
                                setHomeButtonEnabled(true)
                                setDisplayHomeAsUpEnabled(true)
                            }
                            navigate(R.id.mapsToAffiliationRequest)
                        }
                    }
                }
            }
        }

        binding.listButton.setOnClickListener {
            if (currentUser == null) {
                MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.logout)
                    .setMessage(R.string.logout_message_error)
                    .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
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
                        startActivity(intentFor<MainScreen>().newTask().clearTask())
                    }
                    .setNegativeButton(R.string.cancel_message) { _: DialogInterface, _: Int ->
                    }
                    .show()
            }
        }

        binding.cardEventDetail.cardViewDetail.setOnClickListener {
            when (cardView_tag.text) {
                null -> {
                    toast("clicked smth else")
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

    override fun onMapClick(p0: LatLng?) {
        cardView_detail.apply {
            visibility = View.GONE
//            startAnimation(AnimationUtils.loadAnimation(requireContext(), R.anim.to_bottom))
        }
    }

    private fun navigate(destination: Int) {
        findNavController().navigate(destination)
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

    private fun setEventDetails(event: Event) {
        cardView_detail.apply {
            visibility = View.VISIBLE
//            startAnimation(AnimationUtils.loadAnimation(context, R.anim.from_bottom))
        }
        cardView_clubName.text = event.title
        cardView_clubDesc.text = event.description
        cardView_clubImage.setImageResource(MapsUtil.setMarkerType(event))
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
}