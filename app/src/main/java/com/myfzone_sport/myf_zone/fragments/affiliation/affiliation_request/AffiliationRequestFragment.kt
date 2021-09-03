package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_request

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationRequestBinding
import com.myfzone_sport.myf_zone.domain.State
import com.myfzone_sport.myf_zone.screens.MainScreen
import com.myfzone_sport.myf_zone.util.Constants.TRACKING
import com.myfzone_sport.myf_zone.util.Tracking
import kotlinx.android.synthetic.main.fragment_affiliation_request.*
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.clearTask
import org.jetbrains.anko.newTask
import org.jetbrains.anko.sdk27.coroutines.textChangedListener
import org.jetbrains.anko.support.v4.intentFor
import org.jetbrains.anko.support.v4.toast

private const val ARG_PARAM1 = "page"

class AffiliationRequestFragment : Fragment() {
    companion object {
        private val TAG = this::class.java.simpleName
        private var page: Int? = null

        private var sportFirstPass = true
        private var categoryFirstPass = true
        private var subCategoryFirstPass = true

        private lateinit var viewModel: AffiliationRequestViewModel
        private lateinit var binding: FragmentAffiliationRequestBinding
    }

    //region Override Methods
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        TRACKING.logEvent(Tracking.AFFILIATION_TO_CLUB, null)

        arguments?.let {
            page = it.getInt(ARG_PARAM1)
        }

        viewModel = ViewModelProvider(this).get(AffiliationRequestViewModel::class.java)

        lifecycleScope.launch {
            getClubs()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_request,
            container,
            false
        )

        binding.affiliationLaterAffiliate.setOnClickListener {
            when (page) {
                R.id.mapsFragment, R.id.messageFragment, R.id.calendarFragment -> {
                    requireActivity().onBackPressed()
                    //navigate(R.id.globalAffiliationRequestToMaps)
                    //navigate(R.id.globalAffiliationRequestToMessage)
                    //navigate(R.id.globalAffiliationRequestToCalendar)
                }
                else -> {
                    startActivity(intentFor<MainScreen>().newTask().clearTask())
                }
            }
            TRACKING.logEvent(Tracking.AFFILIATION_TO_CLUB_MAYBE_LATER, null)
        }

        binding.affiliateButton.setOnClickListener {
            val affiliationCode = binding.affiliationCodeInput.text.toString()
            val affiliationSport = binding.sportSpinner.selectedItem.toString()
            val affiliationCategory = binding.categorySpinner.selectedItem.toString()
            val affiliationSubCategory = binding.subCategorySpinner.selectedItem.toString()

            showProgressBar()

            if (validateForm()) {
                lifecycleScope.launch {
                    val club = viewModel.checkCode(affiliationCode, viewModel.clubList.value!!)
                    if (club != null) {
                        viewModel.affiliationProcess(
                            affiliationCode,
                            affiliationSport,
                            affiliationCategory,
                            affiliationSubCategory
                        )

                        val bundle = bundleOf("clubId" to affiliationCode)
                        try {
                            withContext(Main) {
                                TRACKING.logEvent(Tracking.AFFILIATION_TO_CLUB_DONE, null)
                                navigate(R.id.affiliationRequestToAffiliationSuccess, bundle)
                            }
                        } catch (e: Exception) {
                            val bundleTracking =
                                bundleOf("Affiliation ${getString(R.string.error_msg)}" to e.localizedMessage)
                            TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                            Log.d(TAG, "${getString(R.string.error_msg)} : ${e.localizedMessage}")
                        }
                    } else {
                        Log.d(TAG, "code is not valid")
                        withContext(Main) {
                            errorMessage()
                        }
                    }
                }
            } else {
                val bundleTracking =
                    bundleOf("Affiliation ${getString(R.string.error_fetch_club)}" to "Form is not valid")
                TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                Log.d(TAG, "form is not valid")
            }

            hideProgressBar()
        }

        binding.affiliationSettings.setOnClickListener {
            navigate(R.id.affiliationRequestToSettings)
        }

        binding.affiliationListRequest.setOnClickListener {
            navigate(R.id.affiliationRequestToAffiliationClubList)
        }

        binding.clubListText.setOnClickListener {
            navigate(R.id.affiliationRequestToAffiliationClubList)
        }

        binding.infoButton.setOnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle(getString(R.string.affiliation_code_text))
                .setIcon(R.drawable.ic_info)
//                .setMessage(getString(R.string.enter_event_msg))
                .setMessage(getString(R.string.affiliation_code_helper))
                .setPositiveButton(R.string.confirm_message) { _: DialogInterface, _: Int ->
                }.show()
        }

        binding.contactUsText.setOnClickListener {
            navigate(R.id.affiliationRequestToAffiliationSuggestion)
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        lifecycleScope.launchWhenResumed {
            showProgressBar()

            //region SPORT SPINNER HANDLER

            val sportList = mutableListOf<String>()
            val sportObjectList = viewModel.querySportList()!!
            for (item in sportObjectList)
                sportList.add(item.name)

            if (!sportList.isNullOrEmpty()) {
                binding.sportSpinner.adapter =
                    ArrayAdapter(requireContext(), R.layout.simple_layout_file, sportList)
            }
            //endregion

            //region CATEGORY SPINNER HANDLER

            val categoryList = mutableListOf<String>()
            val sportId =
                viewModel.querySportIdFromList(
                    binding.sportSpinner.selectedItem.toString(),
                    sportObjectList
                )!!
            val categoryObjectList = viewModel.queryCategoryList(sportId)!!
            if (sportId.isNotEmpty()) {
                for (item in categoryObjectList)
                    categoryList.add(item.name)

                if (!categoryList.isNullOrEmpty()) {
                    binding.categorySpinner.adapter =
                        ArrayAdapter(requireContext(), R.layout.simple_layout_file, categoryList)
                    binding.categorySpinner.visibility = View.VISIBLE
                } else {
                    binding.categorySpinner.visibility = View.GONE
                    binding.subCategorySpinner.visibility = View.GONE
                    binding.affiliateButton.isEnabled = true
                }
            }
            //endregion

            //region SUB-CATEGORY SPINNER HANDLER

            val subCategoryList = mutableListOf<String>()
            val categoryId = viewModel.queryCategoryIdFromList(
                binding.categorySpinner.selectedItem.toString(),
                categoryObjectList
            )!!
            if (sportId.isNotEmpty() && categoryId.isNotEmpty()) {
                for (item in viewModel.querySubCategoryList(sportId, categoryId)!!)
                    subCategoryList.add(item.name)

                if (!subCategoryList.isNullOrEmpty()) {
                    binding.subCategorySpinner.adapter =
                        ArrayAdapter(requireContext(), R.layout.simple_layout_file, subCategoryList)
                    binding.subCategorySpinner.visibility = View.VISIBLE
                } else {
                    binding.subCategorySpinner.visibility = View.GONE
                    binding.affiliateButton.isEnabled = true
                }
            }
            //endregion

            hideProgressBar()
        }

        binding.affiliationCodeInput.textChangedListener {
            onTextChanged { sequence, _,
                            _, _ ->
                if (!sequence?.toString().equals(null)) {
                    validateForm()
                }
            }
        }

        binding.sportSpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
//                validateForm()
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (!sportFirstPass) {

                        lifecycleScope.launchWhenResumed {

                            showProgressBar()

                            val sportObjectList = viewModel.querySportList()!!

                            //region CATEGORY SPINNER HANDLER

                            val categoryList = mutableListOf<String>()
                            val sportId = viewModel.querySportIdFromList(
                                sportSpinner.selectedItem.toString(),
                                sportObjectList
                            )!!
                        val categoryObjectList = viewModel.queryCategoryList(sportId)!!
                        if (sportId.isNotEmpty()) {
                            for (item in categoryObjectList)
                                categoryList.add(item.name)

                            if (!categoryList.isNullOrEmpty()) {
                                categorySpinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    R.layout.simple_layout_file,
                                    categoryList
                                )
                                categorySpinner.visibility = View.VISIBLE
                            } else {
                                categorySpinner.visibility = View.GONE
                                subCategorySpinner.visibility = View.GONE
                                affiliateButton.isEnabled = true
                            }
                        }
                        //endregion

                        //region SUB-CATEGORY SPINNER HANDLER

                        val subCategoryList = mutableListOf<String>()
                        val categoryId = viewModel.queryCategoryIdFromList(
                            categorySpinner.selectedItem.toString(),
                            categoryObjectList
                        )!!
                        if (sportId.isNotEmpty() && categoryId.isNotEmpty()) {
                            for (item in viewModel.querySubCategoryList(sportId, categoryId)!!)
                                subCategoryList.add(item.name)

                            if (!subCategoryList.isNullOrEmpty()) {
                                subCategorySpinner.adapter = ArrayAdapter(
                                    requireContext(),
                                    R.layout.simple_layout_file,
                                    subCategoryList
                                )
                                subCategorySpinner.visibility = View.VISIBLE
                            } else {
                                subCategorySpinner.visibility = View.GONE
                                affiliateButton.isEnabled = true
                            }
                        }
                        //endregion

                        hideProgressBar()
                    }

                    validateForm(false)
                }

                sportFirstPass = false
            }
        }

        binding.categorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
//                validateForm()
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                    if (!categoryFirstPass) {

                        lifecycleScope.launchWhenResumed {

                            showProgressBar()

                            val sportObjectList = viewModel.querySportList()!!
                            val sportId = viewModel.querySportIdFromList(
                                binding.sportSpinner.selectedItem.toString(),
                                sportObjectList
                            )!!
                            val categoryObjectList = viewModel.queryCategoryList(sportId)!!

                            //region SUB-CATEGORY SPINNER HANDLER

                            val subCategoryList = mutableListOf<String>()
                            val categoryId = viewModel.queryCategoryIdFromList(
                                binding.categorySpinner.selectedItem.toString(),
                                categoryObjectList
                            )!!
                            if (sportId.isNotEmpty() && categoryId.isNotEmpty()) {
                                for (item in viewModel.querySubCategoryList(sportId, categoryId)!!)
                                    subCategoryList.add(item.name)

                                if (!subCategoryList.isNullOrEmpty()) {
                                    binding.subCategorySpinner.adapter = ArrayAdapter(
                                        requireContext(),
                                        R.layout.simple_layout_file,
                                        subCategoryList
                                    )
                                    binding.subCategorySpinner.visibility = View.VISIBLE
                                } else {
                                    binding.subCategorySpinner.visibility = View.GONE
                                    binding.affiliateButton.isEnabled = true
                                }
                            }
                            //endregion

                            hideProgressBar()
                        }

                        validateForm(false)
                    }

                    categoryFirstPass = false
                }
            }

        binding.subCategorySpinner.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onNothingSelected(p0: AdapterView<*>?) {
//                validateForm()
                }

                override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                    if (!subCategoryFirstPass) {
                        validateForm(false)
                    }

                    subCategoryFirstPass = false
                }
            }

        binding.sportSpinner.isEnabled = false
    }
    //endregion

    //region Affiliation Process
    private fun validateForm(checkCode: Boolean = true): Boolean {
        var valid = true

        val affiliationCode = binding.affiliationCodeInput.text.toString()

        if (checkCode) {
            if (!TextUtils.isEmpty(affiliationCode.trim())) {
                binding.affiliationCodeLayout.error = null
                binding.affiliateButton.isEnabled = true
            } else {
                val bundleTracking =
                    bundleOf("Affiliation ${getString(R.string.error_msg)}" to getString(R.string.hint_required))
                TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

                binding.affiliationCodeLayout.error = getString(R.string.hint_required)
                valid = false
                binding.affiliateButton.isEnabled = false
            }
        }

        return valid
    }
    //endregion

    //region Club Query
    private suspend fun getClubs() {
        viewModel.getClub().collect { state ->
            when (state) {
                is State.Loading -> {
                    showProgressBar()
                }
                is State.Success -> {
                    hideProgressBar()
                    val list = state.data
                    viewModel.assignClubList(list)
                }
                is State.Failed -> {
                    hideProgressBar()
                    val message = "${getString(R.string.error_fetch_club)} ${state.message}"
                    message.showToast()

                    val bundleTracking =
                        bundleOf(getString(R.string.error_fetch_club) to message)
                    TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)
                }
            }
        }
    }
    //endregion

    //region View Methods
    private fun String.showToast() {
        toast(this)
    }

    private fun errorMessage() {
        val bundleTracking =
            bundleOf(getString(R.string.affiliation_error) to getString(R.string.affiliation_404))
        TRACKING.logEvent(Tracking.ALERT_ERROR, bundleTracking)

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.affiliation_error))
            .setMessage(getString(R.string.affiliation_404))
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
            }
            .show()
    }
    //endregion

    //region Loading
    private fun showProgressBar() {
        binding.affiliationProgressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar() {
        binding.affiliationProgressBar.apply {
            visibility = View.GONE
        }
    }
    //endregion

    //region Navigation
    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }
    //endregion
}