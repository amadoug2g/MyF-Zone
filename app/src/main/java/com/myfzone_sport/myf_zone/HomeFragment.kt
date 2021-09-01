package com.myfzone_sport.myf_zone

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding
import com.myfzone_sport.myf_zone.databinding.FragmentLogin2Binding

class HomeFragment : Fragment() {

    companion object {
        private lateinit var binding: FragmentHomeBinding
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_home,
            container,
            false
        )

        return binding.root
    }
}