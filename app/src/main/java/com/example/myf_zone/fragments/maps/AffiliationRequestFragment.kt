package com.example.myf_zone.fragments.maps

import android.content.DialogInterface
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import com.example.myf_zone.util.club.CategoryUtil.queryCategoryIdFromList
import com.example.myf_zone.util.club.CategoryUtil.queryCategoryList
import com.example.myf_zone.util.club.SportUtil.querySportIdFromList
import com.example.myf_zone.util.club.SportUtil.querySportList
import com.example.myf_zone.util.club.SubCategoryUtil.querySubCategoryList
import com.example.myf_zone.util.user.AffiliationForm.affiliationProcess
import com.example.myf_zone.util.user.AffiliationForm.queryClubFromCode
import com.example.myf_zone.util.user.AffiliationForm.queryIsCodeRegistered
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_affiliation_request.*
import kotlinx.android.synthetic.main.fragment_affiliation_request.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class AffiliationRequestFragment : Fragment() {
    private val TAG = AffiliationRequestFragment::class.java.simpleName

    private var sportFirstPass = true
    private var categoryFirstPass = true
    private var subCategoryFirstPass = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater =
            inflater.inflate(R.layout.fragment_affiliation_request, container, false)

        fragmentInflater.affiliationLaterAffiliate.setOnClickListener {
            navigate(R.id.globalToMaps)
        }

        fragmentInflater.affiliateButton.setOnClickListener {
            val affiliationCode = affiliationCodeInput.text.toString()
            val affiliationSport = sportSpinner.selectedItem.toString()
            val affiliationCategory = categorySpinner.selectedItem.toString()
            val affiliationSubCategory = subCategorySpinner.selectedItem.toString()

            showProgressBar(affiliationProgressBar)

            if (validateForm()) {
                CoroutineScope(IO).launch {
                    Log.d(TAG, "form is valid")
                    if (queryIsCodeRegistered(affiliationCode)) {
                        Log.d(TAG, "code is valid")
                        affiliationProcess(affiliationCode, affiliationSport, affiliationCategory)

                        Log.d(
                            TAG,
                            "Club for [$affiliationCode]: " + queryClubFromCode(affiliationCode).toString()
                        )

                        val bundle = bundleOf("clubId" to affiliationCode)
                        try {
                            withContext(Main) {
//                                navigate(R.id.globalToAffiliationSuccess/*, bundle*/)
                                navigate(R.id.globalToAffiliationSuccess, bundle)
                            }
                        } catch (e: Exception) {
                            Log.d(TAG, "Error: $e")
                        }
                    } else {
                        withContext(Main) {
                            errorMessage()
                        }
                        Log.d(TAG, "code is not valid")
                    }
                }
            } else {
                Log.d(TAG, "form is not valid")
            }

            hideProgressBar(affiliationProgressBar)
        }

        return fragmentInflater
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        CoroutineScope(Main).launch {
            showProgressBar(affiliationProgressBar)

            //SPORT SPINNER HANDLER

            val sportList = mutableListOf<String>()
            val sportObjectList = querySportList()!!
            for (item in sportObjectList)
                sportList.add(item.name)

            if (!sportList.isNullOrEmpty()) {
                sportSpinner.adapter =
                    ArrayAdapter(requireContext(), R.layout.simple_layout_file, sportList)
            }


            //CATEGORY SPINNER HANDLER

            val categoryList = mutableListOf<String>()
            val sportId =
                querySportIdFromList(sportSpinner.selectedItem.toString(), sportObjectList)!!
            val categoryObjectList = queryCategoryList(sportId)!!
            if (sportId.isNotEmpty()) {
                for (item in categoryObjectList)
                    categoryList.add(item.name)

                if (!categoryList.isNullOrEmpty()) {
                    categorySpinner.adapter =
                        ArrayAdapter(requireContext(), R.layout.simple_layout_file, categoryList)
                    categorySpinner.visibility = View.VISIBLE
                } else {
                    categorySpinner.visibility = View.GONE
                    subCategorySpinner.visibility = View.GONE
                    affiliateButton.isEnabled = true
                }
            }


            //SUB-CATEGORY SPINNER HANDLER

            val subCategoryList = mutableListOf<String>()
            val categoryId = queryCategoryIdFromList(
                categorySpinner.selectedItem.toString(),
                categoryObjectList
            )!!
            if (sportId.isNotEmpty() && categoryId.isNotEmpty()) {
                for (item in querySubCategoryList(sportId, categoryId)!!)
                    subCategoryList.add(item.name)

                if (!subCategoryList.isNullOrEmpty()) {
                    subCategorySpinner.adapter =
                        ArrayAdapter(requireContext(), R.layout.simple_layout_file, subCategoryList)
                    subCategorySpinner.visibility = View.VISIBLE
                } else {
                    subCategorySpinner.visibility = View.GONE
                    affiliateButton.isEnabled = true
                }
            }

            hideProgressBar(affiliationProgressBar)
        }

        affiliationCodeInput?.textChangedListener {
            onTextChanged { sequence, i, i2, i3 ->
                if (!sequence?.toString().equals(null)) {
                    validateForm()
                }
            }
        }

        sportSpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
//                validateForm()
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (!sportFirstPass) {

                    CoroutineScope(Main).launch {

                        showProgressBar(affiliationProgressBar)

                        val sportObjectList = querySportList()!!

                        //CATEGORY SPINNER HANDLER

                        val categoryList = mutableListOf<String>()
                        val sportId = querySportIdFromList(
                            sportSpinner.selectedItem.toString(),
                            sportObjectList
                        )!!
                        val categoryObjectList = queryCategoryList(sportId)!!
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


                        //SUB-CATEGORY SPINNER HANDLER

                        val subCategoryList = mutableListOf<String>()
                        val categoryId = queryCategoryIdFromList(
                            categorySpinner.selectedItem.toString(),
                            categoryObjectList
                        )!!
                        if (sportId.isNotEmpty() && categoryId.isNotEmpty()) {
                            for (item in querySubCategoryList(sportId, categoryId)!!)
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

                        hideProgressBar(affiliationProgressBar)
                    }

                    validateForm(false)
                }

                sportFirstPass = false
            }
        }

        categorySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
//                validateForm()
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {

                if (!categoryFirstPass) {

                    CoroutineScope(Main).launch {

                        showProgressBar(affiliationProgressBar)

                        val sportObjectList = querySportList()!!
                        val sportId = querySportIdFromList(
                            sportSpinner.selectedItem.toString(),
                            sportObjectList
                        )!!
                        val categoryObjectList = queryCategoryList(sportId)!!

                        //SUB-CATEGORY SPINNER HANDLER

                        val subCategoryList = mutableListOf<String>()
                        val categoryId = queryCategoryIdFromList(
                            categorySpinner.selectedItem.toString(),
                            categoryObjectList
                        )!!
                        if (sportId.isNotEmpty() && categoryId.isNotEmpty()) {
                            for (item in querySubCategoryList(sportId, categoryId)!!)
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

                        hideProgressBar(affiliationProgressBar)
                    }

                    validateForm(false)
                }

                categoryFirstPass = false
            }
        }

        subCategorySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
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

        sportSpinner.isEnabled = false
    }

    private fun validateForm(checkCode: Boolean = true): Boolean {
        var valid = true

        val affiliationCode = affiliationCodeInput.text.toString()

        if (checkCode) {
            if (!TextUtils.isEmpty(affiliationCode.trim())) {
                affiliationCodeLayout.error = null
                affiliateButton.isEnabled = true
            } else {
                affiliationCodeLayout.error = getString(R.string.hint_required)
                valid = false
                affiliateButton.isEnabled = false
            }
        }

        return valid
    }

    private fun errorMessage() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.affiliation_error))
            .setMessage(getString(R.string.affiliation_404))
            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
            }
            .show()
    }

    private fun showProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.VISIBLE
        }
    }

    private fun hideProgressBar(progressBar: ProgressBar) {
        progressBar.apply {
            visibility = View.GONE
        }
    }

    private fun navigate(destination: Int, extra: Bundle? = null) {
        Navigation
            .findNavController(this.requireView())
            .navigate(destination, extra)
    }

}