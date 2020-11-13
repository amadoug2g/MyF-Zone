package com.example.myf_zone.fragments.secondary

import android.content.DialogInterface
import android.os.Bundle
import android.os.Looper
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.example.myf_zone.R
import com.example.myf_zone.util.FirebaseUtil.auth
import com.example.myf_zone.util.StorageUtil.categoryID
import com.example.myf_zone.util.StorageUtil.checkAffiliationCode
import com.example.myf_zone.util.StorageUtil.checkCodeClub
import com.example.myf_zone.util.StorageUtil.populateSpinners
import com.example.myf_zone.util.StorageUtil.sportID
import com.example.myf_zone.util.StorageUtil.subCategorySpinnerHandler
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.android.synthetic.main.fragment_affiliation_request.*
import kotlinx.android.synthetic.main.fragment_affiliation_request.view.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.launch
import org.jetbrains.anko.sdk27.coroutines.textChangedListener

class AffiliationRequestFragment : Fragment() {
    private val TAG = AffiliationRequestFragment::class.java.simpleName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCREATE")

        (activity as AppCompatActivity).supportActionBar?.apply {
            show()
            setTitle(R.string.affiliation_text)
            setHomeButtonEnabled(true)
            setDisplayHomeAsUpEnabled(true)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val fragmentInflater =
            inflater.inflate(R.layout.fragment_affiliation_request, container, false)

        val currentUser = auth.currentUser!!

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
                    val codeClub = checkCodeClub(affiliationCode)

                    Looper.prepare()

                    if (codeClub) {
                        checkAffiliationCode(affiliationCode, affiliationSport, affiliationCategory)

                        val bundle = bundleOf("clubId" to affiliationCode)
                        try {
                            navigate(R.id.affiliationRequestToAffiliationSuccess, bundle)
                        } catch (e: Exception) {
                            Log.d(TAG, "Error: $e")
                        }
                    } else {
                        MaterialAlertDialogBuilder(requireContext())
                            .setTitle(getString(R.string.affiliation_error))
                            .setMessage(getString(R.string.affiliation_404))
                            .setPositiveButton("OK") { _: DialogInterface, _: Int ->
                            }
                            .show()
                    }
                    Looper.loop()
                }
            }

            hideProgressBar(affiliationProgressBar)
        }

        return fragmentInflater
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        categorySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validateForm()
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 != 0) {
                    validateForm()

                    CoroutineScope(Main).launch {
                        val sportId = sportID(sportSpinner.selectedItem.toString())!!
                        val categoryId =
                            categoryID(sportId, categorySpinner.selectedItem.toString())!!

                        val buttonDisplay = subCategorySpinnerHandler(
                            subCategorySpinner,
                            sportId,
                            categoryId,
                            requireContext(),
                            R.layout.simple_layout_file
                        )

                        if (buttonDisplay)
                            affiliateButton.isEnabled = true
                    }
                } else {
                    validateForm(false)
                }
            }
        }

        subCategorySpinner?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(p0: AdapterView<*>?) {
                validateForm()
            }

            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                if (p2 != 0) {
                    validateForm()
                } else {
                    validateForm(false)
                }
            }
        }

        affiliationCodeInput?.textChangedListener {
            onTextChanged { sequence, i, i2, i3 ->
                if (!sequence?.toString().equals(null)) {
                    validateForm()

                    CoroutineScope(Main).launch {
                        val sportId = sportID(sportSpinner.selectedItem.toString())!!
                        val categoryId =
                            categoryID(sportId, categorySpinner.selectedItem.toString())!!

                        val buttonDisplay = subCategorySpinnerHandler(
                            subCategorySpinner,
                            sportId,
                            categoryId,
                            requireContext(),
                            R.layout.simple_layout_file
                        )

                        if (buttonDisplay)
                            affiliateButton.isEnabled = true
                    }
                }
            }
        }

        sportSpinner.isEnabled = false

        CoroutineScope(Main).launch {
            populateSpinners(
                sportSpinner,
                categorySpinner,
                subCategorySpinner,
                requireContext(),
                R.layout.simple_layout_file
            )
        }
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

    private fun validateForm(checkCode: Boolean = true): Boolean {
        var valid = true

        val affiliationCode = affiliationCodeInput.text.toString()

        var moveToCategory = true
        val moveToSubCategory: Boolean

        if (checkCode) {
            if (!TextUtils.isEmpty(affiliationCode.trim())) {
                affiliationCodeLayout.error = null
                moveToCategory = true
            } else {
                affiliationCodeLayout.error = getString(R.string.hint_required)
                valid = false
                moveToCategory = false
                subCategorySpinner.visibility = View.GONE
                affiliateButton.isEnabled = false
            }
        }

        if (moveToCategory) {
            subCategorySpinner.visibility = View.VISIBLE
            moveToSubCategory = true
        } else {
            subCategorySpinner.visibility = View.GONE
            affiliateButton.isEnabled = false
            valid = false
            moveToSubCategory = false
        }

        if (moveToSubCategory) {
            affiliateButton.isEnabled = true
        } else {
            affiliateButton.isEnabled = false
            valid = false
        }

        return valid
    }

}