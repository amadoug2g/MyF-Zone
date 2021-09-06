package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentMapBinding

private const val ARG_PARAM1 = "eventId"

class MapFragment : Fragment() {
    private var eventId: String? = null

    companion object {
        private lateinit var binding: FragmentMapBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            eventId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_map, container, false
        )
        return binding.root
    }
}