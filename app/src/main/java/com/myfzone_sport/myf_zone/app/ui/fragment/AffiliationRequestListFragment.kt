package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Paint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationRequestListBinding
import com.myfzone_sport.myf_zone.databinding.FragmentHomeBinding

class AffiliationRequestListFragment : Fragment() {

    companion object {
        private lateinit var binding: FragmentAffiliationRequestListBinding
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
            R.layout.fragment_affiliation_request_list,
            container,
            false
        )

        binding.codeAffiliationLink.paintFlags = Paint.UNDERLINE_TEXT_FLAG

        return binding.root
    }
}