package com.myfzone_sport.myf_zone.fragments.affiliation.affiliation_suggestion

import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.myfzone_sport.myf_zone.R
import com.myfzone_sport.myf_zone.databinding.FragmentAffiliationSuggestionBinding
import com.myfzone_sport.myf_zone.fragments.user_sign.manager.ManagerAuth
import com.myfzone_sport.myf_zone.model.State
import com.myfzone_sport.myf_zone.model.club.ClubSuggestion
import kotlinx.coroutines.flow.collect
import org.jetbrains.anko.support.v4.toast
import java.util.*

class AffiliationSuggestionFragment : Fragment() {

    companion object {
        private val TAG = AffiliationSuggestionFragment::class.java.simpleName

        private var clubSuggestion: ClubSuggestion = ClubSuggestion()
        private const val AUTOCOMPLETE_REQUEST_CODE = 1

        private lateinit var viewModel: AffiliationSuggestionViewModel
        private lateinit var binding: FragmentAffiliationSuggestionBinding
    }

    //region Override Methods
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.i(TAG, "Club: $clubSuggestion")
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_affiliation_suggestion,
            container,
            false
        )

        viewModel = ViewModelProvider(this).get(
            AffiliationSuggestionViewModel::class.java
        )

        binding.apply {
            lifecycleOwner = this@AffiliationSuggestionFragment
            executePendingBindings()
        }

        binding.clubSuggestion = clubSuggestion

        resetFields()

        Places.initialize(requireContext(), getString(R.string.google_maps_key))

        binding.clubSuggestionAddressInput.setOnClickListener {
            setupAddressIntent(viewModel.fields.value!!)
        }

        binding.clubSuggestionAcronymInput.filters =
            binding.clubSuggestionAcronymInput.filters + InputFilter.AllCaps()

        binding.clubSuggestionButton.setOnClickListener {
            sendSuggestion()
        }

        ManagerAuth.checkUserStatus()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == AUTOCOMPLETE_REQUEST_CODE) {
                if (data != null) {
                    val place = Autocomplete.getPlaceFromIntent(data)
                    binding.clubSuggestionAddressInput.setText(place.address)
                    clubSuggestion.address = place.address!!
                    clubSuggestion.lat = place.latLng?.latitude!!
                    clubSuggestion.lng = place.latLng?.longitude!!
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        binding.clubSuggestionAddressInput.hideKeyboard()
    }
    //endregion

    //region Suggestion Creation
    private fun sendSuggestion() {
        try {
            if (validateForm()) {
                confirmSuggestion()
            }
        } catch (e: Exception) {
            toast("Erreur: ${e.localizedMessage}")
            Log.d(TAG, "Error: $e")
        }
    }

    private fun confirmSuggestion() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(R.string.send_suggestion))
            .setMessage(getString(R.string.send_suggestion_confirmation))
            .setIcon(R.drawable.ic_info)
            .setPositiveButton(getString(R.string.send_txt)) { _: DialogInterface, _: Int ->
                clubSuggestion.createdDate = Calendar.getInstance().time
                lifecycleScope.launchWhenResumed {
                    createSuggestion()
                }
            }.setNegativeButton(getString(R.string.cancel_message)) { _: DialogInterface, _: Int ->
            }.show()
    }

    private suspend fun createSuggestion() {
        viewModel.createSuggestion(
            clubSuggestion
        ).collect { state ->
            when (state) {
                is State.Loading -> {

                }
                is State.Success -> {
                    Log.i(TAG, "Success! Suggestion is: ${state.data}")
                    getString(R.string.suggestion_success_txt).toast()
                    resetFields()
                    requireActivity().onBackPressed()
                }
                is State.Failed -> {
                    val message = "Erreur : ${state.message}"
                    message.toast()
                }
            }
        }
    }

    private fun setupAddressIntent(fields: MutableList<Place.Field>) {
        val intent =
            Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN, fields).apply {
                setHint(getString(R.string.club_suggestion_address_helper))
            }
                .build(requireContext())
        startActivityForResult(
            intent,
            AUTOCOMPLETE_REQUEST_CODE
        )
    }
    //endregion

    //region Form Validation
    private fun validateForm(): Boolean {
        var valid = true

        val name = binding.clubSuggestionNameInput.text.toString()
        val acronym = binding.clubSuggestionAcronymInput.text.toString()
        val address = binding.clubSuggestionAddressInput.text.toString()

        val nameLayout = binding.clubSuggestionNameLayout
        val acronymLayout = binding.clubSuggestionAcronymLayout
        val addressLayout = binding.clubSuggestionAddressLayout

        when {
            TextUtils.isEmpty(name.trim()) -> {
                nameLayout.error = getString(R.string.hint_required)
                valid = false
            }
            name.trim().length > 21 -> {
                nameLayout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                nameLayout.error = null
            }
        }

        when {
            TextUtils.isEmpty(acronym.trim()) -> {
                acronymLayout.error = getString(R.string.hint_required)
                valid = false
            }
            acronym.trim().length > 51 -> {
                acronymLayout.error = getString(R.string.hint_required)
                valid = false
            }
            else -> {
                acronymLayout.error = null
            }
        }

        if (TextUtils.isEmpty(address.trim())) {
            addressLayout.error = getString(R.string.hint_required)
            valid = false
        } else {
            addressLayout.error = null
        }

        return valid
    }
    //endregion

    //region View Methods
    private fun resetFields() {
        binding.clubSuggestionNameInput.setText("")
        binding.clubSuggestionAcronymInput.setText("")
        binding.clubSuggestionAddressInput.setText("")
    }

    private fun String.toast() {
        toast(this)
    }

    private fun View.hideKeyboard() {
        val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(windowToken, 0)
    }
    //endregion
}