package com.myfzone_sport.myf_zone.app.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentProfile2Binding

private const val ARG_PARAM1 = "coachId"

class ProfileFragment : Fragment() {

    companion object {
        private var coachId: String? = null
        private lateinit var binding: FragmentProfile2Binding
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        arguments?.let {
            coachId = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,R.layout.fragment_profile2, container, false
        )

        return binding.root
    }
    //endregion
}