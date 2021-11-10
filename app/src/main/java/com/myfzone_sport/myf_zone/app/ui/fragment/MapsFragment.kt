package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.adapter.CategoryEventAdapter
import com.myfzone_sport.myf_zone.app.ui.adapter.UserEventAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.HomeViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.HomeViewModelFactory
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import com.myfzone_sport.myf_zone.databinding.FragmentMaps2Binding
import com.myfzone_sport.myf_zone.screens.MainScreen.Companion.binding

private const val ARG_PARAM1 = "param1"

class MapsFragment : Fragment() {
    //region Variables
    private var param1: String? = null

    private lateinit var binding: FragmentMaps2Binding
//    private lateinit var viewModel: HomeViewModel
//    private lateinit var viewModelFactory: HomeViewModelFactory
//    private lateinit var adapterCategory: CategoryEventAdapter
//    private lateinit var adapterUserEvents: UserEventAdapter
    //endregion

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

        googleMap.uiSettings.apply {
            isCompassEnabled = false
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = true
        }

        googleMap.apply {
            setPadding(0, 0, 0, 146)

            setMinZoomPreference(10f)
            setMaxZoomPreference(4f)

            val position = LatLng(48.8550, 2.3452)
            moveCamera(CameraUpdateFactory.newLatLngZoom(position, 9.5f))
        }
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
        }

        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_maps2, container, false
        )

        setupViews()
        setupObservers()

        return binding.root
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
    }

    private fun setupViews() {
        binding.backArrow.apply {
            background = null
            setOnClickListener { requireActivity().onBackPressed() }
        }
    }

    private fun setupObservers() {
    }
    //endregion
}