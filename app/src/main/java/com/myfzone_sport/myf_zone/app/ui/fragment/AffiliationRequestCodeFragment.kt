package com.myfzone_sport.myf_zone.app.ui.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.Navigation
import com.google.android.material.snackbar.Snackbar
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.app.ui.viewmodel.AffiliationRequestListViewModel
import com.myfzone_sport.myf_zone.app.ui.viewmodel.AffiliationRequestListViewModelFactory
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationRequestCodeBinding
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationRequestListBinding

class AffiliationRequestCodeFragment : Fragment() {

    //region Variables
    companion object {
        private lateinit var binding: FragmentAffiliationRequestCodeBinding
//        private lateinit var viewModel: AffiliationRequestListViewModel
//        private lateinit var viewModelFactory: AffiliationRequestListViewModelFactory
    }
    //endregion

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupViewModel()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_request_code,
            container,
            false
        )

        setupViews()
        setupObservers()

        return binding.root
    }

    //endregion

    //region Setups
    private fun setupViewModel() {
//        TODO("Not yet implemented")
    }

    private fun setupViews() {
        binding.exit.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.settings.setOnClickListener {
            navigate(R.id.affiliationCodeToSettings)
        }
    }

    private fun setupObservers() {
//        TODO("Not yet implemented")
    }
    //endregion

    //region Affiliation
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion

    //region View Methods
    private fun loadingStart() {
        binding.progressBar.visibility = View.VISIBLE
    }

    private fun loadingStop() {
        binding.progressBar.visibility = View.INVISIBLE
    }

    private fun showError(message: String? = "") {
        Snackbar.make(
            binding.exit,
            if (!message.isNullOrEmpty()) message else "viewModel.errorMessage.value.toString()",
            Snackbar.LENGTH_LONG
        )
            .setTextColor(Color.WHITE)
            .setActionTextColor(Color.CYAN)
            .show()
    }
    //endregion
}