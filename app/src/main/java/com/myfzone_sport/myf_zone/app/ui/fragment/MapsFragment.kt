package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.framework.RemoteDataSourceImpl
import com.myfzone_sport.myf_zone.app.ui.adapter.MapEventsAdapter
import com.myfzone_sport.myf_zone.app.ui.viewmodel.MapsViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.MapsViewModelFactory
import com.myfzone_sport.myf_zone.data.RepositoryImpl
import com.myfzone_sport.myf_zone.databinding.FragmentMaps2Binding
import com.myfzone_sport.myf_zone.domain.event.Event
import com.myfzone_sport.myf_zone.usecases.detailevent.GetOwnerFromEventUseCase
import com.myfzone_sport.myf_zone.usecases.event.GetAllEventsUseCase
import com.myfzone_sport.myf_zone.usecases.user.*
import org.jetbrains.anko.support.v4.toast
import java.lang.Exception

private const val ARG_PARAM1 = "eventId"

class MapsFragment : Fragment() {
    //region Variables
    private var eventId: String? = null
    private val args by navArgs<MapsFragmentArgs>()

    private lateinit var binding: FragmentMaps2Binding
    private lateinit var viewModel: MapsViewModel
    private lateinit var viewModelFactory: MapsViewModelFactory
    private lateinit var adapterMap: MapEventsAdapter
    private lateinit var scrollListener: RecyclerView.OnScrollListener
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

        if (!viewModel.isMapInitialized.value!!)
            viewModel.assignMap(googleMap, args.coachClub)
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
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

    override fun onResume() {
        super.onResume()

//        viewModel.initializeHome()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }
    //endregion

    //region Setups
    private fun setupViewModel() {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getAllEventsUseCase = GetAllEventsUseCase(repository)
        val getUserUseCase = GetUserUseCase(repository)
        val getUserEventListUseCase = GetUserEventListUseCase(repository)
        val getUserClubUseCase = GetUserClubUseCase(repository)
        val getUserAffiliationUseCase = GetUserClubAffiliationUseCase(repository)
        val getOwnerFromEventUseCase = GetOwnerFromEventUseCase(repository)

        viewModelFactory = MapsViewModelFactory(
            getAllEventsUseCase,
            getUserUseCase,
            getUserEventListUseCase,
            getUserClubUseCase,
            getUserAffiliationUseCase,
            getOwnerFromEventUseCase
        )

        viewModel = ViewModelProvider(this, viewModelFactory)
            .get(MapsViewModel::class.java)
    }

    private fun setupViews() {
        binding.backArrow.apply {
            background = null
            setOnClickListener { requireActivity().onBackPressed() }
        }

//        setupEventRecycler()
    }

    private fun setupObservers() {
        viewModel.map.observe(viewLifecycleOwner, {
            setupEventRecycler(it)
        })
    }
    //endregion

    //region RecyclerView
    private fun setupEventRecycler(map: GoogleMap) {
        val remoteDataSource = RemoteDataSourceImpl()
        val repository = RepositoryImpl(remoteDataSource)

        val getImageReferenceUseCase = GetImageReferenceUseCase(repository)

        adapterMap = MapEventsAdapter(getImageReferenceUseCase)

        val snapHelper = LinearSnapHelper()

        binding.recyclerView.adapter = adapterMap
        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager
        binding.recyclerView.onFlingListener = null
        snapHelper.attachToRecyclerView(binding.recyclerView)

//        scrollListener = object : RecyclerView.OnScrollListener() {
//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                val centerView = snapHelper.findSnapView(layoutManager)
//                val pos = layoutManager.getPosition(centerView!!)
//                if (newState == RecyclerView.SCROLL_STATE_IDLE || (pos == 0 && newState == RecyclerView.SCROLL_STATE_DRAGGING)) {
//                    toast("pos: $pos")
//                    Log.d("BINDING", "positionView SCROLL_STATE_IDLE: $pos")
//                }
//            }

//            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
//                super.onScrollStateChanged(recyclerView, newState)
//                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
//                    val centerView = snapHelper.findSnapView(layoutManager)
////                    val position = layoutManager.getPosition(centerView!!)
//                    try {
//                        toast("position: ${layoutManager.getPosition(centerView!!)}")
//                    } catch (e: Exception) {
//                        toast("error: $e")
//                    }
//
////                    viewModel.closeEventsList.observe(viewLifecycleOwner, {
////                        centerMapOnEvent(it[position], map)
////                    })
//                }
//            }
//        }

        viewModel.closeEventsList.observe(viewLifecycleOwner, {
            if (it.isNotEmpty()) {
                adapterMap.setData(it)
                placeEvents(it, map)
            }
        })
    }
    //endregion

    //region Event
    private fun placeEvents(list: MutableList<Event>, map: GoogleMap) {
        for (event in list) {
            map.addMarker(setEventMarkerOptions(event))
        }
    }

    private fun centerMapOnEvent(event: Event, map: GoogleMap) {
        toast("event: ${event.title}")
        val positionEvent = LatLng(event.lat, event.lng)
        map.animateCamera(CameraUpdateFactory.newLatLngZoom(positionEvent, 13.5f))
    }

    private fun setEventMarkerOptions(event: Event): MarkerOptions {
        return MarkerOptions().apply {
            position(event.getPosition())
            title(event.getAcronym())
            snippet(event.title)
        }
    }
    //endregion
}