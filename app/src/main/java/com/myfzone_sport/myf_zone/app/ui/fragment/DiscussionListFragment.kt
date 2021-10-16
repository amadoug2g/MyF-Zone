package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationRequestListBinding
import com.myfzone_sport.myf_zone.databinding.FragmentDiscussionListBinding

class DiscussionListFragment : Fragment() {

    companion object {
        private lateinit var binding: FragmentDiscussionListBinding
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
            R.layout.fragment_discussion_list,
            container,
            false
        )

        return binding.root
    }
}